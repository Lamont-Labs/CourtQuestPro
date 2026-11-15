import unittest
import tempfile
from pathlib import Path
from contextlib import ExitStack
from unittest.mock import patch
from xml.etree import ElementTree as ET

import verify_premium_visuals as visual


def dock_fixture():
    root = ET.Element('node', {'resource-id': 'player-bottom-dock', 'bounds': '[24,1400][696,1512]'})
    for i, (label, route) in enumerate(visual.ui.NAV_ROUTES.items()):
        left = 26 + i * 132
        ET.SubElement(root, 'node', {'resource-id': f'dock-{route}', 'bounds': f'[{left},1408][{left+130},1504]',
                                    'enabled': 'true', 'content-desc': label})
    return root


class NativeVisualGuardTest(unittest.TestCase):
    def test_avatar_cannot_cross_the_circular_artwork_clip(self):
        jersey = ET.Element('node', {'resource-id': 'me-jersey-hero', 'bounds': '[20,20][400,400]'})
        avatar = ET.Element('node', {'resource-id': 'me-neutral-avatar', 'bounds': '[288,288][376,376]'})
        with patch.object(visual.ui, 'nodes', return_value=[jersey, avatar]), self.assertRaises(AssertionError):
            visual.audit_me_avatar()

    def test_avatar_fits_inside_the_original_jersey_artwork(self):
        jersey = ET.Element('node', {'resource-id': 'me-jersey-hero', 'bounds': '[20,20][400,400]'})
        avatar = ET.Element('node', {'resource-id': 'me-neutral-avatar', 'bounds': '[252,252][340,340]'})
        with patch.object(visual.ui, 'nodes', return_value=[jersey, avatar]):
            visual.audit_me_avatar()

    def test_live_primary_rejects_a_recording_button_below_the_viewport(self):
        viewport = ET.Element('node', {'scrollable': 'true', 'bounds': '[0,48][720,1552]'})
        stats = ET.Element('node', {'resource-id': 'live-drill-stats', 'bounds': '[40,780][680,900]'})
        button = ET.Element('node', {'resource-id': 'live-record-success', 'bounds': '[40,1480][680,1610]',
                                    'enabled': 'true'})
        with patch.object(visual.ui, 'nodes', return_value=[viewport, stats, button]), self.assertRaises(AssertionError):
            visual.audit_live_primary()

    def test_live_primary_accepts_visible_stats_and_an_enabled_large_target(self):
        viewport = ET.Element('node', {'scrollable': 'true', 'bounds': '[0,48][720,1552]'})
        stats = ET.Element('node', {'resource-id': 'live-drill-stats', 'bounds': '[40,780][680,900]'})
        button = ET.Element('node', {'resource-id': 'live-record-success', 'bounds': '[40,920][680,1050]',
                                    'enabled': 'true'})
        with patch.object(visual.ui, 'nodes', return_value=[viewport, stats, button]):
            visual.audit_live_primary()

    def test_large_progress_rejects_narrow_record_columns(self):
        item = ET.Element('node', {'resource-id': 'progress-record-sessions', 'bounds': '[320,500][600,700]'})
        viewport = ET.Element('node', {'scrollable': 'true', 'bounds': '[0,96][640,1232]'})
        with patch.object(visual.ui, 'nodes', return_value=[item, viewport]), patch.object(visual.ui, 'shell'), \
                self.assertRaises(AssertionError):
            visual.audit_large_controls(['progress-record-sessions'], 'progress-record-large')

    def test_large_library_requires_readable_full_width_controls(self):
        rail = ET.Element('node', {'bounds': '[40,300][600,420]'})
        items = [ET.Element('node', {'resource-id': tag, 'bounds': '[40,500][600,650]'}) for tag in
                 ['library-create', 'library-build-mission']]
        items.append(ET.Element('node', {'scrollable': 'true', 'bounds': '[0,96][640,1232]'}))
        items.extend(ET.Element('node', {'resource-id': 'segment-'+label, 'bounds': '[40,300][600,420]'})
                     for label in ['today', 'library', 'custom'])
        with patch.object(visual.ui, 'wait_tagged', return_value=rail), \
                patch.object(visual.ui, 'tap'), patch.object(visual.ui, 'scroll_to'), \
                patch.object(visual.ui, 'nodes', return_value=items), patch.object(visual, 'capture') as capture:
            visual.audit_large_library()
        self.assertEqual(capture.call_args_list[-1].args, ('library-actions-large',))
        self.assertEqual(capture.call_count, 5)

    def test_large_library_rejects_narrow_split_word_layout(self):
        rail = ET.Element('node', {'resource-id':'segment-today', 'bounds': '[40,300][200,420]'})
        viewport = ET.Element('node', {'scrollable': 'true', 'bounds': '[0,96][640,1232]'})
        with patch.object(visual.ui, 'nodes', return_value=[rail, viewport]), \
                patch.object(visual.ui, 'shell'), self.assertRaises(AssertionError):
            visual.audit_large_library()

    def test_large_library_rejects_unreachable_actions(self):
        rail = ET.Element('node', {'bounds': '[40,300][600,420]'})
        viewport = ET.Element('node', {'scrollable': 'true', 'bounds': '[0,96][640,1232]'})
        with patch.object(visual.ui, 'wait_tagged', return_value=rail), \
                patch.object(visual.ui, 'nodes', return_value=[viewport]), patch.object(visual.ui, 'shell'), \
                self.assertRaises(AssertionError):
            visual.audit_large_library()

    def test_large_controls_reject_partially_clipped_rows(self):
        items = [ET.Element('node', {'resource-id': 'library-create', 'bounds': '[40,1100][600,1232]'}),
                 ET.Element('node', {'scrollable': 'true', 'bounds': '[0,96][640,1232]'})]
        with patch.object(visual.ui, 'nodes', return_value=items), patch.object(visual.ui, 'shell'), \
                self.assertRaises(AssertionError):
            visual.audit_large_controls(['library-create'], 'clipped')

    def test_before_capture_uses_the_existing_navigation_contract(self):
        with tempfile.TemporaryDirectory() as tmp, ExitStack() as mocks:
            mocks.enter_context(patch.object(visual, 'CONFIGS', [('normal', 720, 1600, '1.0')]))
            mocks.enter_context(patch.object(visual.time, 'sleep'))
            mocks.enter_context(patch.object(visual, 'require_qa'))
            mocks.enter_context(patch.object(visual, 'capture'))
            mocks.enter_context(patch.object(visual, 'training_action'))
            open_app = mocks.enter_context(patch.object(visual.ui, 'open_app'))
            for name in ['shell', 'seed', 'wait_tagged', 'tap', 'require', 'nodes']:
                mocks.enter_context(patch.object(visual.ui, name))
            visual.ui.shell.return_value = 'Success'
            navigate = mocks.enter_context(patch.object(visual.ui, 'navigate'))
            original_output = visual.ui.OUT
            try:
                visual.capture_phase('before', Path(tmp))
                self.assertEqual([c.args[0] for c in navigate.call_args_list],
                                 ['Court', 'Court', 'Train', 'Progress', 'Quests', 'Me', 'Court', 'Me'])
                self.assertEqual(open_app.call_count, 2)
            finally:
                visual.ui.OUT = original_output

    def test_physical_phone_is_rejected_before_any_fixture_access(self):
        with patch.object(visual.ui, 'shell', return_value='0'), \
                patch.object(visual.ui, 'adb') as adb, self.assertRaises(AssertionError):
            visual.require_qa()
        adb.assert_not_called()

    def test_wrong_named_emulator_is_rejected(self):
        with patch.object(visual.ui, 'shell', return_value='1'), \
                patch.object(visual.ui, 'adb', return_value=b'Personal_AVD\nOK\n'), \
                self.assertRaises(AssertionError):
            visual.require_qa()

    def test_five_named_nonoverlapping_48dp_targets_pass(self):
        with patch.object(visual.ui, 'nodes', return_value=list(dock_fixture().iter('node'))):
            self.assertEqual(len(visual.audit_dock()), 5)

    def test_small_disabled_or_overlapping_destinations_fail(self):
        for attribute, value in [('bounds', '[26,1408][90,1504]'), ('enabled', 'false'),
                                 ('bounds', '[158,1408][288,1504]')]:
            root = dock_fixture()
            root[0].set(attribute, value)
            with self.subTest(attribute=attribute, value=value), \
                    patch.object(visual.ui, 'nodes', return_value=list(root.iter('node'))), \
                    self.assertRaises(AssertionError):
                visual.audit_dock()

    def test_tall_navigation_is_rejected(self):
        root = dock_fixture()
        root.set('bounds', '[24,1300][696,1512]')
        with patch.object(visual.ui, 'nodes', return_value=list(root.iter('node'))), \
                self.assertRaises(AssertionError):
            visual.audit_dock()


if __name__ == '__main__':
    unittest.main()
