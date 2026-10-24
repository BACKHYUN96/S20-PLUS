// Compile the production transport with a deterministic usbfs syscall mock.
#include "../../main/cpp/usb_direct.cpp"
#include <cassert>
#include <cstdarg>
#include <deque>
#include <iostream>

enum class Fault {None,Denied,Short,Reordered,Corrupt};
static Fault fault=Fault::None;
static std::deque<usbdevfs_urb*> pending;
static int closed=0;
static bool volumeMock=false,clockMock=false;
enum class ClockFault {None,IdleRejected,IdleReadWrong,ActiveReadWrong,SetRejected,RateMismatch,LateRateChange};
static ClockFault clockFault=ClockFault::None;
static int clockAlt=0,reportedRate=96000,physicalRate=48000,clockReads=0;
static std::vector<std::string> clockEvents;
enum class VolumeFault {None,ReadFail,MuteIgnored,VolumeIgnored,BadRange,ClaimDenied,CancelOnUnmute};
static VolumeFault volumeFault=VolumeFault::None;
static int hwVolume=0,hwMute=0;
static std::vector<std::string> volumeEvents;
static std::vector<uint8_t> config(){
    std::vector<uint8_t> b={9,2,52,0,1,1,0,0x80,50,9,4,0,0,0,1,1,0,0,
        12,0x24,2,4,1,1,0,2,3,0,0,0,13,0x24,6,5,4,2,3,0,0,0,0,0,0,9,0x24,3,6,2,4,1,5,0};return b;
}
extern "C" int __wrap_ioctl(int,unsigned long request,...) {
    va_list args;va_start(args,request);void* p=va_arg(args,void*);va_end(args);
    if(clockMock) {
        if(request==USBDEVFS_SETINTERFACE) {
            auto* a=static_cast<usbdevfs_setinterface*>(p);
            clockEvents.push_back("ALT_"+std::to_string(a->altsetting));
            if(a->altsetting==0&&clockFault==ClockFault::IdleRejected){errno=EPIPE;return -1;}
            clockAlt=a->altsetting;return 0;
        }
        if(request==USBDEVFS_CONTROL) {
            auto* c=static_cast<usbdevfs_ctrltransfer*>(p);auto* b=static_cast<uint8_t*>(c->data);
            if(c->bRequestType==0x81&&c->bRequest==10) {
                clockEvents.push_back("GET_ALT");b[0]=uint8_t(clockAlt);
                if((clockAlt==0&&clockFault==ClockFault::IdleReadWrong)||(clockAlt!=0&&clockFault==ClockFault::ActiveReadWrong))b[0]=9;
                return 1;
            }
            if(c->bRequestType==0x22) {
                int value=b[0]|(b[1]<<8)|(b[2]<<16);clockEvents.push_back("SET_RATE_"+std::to_string(value));
                if(clockFault==ClockFault::SetRejected){errno=EPIPE;return -1;}
                physicalRate=reportedRate=value;return 3;
            }
            if(c->bRequestType==0xa2) {
                clockEvents.push_back("GET_RATE");clockReads++;int value=reportedRate;
                if((clockFault==ClockFault::RateMismatch&&clockReads>1)||(clockFault==ClockFault::LateRateChange&&clockReads>2))value=48000;
                b[0]=uint8_t(value);b[1]=uint8_t(value>>8);b[2]=uint8_t(value>>16);return 3;
            }
        }
    }
    if(volumeMock){
        if(request==USBDEVFS_CLAIMINTERFACE){if(volumeFault==VolumeFault::ClaimDenied){errno=EPERM;return -1;}return 0;}
        if(request==USBDEVFS_RELEASEINTERFACE)return 0;
        if(request==USBDEVFS_CONTROL){
            auto* c=static_cast<usbdevfs_ctrltransfer*>(p);
            if(c->bRequest==6){auto b=config();std::memcpy(c->data,b.data(),std::min(size_t(c->wLength),b.size()));return c->wLength;}
            if(c->wIndex!=0x0500){errno=EPIPE;return -1;}
            auto* b=static_cast<uint8_t*>(c->data);
            if(c->bRequestType==0xa1){
                if(volumeFault==VolumeFault::ReadFail){errno=EPIPE;return -1;}
                if(c->wValue==0x0100){b[0]=uint8_t(hwMute);volumeEvents.push_back("GET_MUTE");return 1;}
                int value=c->bRequest==0x81?hwVolume:c->bRequest==0x82?-90*256:c->bRequest==0x83?0:256;
                if(c->bRequest==0x84&&volumeFault==VolumeFault::BadRange)value=0;
                b[0]=uint8_t(value);b[1]=uint8_t(value>>8);volumeEvents.push_back("GET_VOLUME");return 2;
            }
            if(c->wValue==0x0100){volumeEvents.push_back("SET_MUTE_"+std::to_string(b[0]));if(volumeFault!=VolumeFault::MuteIgnored)hwMute=b[0];
                if(volumeFault==VolumeFault::CancelOnUnmute&&b[0]==0)playbackFence.fetch_add(1);
                return 1;}
            int value=int16_t(b[0]|(b[1]<<8));volumeEvents.push_back("SET_VOLUME_"+std::to_string(value));
            if(volumeFault!=VolumeFault::VolumeIgnored)hwVolume=value;
            return 2;
        }
    }
    if(request==USBDEVFS_SUBMITURB){
        if(fault==Fault::Denied){errno=EPERM;return -1;}
        pending.push_back(static_cast<usbdevfs_urb*>(p));return 0;
    }
    if(request==USBDEVFS_REAPURBNDELAY){
        if(pending.empty()){errno=EAGAIN;return -1;}
        auto* u=pending.front();pending.pop_front();
        if(fault==Fault::Reordered&&!pending.empty()){pending.push_front(u);u=pending.back();pending.pop_back();}
        u->status=0;for(int i=0;i<u->number_of_packets;i++){u->iso_frame_desc[i].actual_length=u->iso_frame_desc[i].length;u->iso_frame_desc[i].status=0;}
        if(fault==Fault::Short)u->iso_frame_desc[0].actual_length--;
        if(fault==Fault::Corrupt)static_cast<uint8_t*>(u->buffer)[0]^=1;
        *static_cast<void**>(p)=u;return 0;
    }
    if(request==USBDEVFS_DISCARDURB)return 0;
    errno=ENOTTY;return -1;
}
extern "C" int __real_close(int);
extern "C" int __wrap_close(int fd){if(fd==99){closed++;return 0;}return __real_close(fd);}

