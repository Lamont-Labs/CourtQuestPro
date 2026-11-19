"""Build offline, pose-specific textile lighting masks from original CourtQuest artwork.

The polygons are hand-authored garment/face boundaries, not automatic skin selection.
Original generated PNGs stay in ignored generated-artifacts; optimized production plates
and the complete reproducible boundary manifest are versioned with the app.
"""
import argparse
import hashlib
import io
import json
import struct
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageOps

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "app/src/main/assets/player_cards"
SOURCES = ROOT / "attached_assets/generated_images"

POSES = {
    "ready": {
        "source":"player_card_ready.png",
        "shirt":[(442,174),(478,158),(514,194),(552,160),(591,177),(591,237),(604,288),(594,356),(594,393),(565,409),(441,408),(421,393),(433,311),(433,241)],
        "shorts":[(428,416),(592,416),(608,468),(633,628),(590,644),(525,645),(509,552),(493,644),(431,644),(392,630),(410,470)],
        "collar":[(477,158),(514,193),(552,160),(557,170),(514,207),(472,169)],
        "arms":[[(442,175),(450,175),(443,238),(438,275),(430,273),(435,236)],[(583,175),(592,177),(592,239),(605,279),(598,284),(584,243)]],
        "face":[(478,57),(513,43),(547,59),(548,109),(536,141),(514,156),(490,140),(477,108)],
        "occlusions":[[(377,396),(404,393),(456,417),(455,439),(435,456),(412,444),(391,429)],[(571,406),(625,394),(643,414),(608,448),(585,459),(565,443)]],
        "print":[449,224,576,226,581,360,443,360],"face_center":[514,101],"face_size":[66,99]
    },
    "followthrough": {
        "source":"player_card_followthrough.png",
        "shirt":[(480,247),(500,265),(538,283),(566,256),(586,249),(592,300),(594,426),(578,451),(513,455),(459,443),(441,431),(447,364),(447,323),(464,293)],
        "shorts":[(450,454),(579,462),(593,495),(615,642),(578,657),(524,658),(506,553),(499,657),(450,655),(411,644),(425,533)],
        "collar":[(497,262),(537,282),(566,254),(573,260),(540,297),(495,275)],
        "arms":[[(479,247),(487,251),(470,295),(454,335),(447,345),(441,338),(457,293)],[(578,251),(586,249),(593,295),(599,321),(590,325),(583,294)]],
        "face":[(496,155),(530,143),(560,161),(562,202),(548,239),(532,253),(507,231),(494,198)],
        "print":[475,311,572,319,576,420,463,412],"face_center":[529,201],"face_size":[63,98]
    },
    "standing": {
        "source": "player_card_standing.png",
        "shirt": [(429,181),(467,164),(505,195),(547,163),(590,181),(590,244),(598,393),(589,404),(433,406),(421,394),(431,259)],
        "shorts": [(432,412),(589,409),(606,454),(629,626),(586,641),(526,641),(507,553),(490,640),(432,644),(396,630),(412,466)],
        "collar": [(464,164),(505,194),(548,164),(554,173),(507,209),(459,174)],
        "arms": [[(429,182),(438,182),(439,238),(432,278),(424,275),(429,232)],[(582,179),(590,181),(590,229),(598,273),(591,276),(583,230)]],
        "face": [(476,57),(509,51),(537,58),(540,109),(526,146),(509,155),(489,140),(475,108)],
        "print": [447,224,577,228,575,358,443,354],
        "face_center": [507,104],
        "face_size": [57,91],
    },
    "dribbling": {
        "source": "player_card_dribbling.png",
        "shirt": [(435,172),(473,155),(500,175),(543,190),(581,179),(611,202),(620,263),(591,339),(564,425),(551,446),(490,451),(402,428),(366,411),(379,340),(399,277),(424,229)],
        "shorts": [(366,425),(411,436),(482,453),(548,448),(566,456),(606,489),(648,522),(677,549),(639,561),(610,587),(579,615),(565,652),(529,650),(500,622),(477,589),(450,575),(418,566),(382,561),(347,579),(339,552),(341,494)],
        "collar": [(471,156),(484,171),(501,181),(543,198),(581,179),(587,189),(548,212),(515,204),(480,185),(464,166)],
        "arms": [[(434,171),(443,172),(424,225),(403,279),(389,309),(382,302),(395,268),(416,220)],[(600,191),(611,202),(620,251),(615,276),(607,272),(611,248),(603,208)]],
        "face": [(503,62),(548,58),(579,79),(574,122),(550,168),(520,176),(496,149),(489,108)],
        "print": [440,233,580,262,540,390,402,359],
        "face_center": [538,119],
        "face_size": [70,100],
    },
    "action": {
        "source": "player_card_action.png",
        "shirt": [(385,160),(410,149),(448,164),(485,155),(517,175),(548,214),(550,252),(536,299),(540,348),(512,362),(463,382),(420,397),(395,383),(386,347),(381,297),(369,257),(370,213)],
        "shorts": [(420,389),(461,381),(514,364),(541,351),(566,363),(560,395),(549,423),(525,450),(511,476),(487,491),(474,514),(472,547),(465,581),(452,599),(420,607),(399,617),(385,605),(389,565),(392,520),(399,468)],
        "collar": [(410,149),(446,165),(485,155),(490,163),(449,178),(408,159)],
        "arms": [[(384,160),(391,163),(379,204),(380,239),(398,273),(392,280),(371,248),(368,214)],[(510,170),(517,175),(542,207),(550,230),(543,235),(535,210)]],
        "face": [(430,49),(461,43),(479,61),(480,85),(491,111),(482,136),(461,151),(442,129),(424,109),(420,75)],
        "occlusions": [[(379,192),(398,204),(444,230),(544,183),(565,202),(484,271),(430,302),(402,280),(377,240)]],
        "print": [423,301,525,273,536,346,422,373],
        "face_center": [457,98],
        "face_size": [60,96],
    },
}


