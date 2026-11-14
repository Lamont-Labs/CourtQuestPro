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
    def test_before_capture_uses_the_existing_navigation_contract(self):
        with tempfile.TemporaryDirectory() as tmp, ExitStack() as mocks:
            mocks.enter_context(patch.object(visual, 'CONFIGS', [('normal', 720, 1600, '1.0')]))
            mocks.enter_context(patch.object(visual.time, 'sleep'))
            mocks.enter_context(patch.object(visual, 'capture'))
            mocks.enter_context(patch.object(visual, 'training_action'))
            for name in ['shell', 'seed', 'wait_tagged', 'tap', 'require', 'nodes']:
                mocks.enter_context(patch.object(visual.ui, name))
            navigate = mocks.enter_context(patch.object(visual.ui, 'navigate'))
            original_output = visual.ui.OUT
            try:
                visual.capture_phase('before', Path(tmp))
                self.assertEqual([c.args[0] for c in navigate.call_args_list],
                                 ['Court', 'Court', 'Train', 'Progress', 'Quests', 'Me', 'Court'])
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
