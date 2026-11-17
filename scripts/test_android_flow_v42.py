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
        clipped = Element('node', {'resource-id':'drill-art-s1','bounds':'[40,1200][600,1400]'})
        visible = Element('node', {'resource-id':'drill-art-s1','bounds':'[40,300][600,700]'})
        region = Element('node', {'scrollable':'true','bounds':'[0,48][640,1300]'})
        with patch.object(batch.ui,'nodes',side_effect=[[hidden,region],[clipped,region],[visible,region]]), \
                patch.object(batch.ui,'shell') as shell, patch.object(batch.time,'sleep'):
            self.assertIs(visible,batch.scroll_to_tag('drill-art-s1'))
        self.assertEqual(2,shell.call_count)

    def test_save_transition_sends_one_tap_then_waits_for_receipt(self):
        button = Element('node', {'bounds':'[20,100][620,200]'})
        receipt = Element('node', {'resource-id':'drill-saved-receipt',
                                  'bounds':'[20,220][620,600]'})
        with patch.object(batch.ui,'tap') as tap, \
                patch.object(batch.ui,'wait_tagged',return_value=receipt) as wait:
            self.assertIs(receipt,batch.tap_once_then_wait(button,'drill-saved-receipt'))
        tap.assert_called_once_with(button)
        wait.assert_called_once_with('drill-saved-receipt',attempts=12)

    def test_persistence_check_waits_for_observed_state_not_a_fixed_delay(self):
        reads = iter([{'count':13},{'count':14},{'count':15}])
        with patch.object(batch.time,'monotonic',side_effect=[0.0,0.1,0.2,0.3]), \
                patch.object(batch.time,'sleep') as sleep:
            value=batch.wait_until(lambda: next(reads),lambda state: state['count']==15,'saved counter')
        self.assertEqual(15,value['count'])
        self.assertEqual(2,sleep.call_count)

    def test_persistence_check_times_out_with_the_last_observed_state(self):
        reads=iter([{'count':14},{'count':14},{'count':14}])
        with patch.object(batch.time,'monotonic',side_effect=[0.0,0.2,0.4]), \
                patch.object(batch.time,'sleep'):
            with self.assertRaisesRegex(AssertionError,'Persisted drill counters'):
                batch.wait_until(lambda:next(reads),lambda state:state['count']==15,
                                 'Persisted drill counters',timeout=0.3)


if __name__ == '__main__':
    unittest.main()
