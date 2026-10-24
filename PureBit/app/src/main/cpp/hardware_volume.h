#pragma once
#include <cstdint>
#include <cstddef>
#include <stdexcept>

namespace purebit {
inline int quietVolume(int current,int minimum,int maximum,int resolution) {
    constexpr int ceiling=-50*256;
    if(minimum<=-32768||minimum>=maximum||maximum>0||resolution<=0||resolution>maximum-minimum||current<minimum||current>maximum)
        throw std::runtime_error("Hardware volume range/current/resolution unsupported; music refused");
    int requested=current<ceiling?current:ceiling;
    if(requested<minimum)throw std::runtime_error("Hardware cannot provide <=-50 dB; music refused");
    int target=minimum+(requested-minimum)/resolution*resolution;
    if(target>ceiling||target>current)throw std::runtime_error("Hardware volume would increase; refused");
    return target;
}
// Exact observed playback topology: USB input terminal 4 -> master FU5 -> output terminal 6.
inline bool samsungVolumeTopology(const uint8_t* data,size_t size) {
    bool control=false,input=false,unit=false,output=false;
    for(size_t pos=0;pos<size;) {
        size_t n=data[pos];if(n<2||pos+n>size)return false;
        const uint8_t* p=data+pos;
        if(p[1]==4&&n>=9)control=p[2]==0&&p[5]==1&&p[6]==1&&p[7]==0;
        if(control&&p[1]==0x24&&n>=3) {
            if(p[2]==2&&n>=12&&p[3]==4)input=p[4]==1&&p[5]==1&&p[7]==2&&p[8]==3&&p[9]==0;
            if(p[2]==6&&n>=13&&p[3]==5)unit=p[4]==4&&p[5]==2&&(p[6]&3)==3&&p[7]==0;
            if(p[2]==3&&n>=9&&p[3]==6)output=p[7]==5;
        }
        pos+=n;
    }
    return input&&unit&&output;
}
}
