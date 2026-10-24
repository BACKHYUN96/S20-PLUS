"""Deterministic full-range PCM fixtures for automated tests, NOT headphone playback.
Requires Python 3 and FFmpeg. FLAC bytes may vary by FFmpeg version; decoded PCM does not.
"""
from pathlib import Path
import subprocess
import tempfile
import wave

destination = Path(__file__).resolve().parents[1] / "app/src/test/resources/fixtures"
destination.mkdir(parents=True, exist_ok=True)
for bits in (16, 24):
    values = [-(1 << (bits-1)), -123, -1, 0, 1, 123, (1 << (bits-1))-1]
    samples = [values[(i*3) % len(values)] for i in range(257*2)]
    pcm = b"".join(v.to_bytes(bits//8, "little", signed=True) for v in samples)
    (destination / f"int{bits}.pcm").write_bytes(pcm)
    with tempfile.TemporaryDirectory() as directory:
        wav = Path(directory) / "input.wav"
        with wave.open(str(wav), "wb") as out:
            out.setnchannels(2); out.setsampwidth(bits//8); out.setframerate(96000); out.writeframes(pcm)
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", str(wav), "-c:a", "flac", "-compression_level", "5",
                        str(destination / f"int{bits}.flac")], check=True)
