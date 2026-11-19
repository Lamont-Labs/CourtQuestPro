"""Bundled collector surfaces and cosmetic mattes are complete and cannot repaint cloth."""
import hashlib
import io
import json
import struct
import unittest
from pathlib import Path
from PIL import Image, ImageChops

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

    def testAllThreeMaterialPlatesExistAreDistinctAndSharp(self):
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

    def testPhysicalFootwearModelsHaveDifferentSilhouettesNotOnlyDifferentPixels(self):
        manifest=json.loads((ROOT/"premium-manifest.json").read_text())
        self.assertEqual(set(POSES),set(manifest["poses"]))
        for pose,entry in manifest["poses"].items():
            file=ROOT/entry["file"]
            self.assertEqual(entry["sha256"],hashlib.sha256(file.read_bytes()).hexdigest())
            shoes=layers(file,4)
            for a,b in [(1,2),(1,3),(2,3)]:
                difference=ImageChops.difference(shoes[a].getchannel("A"),shoes[b].getchannel("A"))
                self.assertGreater(sum(difference.histogram()[32:]),300,(pose,a,b))
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
