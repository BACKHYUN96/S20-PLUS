#!/usr/bin/env bash
set -euo pipefail
purebit_log_dir="${1:-purebit-device-logs}"
mkdir -p "$purebit_log_dir"
adb shell getprop ro.product.model > "$purebit_log_dir/model.txt"
adb shell getprop ro.build.version.release > "$purebit_log_dir/android.txt"
adb shell getprop ro.build.version.sdk > "$purebit_log_dir/sdk.txt"
adb shell getprop ro.build.fingerprint > "$purebit_log_dir/fingerprint.txt"
adb shell dumpsys media.audio_flinger > "$purebit_log_dir/audio-flinger.txt"
adb shell dumpsys media.audio_policy > "$purebit_log_dir/audio-policy.txt"
adb shell dumpsys audio > "$purebit_log_dir/audio-manager.txt"
adb shell dumpsys usb > "$purebit_log_dir/usb.txt"
adb logcat -d -v threadtime -s PureBitAudio:I '*:S' > "$purebit_log_dir/purebit-audio.log"
printf 'Saved diagnostics to %s\n' "$purebit_log_dir"
