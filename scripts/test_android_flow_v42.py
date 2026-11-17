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

    def test_released_welcome_uses_its_actual_existing_enter_court_control(self):
        button = Element('node', {'resource-id':'welcome-get-started',
                                 'bounds':'[56,1258][664,1374]'})
        with patch.object(batch.ui, 'nodes', return_value=[button]), \
                patch.object(batch.ui, 'tap') as tap, \
                patch.object(batch.ui, 'wait_tagged') as wait:
            batch.enter_court()
        tap.assert_called_once_with(button)
        wait.assert_called_once_with('player-bottom-dock', attempts=20)

    def test_artwork_requires_visible_area_not_just_an_exported_tag(self):
        hidden = Element('node', {'resource-id':'drill-art-s1','bounds':'[0,0][0,0]'})
        visible = Element('node', {'resource-id':'drill-art-s1','bounds':'[40,300][600,700]'})
        region = Element('node', {'scrollable':'true','bounds':'[0,48][640,1300]'})
        with patch.object(batch.ui,'nodes',side_effect=[[hidden,region],[visible,region]]), \
                patch.object(batch.ui,'shell') as shell, patch.object(batch.time,'sleep'):
            self.assertIs(visible,batch.scroll_to_tag('drill-art-s1'))
        shell.assert_called_once()


if __name__ == '__main__':
    unittest.main()
