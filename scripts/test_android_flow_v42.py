import unittest
from unittest.mock import patch
from xml.etree.ElementTree import Element
import capture_v42_solo_baseline as batch


class SoloBatchCaptureTest(unittest.TestCase):
    def test_receipt_progress_action_returns_to_primary_navigation(self):
        button = Element('node', {'text':'View Progress','bounds':'[48,1368][672,1464]'})
        with patch.object(batch.ui,'scroll_to',return_value=button) as scroll, \
                patch.object(batch.ui,'tap') as tap, \
                patch.object(batch.ui,'wait_tagged') as wait:
            batch.open_progress_from_receipt()
        scroll.assert_called_once_with('View Progress',18)
        tap.assert_called_once_with(button)
        wait.assert_called_once_with('player-bottom-dock',attempts=20)

    def test_legacy_result_default_is_compared_semantically(self):
        before = {
            'drillResults': [
                {'date':'2026-11-17','slotId':'slot-1','drillId':'s1',
                 'completed':15,'attempted':20}
            ]
        }
        after = {
            'drillResults': [
                {'date':'2026-11-17','slotId':'slot-1','drillId':'s1',
                 'completed':15,'attempted':20,'practice':False}
            ]
        }
        self.assertEqual(batch.without_progression_migration(before),
                         batch.without_progression_migration(after))

    def test_result_data_changes_are_still_rejected_by_migration_comparison(self):
        before = {'drillResults': [{'slotId':'slot-1','completed':15}]}
        after = {'drillResults': [{'slotId':'slot-1','completed':14}]}
        self.assertNotEqual(batch.without_progression_migration(before),
                            batch.without_progression_migration(after))

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
        wait.assert_called_once_with('player-bottom-dock', attempts=4)

    def test_welcome_retries_only_when_same_actionable_button_remains(self):
        button = Element('node', {'resource-id':'welcome-get-started',
                                 'bounds':'[56,1258][664,1374]',
                                 'clickable':'true','enabled':'true'})
        dock = Element('node', {'resource-id':'player-bottom-dock',
                               'bounds':'[22,1380][698,1496]'})
        with patch.object(batch.ui, 'nodes', side_effect=[[button], [button]]), \
                patch.object(batch.ui, 'tap') as tap, \
                patch.object(batch.ui, 'wait_tagged',
                             side_effect=[AssertionError('not yet'), dock]) as wait:
            self.assertIs(dock, batch.enter_court())
        self.assertEqual([button, button], [call.args[0] for call in tap.call_args_list])
        self.assertEqual([('player-bottom-dock',), ('player-bottom-dock',)],
                         [call.args for call in wait.call_args_list])
        self.assertEqual([4, 20], [call.kwargs['attempts'] for call in wait.call_args_list])

    def test_welcome_does_not_replay_if_the_ui_changed(self):
        button = Element('node', {'resource-id':'welcome-get-started',
                                 'bounds':'[56,1258][664,1374]'})
        with patch.object(batch.ui, 'nodes', side_effect=[[button], []]), \
                patch.object(batch.ui, 'tap') as tap, \
                patch.object(batch.ui, 'wait_tagged',
                             side_effect=AssertionError('not yet')):
            with self.assertRaisesRegex(AssertionError, 'refusing to replay navigation'):
                batch.enter_court()
        tap.assert_called_once_with(button)

    def test_artwork_requires_visible_area_not_just_an_exported_tag(self):
        hidden = Element('node', {'resource-id':'drill-art-s1','bounds':'[0,0][0,0]'})
        clipped = Element('node', {'resource-id':'drill-art-s1','bounds':'[40,1200][600,1400]'})
        visible = Element('node', {'resource-id':'drill-art-s1','bounds':'[40,300][600,700]'})
        region = Element('node', {'scrollable':'true','bounds':'[0,48][640,1300]'})
        with patch.object(batch.ui,'nodes',side_effect=[[hidden,region],[clipped,region],[visible,region]]), \
                patch.object(batch.ui,'shell') as shell, patch.object(batch.time,'sleep'):
            self.assertIs(visible,batch.scroll_to_tag('drill-art-s1'))
        self.assertEqual(2,shell.call_count)

    def test_visible_control_without_an_exported_scroll_node_is_tappable(self):
        target=Element('node',{'resource-id':'library-drill-s1','bounds':'[40,819][680,963]'})
        dock=Element('node',{'resource-id':'player-bottom-dock','bounds':'[22,1380][698,1496]'})
        with patch.object(batch.ui,'nodes',return_value=[target,dock]), \
                patch.object(batch.ui,'shell',return_value='Override size: 720x1600') as shell:
            self.assertIs(target,batch.scroll_to_tag('library-drill-s1'))
        shell.assert_called_once_with('wm','size')

    def test_offscreen_control_without_scroll_support_is_not_tapped(self):
        target=Element('node',{'resource-id':'live-save-results','bounds':'[72,1333][568,1392]'})
        with patch.object(batch.ui,'nodes',return_value=[target]), \
                patch.object(batch.ui,'shell',return_value='Override size: 640x1440'):
            with self.assertRaisesRegex(AssertionError,'outside the safe screen'):
                batch.scroll_to_tag('live-save-results')

    def test_topward_scroll_returns_to_an_above_the_fold_tab(self):
        hidden = Element('node', {'resource-id':'quests-tab-achievements',
                                 'bounds':'[40,-50][432,46]'})
        visible = Element('node', {'resource-id':'quests-tab-achievements',
                                  'bounds':'[40,574][432,670]'})
        region = Element('node', {'scrollable':'true','bounds':'[0,48][640,1300]'})
        with patch.object(batch.ui,'nodes',side_effect=[[hidden,region],[visible,region]]), \
                patch.object(batch.ui,'shell') as shell, \
                patch.object(batch.time,'sleep'):
            self.assertIs(visible,batch.scroll_to_tag_top('quests-tab-achievements'))
        shell.assert_called_once()

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
