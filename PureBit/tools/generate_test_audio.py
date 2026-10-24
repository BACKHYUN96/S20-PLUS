"""Manual test files: integer stereo WAV at 0.01 peak amplitude; optional FFmpeg FLAC."""
import math
from pathlib import Path
import shutil
import subprocess
import sys
import wave

destination = Path(sys.argv[1] if len(sys.argv) > 1 else "test-audio")
destination.mkdir(parents=True, exist_ok=True)
for bits, rate in ((16,44100),(24,48000),(24,96000),(32,96000)):
    name = destination / f"purebit-{bits}-{rate}-minus40dB.wav"
    frames = bytearray()
    for i in range(rate * 5):
        # Silence at both ends avoids an abrupt waveform edge; no playback DSP needed.
        amplitude = 0 if i < rate//2 or i >= rate*9//2 else 0.01
        sample = round(amplitude * ((1 << (bits-1))-1) * math.sin(2*math.pi*440*i/rate))
        frames.extend(sample.to_bytes(bits//8,"little",signed=True) * 2)
    with wave.open(str(name),"wb") as out:
        out.setnchannels(2); out.setsampwidth(bits//8); out.setframerate(rate); out.writeframes(frames)
    if bits in (16,24) and shutil.which("ffmpeg"):
        subprocess.run(["ffmpeg","-v","error","-y","-i",str(name),"-c:a","flac",str(name.with_suffix(".flac"))],check=True)
    print(name)
