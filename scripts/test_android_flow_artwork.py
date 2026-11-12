"""The native loader must package the original approved atlas, not a blank fallback."""
import hashlib
import re
import struct
import unittest
import zlib
from pathlib import Path


class NavigationArtworkTest(unittest.TestCase):
    def test_original_navigation_artwork_is_present_and_valid(self):
        root = Path(__file__).resolve().parents[1]
        source = (root / 'app/src/main/java/com/courtquestpro/CourtQuestNavigation.kt').read_text()
        name = re.search(r'private const val NAV_ATLAS_FILE = "([^"]+)"', source)
        self.assertIsNotNone(name, 'Native atlas loader must identify its packaged asset')
        data = (root / 'app/src/main/assets' / name.group(1)).read_bytes()
        self.assertEqual(hashlib.sha256(data).hexdigest(),
                         'c7c28958c9d051b1b0ef0f6444672ad1b78e54b6383ea31086a9a61687158620',
                         'Preserve the exact original approved navigation artwork')
        self.assertEqual(data[:8], b'\x89PNG\r\n\x1a\n')
        self.assertEqual(struct.unpack('>II', data[16:24]), (2172, 724))
        offset, compressed, chunks = 8, bytearray(), []
        while offset < len(data):
            length = struct.unpack('>I', data[offset:offset + 4])[0]
            kind = data[offset + 4:offset + 8]
            payload = data[offset + 8:offset + 8 + length]
            crc = struct.unpack('>I', data[offset + 8 + length:offset + 12 + length])[0]
            self.assertEqual(zlib.crc32(kind + payload) & 0xffffffff, crc)
            chunks.append(kind)
            if kind == b'IDAT':
                compressed.extend(payload)
            offset += length + 12
        self.assertEqual(offset, len(data))
        self.assertEqual(chunks[-1], b'IEND')
        self.assertTrue(zlib.decompress(compressed), 'PNG pixel stream must decode')


if __name__ == '__main__':
    unittest.main()
