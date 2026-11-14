import unittest
from xml.etree import ElementTree as ET

from verify_android_daily_flow import assert_empty_progress


def empty_progress(xp='0', sessions='0', days='0 training days', heading='PLAYER RECORD'):
    root = ET.Element('node')
    for text in [heading, 'TOTAL XP', xp, days]:
        ET.SubElement(root, 'node', {'text': text, 'bounds': '[40,300][600,360]'})
    record = ET.SubElement(root, 'node', {'resource-id': 'progress-record-sessions',
                                        'bounds': '[40,400][600,520]'})
    for text in ['SESSIONS', sessions]:
        ET.SubElement(record, 'node', {'text': text, 'bounds': '[40,400][600,460]'})
    return list(root.iter('node'))


class EmptyProgressTest(unittest.TestCase):
    def test_redesigned_record_requires_real_empty_values(self):
        assert_empty_progress(empty_progress())

    def test_nonempty_or_obsolete_record_is_rejected(self):
        for change in [dict(xp='100'), dict(sessions='2'), dict(days='1 training days'),
                       dict(heading='YOUR TRAINING RECORD')]:
            with self.subTest(change=change), self.assertRaises(AssertionError):
                assert_empty_progress(empty_progress(**change))


if __name__ == '__main__':
    unittest.main()
