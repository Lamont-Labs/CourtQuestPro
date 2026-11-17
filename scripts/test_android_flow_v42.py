import unittest
from unittest.mock import patch
from xml.etree.ElementTree import Element
import capture_v42_solo_baseline as batch


class SoloBatchCaptureTest(unittest.TestCase):
    def dock(self, narrow=False):
        dock = Element('node', {'bounds': '[16,1400][704,1516]'})
        items = []
        for i, route in enumerate(batch.ui.NAV_ROUTES.values()):
            x = 20 + i * 136
            items.append(Element('node', {
                'resource-id': 'dock-'+route,
                'bounds': f'[{x},1400][{x+(80 if narrow else 132)},1516]'
            }))
        return dock, items

    def test_five_equal_native_targets_fit_the_compact_single_row(self):
        dock, items = self.dock()
        with patch.object(batch.ui, 'nodes', return_value=items), \
                patch.object(batch.ui, 'wait_tagged', return_value=dock):
            batch.assert_dock()

    def test_under_48dp_target_is_rejected(self):
        dock, items = self.dock(narrow=True)
        with patch.object(batch.ui, 'nodes', return_value=items), \
                patch.object(batch.ui, 'wait_tagged', return_value=dock):
            with self.assertRaises(AssertionError):
                batch.assert_dock()

    def test_signed_bounds_are_not_mistaken_for_positive_coordinates(self):
        self.assertEqual([-5, -10, 20, 40],
                         batch.bounds(Element('node', {'bounds':'[-5,-10][20,40]'})))


if __name__ == '__main__':
    unittest.main()
