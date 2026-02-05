import sys
from guano import GuanoFile

# usage:
# python guano_delete.py <wavPath> key1 key2 ...

wav_path = sys.argv[1]
keys = sys.argv[2:]

g = GuanoFile(wav_path)

for k in keys:
    if k in g:
        del g[k]

g.write(wav_path)
print("OK")
