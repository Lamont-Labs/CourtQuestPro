"""Original texture parts and pose-matched, non-destructive footwear plates.

Approved masters/plates are never modified. Shoe alpha is removed only from the
alternative body plate used when a physical footwear model is explicitly selected.
"""
import hashlib
import io
import json
import struct
from pathlib import Path
from PIL import Image,ImageChops,ImageDraw,ImageFilter
from build_assets import OUT,SOURCES,encode
from build_avatar_assets import read
from build_footwear_styles import SOURCES as SHOE_SOURCES

FEET={
 "standing":[(322,874,432,998),(591,874,701,999)],
 "dribbling":[(165,835,301,1003),(626,835,796,959)],
 "action":[(307,862,395,1013),(585,595,729,727)],
 "ready":[(331,847,434,958),(583,847,679,959)],
 "followthrough":[(368,866,445,971),(557,866,645,969)]
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
    shoe_sources=json.loads((SHOE_SOURCES/"manifest.json").read_text())
    for name in ["hair-curls","hair-afro","hair-locs","hair-braids","footwear-original","footwear-low","footwear-high","footwear-strap"]:
        p=OUT/f"{name}.webp";manifest["parts"][name]={"file":p.name,"sha256":hashlib.sha256(p.read_bytes()).hexdigest()}
    for pose,feet in FEET.items():
        original=read(OUT/f"{pose}.pcmat");body=Image.open(io.BytesIO(original[0])).convert("RGBA")
        old_shoe=Image.open(io.BytesIO(read(OUT/f"{pose}-avatar.pcmat",expected_count=2)[1])).getchannel("A")
        # Clear only the source shoe silhouette and a one-pixel antialias edge.
        # The previous broad rectangles also removed sock and ankle pixels.
        remove=old_shoe.point(lambda value:255 if value else 0).filter(ImageFilter.MaxFilter(3))
        clean=body.copy();clean.putalpha(ImageChops.subtract(body.getchannel("A"),remove))
        layers=[clean]
        pose_art={}
        for model in ("low","high","strap"):
            item=shoe_sources[f"{pose}/{model}"]
            cutout=Image.open(SHOE_SOURCES/item["file"]).convert("RGBA")
            if cutout.size!=tuple(item["dimensions"]):
                raise ValueError(f"Footwear source dimensions changed: {pose}/{model}")
            layer=Image.new("RGBA",body.size)
            layer.alpha_composite(cutout,tuple(item["origin"]))
            layers.append(layer)
            pose_art[model]={**item,"source":f"attached_assets/generated_images/player_shoe_edits/{item['file']}"}
        manifest["poses"][pose]={**pack(OUT/f"{pose}-footwear.pcmat",layers),"feet":feet,
            "attachment":"pose-matched source-coordinate cutout; no runtime scaling or rotation",
            "shoe_art":pose_art}
    (OUT/"premium-manifest.json").write_text(json.dumps(manifest,indent=2)+"\n")

if __name__=="__main__":main()
