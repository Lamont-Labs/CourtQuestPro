"""Regression coverage for the native illustration journey's input and captures."""
import unittest
from unittest.mock import patch
from xml.etree.ElementTree import Element

import verify_android_library_flow as library


class LibraryArtworkJourneyTest(unittest.TestCase):
    def test_empty_search_is_cleared_without_invalid_android_text_command(self):
        node = Element('node', {'text': 'Previous search'})
        with patch.object(library, 'tagged', return_value=node), \
                patch.object(library.ui, 'tap'), \
                patch.object(library.ui, 'shell') as shell:
            library.fill('library-search', '')
        calls = [call.args for call in shell.call_args_list]
        self.assertNotIn(('input', 'text', ''), calls)
        self.assertIn(('input', 'keyevent', 'KEYCODE_BACK'), calls)
        self.assertTrue(any('KEYCODE_DEL' in args for args in calls))

    def test_nonempty_search_still_types_the_requested_title(self):
        node = Element('node', {'text': ''})
        with patch.object(library, 'tagged', return_value=node), \
                patch.object(library.ui, 'tap'), \
                patch.object(library.ui, 'shell') as shell:
            library.fill('library-search', 'Pass and Cut')
        shell.assert_any_call('input', 'text', 'Pass%sand%sCut')

    def test_capture_rejects_an_image_clipped_by_the_dock(self):
        clipped = Element('node', {'bounds': '[40,100][680,400]'})
        viewport = Element('node', {'bounds': '[0,48][720,1372]', 'scrollable': 'true'})
        entry = {'id': 's1', 'width': 1024, 'height': 1024}
        with patch.object(library, 'tagged', return_value=clipped), \
                patch.object(library.ui, 'nodes', return_value=[viewport]), \
                patch.object(library.ui, 'shell'), \
                patch.object(library.time, 'sleep'):
            with self.assertRaisesRegex(AssertionError, 'Illustration is clipped'):
                library.capture_artwork(entry)

    def test_capture_scrolls_inside_actual_viewport_then_requires_full_plate(self):
        clipped = Element('node', {'bounds': '[64,945][656,1372]'})
        visible = Element('node', {'bounds': '[64,716][656,1308]'})
        viewport = Element('node', {'bounds': '[0,48][720,1372]', 'scrollable': 'true'})
        entry = {'id': 's1', 'width': 1024, 'height': 1024}
        with patch.object(library, 'tagged', side_effect=[clipped, visible]), \
                patch.object(library.ui, 'nodes', return_value=[viewport]), \
                patch.object(library.ui, 'shell') as shell, \
                patch.object(library.time, 'sleep'), \
                patch.object(library, 'snap') as snap:
            library.capture_artwork(entry)
        shell.assert_called_once_with('input', 'swipe', '360', '710', '360', '481', '650')
        snap.assert_called_once_with('art-s1')


if __name__ == '__main__':
    unittest.main()