static void runTransport(Fault inject,bool streaming=false,bool validated=false,int64_t rangeStart=0,int64_t rangeEnd=5000,int rate=48000,int bits=24) {
    pending.clear();fault=inject;closed=0;
    Session s;s.fd=99;s.rate=rate;s.bits=bits;s.frameBytes=bits/8*2;s.packet=rate/1000*s.frameBytes;s.running=true;
    const auto frameBytes=size_t(s.frameBytes),sampleBytes=size_t(bits/8);
    s.pcm.resize(5000*frameBytes);for(size_t i=0;i<s.pcm.size();i+=sampleBytes)s.pcm[i]=uint8_t(1+(i/sampleBytes)%25);
    auto expected=s.pcm;
    s.setRange(rangeStart,rangeEnd);
    std::string sourcePath;
    purebit::PcmSourceCache cache;
    if(streaming){char name[]="/tmp/purebit-stream-XXXXXX";int fd=mkstemp(name);assert(fd>=0);unlink(name);assert(write(fd,s.pcm.data(),s.pcm.size())==ssize_t(s.pcm.size()));s.pcmFd=fd;s.pcmLength=s.pcm.size();s.pcm.clear();}
    if(validated){char name[]="/tmp/purebit-validated-XXXXXX";int fd=mkstemp(name);assert(fd>=0);sourcePath=name;
        assert(write(fd,s.pcm.data(),s.pcm.size())==ssize_t(s.pcm.size()));::close(fd);
        purebit::Sha256 hash;hash.update(expected.data(),expected.size());bool reused=false;
        s.validatedSource=cache.acquire(sourcePath,hash.hex(),bits,s.frameBytes,reused);assert(!reused);s.pcm.clear();}
    for(auto& slot:s.slots){slot.urb=static_cast<usbdevfs_urb*>(std::calloc(1,sizeof(usbdevfs_urb)+8*sizeof(usbdevfs_iso_packet_desc)));slot.buffer.resize(s.packet*8);}
    s.run();assert(!s.running);assert(s.fd==-1);assert(closed==1);
    if(inject==Fault::None){
        assert(s.errors==0);assert(s.completed==uint64_t(rangeEnd-rangeStart));assert(s.submitted==uint64_t(rangeEnd-rangeStart));assert(s.equal);
        assert(s.nextByte==size_t(rangeEnd)*frameBytes);assert(s.completedByte==size_t(rangeEnd)*frameBytes);
        purebit::Sha256 reference;reference.update(expected.data()+rangeStart*frameBytes,size_t(rangeEnd-rangeStart)*frameBytes);
        assert(s.reference.hex()==reference.hex());assert(s.wire.hex()==reference.hex());
    }else{assert(s.errors==1);assert(!s.error.empty());if(inject!=Fault::Corrupt)assert(s.completed==0);}
    if(inject==Fault::Denied)assert(s.error.find("errno=1")!=std::string::npos);
    if(inject==Fault::Corrupt)assert(!s.equal);
    if(validated){cache.release(sourcePath);s.validatedSource.reset();unlink(sourcePath.c_str());}
}
static void volumeTests(){
    assert(purebit::quietVolume(0,-90*256,0,256)==-50*256);
    assert(purebit::quietVolume(-60*256,-90*256,0,256)==-60*256);
    assert(purebit::quietVolume(0,-90*256,0,3*256)<=-50*256);
    for(auto fault:{VolumeFault::None,VolumeFault::ReadFail,VolumeFault::MuteIgnored,VolumeFault::VolumeIgnored,VolumeFault::BadRange,VolumeFault::ClaimDenied}){
        volumeMock=true;volumeFault=fault;hwVolume=0;hwMute=0;volumeEvents.clear();pending.clear();
        Session s;s.fd=99;bool passed=false;
        try{s.prepareVolume();passed=true;}catch(const std::exception&){}
        assert(passed==(fault==VolumeFault::None));assert(pending.empty());
        if(passed){assert(hwVolume==-50*256);assert(hwMute==1);assert(s.volumePrepared);
            auto mute=std::find(volumeEvents.begin(),volumeEvents.end(),"SET_MUTE_1");auto vol=std::find(volumeEvents.begin(),volumeEvents.end(),"SET_VOLUME_-12800");assert(mute<vol);
        }
        if(fault==VolumeFault::MuteIgnored)assert(std::none_of(volumeEvents.begin(),volumeEvents.end(),[](const std::string& e){return e.find("SET_VOLUME_")==0;}));
        s.restore();if(passed){assert(hwVolume==-50*256);assert(hwMute==0);} // No louder-volume restoration.
    }
    volumeFault=VolumeFault::None;hwVolume=0;hwMute=0;pending.clear();
    {
        Session s;s.fd=99;s.volumeTouched=true;s.volumePrepared=true;s.safeVolume=-50*256;s.originalMute=0;
        s.rate=48000;s.bits=24;s.frameBytes=6;s.packet=288;s.running=true;s.pcm.resize(288,0);
        s.run();assert(s.errors==1);assert(s.submitted==0);assert(s.completed==0);assert(pending.empty());assert(s.volumeChecks==1);assert(s.lastVolume==0);assert(s.lastMute==0);
    }
    hwVolume=-50*256;hwMute=0;pending.clear();fault=Fault::None;
    {
        Session s;s.fd=99;s.volumeTouched=true;s.volumePrepared=true;s.safeVolume=-50*256;s.originalMute=1;
        s.rate=48000;s.bits=24;s.frameBytes=6;s.packet=288;s.running=true;s.pcm.resize(5000*6);
        for(size_t i=0;i<s.pcm.size();i+=3){s.pcm[i]=255;s.pcm[i+1]=255;s.pcm[i+2]=127;}
        purebit::Sha256 expected;expected.update(s.pcm.data(),s.pcm.size());
        for(auto& slot:s.slots){slot.urb=static_cast<usbdevfs_urb*>(std::calloc(1,sizeof(usbdevfs_urb)+8*sizeof(usbdevfs_iso_packet_desc)));slot.buffer.resize(s.packet*8);}
        s.run();assert(s.errors==0);assert(s.completed==5000);assert(s.reference.hex()==expected.hex());assert(s.wire.hex()==expected.hex());
        assert(s.volumeChecks>=1);assert(s.lastVolume==-50*256);assert(s.lastMute==0);
        assert(hwMute==1);assert(hwVolume==-50*256); // Original mute restored; original louder volume never restored.
    }
    volumeMock=false;
    auto b=config();assert(purebit::samsungVolumeTopology(b.data(),b.size()));b[36]=0;assert(!purebit::samsungVolumeTopology(b.data(),b.size()));
}
static void initializationTests() {
    clockMock=true;
    for(auto inject:{ClockFault::None,ClockFault::IdleRejected,ClockFault::IdleReadWrong,ClockFault::ActiveReadWrong,ClockFault::SetRejected,ClockFault::RateMismatch,ClockFault::LateRateChange}) {
        clockFault=inject;clockAlt=2;reportedRate=96000;physicalRate=48000;clockReads=0;clockEvents.clear();pending.clear();
        Session s;s.fd=99;s.rate=96000;s.bits=24;s.alt=2;s.packet=576;
        bool passed=false;try{assert(s.configureOutput()==96000);passed=true;}catch(const std::exception&){}
        assert(passed==(inject==ClockFault::None));assert(s.submitted==0);assert(pending.empty());
        if(passed) {
            // GET_CUR originally lied about the physical clock: forced SET must still occur.
            assert(physicalRate==96000);assert(clockReads==3);
            auto idle=std::find(clockEvents.begin(),clockEvents.end(),"ALT_0");
            auto active=std::find(clockEvents.begin(),clockEvents.end(),"ALT_2");
            auto set=std::find(clockEvents.begin(),clockEvents.end(),"SET_RATE_96000");
            assert(idle<active&&active<set&&set!=clockEvents.end());
            assert(s.initTrace.find("forced even when reported equal")!=std::string::npos);
        }
        s.fd=-1;
    }
    clockFault=ClockFault::None;reportedRate=physicalRate=48000;clockReads=0;clockEvents.clear();
    {Session s;s.fd=99;s.rate=48000;s.alt=2;s.packet=288;assert(s.configureOutput()==48000);
        assert(std::find(clockEvents.begin(),clockEvents.end(),"ALT_0")==clockEvents.end());
        assert(std::find(clockEvents.begin(),clockEvents.end(),"SET_RATE_48000")==clockEvents.end());s.fd=-1;}
    reportedRate=96000;clockEvents.clear();
    {Session s;s.fd=99;s.rate=48000;s.alt=2;s.packet=288;assert(s.configureOutput()==48000);
        assert(std::find(clockEvents.begin(),clockEvents.end(),"SET_RATE_48000")!=clockEvents.end());s.fd=-1;}
    clockMock=false;volumeMock=true;volumeFault=VolumeFault::None;hwMute=1;hwVolume=-50*256;
    {Session s;s.fd=99;s.safeVolume=-50*256;s.verifyMutedAfterConfiguration();
        hwMute=0;bool rejected=false;try{s.verifyMutedAfterConfiguration();}catch(...){rejected=true;}assert(rejected);
        hwMute=1;hwVolume=0;rejected=false;try{s.verifyMutedAfterConfiguration();}catch(...){rejected=true;}assert(rejected);
        assert(s.submitted==0);s.fd=-1;}
    volumeMock=false;
}
static void sourceCacheTests() {
    char name[]="/tmp/purebit-identity-XXXXXX";int fd=mkstemp(name);assert(fd>=0);
    std::vector<uint8_t> bytes(2400,0);bytes[0]=20;assert(write(fd,bytes.data(),bytes.size())==ssize_t(bytes.size()));
    purebit::Sha256 hash;hash.update(bytes.data(),bytes.size());purebit::PcmSourceCache cache;bool reused=true;
    auto first=cache.acquire(name,hash.hex(),24,6,reused);assert(!reused);assert(first->peakSafe);
    auto second=cache.acquire(name,hash.hex(),24,6,reused);assert(reused);assert(first==second);
    uint8_t sample[6]{};second->read(0,sample,6);assert(sample[0]==20);
    bool refused=false;try{cache.acquire(name,std::string(64,'0'),24,6,reused);}catch(...){refused=true;}assert(refused);
    refused=false;try{cache.acquire(name,hash.hex(),16,4,reused);}catch(...){refused=true;}assert(refused);
    // Even if the caller restores mtime, ctime or the actual file identity invalidates reuse.
    struct stat old{};assert(fstat(fd,&old)==0);uint8_t changed=30;assert(pwrite(fd,&changed,1,0)==1);
    struct timespec dates[2]={old.st_atim,old.st_mtim};assert(futimens(fd,dates)==0);
    refused=false;try{second->read(0,sample,6);}catch(...){refused=true;}assert(refused);
    refused=false;try{cache.acquire(name,hash.hex(),24,6,reused);}catch(...){refused=true;}assert(refused);
    cache.release(name);first.reset();second.reset();::close(fd);unlink(name);
    char replacement[]="/tmp/purebit-replacement-XXXXXX";fd=mkstemp(replacement);assert(fd>=0);
    assert(write(fd,bytes.data(),bytes.size())==ssize_t(bytes.size()));first=cache.acquire(replacement,hash.hex(),24,6,reused);assert(!reused);
    unlink(replacement);int newfd=open(replacement,O_CREAT|O_WRONLY|O_EXCL,0600);assert(newfd>=0);
    assert(write(newfd,bytes.data(),bytes.size())==ssize_t(bytes.size()));::close(newfd);
    refused=false;try{cache.acquire(replacement,hash.hex(),24,6,reused);}catch(...){refused=true;}assert(refused);
    cache.release(replacement);first.reset();::close(fd);unlink(replacement);
    // An uncached source with incorrect bytes is rejected before any USB operation.
    char mismatch[]="/tmp/purebit-mismatch-XXXXXX";fd=mkstemp(mismatch);assert(fd>=0);bytes[0]=40;
    assert(write(fd,bytes.data(),bytes.size())==ssize_t(bytes.size()));refused=false;
    try{cache.acquire(mismatch,hash.hex(),24,6,reused);}catch(...){refused=true;}assert(refused);
    ::close(fd);unlink(mismatch);
}
static void cancellationTests() {
    volumeMock=true;volumeFault=VolumeFault::None;volumeEvents.clear();pending.clear();hwVolume=-50*256;hwMute=1;
    playbackFence.store(1);
    {
        Session s;s.fd=99;s.intentToken=0;s.volumePrepared=s.volumeTouched=true;s.safeVolume=-50*256;s.originalMute=1;
        bool refused=false;try{s.begin();}catch(const PlaybackCancelled&){refused=true;}
        assert(refused);assert(!s.thread.joinable());assert(s.submitted==0);assert(pending.empty());assert(hwMute==1);
        assert(std::find(volumeEvents.begin(),volumeEvents.end(),"SET_MUTE_0")==volumeEvents.end());
        s.restore();s.fd=-1;
    }
    playbackFence.store(0);volumeFault=VolumeFault::CancelOnUnmute;volumeEvents.clear();hwMute=1;
    {
        Session s;s.fd=99;s.intentToken=0;s.volumePrepared=s.volumeTouched=true;s.safeVolume=-50*256;s.originalMute=1;
        bool refused=false;try{s.begin();}catch(const PlaybackCancelled&){refused=true;}
        assert(refused);assert(!s.thread.joinable());assert(s.submitted==0);assert(pending.empty());
        s.restore();assert(hwMute==1);s.fd=-1;
    }
    volumeMock=false;
    {
        Session s;s.fd=99;s.intentToken=0;s.running=true; // Fence=1: run must cancel before its first submission.
        s.run();assert(s.submitted==0);assert(s.completed==0);assert(s.errors==0);assert(!s.running);assert(pending.empty());
    }
    playbackFence.store(0);
    char name[]="/tmp/purebit-cancel-hash-XXXXXX";int fd=mkstemp(name);assert(fd>=0);
    std::vector<uint8_t> bytes(300000,0);assert(write(fd,bytes.data(),bytes.size())==ssize_t(bytes.size()));::close(fd);
    purebit::Sha256 hash;hash.update(bytes.data(),bytes.size());purebit::PcmSourceCache cache;bool reused=true,refused=false;int checks=0;
    try{cache.acquire(name,hash.hex(),24,6,reused,[&]{if(++checks>=3)throw PlaybackCancelled();});}catch(const PlaybackCancelled&){refused=true;}
    assert(refused);auto source=cache.acquire(name,hash.hex(),24,6,reused);assert(!reused); // Aborted hash is never cached as valid.
    cache.release(name);source.reset();unlink(name);
}
static void userVolumeTests() {
    volumeMock=true;volumeFault=VolumeFault::None;hwVolume=-50*256;hwMute=0;volumeEvents.clear();pending.clear();
    Session s;s.fd=99;s.intentToken=playbackFence.load();s.volumePrepared=true;s.safeVolume=hwVolume;
    s.volumeMin=-60*256;s.volumeMax=0;s.volumeRes=128;
    s.pendingVolumeDirection=1;s.adjustVolume();assert(hwVolume==-49*256);assert(s.safeVolume==hwVolume);assert(s.volumeUpdates==1);
    s.pendingVolumeDirection=-1;s.adjustVolume();assert(hwVolume==-50*256);
    hwVolume=s.safeVolume=0;s.pendingVolumeDirection=1;s.adjustVolume();assert(hwVolume==0);
    hwVolume=s.safeVolume=-60*256;s.pendingVolumeDirection=-1;s.adjustVolume();assert(hwVolume==-60*256);
    hwVolume=s.safeVolume=-50*256;volumeFault=VolumeFault::VolumeIgnored;s.pendingVolumeDirection=1;
    bool rejected=false;try{s.adjustVolume();}catch(...){rejected=true;}assert(rejected);assert(s.safeVolume==-50*256);
    volumeFault=VolumeFault::None;hwMute=1;s.pendingVolumeDirection=1;rejected=false;try{s.adjustVolume();}catch(...){rejected=true;}assert(rejected);
    s.fd=-1;
    volumeFault=VolumeFault::None;hwVolume=-4*256;hwMute=0;
    {Session remembered;remembered.fd=99;remembered.prepareVolume(-40*256);assert(hwVolume==-40*256);remembered.restore();remembered.fd=-1;}
    hwVolume=-55*256;hwMute=0;
    {Session quieter;quieter.fd=99;quieter.prepareVolume(-40*256);assert(hwVolume==-55*256);quieter.restore();quieter.fd=-1;}
    volumeMock=false;
}
int main(){
    userVolumeTests();
    purebit::Sha256 empty;assert(empty.hex()=="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    purebit::Sha256 abc;abc.update(reinterpret_cast<const uint8_t*>("abc"),3);assert(abc.hex()=="ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    purebit::Sha256 million;std::vector<uint8_t> a(1000000,'a');million.update(a.data(),a.size());
    assert(million.hex()=="cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0");
    assert(purebit::packetBytes(48000,24,2,576)==288);assert(purebit::packetBytes(96000,24,2,576)==576);
    bool rejected=false;try{purebit::packetBytes(44100,24,2,576);}catch(...){rejected=true;}assert(rejected);
    for(int bits:{16,24,32}){
        const int width=bits/8;std::vector<uint8_t> silence(width*2);assert(purebit::safePeak(silence,bits));
        int64_t limit=((int64_t(1)<<(bits-1))-1)/1000;
        for(int sign:{-1,1}){
            int64_t sample=limit*sign;for(int b=0;b<width;b++)silence[b]=uint8_t(uint64_t(sample)>>(8*b));assert(purebit::safePeak(silence,bits));
            sample=(limit+1)*sign;for(int b=0;b<width;b++)silence[b]=uint8_t(uint64_t(sample)>>(8*b));assert(!purebit::safePeak(silence,bits));
        }
    }
    for(Fault f:{Fault::None,Fault::Denied,Fault::Short,Fault::Reordered,Fault::Corrupt})runTransport(f);
    runTransport(Fault::None,true);runTransport(Fault::None,false,true);
    // Non-packet-aligned A/B, final partial packet, short range, full EOF and pinned-source reuse.
    for(int rate:{48000,96000})for(int bits:{16,24,32}) {
        runTransport(Fault::None,false,true,1001,3002,rate,bits);runTransport(Fault::None,true,false,17,4999,rate,bits);
        runTransport(Fault::None,false,false,4999,5000,rate,bits);
        runTransport(Fault::None,false,true,1001,3002,rate,bits); // Fresh repeat of the same interval.
    }
    {
        Session s;s.frameBytes=6;s.pcm.resize(5000*6);
        for(auto range:std::vector<std::pair<int64_t,int64_t>>{{-1,2},{0,0},{2,1},{0,5001},{5000,5000},{INT64_MAX,INT64_MAX}}) {
            bool rejected=false;try{s.setRange(range.first,range.second);}catch(const std::runtime_error&){rejected=true;}assert(rejected);
        }
    }
    volumeTests();initializationTests();sourceCacheTests();cancellationTests();
    std::cout<<"Native checks passed: SHA-256, packet/peak, ISO success/failure, bounded A/B source ranges/partial packets/exact subrange hashes/invalid bounds, streaming cache, hardware range policy, mute-before-volume, SET/readback refusal, topology guard, no volume increase on cleanup, 96k forced clock startup/failure gates, 48k baseline, validated source reuse, write/path/hash/format invalidation, prepare/begin cancellation before PCM/unmute, cancel-during-unmute/hash and normal zero-error cancellation\n";
}
