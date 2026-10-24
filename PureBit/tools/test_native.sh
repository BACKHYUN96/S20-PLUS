#!/usr/bin/env bash
set -euo pipefail
purebit_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
purebit_temp="$(mktemp -d)"
trap 'rm -rf "$purebit_temp"' EXIT
test -f "${JAVA_HOME:?Set JAVA_HOME to a full JDK}/include/jni.h"
"${CXX:-g++}" -std=c++17 -Wall -Wextra -Werror -pthread -fsanitize=address,undefined -fno-omit-frame-pointer \
    -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -I"$purebit_root/app/src/test/native/include" \
    "$purebit_root/app/src/test/native/usb_native_test.cpp" -Wl,--wrap=ioctl -Wl,--wrap=close -o "$purebit_temp/usb-native-test"
"$purebit_temp/usb-native-test"
