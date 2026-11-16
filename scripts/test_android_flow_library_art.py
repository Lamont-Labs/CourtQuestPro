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
        entry = {'id': 's1', 'width': 1024, 'height': 1024}
        with patch.object(library, 'tagged', return_value=clipped):
            with self.assertRaisesRegex(AssertionError, 'Illustration is clipped'):
                library.capture_artwork(entry)


if __name__ == '__main__':
    unittest.main()
