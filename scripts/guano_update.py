import sys
from guano import GuanoFile

# usage:
# python guano_update.py <wavPath> key=value key=value ...

wav_path = sys.argv[1]
pairs = sys.argv[2:]

g = GuanoFile(wav_path)

for p in pairs:
    k, v = p.split("=", 1)
    g[k] = v

g.write(wav_path)
print("OK")
