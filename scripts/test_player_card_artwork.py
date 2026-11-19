"""Bundled collector surfaces and cosmetic mattes are complete and cannot repaint cloth."""
import hashlib
import io
import json
import struct
import unittest
from pathlib import Path
from PIL import Image, ImageChops

ROOT=Path(__file__).resolve().parents[1]/"app/src/main/assets/player_cards"

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
        for pose in ("standing","dribbling","action"):
            with self.subTest(pose=pose):
                for image in layers(ROOT/f"{pose}-avatar.pcmat",2):
                    self.assertEqual((0,255),image.getchannel("A").getextrema())
                    self.assertGreater(sum(image.getchannel("A").histogram()[128:]),500)

    def testSkinDyesCannotCoverGarmentInteriorsOrTransparentSpace(self):
        for pose in ("standing","dribbling","action"):
            original=layers(ROOT/f"{pose}.pcmat",11)
            skin=layers(ROOT/f"{pose}-avatar.pcmat",2)[0].getchannel("A")
            cloth=ImageChops.add(original[2].getchannel("A"),original[3].getchannel("A"))
            interior=cloth.point(lambda v:255 if v>=254 else 0)
            self.assertIsNone(ImageChops.multiply(skin,interior).getbbox())
            self.assertIsNone(ImageChops.subtract(skin,original[0].getchannel("A")).getbbox())

    def testAvatarManifestHashesMatchEveryBundledPose(self):
        entries=json.loads((ROOT/"avatar-manifest.json").read_text())
        self.assertEqual({"standing","dribbling","action"},set(entries))
        for entry in entries.values():
            self.assertEqual(entry["sha256"],hashlib.sha256((ROOT/entry["file"]).read_bytes()).hexdigest())

    def testAllThreeMaterialPlatesExistAreDistinctAndSharp(self):
        entries=json.loads((ROOT/"surface-manifest.json").read_text())
        self.assertEqual({"holographic","classic","battle"},set(entries))
        hashes=set()
        for entry in entries.values():
            file=ROOT/entry["file"];checksum=hashlib.sha256(file.read_bytes()).hexdigest()
            self.assertEqual(entry["sha256"],checksum);hashes.add(checksum)
            with Image.open(file) as image:
                self.assertEqual((828,1200),image.size);image.verify()
            self.assertLess(file.stat().st_size,600_000)
        self.assertEqual(3,len(hashes))

if __name__=="__main__":unittest.main()
