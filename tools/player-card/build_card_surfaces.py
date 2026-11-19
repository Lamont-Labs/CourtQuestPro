"""Optimize original material plates; runtime lettering/athletes remain live native layers."""
import hashlib
import json
from pathlib import Path
from PIL import Image

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/"app/src/main/assets/player_cards"

def main():
    records={}
    for edition,crop in (("holographic",None),("classic",None),("battle",(96,0,928,1024))):
        name="card_material_classic_sports_v3.png" if edition=="classic" else f"card_material_{edition}_v3.png"
        source=ROOT/"attached_assets/generated_images"/name
        im=Image.open(source).convert("RGB")
        if crop: im=im.crop(crop)
        im=im.resize((828,1200),Image.Resampling.LANCZOS)
        path=OUT/f"surface-{edition}.webp"
        im.save(path,"WEBP",quality=91,method=6)
        records[edition]={"file":path.name,"size":list(im.size),"bytes":path.stat().st_size,
            "sha256":hashlib.sha256(path.read_bytes()).hexdigest(),"original_generated_source":source.name,
            "crop":crop,"contains_player_or_text":False}
    (OUT/"surface-manifest.json").write_text(json.dumps(records,indent=2)+"\n")

if __name__=="__main__":main()