def mask(polygons, alpha):
    result = Image.new("L", alpha.size)
    draw = ImageDraw.Draw(result)
    for polygon in polygons:
        draw.polygon(polygon, fill=255)
    return ImageChops.multiply(result.filter(ImageFilter.GaussianBlur(.65)), alpha)


def encode(im):
    out = io.BytesIO()
    im.save(out, "WEBP", lossless=True, method=6)
    return out.getvalue()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--poses",nargs="+",choices=list(POSES))
    args = parser.parse_args()
    if args.check:
        data = json.loads((OUT / "manifest.json").read_text())
        for pose in POSES:
            for file, entry in data["poses"][pose]["files"].items():
                path = OUT / file
                assert path.is_file(), path
                assert hashlib.sha256(path.read_bytes()).hexdigest() == entry["sha256"], path
            validate_plate(OUT / f"{pose}.pcmat")
        print(f"Verified {len(POSES)} athletes, {len(POSES)*4} pose/template garment maps, face masks, hashes and image integrity.")
        return
    OUT.mkdir(parents=True, exist_ok=True)
    manifest = json.loads((OUT/"manifest.json").read_text()) if (OUT/"manifest.json").exists() else {"format":1,"canvas":[1024,1024],"poses":{}}
    for pose, info in POSES.items():
        if args.poses and pose not in args.poses: continue
        source = Image.open(SOURCES / info["source"]).convert("RGBA")
        assert source.size == (1024, 1024)
        assert source.getchannel("A").getextrema() == (0,255), "Source must have genuine transparency"
        # Remove generated shoe trademarks rather than shipping third-party branding.
        if pose == "standing":
            clean = source.copy()
            for region in [(388,931,423,956), (605,935,648,960)]:
                patch = source.crop(region).convert("RGB")
                patch = ImageChops.multiply(patch, Image.new("RGB", patch.size, (48,48,48)))
                clean.paste(patch, region)
            source = clean
        alpha = source.getchannel("A")
        garment = mask([info["shirt"], info["shorts"]], alpha)
        if info.get("occlusions"):
            garment = ImageChops.subtract(garment, mask(info["occlusions"], alpha))
        collar = mask([info["collar"]], garment)
        armholes = mask(info["arms"], garment)
        binding = ImageChops.lighter(collar, armholes)
        light = ImageOps.grayscale(source).point(lambda p: min(255, round(p * 1.19 + 30)))
        # Preserve mesh/folds as diffuse light and small additive specular highlights.
        neutral = light.point(lambda p: min(255, round(p / 225 * 255)))
        highlights = light.point(lambda p: max(0, (p - 225) * 3))
        spec = Image.new("RGBA", source.size, "white")
        spec.putalpha(ImageChops.multiply(highlights, garment))
        face = source.copy()
        face.putalpha(mask([info["face"]], alpha))
        layers = [source, face]
        for template in ("arena","classic","fastbreak","retro"):
            zones = Image.new("L", source.size)
            z = ImageDraw.Draw(zones)
            x0,y0,x1,y1 = garment.getbbox()
            w,h = x1-x0,y1-y0
            # Silhouette-constrained paneling follows each pose; no colored rectangles
            # extend across skin, ball or shoes. The cuts retain their named seam language.
            if template == "arena":
                z.polygon([(x0,y0),(x0+w*.16,y0),(x0+w*.22,y1),(x0,y1)], fill=2)
                z.polygon([(x1-w*.14,y0),(x1,y0),(x1,y1),(x1-w*.21,y1)], fill=2)
                z.rectangle((x0,y0,x1,y0+h*.055), fill=1)
            elif template == "classic":
                z.rectangle((x0,y0,x0+w*.10,y1),fill=2)
                z.rectangle((x1-w*.1,y0,x1,y1),fill=2)
                z.rectangle((x0,y1-h*.035,x1,y1),fill=1)
            elif template == "fastbreak":
                z.polygon([(x0,y0+h*.2),(x0+w*.30,y0+h*.65),(x0+w*.20,y1),(x0,y1)],fill=2)
                z.polygon([(x1,y0+h*.2),(x1-w*.26,y0+h*.60),(x1-w*.16,y1),(x1,y1)],fill=2)
                z.polygon([(x0,y1-h*.17),(x1,y1-h*.10),(x1,y1),(x0,y1-h*.08)],fill=1)
            else:
                for x in [x0+w*.10,x1-w*.20]:
                    z.rectangle((x,y0,x+w*.10,y1),fill=2)
                    z.rectangle((x+w*.035,y0,x+w*.06,y1),fill=1)
                z.rectangle((x0,y1-h*.045,x1,y1-h*.025),fill=1)
            channels = []
            for channel,zone in enumerate([0,1,2]):
                band = zones.point(lambda p, v=zone: 255 if p==v else 0)
                channels.append(ImageChops.multiply(neutral,band))
            body = Image.merge("RGBA",(*channels,ImageChops.subtract(garment,binding)))
            trim = Image.merge("RGBA",(ImageChops.multiply(neutral,collar),
                ImageChops.multiply(neutral,ImageChops.subtract(armholes,collar)),Image.new("L",source.size),binding))
            layers.extend([body,trim])
        layers.append(spec)
        target = OUT / f"{pose}.pcmat"
        with target.open("wb") as out:
            out.write(struct.pack(">4sHHB",b"CQP2",1024,1024,len(layers)))
            for layer in layers:
                data=encode(layer)
                out.write(struct.pack(">I",len(data)));out.write(data)
        validate_plate(target)
        manifest["poses"][pose] = {**info, "files": {target.name: {"bytes":target.stat().st_size,
            "sha256":hashlib.sha256(target.read_bytes()).hexdigest()}}}
        print(target.name,target.stat().st_size)
    (OUT / "manifest.json").write_text(json.dumps(manifest,indent=2)+"\n")


def validate_plate(path):
    with path.open("rb") as f:
        magic,w,h,count=struct.unpack(">4sHHB",f.read(9))
        assert magic==b"CQP2" and (w,h)==(1024,1024) and count==11
        for index in range(count):
            length=struct.unpack(">I",f.read(4))[0]
            data=f.read(length)
            assert len(data)==length and 0<length<5_000_000
            with Image.open(io.BytesIO(data)) as im:
                assert im.size==(w,h) and im.mode=="RGBA"
                assert im.getchannel("A").getextrema()==(0,255) or index==10
                if index in (2,4,6,8):
                    # Every template must carry all three live textile roles.
                    for channel in ("R","G","B"):
                        visible=ImageChops.multiply(im.getchannel(channel),im.getchannel("A"))
                        assert sum(visible.histogram()[1:])>200
                if index in (3,5,7,9):
                    for channel in ("R","G"):
                        visible=ImageChops.multiply(im.getchannel(channel),im.getchannel("A"))
                        assert sum(visible.histogram()[1:])>200
                im.verify()
        assert not f.read(1)


if __name__ == "__main__":
    main()
