#pragma once
#include "iso_core.h"
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <cerrno>
#include <map>
#include <memory>
#include <mutex>
#include <functional>

namespace purebit {
inline bool samePcmIdentity(const struct stat& a,const struct stat& b) {
    return a.st_dev==b.st_dev && a.st_ino==b.st_ino && a.st_size==b.st_size && a.st_mode==b.st_mode &&
        a.st_mtim.tv_sec==b.st_mtim.tv_sec && a.st_mtim.tv_nsec==b.st_mtim.tv_nsec &&
        a.st_ctim.tv_sec==b.st_ctim.tv_sec && a.st_ctim.tv_nsec==b.st_ctim.tv_nsec;
}
/** Process-local, pinned read-only source. Metadata is an invalidation guard, not a new hash proof. */
class ValidatedPcmSource {
    int fd=-1;
    struct stat identity{};
public:
    std::string path,sha;
    int bits=0,frameBytes=0;
    bool peakSafe=true;
    size_t length=0;
    explicit ValidatedPcmSource(const std::string& name):path(name) {
        fd=::open(name.c_str(),O_RDONLY|O_CLOEXEC);
        if(fd<0)throw std::runtime_error("PCM cache open failed");
        if(fstat(fd,&identity)<0){::close(fd);fd=-1;throw std::runtime_error("PCM cache stat failed");}
        if(!S_ISREG(identity.st_mode)||identity.st_size<=0||identity.st_size>int64_t(2)*1024*1024*1024) {
            ::close(fd);fd=-1;throw std::runtime_error("PCM cache length invalid (2 GiB maximum)");
        }
        length=size_t(identity.st_size);
    }
    ~ValidatedPcmSource(){if(fd>=0)::close(fd);}
    ValidatedPcmSource(const ValidatedPcmSource&)=delete;
    void checkUnchanged(bool checkPath=false) const {
        struct stat current{};
        if(fstat(fd,&current)<0||!samePcmIdentity(identity,current))
            throw std::runtime_error("Validated PCM cache changed; playback refused");
        if(checkPath&&(stat(path.c_str(),&current)<0||!samePcmIdentity(identity,current)))
            throw std::runtime_error("Validated PCM cache path replaced/deleted; playback refused");
    }
    void read(size_t offset,uint8_t* dst,size_t n) const {
        if(offset>length||n>length-offset)throw std::runtime_error("PCM source read out of range");
        checkUnchanged();
        size_t done=0;
        while(done<n) {
            ssize_t count=pread(fd,dst+done,n-done,off_t(offset+done));
            if(count<0&&errno==EINTR)continue;
            if(count<=0)throw std::runtime_error("PCM source truncated/read failed");
            done+=size_t(count);
        }
        checkUnchanged(); // Reject a concurrent write before these bytes can be submitted.
    }
};
class PcmSourceCache {
    std::mutex mutex;
    std::map<std::string,std::shared_ptr<ValidatedPcmSource>> sources;
public:
    std::shared_ptr<ValidatedPcmSource> acquire(const std::string& path,const std::string& expected,int bits,int frameBytes,bool& reused,
            const std::function<void()>& cancelCheck={}) {
        std::lock_guard<std::mutex> lock(mutex);
        if(cancelCheck)cancelCheck();
        auto it=sources.find(path);
        if(it!=sources.end()) {
            auto source=it->second;source->checkUnchanged(true);
            if(source->sha!=expected||source->bits!=bits||source->frameBytes!=frameBytes)
                throw std::runtime_error("Validated PCM cache identity/format/hash mismatch");
            reused=true;return source;
        }
        auto source=std::make_shared<ValidatedPcmSource>(path);
        if(frameBytes<=0||source->length%size_t(frameBytes)!=0||(bits!=16&&bits!=24&&bits!=32))
            throw std::runtime_error("PCM cache frame alignment/format invalid");
        source->bits=bits;source->frameBytes=frameBytes;
        Sha256 hash;std::vector<uint8_t> block(size_t(frameBytes)*8192);
        for(size_t pos=0;pos<source->length;) {
            if(cancelCheck)cancelCheck();
            size_t n=std::min(block.size(),source->length-pos);source->read(pos,block.data(),n);hash.update(block.data(),n);
            if(source->peakSafe) {
                std::vector<uint8_t> samples(block.begin(),block.begin()+n);
                source->peakSafe=safePeak(samples,bits);
            }
            pos+=n;
        }
        source->checkUnchanged(true);
        if(cancelCheck)cancelCheck();
        source->sha=hash.hex();
        if(source->sha!=expected)throw std::runtime_error("Java cache/native source SHA-256 mismatch");
        sources[path]=source;reused=false;return source;
    }
    void release(const std::string& path) {std::lock_guard<std::mutex> lock(mutex);sources.erase(path);}
};
}
