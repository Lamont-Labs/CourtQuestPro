"""Fast offline checks for the cinematic presentation's preservation boundaries."""
import hashlib
import struct
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class CinematicResourceTest(unittest.TestCase):
    def test_original_signature_and_jersey_resources_remain_pinned(self):
        # These files are already independently pinned by the native artwork gate.
        data = (ROOT / 'app/src/main/assets/courtquest_navigation_atlas.png').read_bytes()
        self.assertEqual(hashlib.sha256(data).hexdigest(),
                         'c7c28958c9d051b1b0ef0f6444672ad1b78e54b6383ea31086a9a61687158620')

    def test_original_arena_images_are_offline_webp_not_large_masters(self):
        for name in ['court_arena_player', 'court_arena_hoop']:
            with self.subTest(name=name):
                data = (ROOT / f'app/src/main/res/drawable-nodpi/{name}.webp').read_bytes()
                self.assertEqual(data[:4], b'RIFF')
                self.assertEqual(data[8:12], b'WEBP')
                self.assertEqual(struct.unpack('<I', data[4:8])[0] + 8, len(data))
                self.assertGreater(len(data), 10_000)
                self.assertLess(len(data), 300_000)

    def test_visual_comparison_is_pinned_to_the_previous_native_redesign(self):
        import verify_premium_visuals as visual
        self.assertEqual(visual.BASELINE_SHA256,
                         '5125f01e191d26e03fc779ebd96affa3e9dbd5db2900dcedbc8f8660ac9f63bd')


if __name__ == '__main__':
    unittest.main()
