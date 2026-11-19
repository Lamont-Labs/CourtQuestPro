"""Add bounded, pose-specific skin and footwear mattes without changing approved athlete art."""
import io
import json
import struct
import hashlib
from pathlib import Path
from PIL import Image, ImageChops, ImageOps, ImageDraw, ImageFilter
from build_assets import POSES, OUT, encode, mask

SKIN = {
 "standing":[[(468,137),(503,155),(547,136),(548,170),(505,194),(468,166)],
  [(429,181),(438,245),(427,290),(398,371),(364,440),(351,472),(325,490),(305,479),(307,453),(338,381),(369,309),(379,233),(397,191)],
  [(588,180),(611,202),(623,259),(630,339),(650,427),(660,494),(645,531),(624,530),(624,487),(611,423),(591,355),(581,278)],
  [(400,632),(492,636),(469,724),(442,826),(414,824),(417,753),(411,697)],
  [(526,635),(624,634),(622,709),(619,826),(591,829),(573,754),(549,702)]],
 "dribbling":[[(502,150),(550,169),(578,151),(579,185),(543,197),(501,181),(475,165)],
  [(431,172),(419,238),(393,288),(376,361),(360,447),(352,527),(365,583),(349,620),(329,631),(307,612),(315,579),(317,512),(332,416),(346,327),(377,229),(394,189)],
  [(611,200),(633,235),(643,287),(665,367),(693,470),(713,565),(747,606),(768,633),(755,646),(721,622),(700,611),(681,582),(667,516),(642,447),(610,358),(596,302)],
  [(345,578),(414,563),(455,584),(439,626),(413,676),(368,735),(317,815),(294,801),(329,711),(351,651)],
  [(570,613),(612,582),(641,561),(674,610),(678,658),(660,719),(643,797),(613,796),(615,720),(603,672)]],
 "action":[[(440,129),(462,151),(482,135),(485,157),(448,172),(410,153)],
  [(382,160),(409,168),(426,211),(458,234),(538,192),(569,161),(597,153),(606,176),(585,208),(547,237),(478,280),(431,303),(400,281),(372,238),(365,194)],
  [(516,174),(544,197),(560,224),(585,215),(601,193),(617,166),(634,176),(627,211),(608,249),(580,269),(554,268),(530,246)],
  [(399,613),(448,598),(459,615),(447,696),(408,773),(378,850),(355,842),(371,783),(387,706)],
  [(524,404),(543,365),(572,351),(592,367),(605,396),(599,445),(585,487),(578,529),(548,527),(540,481),(520,451)]]
}
SHOES={
 "standing":[[(365,843),(445,843),(450,985),(351,985)],[(581,846),(656,846),(679,985),(571,985)]],
 "dribbling":[[(256,835),(332,835),(334,990),(208,990)],[(590,819),(658,818),(699,948),(551,959)]],
 "action":[[(328,864),(389,859),(403,1020),(300,1020)],[(550,578),(648,553),(671,665),(519,683)]]
}

def read(path):
    with path.open("rb") as f:
        header=f.read(9);magic,w,h,n=struct.unpack(">4sHHB",header)
        assert magic==b"CQP2" and n==11
        layers=[]
        for _ in range(n):
            layers.append(f.read(struct.unpack(">I",f.read(4))[0]))
        assert not f.read(1)
    return layers

def main():
    manifest={}
    for pose,info in POSES.items():
        original=read(OUT/f"{pose}.pcmat")
        source=Image.open(io.BytesIO(original[0])).convert("RGBA")
        alpha=source.getchannel("A")
        skin=mask(SKIN[pose]+[info["face"]],alpha)
        cloth=ImageChops.add(Image.open(io.BytesIO(original[2])).getchannel("A"),
            Image.open(io.BytesIO(original[3])).getchannel("A")).point(lambda x:255 if x>=254 else x)
        skin=ImageChops.subtract(skin,cloth)
        # Preserve source light/shadow; all color is applied at runtime inside this matte.
        skin_plate=source.copy();skin_plate.putalpha(skin)
        shoe=mask(SHOES[pose],alpha)
        # Keep bright original soles/socks rather than dyeing their white rubber.
        luminance=ImageOps.grayscale(source)
        shoe=ImageChops.multiply(shoe,luminance.point(lambda x:255 if x<190 else 0))
        shoe_plate=source.copy();shoe_plate.putalpha(shoe)
        dest=OUT/f"{pose}-avatar.pcmat"
        with dest.open("wb") as f:
            f.write(struct.pack(">4sHHB",b"CQP2",1024,1024,2))
            for im in (skin_plate,shoe_plate):
                data=encode(im);f.write(struct.pack(">I",len(data)));f.write(data)
        manifest[pose]={"file":dest.name,"sha256":hashlib.sha256(dest.read_bytes()).hexdigest(),
            "skin_polygons":SKIN[pose],"shoe_polygons":SHOES[pose]}
    (OUT/"avatar-manifest.json").write_text(json.dumps(manifest,indent=2)+"\n")

if __name__=="__main__":main()
