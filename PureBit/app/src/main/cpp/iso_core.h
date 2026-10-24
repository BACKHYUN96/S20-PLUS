#pragma once
#include <array>
#include <algorithm>
#include <cstdint>
#include <cstring>
#include <iomanip>
#include <sstream>
#include <stdexcept>
#include <vector>

namespace purebit {
// SHA-256 per FIPS 180-4. Used as a PCM checksum, not as a security/authenticity claim.
class Sha256 {
    std::array<uint32_t, 8> h{0x6a09e667,0xbb67ae85,0x3c6ef372,0xa54ff53a,0x510e527f,0x9b05688c,0x1f83d9ab,0x5be0cd19};
    std::array<uint8_t,64> block{};
    uint64_t bytes=0; size_t used=0;
    static uint32_t r(uint32_t v,int n) { return (v>>n)|(v<<(32-n)); }
    void compress() {
        static constexpr uint32_t k[64]={
            0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
            0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
            0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
            0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
            0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
            0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
            0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
            0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2};
        uint32_t w[64];
        for(int i=0;i<16;i++) w[i]=(uint32_t(block[4*i])<<24)|(uint32_t(block[4*i+1])<<16)|(uint32_t(block[4*i+2])<<8)|block[4*i+3];
        for(int i=16;i<64;i++) w[i]=w[i-16]+(r(w[i-15],7)^r(w[i-15],18)^(w[i-15]>>3))+w[i-7]+(r(w[i-2],17)^r(w[i-2],19)^(w[i-2]>>10));
        uint32_t a=h[0],b=h[1],c=h[2],d=h[3],e=h[4],f=h[5],g=h[6],v=h[7];
        for(int i=0;i<64;i++) {
            uint32_t t1=v+(r(e,6)^r(e,11)^r(e,25))+((e&f)^(~e&g))+k[i]+w[i];
            uint32_t t2=(r(a,2)^r(a,13)^r(a,22))+((a&b)^(a&c)^(b&c));
            v=g;g=f;f=e;e=d+t1;d=c;c=b;b=a;a=t1+t2;
        }
        h[0]+=a;h[1]+=b;h[2]+=c;h[3]+=d;h[4]+=e;h[5]+=f;h[6]+=g;h[7]+=v;
    }
public:
    void update(const uint8_t* p,size_t n) {
        bytes+=n;
        while(n>0) { size_t take=std::min(n,64-used);std::memcpy(block.data()+used,p,take);p+=take;n-=take;used+=take;if(used==64){compress();used=0;} }
    }
    std::string hex() const {
        Sha256 s=*this; uint64_t bits=s.bytes*8;uint8_t one=0x80,zero=0;
        s.update(&one,1);while(s.used!=56) s.update(&zero,1);
        uint8_t end[8];for(int i=0;i<8;i++) end[7-i]=uint8_t(bits>>(8*i));s.update(end,8);
        std::ostringstream out;out<<std::hex<<std::setfill('0');for(auto value:s.h)out<<std::setw(8)<<value;return out.str();
    }
};
inline int packetBytes(int rate,int bits,int channels,int maxPacket) {
    if((rate!=48000&&rate!=96000)||(bits!=16&&bits!=24&&bits!=32)||channels!=2) throw std::runtime_error("unsupported exact PCM format");
    int n=rate/1000*(bits/8)*channels;
    if(maxPacket<n||maxPacket>1023) throw std::runtime_error("full-speed endpoint capacity invalid");
    return n;
}
inline bool safePeak(const std::vector<uint8_t>& pcm,int bits) {
    if(bits!=16&&bits!=24&&bits!=32) return false;
    const size_t width=bits/8;if(pcm.empty()||pcm.size()%width!=0)return false;
    const int64_t maximum=(int64_t(1)<<(bits-1))-1;
    for(size_t i=0;i<pcm.size();i+=width) {
        uint32_t raw=0;for(size_t b=0;b<width;b++)raw|=uint32_t(pcm[i+b])<<(8*b);
        int64_t value=raw;if(raw&(uint32_t(1)<<(bits-1)))value-=int64_t(1)<<bits;
        if(value<0)value=-value;
        if(value*1000>maximum)return false; // <=0.001 peak amplitude (-60 dBFS); no scaling.
    }
    return true;
}
}
