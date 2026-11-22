"""Validate foreground isolation using Android-rendered artwork crops and the registered master mattes."""
import argparse
import io
import json
import zipfile
from pathlib import Path
import numpy as np
from PIL import Image, ImageChops, ImageFilter

ROOT=Path(__file__).resolve().parents[2]
WIDTHS={"standing":560,"dribbling":780,"action":580,"ready":560,"followthrough":500}
COUNTS={"hair":14,"ink":6,"headwear":5,"skin":10,"build":4,"footwear":4,"shoes":7,"gear":6}

def check(folder):
    report={}
    for pose,width in WIDTHS.items():
        location=folder/pose
        with zipfile.ZipFile(ROOT/f"app/src/main/assets/player_cards/anatomy/{pose}.zip") as z:
            bounds=json.loads(z.read("bounds.json"))
            def mask(name):
                image=Image.new("L",(1024,1024));piece=Image.open(io.BytesIO(z.read(name+".webp"))).convert("RGBA")
                image.paste(piece.getchannel("A"),tuple(bounds[name][:2]))
                return image
            def project(m,size):
                w,h=size;factor=min(w/width,h/1024)
                x=w*.5-512*factor;y=(h-1024*factor)*.5
                return m.transform(size,Image.Transform.AFFINE,(1/factor,0,-x/factor,0,1/factor,-y/factor),Image.Resampling.BILINEAR)
            rows={}
            for group,expected in COUNTS.items():
                files=sorted(location.glob(f"{pose}-{group}-*.png"))
                assert len(files)==expected,(pose,group,len(files),expected)
                if group not in ("hair","ink","skin","footwear","shoes"):
                    rows[group]={"distinct_android_images":len(files)};continue
                baseline=Image.open(location/f"{pose}-{group}-original.png").convert("RGB")
                base=np.asarray(baseline).astype("int16")
                scores=[]
                for path in files:
                    variant=path.stem.split("-")[-1]
                    if variant=="original":continue
                    rendered=np.asarray(Image.open(path).convert("RGB")).astype("int16")
                    diff=np.max(np.abs(rendered-base),axis=-1)>4
                    if group=="skin": region=mask("skin")
                    elif group in ("footwear","shoes"):
                        region=mask("shoe-original")
                        if group=="footwear":region=ImageChops.lighter(region,mask("shoe-"+variant))
                    else:
                        region=mask("hair-original")
                        if group=="hair":region=ImageChops.lighter(region,mask("hair-"+variant))
                    roi=np.asarray(project(region,baseline.size).filter(ImageFilter.MaxFilter(11)))>0
                    inside=int(np.count_nonzero(diff&roi));outside=int(np.count_nonzero(diff&~roi))
                    assert inside>0,(pose,group,variant,"unchanged")
                    assert outside==0,(pose,group,variant,"outside registered anatomy",outside)
                    scores.append({"option":variant,"changed_pixels":inside,"outside_matte":outside})
                rows[group]=scores
        report[pose]=rows
    (folder/"pixel-isolation.json").write_text(json.dumps(report,indent=2)+"\n")
    return report

if __name__=="__main__":
    parser=argparse.ArgumentParser();parser.add_argument("folder",type=Path)
    check(parser.parse_args().folder)
