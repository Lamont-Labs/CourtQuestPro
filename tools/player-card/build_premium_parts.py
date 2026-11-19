"""Original texture parts, fitted physical footwear models and non-destructive body plates.

Approved masters/plates are never modified. Shoe alpha is removed only from the
alternative body plate used when a physical footwear model is explicitly selected.
"""
import hashlib
import io
import json
import struct
from pathlib import Path
from PIL import Image,ImageChops,ImageDraw,ImageFilter,ImageOps
from build_assets import OUT,SOURCES,encode
from build_avatar_assets import read

FEET={
 "standing":[(350,890,453,989),(569,890,681,989)],
 "dribbling":[(208,849,338,992),(549,821,704,963)],
 "action":[(299,894,406,1024),(516,565,690,688)],
 "ready":[(331,846,435,961),(580,847,679,961)],
 "followthrough":[(367,868,451,974),(551,867,646,974)]
}

def pack(path,images):
    with path.open("wb") as f:
        w,h=images[0].size;f.write(struct.pack(">4sHHB",b"CQP2",w,h,len(images)))
        for im in images:
            data=encode(im);f.write(struct.pack(">I",len(data)));f.write(data)
    return {"file":path.name,"sha256":hashlib.sha256(path.read_bytes()).hexdigest(),"layers":len(images)}

def cut(source,box):
    im=source.crop(box);alpha=im.getchannel("A");bounds=alpha.getbbox()
    assert alpha.getextrema()==(0,255) and bounds
    return im.crop(bounds)

def component(source,seed):
    marked=source.getchannel("A").point(lambda v:255 if v>12 else 0)
    assert marked.getpixel(seed)==255
    ImageDraw.floodfill(marked,seed,128)
    selection=marked.point(lambda v:255 if v==128 else 0).filter(ImageFilter.MaxFilter(3))
    result=source.copy();result.putalpha(ImageChops.multiply(selection,source.getchannel("A")))
    bounds=result.getchannel("A").getbbox();assert bounds and bounds[2]-bounds[0]>200
    return result.crop(bounds)

def main():
    hair=Image.open(SOURCES/"avatar_hair_materials.png").convert("RGBA")
    hairs=[cut(hair,b) for b in [(0,0,512,510),(512,0,1024,510),(0,512,512,1024),(512,512,1024,1024)]]
    # Sprite cells are alpha-cut independently. Each shoe has distinct actual geometry.
    shoes=Image.open(SOURCES/"avatar_footwear_models.png").convert("RGBA")
    models=[component(shoes,seed) for seed in [(180,500),(505,500),(840,500)]]
    for name,im in zip(["low","high","strap"],models):
        preview=im.copy();preview.thumbnail((256,300),Image.Resampling.LANCZOS)
        preview.save(OUT/f"footwear-{name}.webp","WEBP",lossless=True,method=6)
    original=Image.open(io.BytesIO(read(OUT/"standing.pcmat")[0])).convert("RGBA").crop(FEET["standing"][0])
    original.save(OUT/"footwear-original.webp","WEBP",lossless=True,method=6)
    for name,im in zip(["curls","afro","locs","braids"],hairs):
        im.thumbnail((512,512),Image.Resampling.LANCZOS)
        im.save(OUT/f"hair-{name}.webp","WEBP",lossless=True,method=6)
    manifest={"poses":{},"parts":{}}
    for name in ["hair-curls","hair-afro","hair-locs","hair-braids","footwear-original","footwear-low","footwear-high","footwear-strap"]:
        p=OUT/f"{name}.webp";manifest["parts"][name]={"file":p.name,"sha256":hashlib.sha256(p.read_bytes()).hexdigest()}
    for pose,feet in FEET.items():
        original=read(OUT/f"{pose}.pcmat");body=Image.open(io.BytesIO(original[0])).convert("RGBA")
        remove=Image.new("L",body.size);d=ImageDraw.Draw(remove)
        for box in feet:d.rectangle(box,fill=255)
        clean=body.copy();clean.putalpha(ImageChops.subtract(body.getchannel("A"),remove))
        layers=[clean]
        for index,shoe in enumerate(models):
            layer=Image.new("RGBA",body.size)
            for side,(x0,y0,x1,y1) in enumerate(feet):
                # High collars rise above low-cut collars, rather than stretching the same silhouette.
                rise=18 if index==1 else 8 if index==2 else 0
                sprite=ImageOps.mirror(shoe) if side else shoe
                sprite=sprite.resize((x1-x0,y1-y0+rise),Image.Resampling.LANCZOS)
                layer.alpha_composite(sprite,(x0,y0-rise))
            layers.append(layer)
        manifest["poses"][pose]={**pack(OUT/f"{pose}-footwear.pcmat",layers),"feet":feet}
    (OUT/"premium-manifest.json").write_text(json.dumps(manifest,indent=2)+"\n")

if __name__=="__main__":main()
