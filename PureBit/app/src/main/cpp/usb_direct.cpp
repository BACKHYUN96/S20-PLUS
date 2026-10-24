#include "iso_core.h"
#include "hardware_volume.h"
#include "pcm_source.h"
#include <jni.h>
#include <android/log.h>
#include <linux/usbdevice_fs.h>
#include <linux/usb/ch9.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <cerrno>
#include <cstdlib>
#include <atomic>
#include <chrono>
#include <fstream>
#include <limits>
#include <memory>
#include <mutex>
#include <thread>

namespace {
purebit::PcmSourceCache sourceCache;
std::atomic<uint64_t> playbackFence{0};
struct PlaybackCancelled : std::exception {
    const char* what() const noexcept override {return "Playback intent cancelled; no automatic restart";}
};
void log(const std::string& s) { __android_log_print(ANDROID_LOG_INFO,"PureBitAudio","native_usb: %s",s.c_str()); }
std::string failure(const std::string& stage) { return stage+" errno="+std::to_string(errno)+" ("+std::strerror(errno)+")"; }
void requireIoctl(int result,const std::string& stage) { if(result<0)throw std::runtime_error(failure(stage)); }
int control(int fd,int type,int req,int val,int index,void* data,int len) {
    usbdevfs_ctrltransfer c{};c.bRequestType=type;c.bRequest=req;c.wValue=val;c.wIndex=index;c.wLength=len;c.timeout=200;c.data=data;
    return ioctl(fd,USBDEVFS_CONTROL,&c);
}
int getAlt(int fd,int iface) { uint8_t alt=0;int n=control(fd,0x81,10,0,iface,&alt,1);return n==1?alt:(n<0?-errno:-EPROTO); }
int getRate(int fd,int endpoint) { uint8_t b[3]{};int n=control(fd,0xa2,0x81,0x0100,endpoint,b,3);return n==3?(b[0]|(b[1]<<8)|(b[2]<<16)):(n<0?-errno:-EPROTO); }
int setRate(int fd,int endpoint,int rate) { uint8_t b[3]={uint8_t(rate),uint8_t(rate>>8),uint8_t(rate>>16)};return control(fd,0x22,1,0x0100,endpoint,b,3); }
int setAlt(int fd,int iface,int alt) { usbdevfs_setinterface s{};s.interface=iface;s.altsetting=alt;return ioctl(fd,USBDEVFS_SETINTERFACE,&s); }
int getVolume(int fd,int req) {uint8_t b[2]{};int n=control(fd,0xa1,req,0x0200,0x0500,b,2);if(n!=2)throw std::runtime_error("FU5 volume GET request="+std::to_string(req)+" rc="+std::to_string(n)+" errno="+std::to_string(errno));return int16_t(b[0]|(b[1]<<8));}
int getMute(int fd){uint8_t b=0;int n=control(fd,0xa1,0x81,0x0100,0x0500,&b,1);if(n!=1||b>1)throw std::runtime_error("FU5 mute GET unsupported rc="+std::to_string(n)+" errno="+std::to_string(errno));return b;}
void setMuteChecked(int fd,int mute){uint8_t b=uint8_t(mute);if(control(fd,0x21,1,0x0100,0x0500,&b,1)!=1||getMute(fd)!=mute)throw std::runtime_error("FU5 mute SET/readback failed");}
void setVolumeChecked(int fd,int volume){uint8_t b[2]={uint8_t(volume),uint8_t(volume>>8)};if(control(fd,0x21,1,0x0200,0x0500,b,2)!=2||getVolume(fd,0x81)!=volume)throw std::runtime_error("FU5 volume SET/readback failed");}
struct Slot {
    usbdevfs_urb* urb=nullptr;std::vector<uint8_t> buffer,reference;size_t sourceOffset=0;bool queued=false;
    ~Slot(){std::free(urb);}
};
struct Session {
    uint64_t intentToken=0;
    int fd=-1,iface=2,alt=0,endpoint=3,rate=0,bits=0,frameBytes=0,packet=0,oldAlt=-1,oldRate=-1;
    bool claimed=false,disconnectAttempted=false,selected=false,originalAltKnown=false,originalRateKnown=false;
    bool restorationDone=false;
    bool controlClaimed=false,controlDisconnected=false,volumeTouched=false,volumePrepared=false;
    int originalVolume=0,originalMute=-1,safeVolume=0,volumeMin=0,volumeMax=0,volumeRes=0;
    std::atomic<int> pendingVolumeDirection{0};
    uint64_t volumeUpdates=0;
    std::string volumeReport="Hardware volume unchanged/UNKNOWN (low-level test only)";
    int pcmFd=-1;size_t pcmLength=0;
    std::shared_ptr<purebit::ValidatedPcmSource> validatedSource;
    std::vector<uint8_t> pcm; // Host mock only; production reads the private cache with pread().
    size_t startByte=0,nextByte=0,completedByte=0,endByte=0;
    std::array<Slot,4> slots;
    std::atomic<bool> stop{false};std::thread thread;
    std::mutex mutex;bool running=false,equal=true;uint64_t submitted=0,completed=0;int errors=0;
    uint64_t volumeChecks=0; int lastVolume=0,lastMute=-1;
    std::string setup,error,cleanup,initTrace;
    std::chrono::steady_clock::time_point initStarted=std::chrono::steady_clock::now();purebit::Sha256 reference,wire;
    ~Session(){if(thread.joinable()){stop=true;thread.join();}if(fd>=0)::close(fd);if(pcmFd>=0)::close(pcmFd);}
    size_t sourceSize() const {return validatedSource?validatedSource->length:(pcmFd>=0?pcmLength:pcm.size());}
    size_t outputEnd() const {return endByte==0?sourceSize():endByte;}
    void setRange(int64_t startFrame,int64_t endFrame) {
        const auto frames=sourceSize()/frameBytes;
        if(startFrame<0||endFrame<=startFrame||uint64_t(endFrame)>frames)throw std::runtime_error("Native PCM range must satisfy 0 <= start < end <= source frames");
        startByte=size_t(startFrame)*frameBytes;endByte=size_t(endFrame)*frameBytes;nextByte=completedByte=startByte;
    }
    void checkIntent() const {if(playbackFence.load()!=intentToken||stop)throw PlaybackCancelled();}
    void readSource(size_t offset,uint8_t* dst,size_t n) {
        if(offset>sourceSize()||n>sourceSize()-offset)throw std::runtime_error("PCM source read out of range");
        if(validatedSource){validatedSource->read(offset,dst,n);return;}
        if(pcmFd<0){std::memcpy(dst,pcm.data()+offset,n);return;}
        size_t done=0;while(done<n){ssize_t count=pread(pcmFd,dst+done,n-done,off_t(offset+done));if(count<0&&errno==EINTR)continue;if(count<=0)throw std::runtime_error("PCM source truncated/read failed");done+=size_t(count);}
    }
    void stage(const std::string& text) {
        auto ms=std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now()-initStarted).count();
        std::string line="+"+std::to_string(ms)+"ms "+text;
        {std::lock_guard<std::mutex> lock(mutex);initTrace+=line+"\n";}
        log(line);
    }
    // 96 kHz experimental startup. A GET_CUR reply is not physical clock proof.
    int configureOutput() {
        selected=true; // Cleanup must return to idle even if a later setup step fails.
        stage(rate==96000?"init strategy=96K_IDLE_FORCE_SET_V1 (experimental)":"init strategy=48K_BASELINE_V1");
        if(rate==96000) {
            int rc=setAlt(fd,iface,0);stage("idle SET_INTERFACE IF="+std::to_string(iface)+" alt=0 rc="+std::to_string(rc));
            requireIoctl(rc,"96k idle SET_INTERFACE");
            int reported=getAlt(fd,iface);stage("idle GET_INTERFACE="+std::to_string(reported));
            if(reported!=0)throw std::runtime_error("96k idle alternate not confirmed");
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
            stage("idle settling delay=10ms (experimental, not a device guarantee)");
        }
        int rc=setAlt(fd,iface,alt);stage("active SET_INTERFACE IF="+std::to_string(iface)+" alt="+std::to_string(alt)+" rc="+std::to_string(rc));
        requireIoctl(rc,"SET_INTERFACE exact alternate");
        int reported=getAlt(fd,iface);stage("active GET_INTERFACE="+std::to_string(reported));
        if(reported!=alt)throw std::runtime_error("GET_INTERFACE did not confirm selected alternate");
        int before=getRate(fd,endpoint);stage("endpoint GET_CUR before SET="+std::to_string(before)+" Hz; EP="+std::to_string(endpoint));
        if(oldRate<0&&before>0)oldRate=before; // Fallback cleanup state, distinct from a pre-claim observation.
        if(rate==96000||before!=rate) {
            int result=setRate(fd,endpoint,rate);
            stage("endpoint SET_CUR="+std::to_string(rate)+" Hz rc="+std::to_string(result)+(rate==96000?" forced even when reported equal":" changed from prior report"));
            if(result!=3)throw std::runtime_error(failure("SET_CUR exact frequency (3-byte transfer required)"));
        } else stage("endpoint SET_CUR skipped: matching 48k report (baseline retained)");
        if(rate==96000)std::this_thread::sleep_for(std::chrono::milliseconds(20));
        int actual=getRate(fd,endpoint);stage("endpoint GET_CUR confirmation1="+std::to_string(actual)+" Hz");
        if(actual!=rate)throw std::runtime_error("GET_CUR did not confirm exact frequency; reported="+std::to_string(actual));
        if(rate==96000) {
            std::this_thread::sleep_for(std::chrono::milliseconds(20));
            int again=getRate(fd,endpoint);stage("endpoint GET_CUR confirmation2 (+20ms)="+std::to_string(again)+" Hz");
            if(again!=rate)throw std::runtime_error("96k repeated clock readback mismatch");
        }
        stage("configured packet bytes="+std::to_string(packet)+"; frames/ms="+std::to_string(rate/1000)+"; no PCM sent during initialization");
        return actual;
    }
    void prepareVolume(int requestedVolume=std::numeric_limits<int>::min()) {
        uint8_t header[9]{};if(control(fd,0x80,6,0x0200,0,header,9)!=9)throw std::runtime_error("Configuration descriptor read failed; volume refused");
        int length=header[2]|(header[3]<<8);if(length<9||length>4096)throw std::runtime_error("Configuration length unsupported");
        std::vector<uint8_t> config(size_t(length),0);
        if(control(fd,0x80,6,0x0200,0,config.data(),length)!=length||!purebit::samsungVolumeTopology(config.data(),config.size()))
            throw std::runtime_error("FU5 master mute/volume playback topology not confirmed; refused");
        unsigned id=0;
        if(ioctl(fd,USBDEVFS_CLAIMINTERFACE,&id)<0){
            if(errno!=EBUSY)throw std::runtime_error(failure("CLAIM control IF0"));
            usbdevfs_disconnect_claim d{};d.interface=0;controlDisconnected=true;
            requireIoctl(ioctl(fd,USBDEVFS_DISCONNECT_CLAIM,&d),"DISCONNECT_CLAIM control IF0");
        }
        controlClaimed=true;
        originalMute=getMute(fd);originalVolume=getVolume(fd,0x81);
        int min=getVolume(fd,0x82),max=getVolume(fd,0x83),res=getVolume(fd,0x84);
        volumeReport="FU5 master: original="+std::to_string(originalVolume/256.0)+" dB, mute="+std::to_string(originalMute)+
            "; min="+std::to_string(min/256.0)+" max="+std::to_string(max/256.0)+" res="+std::to_string(res/256.0)+
            "; raw current/min/max/res="+std::to_string(originalVolume)+"/"+std::to_string(min)+"/"+std::to_string(max)+"/"+std::to_string(res)+"; no PCM scaling";
        safeVolume=purebit::quietVolume(originalVolume,min,max,res);
        volumeMin=min;volumeMax=max;volumeRes=res;
        if(requestedVolume!=std::numeric_limits<int>::min()) {
            if(requestedVolume<min||requestedVolume>max||(requestedVolume-min)%res!=0)
                throw std::runtime_error("Remembered user hardware volume outside device range/step");
            safeVolume=std::min(requestedVolume,originalVolume);
            safeVolume=min+((safeVolume-min)/res)*res;
            volumeReport+="; explicit prior user setting requested="+std::to_string(requestedVolume/256.0)+" dB; no automatic increase above current device gain";
        }
        volumeReport+="; target="+std::to_string(safeVolume/256.0)+" dB";
        volumeTouched=true;setMuteChecked(fd,1);setVolumeChecked(fd,safeVolume);volumePrepared=true;
        volumeReport+="; mute-before-SET and volume readback CONFIRMED; acoustic attenuation not independently verified";
    }
    void verifyMutedAfterConfiguration() {
        int volume=getVolume(fd,0x81),mute=getMute(fd);
        stage("post-clock FU5 volume raw="+std::to_string(volume)+" mute="+std::to_string(mute)+"; expected quiet volume and mute=1");
        if(volume!=safeVolume||mute!=1)throw std::runtime_error("Hardware controls changed during clock setup; no PCM sent");
    }
    void restore() noexcept {
        if(fd<0||restorationDone)return;
        restorationDone=true;
        try {
            std::ostringstream r;
            auto result=[&](const char* label,int rc){r<<label<<" rc="<<rc;if(rc<0)r<<" errno="<<errno<<" ("<<std::strerror(errno)<<")";r<<"; ";};
            if(volumeTouched){try{setMuteChecked(fd,1);r<<"hardware mute confirmed; ";}catch(const std::exception& e){r<<"mute cleanup failed: "<<e.what()<<"; ";}}
            if(selected) {
                if(oldRate>0){result("restore clock",setRate(fd,endpoint,oldRate));r<<"original clock confirmed="<<originalRateKnown<<"; ";}
                else r<<"original clock UNKNOWN; ";
                result("restore alternate",setAlt(fd,iface,volumeTouched?0:(oldAlt>=0?oldAlt:0)));r<<"value="<<(volumeTouched?0:oldAlt)<<" original alternate confirmed="<<originalAltKnown<<"; ";
            }
            if(volumeTouched){
                try{if(volumePrepared)setVolumeChecked(fd,safeVolume);setMuteChecked(fd,originalMute);r<<"hardware volume retained at last confirmed setting; no automatic gain increase; ";}
                catch(const std::exception& e){r<<"hardware cleanup failed: "<<e.what()<<"; unplug/replug with playback stopped; ";}
            }
            if(claimed){unsigned int n=iface;result("release",ioctl(fd,USBDEVFS_RELEASEINTERFACE,&n));claimed=false;}
            if(disconnectAttempted){usbdevfs_ioctl c{};c.ifno=iface;c.ioctl_code=USBDEVFS_CONNECT;result("driver reconnect",ioctl(fd,USBDEVFS_IOCTL,&c));r<<"Android availability UNKNOWN; ";}
            if(controlClaimed){unsigned id=0;result("control IF0 release",ioctl(fd,USBDEVFS_RELEASEINTERFACE,&id));controlClaimed=false;}
            if(controlDisconnected){usbdevfs_ioctl c{};c.ifno=0;c.ioctl_code=USBDEVFS_CONNECT;result("control driver reconnect",ioctl(fd,USBDEVFS_IOCTL,&c));r<<"Android availability UNKNOWN; ";}
            {std::lock_guard<std::mutex> lock(mutex);cleanup=r.str();}log(r.str());
        }catch(...){log("restore logging failed; unplug/replug required");}
    }
    void submit(Slot& slot) {
        checkIntent();
        if(nextByte>=outputEnd())return;
        size_t n=std::min(size_t(packet*8),outputEnd()-nextByte);
        slot.reference.resize(n);readSource(nextByte,slot.reference.data(),n);
        std::memcpy(slot.buffer.data(),slot.reference.data(),n);slot.sourceOffset=nextByte;
        if(std::memcmp(slot.buffer.data(),slot.reference.data(),n)!=0)throw std::runtime_error("pre-submit PCM mismatch");
        const size_t alloc=sizeof(usbdevfs_urb)+8*sizeof(usbdevfs_iso_packet_desc);
        std::memset(slot.urb,0,alloc);auto& u=*slot.urb;
        u.type=USBDEVFS_URB_TYPE_ISO;u.endpoint=endpoint;u.flags=USBDEVFS_URB_ISO_ASAP;
        u.buffer=slot.buffer.data();u.buffer_length=int(n);u.number_of_packets=int((n+packet-1)/packet);u.usercontext=&slot;
        size_t left=n;for(int i=0;i<u.number_of_packets;i++){unsigned len=unsigned(std::min(left,size_t(packet)));u.iso_frame_desc[i].length=len;left-=len;}
        checkIntent();
        requireIoctl(ioctl(fd,USBDEVFS_SUBMITURB,&u),"SUBMITURB (no Android fallback)");
        slot.queued=true;nextByte+=n;std::lock_guard<std::mutex> lock(mutex);submitted+=n/frameBytes;
    }
    void reap(Slot& slot) {
        auto& u=*slot.urb;
        if(!slot.queued||u.status!=0||u.error_count!=0)throw std::runtime_error("ISO URB completion failure status="+std::to_string(u.status)+" errors="+std::to_string(u.error_count));
        for(int i=0;i<u.number_of_packets;i++)if(u.iso_frame_desc[i].status!=0||u.iso_frame_desc[i].actual_length!=u.iso_frame_desc[i].length)
            throw std::runtime_error("ISO packet failed/short status="+std::to_string(u.iso_frame_desc[i].status)+" actual="+std::to_string(u.iso_frame_desc[i].actual_length)+" requested="+std::to_string(u.iso_frame_desc[i].length));
        if(slot.sourceOffset!=completedByte)throw std::runtime_error("Out-of-order ISO completion; comparison refused");
        size_t n=size_t(u.buffer_length);
        bool match=std::memcmp(slot.reference.data(),slot.buffer.data(),n)==0;
        {std::lock_guard<std::mutex> lock(mutex);equal=equal&&match;reference.update(slot.reference.data(),n);wire.update(slot.buffer.data(),n);completed+=n/frameBytes;}
        if(!match)throw std::runtime_error("USB output buffer mismatch");
        completedByte+=n;slot.queued=false;
    }
    void verifyVolume() {
        if(!volumePrepared)return;
        int volume=getVolume(fd,0x81),mute=getMute(fd);
        {std::lock_guard<std::mutex> lock(mutex);lastVolume=volume;lastMute=mute;volumeChecks++;}
        if(volume!=safeVolume||mute!=0)
            throw std::runtime_error("Hardware volume/mute changed; music stopped: volume raw="+std::to_string(volume)+" mute="+std::to_string(mute));
    }
    void adjustVolume() {
        int direction=pendingVolumeDirection.exchange(0);
        if(direction==0||!volumePrepared)return;
        checkIntent();verifyVolume();
        int step=((256+volumeRes-1)/volumeRes)*volumeRes; // At least 1 dB, quantized to the device step.
        int target=std::clamp(safeVolume+direction*step,volumeMin,volumeMax);
        if(target==safeVolume)return;
        setVolumeChecked(fd,target);
        if(getMute(fd)!=0)throw std::runtime_error("Hardware mute changed during user volume adjustment");
        {std::lock_guard<std::mutex> lock(mutex);safeVolume=target;lastVolume=target;volumeUpdates++;}
        log("USER hardware volume SET/readback="+std::to_string(target/256.0)+" dB; PCM untouched");
    }
    void begin() {
        if(thread.joinable()||fd<0||restorationDone)throw std::runtime_error("USB session cannot begin twice/after close");
        checkIntent();
        if(volumePrepared) {
            verifyMutedAfterConfiguration();checkIntent();
            setMuteChecked(fd,0);checkIntent();
            stage("explicit BEGIN: FU5 mute=0 SET/readback confirmed; current intent; volume not raised");
        }
        checkIntent();running=true;thread=std::thread([this]{run();});
    }
    void run() noexcept {
        try {
            checkIntent();
            verifyVolume(); // Fresh check before the very first PCM packet.
            stage(volumePrepared?"first PCM submission follows hardware verification; USB ISO queue=4x8 packets":"first PCM submission: legacy low-level peak gate; hardware volume UNKNOWN; USB ISO queue=4x8 packets");
            for(auto& s:slots)submit(s);
            auto last=std::chrono::steady_clock::now();
            auto volumeCheck=last;
            while(!stop&&completedByte<outputEnd()) {
                adjustVolume();
                checkIntent();
                if(volumePrepared&&std::chrono::steady_clock::now()-volumeCheck>std::chrono::milliseconds(250)) {
                    verifyVolume();
                    volumeCheck=std::chrono::steady_clock::now();
                }
                void* result=nullptr;
                int rc=ioctl(fd,USBDEVFS_REAPURBNDELAY,&result);
                if(rc<0){
                    if(errno!=EAGAIN)throw std::runtime_error(failure("REAPURBNDELAY"));
                    if(std::chrono::steady_clock::now()-last>std::chrono::seconds(2))throw std::runtime_error("ISO completion timeout (>2s); stopped");
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));continue;
                }
                auto it=std::find_if(slots.begin(),slots.end(),[&](const Slot& s){return s.urb==result;});
                if(it==slots.end())throw std::runtime_error("Unknown URB completion pointer");
                bool firstCompletion=false;{std::lock_guard<std::mutex> lock(mutex);firstCompletion=completed==0;}
                reap(*it);if(firstCompletion)stage("first successful kernel ISO completion; DAC samples still unverified");last=std::chrono::steady_clock::now();if(!stop)submit(*it);
            }
        } catch(const PlaybackCancelled& e) {std::lock_guard<std::mutex> lock(mutex);error=e.what();log(error);}
        catch(const std::exception& e) {std::lock_guard<std::mutex> lock(mutex);errors++;error=e.what();log(error);}
        for(auto& s:slots)if(s.queued)ioctl(fd,USBDEVFS_DISCARDURB,s.urb);
        // Reap cancelled URBs before restoring. No buffer is freed while the fd remains active.
        auto deadline=std::chrono::steady_clock::now()+std::chrono::milliseconds(500);
        while(std::any_of(slots.begin(),slots.end(),[](const Slot& s){return s.queued;})&&std::chrono::steady_clock::now()<deadline){
            void* p=nullptr;int rc=ioctl(fd,USBDEVFS_REAPURBNDELAY,&p);
            if(rc==0){for(auto& s:slots)if(s.urb==p)s.queued=false;}
            else if(errno!=EAGAIN)break;else std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }
        // close() of the final usbfs fd synchronously kills remaining URBs before slot destruction.
        restore();::close(fd);fd=-1;
        {std::lock_guard<std::mutex> lock(mutex);running=false;}
    }
};
void throwJava(JNIEnv* e,const std::string& text){e->ThrowNew(e->FindClass("java/lang/IllegalStateException"),text.c_str());}
std::string string(JNIEnv* e,jstring s){const char* p=e->GetStringUTFChars(s,nullptr);if(!p)throw std::runtime_error("JNI string allocation failed");std::string out(p);e->ReleaseStringUTFChars(s,p);return out;}
Session* session(jlong h){return reinterpret_cast<Session*>(h);}
}

