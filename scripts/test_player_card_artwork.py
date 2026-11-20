"""Bundled collector surfaces and cosmetic mattes are complete and cannot repaint cloth."""
import hashlib
import io
import json
import struct
import unittest
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageOps

ROOT=Path(__file__).resolve().parents[1]/"app/src/main/assets/player_cards"
POSES=("standing","dribbling","action","ready","followthrough")

def layers(path,count):
    with path.open("rb") as f:
        magic,w,h,n=struct.unpack(">4sHHB",f.read(9))
        assert(magic,w,h,n)==(b"CQP2",1024,1024,count)
        result=[]
        for _ in range(n):
            size=struct.unpack(">I",f.read(4))[0]
            assert 0<size<5_000_000
            data=f.read(size);assert len(data)==size
            im=Image.open(io.BytesIO(data));im.load();assert im.size==(1024,1024) and im.mode=="RGBA"
            result.append(im)
        assert not f.read(1)
    return result

class CollectorArtworkTest(unittest.TestCase):
    def testPoseMatchedHairPlatesAreCompleteTransparentAndVisuallyDistinct(self):
        entries=json.loads((ROOT/"head-style-manifest.json").read_text())["assets"]
        styles=("bald","fade","curls","locs","braids","afro","topknot","waves")
        self.assertEqual(len(POSES)*len(styles),len(entries))
        for pose in POSES:
            heads=[]
            for style in styles:
                entry=entries[f"{pose}/{style}"]
                path=ROOT/entry["file"]
                self.assertEqual(entry["sha256"],hashlib.sha256(path.read_bytes()).hexdigest())
                with Image.open(path) as image:
                    image.load()
                    self.assertEqual("RGBA",image.mode)
                    self.assertEqual(tuple(entry["dimensions"]),image.size)
                    self.assertEqual((0,255),image.getchannel("A").getextrema())
                    self.assertIsNotNone(image.getchannel("A").getbbox())
                    heads.append(image.copy())
            for i,a in enumerate(heads):
                for j,b in enumerate(heads[i+1:],i+1):
                    delta=ImageChops.difference(a.getchannel("A"),b.getchannel("A"))
                    self.assertGreater(sum(delta.histogram()[24:]),50,(pose,styles[i],styles[j]))
    def testHairSwitchClearsOnlyTheOriginalHeadSilhouetteBeforeDrawingTheNewOne(self):
        manifest=json.loads((ROOT/"head-style-manifest.json").read_text())
        for pose,item in manifest["cleanBases"].items():
            source=Image.open(Path(__file__).resolve().parents[1]/
                f"attached_assets/generated_images/player_card_{pose}.png").convert("RGBA")
            clean=Image.open(ROOT/item["file"]).convert("RGBA")
            self.assertEqual(source.size,clean.size)
            region=Image.new("L",source.size)
            ImageDraw.Draw(region).polygon([tuple(point) for point in item["cleared_region"]],fill=255)
            removed=ImageChops.subtract(source.getchannel("A"),clean.getchannel("A"))
            expected=ImageChops.multiply(source.getchannel("A"),region)
            self.assertIsNone(ImageChops.difference(removed,expected).getbbox(),pose)
    def testNewPoseSkinMattesCoverCalvesAndFingertipsWithoutDyeingSocks(self):
        landmarks={"ready":[(390,730)],"followthrough":[(421,815),(591,815),(550,35)]}
        socks={"ready":[(409,807),(616,808)],"followthrough":[(421,850),(590,850)]}
        for pose,points in landmarks.items():
            matte=layers(ROOT/f"{pose}-avatar.pcmat",2)[0].getchannel("A")
            for point in points:self.assertGreater(matte.getpixel(point),250,(pose,point))
            for point in socks[pose]:self.assertLess(matte.getpixel(point),4,(pose,point))
            self.assertLess(matte.getpixel((514,300)),4,"Garment must not receive a skin tint")
    def testReplacementFootwearRemovesTheEntireOldShoeIncludingOuterToes(self):
        for pose in POSES:
            original=layers(ROOT/f"{pose}.pcmat",11)[0]
            old_shoes=layers(ROOT/f"{pose}-avatar.pcmat",2)[1].getchannel("A")
            clean=layers(ROOT/f"{pose}-footwear.pcmat",4)[0]
            self.assertIsNone(ImageChops.multiply(clean.getchannel("A"),old_shoes).getbbox(),pose)
            removed=ImageChops.subtract(original.getchannel("A"),clean.getchannel("A"))
            expected=ImageChops.multiply(original.getchannel("A"),
                old_shoes.point(lambda value:255 if value else 0).filter(ImageFilter.MaxFilter(3)))
            self.assertIsNone(ImageChops.difference(removed,expected).getbbox(),
                f"{pose}: preserve socks/ankles outside the traced shoe matte")
        # Toe areas missed by the previous hand-authored bounds. Both the dye
        # matte and the replacement-body removal must cover their real pixels.
        toes={"standing":[(323,970,350,997),(680,975,700,997)],
              "dribbling":[(166,974,205,1001),(769,923,794,953)],
              "action":[(319,986,363,1011),(703,641,727,684)]}
        for pose,regions in toes.items():
            source=layers(ROOT/f"{pose}.pcmat",11)[0]
            original=source.getchannel("A")
            # Bright original soles deliberately keep their source material.
            expected_dye=ImageChops.multiply(original,ImageOps.grayscale(source).point(lambda v:255 if v<190 else 0))
            matte=layers(ROOT/f"{pose}-avatar.pcmat",2)[1].getchannel("A")
            clean=layers(ROOT/f"{pose}-footwear.pcmat",4)[0].getchannel("A")
            for region in regions:
                self.assertGreater(original.crop(region).getextrema()[1],180,(pose,region))
                self.assertIsNone(ImageChops.subtract(expected_dye.crop(region),matte.crop(region)).getbbox(),(pose,region))
                self.assertGreater(matte.crop(region).getextrema()[1],180,(pose,region))

    def testEveryPoseHasGenuineTransparentSkinAndFootwearLayers(self):
        for pose in POSES:
            with self.subTest(pose=pose):
                for image in layers(ROOT/f"{pose}-avatar.pcmat",2):
                    self.assertEqual((0,255),image.getchannel("A").getextrema())
                    self.assertGreater(sum(image.getchannel("A").histogram()[128:]),500)

    def testSkinDyesCannotCoverGarmentInteriorsOrTransparentSpace(self):
        for pose in POSES:
            original=layers(ROOT/f"{pose}.pcmat",11)
            skin=layers(ROOT/f"{pose}-avatar.pcmat",2)[0].getchannel("A")
            cloth=ImageChops.add(original[2].getchannel("A"),original[3].getchannel("A"))
            interior=cloth.point(lambda v:255 if v>=254 else 0)
            self.assertIsNone(ImageChops.multiply(skin,interior).getbbox())
            self.assertIsNone(ImageChops.subtract(skin,original[0].getchannel("A")).getbbox())

    def testAvatarManifestHashesMatchEveryBundledPose(self):
        entries=json.loads((ROOT/"avatar-manifest.json").read_text())
        self.assertEqual(set(POSES),set(entries))
        for entry in entries.values():
            self.assertEqual(entry["sha256"],hashlib.sha256((ROOT/entry["file"]).read_bytes()).hexdigest())

    def testAllFourMaterialPlatesExistAreDistinctAndSharp(self):
        entries=json.loads((ROOT/"surface-manifest.json").read_text())
        self.assertEqual({"holographic","classic","battle","obsidian"},set(entries))
        hashes=set()
        for entry in entries.values():
            file=ROOT/entry["file"];checksum=hashlib.sha256(file.read_bytes()).hexdigest()
            self.assertEqual(entry["sha256"],checksum);hashes.add(checksum)
            with Image.open(file) as image:
                self.assertEqual((828,1200),image.size);image.verify()
            self.assertLess(file.stat().st_size,600_000)
        self.assertEqual(4,len(hashes))

    def testPoseMatchedFootwearUsesNoSpriteFittingAndEachModelHasRealShoeDetail(self):
        manifest=json.loads((ROOT/"premium-manifest.json").read_text())
        self.assertEqual(set(POSES),set(manifest["poses"]))
        for pose,entry in manifest["poses"].items():
            self.assertIn("pose-matched source-coordinate cutout",entry["attachment"])
            self.assertEqual({"low","high","strap"},set(entry["shoe_art"]))
            for source in entry["shoe_art"].values():
                self.assertEqual(source["sha256"],hashlib.sha256((Path(__file__).resolve().parents[1]/source["source"]).read_bytes()).hexdigest())
            file=ROOT/entry["file"]
            self.assertEqual(entry["sha256"],hashlib.sha256(file.read_bytes()).hexdigest())
            shoes=layers(file,4)
            original_mask=layers(ROOT/f"{pose}-avatar.pcmat",2)[1].getchannel("A")
            expected_outline=original_mask.filter(ImageFilter.MaxFilter(3))
            for index in (1,3):
                delta=ImageChops.difference(shoes[index].getchannel("A"),expected_outline)
                self.assertEqual(0,sum(delta.histogram()[5:]),
                    (pose,index,"pose-specific cutout must preserve authored foot geometry"))
            high_mask=shoes[2].getchannel("A")
            missing=ImageChops.subtract(expected_outline,high_mask)
            self.assertEqual(0,sum(missing.histogram()[5:]),pose)
            if pose in ("standing","dribbling","action"):
                self.assertGreater(sum(ImageChops.subtract(high_mask,expected_outline).histogram()[1:]),100,
                    f"{pose}: high collar reaches the authored ankle boundary")
            for a,b in [(1,2),(1,3),(2,3)]:
                difference=ImageChops.difference(shoes[a],shoes[b]).convert("RGB").convert("L")
                self.assertGreater(sum(difference.histogram()[20:]),1000,(pose,a,b))
            original=layers(ROOT/f"{pose}.pcmat",11)[0]
            self.assertIsNone(ImageChops.subtract(shoes[0].getchannel("A"),original.getchannel("A")).getbbox())

    def testEveryOriginalDetailPartIsTransparentAndHashVerified(self):
        entries=json.loads((ROOT/"premium-manifest.json").read_text())["parts"]
        self.assertEqual(8,len(entries))
        for entry in entries.values():
            file=ROOT/entry["file"]
            self.assertEqual(entry["sha256"],hashlib.sha256(file.read_bytes()).hexdigest())
            with Image.open(file) as image:
                self.assertEqual((0,255),image.getchannel("A").getextrema())
                self.assertLessEqual(max(image.size),512)

if __name__=="__main__":unittest.main()