extern "C" JNIEXPORT jstring JNICALL Java_app_purebit_audio_NativeUsb_probe(JNIEnv* e,jobject,jint fd){
    int speed=ioctl(fd,USBDEVFS_GET_SPEED,nullptr);int speedErr=errno;unsigned caps=0;int rc=ioctl(fd,USBDEVFS_GET_CAPABILITIES,&caps);int capsErr=errno;
    std::ostringstream out;out<<"READ-ONLY usbfs probe: speed="<<speed<<" (FULL="<<USB_SPEED_FULL<<", HIGH="<<USB_SPEED_HIGH<<")";
    if(speed<0)out<<" errno="<<speedErr<<" "<<std::strerror(speedErr);
    out<<"; capabilities rc="<<rc<<" flags=0x"<<std::hex<<caps;if(rc<0)out<<" errno="<<std::dec<<capsErr;
    out<<"; no claim/detach/SET/PCM performed; isochronous permission still unverified";return e->NewStringUTF(out.str().c_str());
}
extern "C" JNIEXPORT jlong JNICALL Java_app_purebit_audio_NativeUsb_start(JNIEnv* e,jobject,jint original,jstring path,jstring expectedSha,
        jint rate,jint bits,jint channels,jint iface,jint alt,jint endpoint,jint maxPacket,jlong startFrame,jboolean hardwareMusic,jlong intentToken,jint requestedVolume,jlong endFrameExclusive){
    auto s=std::make_unique<Session>();
    try {
        s->intentToken=uint64_t(intentToken);s->checkIntent();
        s->packet=purebit::packetBytes(rate,bits,channels,maxPacket);s->frameBytes=bits/8*channels;s->rate=rate;s->bits=bits;
        if(iface!=2||endpoint!=3||alt!=bits/8-1)throw std::runtime_error("Unexpected Samsung alternate/interface/endpoint; refused");
        s->iface=iface;s->alt=alt;s->endpoint=endpoint;
        bool reused=false;
        s->validatedSource=sourceCache.acquire(string(e,path),string(e,expectedSha),bits,s->frameBytes,reused,[&]{s->checkIntent();});
        s->stage(reused?"PCM validation REUSED: pinned read-only fd; device/inode/size/mtime/ctime unchanged; per-buffer checks retained":"PCM validation NEW: full source SHA-256 matched Java cache; pinned read-only fd retained for this source");
        if(!hardwareMusic&&s->sourceSize()>size_t(rate)*s->frameBytes*30)throw std::runtime_error("Low-level test mode: <=30s only");
        if(!hardwareMusic&&!s->validatedSource->peakSafe)throw std::runtime_error("SAFETY REFUSED: test PCM peak exceeds -60 dBFS; no scaling");
        s->setRange(startFrame,endFrameExclusive);
        s->stage("PCM output frame range=["+std::to_string(startFrame)+","+std::to_string(endFrameExclusive)+"); exact source bytes; no samples at/after exclusive end");
        s->checkIntent();
        s->fd=dup(original);requireIoctl(s->fd,"dup(permission fd)");
        int speed=ioctl(s->fd,USBDEVFS_GET_SPEED,nullptr);requireIoctl(speed,"GET_SPEED");
        if(speed!=USB_SPEED_FULL)throw std::runtime_error("Only measured FULL speed implemented; actual speed="+std::to_string(speed));
        uint8_t descriptor[18]{};int descRc=control(s->fd,0x80,6,0x0100,0,descriptor,18);requireIoctl(descRc,"GET_DESCRIPTOR");
        if(descRc!=18||descriptor[8]!=0xe8||descriptor[9]!=4||descriptor[10]!=0x5b||descriptor[11]!=0xa0)
            throw std::runtime_error("Samsung VID/PID device descriptor check failed");
        s->oldAlt=getAlt(s->fd,iface);s->oldRate=getRate(s->fd,endpoint);
        s->stage("pre-claim IF2 alternate="+std::to_string(s->oldAlt)+" endpoint clock="+std::to_string(s->oldRate)+" Hz; negative=read failure/unknown");
        s->originalAltKnown=s->oldAlt>=0;s->originalRateKnown=s->oldRate>0;
        s->checkIntent();
        if(hardwareMusic){s->prepareVolume(requestedVolume);s->stage("FU5 hardware mute=1 and quiet volume SET/readback confirmed before streaming configuration");}
        unsigned id=iface;
        if(ioctl(s->fd,USBDEVFS_CLAIMINTERFACE,&id)<0){
            if(errno!=EBUSY)throw std::runtime_error(failure("CLAIMINTERFACE"));
            usbdevfs_disconnect_claim d{};d.interface=iface;s->disconnectAttempted=true;
            requireIoctl(ioctl(s->fd,USBDEVFS_DISCONNECT_CLAIM,&d),"DISCONNECT_CLAIM (authorized experimental takeover)");
        }
        s->claimed=true;
        if(s->oldAlt<0)s->oldAlt=getAlt(s->fd,iface);
        s->stage("streaming IF2 claim/takeover confirmed; disconnect attempt="+std::to_string(s->disconnectAttempted));
        int actual=s->configureOutput();
        s->checkIntent();
        for(auto& slot:s->slots){slot.urb=static_cast<usbdevfs_urb*>(std::calloc(1,sizeof(usbdevfs_urb)+8*sizeof(usbdevfs_iso_packet_desc)));if(!slot.urb)throw std::bad_alloc();slot.buffer.resize(size_t(s->packet*8));}
        s->setup="USB Direct native usbfs: FULL speed; IF="+std::to_string(iface)+" alt="+std::to_string(alt)+" EP="+std::to_string(endpoint)+
            "; GET_INTERFACE confirmed; endpoint GET_CUR="+std::to_string(actual)+" Hz; selected valid bits="+std::to_string(bits)+
            "; original alternate confirmed="+std::to_string(s->originalAltKnown)+" original clock confirmed="+std::to_string(s->originalRateKnown)+
            "; "+s->volumeReport+(hardwareMusic?"; hardware-gated music mode":"; -60 dBFS PCM peak gate passed")+"; no AudioTrack; DEVICE PATH NOT VERIFIED";
        // Preparation cannot unmute or submit PCM. The caller must validate its intent then begin.
        if(hardwareMusic)s->verifyMutedAfterConfiguration();
        s->checkIntent();s->stage("PREPARED ONLY: no unmute/thread/PCM; waiting for explicit begin with current playback intent");
        log(s->setup);return reinterpret_cast<jlong>(s.release());
    }catch(const std::exception& error){std::string text="USB Direct FAILED: "+std::string(error.what());s->stage("startup failed: "+std::string(error.what()));s->restore();text+="\nINITIALIZATION TRACE\n"+s->initTrace+"\n"+s->cleanup;throwJava(e,text);return 0;}
}
extern "C" JNIEXPORT jlong JNICALL Java_app_purebit_audio_NativeUsb_invalidatePreparation(JNIEnv*,jobject){
    return jlong(playbackFence.fetch_add(1)+1);
}
extern "C" JNIEXPORT void JNICALL Java_app_purebit_audio_NativeUsb_begin(JNIEnv* e,jobject,jlong h){
    auto* s=session(h);
    try {
        s->begin();
    }catch(const std::exception& error){s->restore();throwJava(e,std::string("USB begin refused: ")+error.what());}
}
extern "C" JNIEXPORT void JNICALL Java_app_purebit_audio_NativeUsb_releaseSource(JNIEnv* e,jobject,jstring path){
    try{sourceCache.release(string(e,path));}catch(const std::exception& error){throwJava(e,error.what());}
}
extern "C" JNIEXPORT jstring JNICALL Java_app_purebit_audio_NativeUsb_volumeTest(JNIEnv* e,jobject,jint original){
    auto s=std::make_unique<Session>();
    try {
        s->fd=dup(original);requireIoctl(s->fd,"dup(volume permission fd)");
        uint8_t d[18]{};if(control(s->fd,0x80,6,0x0100,0,d,18)!=18||d[8]!=0xe8||d[9]!=4||d[10]!=0x5b||d[11]!=0xa0)throw std::runtime_error("Samsung VID/PID check failed");
        s->prepareVolume();s->restore();
        std::string report="HARDWARE VOLUME CONFIGURATION TEST: SET/readback passed; NO PCM transmitted\n"+s->volumeReport+"\n"+s->cleanup+
            "\nPureBit did not restore a louder hardware volume; Android reconnect may change it. Actual acoustic attenuation UNVERIFIED. General music rechecks on every PLAY.";
        return e->NewStringUTF(report.c_str());
    }catch(const std::exception& error){std::string report="HARDWARE VOLUME TEST FAILED: "+std::string(error.what());s->restore();report+="\n"+s->volumeReport+"\n"+s->cleanup+"\nNO PCM transmitted; general music remains blocked without a fresh successful handshake.";return e->NewStringUTF(report.c_str());}
}
extern "C" JNIEXPORT jlongArray JNICALL Java_app_purebit_audio_NativeUsb_poll(JNIEnv* e,jobject,jlong h){
    auto* s=session(h);std::lock_guard<std::mutex> lock(s->mutex);jlong values[]={s->running?1:0,jlong(s->completed),jlong(s->submitted),s->errors,s->equal?1:0,s->safeVolume,s->volumeMin,s->volumeMax,s->volumeRes,jlong(s->volumeUpdates)};
    jlongArray out=e->NewLongArray(10);if(out)e->SetLongArrayRegion(out,0,10,values);return out;
}
extern "C" JNIEXPORT void JNICALL Java_app_purebit_audio_NativeUsb_volumeStep(JNIEnv*,jobject,jlong h,jint direction){
    auto* s=session(h);if(direction!=1&&direction!=-1)return;
    std::lock_guard<std::mutex> lock(s->mutex);
    if(s->running&&s->volumePrepared)s->pendingVolumeDirection=direction;
}
extern "C" JNIEXPORT jstring JNICALL Java_app_purebit_audio_NativeUsb_details(JNIEnv* e,jobject,jlong h){
    auto* s=session(h);std::lock_guard<std::mutex> lock(s->mutex);std::ostringstream out;out<<s->setup<<"\nRunning="<<s->running<<" submitted frames="<<s->submitted<<" kernel-completed frames="<<s->completed<<" errors="<<s->errors;
    out<<"\nINITIALIZATION TRACE\n"<<s->initTrace;
    out<<"\nPCM source frame range=["<<s->startByte/s->frameBytes<<","<<s->outputEnd()/s->frameBytes<<"); exclusive end; no device sample verification";
    out<<"\nUSER HARDWARE VOLUME: accepted updates="<<s->volumeUpdates<<" current target="<<s->safeVolume/256.0<<" dB; no PCM scaling";
    out<<"\nPLAYBACK HARDWARE READBACK: checks="<<s->volumeChecks;
    if(s->volumeChecks>0)out<<" last volume="<<s->lastVolume/256.0<<" dB (raw="<<s->lastVolume<<") last mute="<<s->lastMute<<" (0=unmuted, 1=muted); target="<<s->safeVolume/256.0<<" dB; initial then every ~250ms; device-reported controls, acoustic output UNKNOWN";
    else out<<" NOT VERIFIED";
    out<<"\nAPP USB OUTPUT BUFFER "<<(s->completed>0&&s->equal?"VERIFIED":"NOT VERIFIED")<<" (successful ISO completions; not DAC sample capture)";
    out<<"\nReference SHA-256: "<<s->reference.hex()<<"\nUSB payload SHA-256: "<<s->wire.hex();
    out<<"\nError: "<<s->error<<"\nCleanup: "<<s->cleanup<<"\nPhysical DAC samples / HAL DSP: UNKNOWN; BIT-PERFECT NOT VERIFIED";return e->NewStringUTF(out.str().c_str());
}
extern "C" JNIEXPORT jstring JNICALL Java_app_purebit_audio_NativeUsb_close(JNIEnv* e,jobject,jlong h){
    auto* s=session(h);s->stop=true;if(s->thread.joinable())s->thread.join();
    else s->restore(); // Prepared-but-cancelled sessions still release the interfaces and retain quiet gain.
    jstring report=Java_app_purebit_audio_NativeUsb_details(e,nullptr,h);delete s;return report;
}
