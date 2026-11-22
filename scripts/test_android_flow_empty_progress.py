import unittest
from xml.etree import ElementTree as ET

from verify_android_daily_flow import assert_empty_progress


def empty_progress(xp='0', drills='0', days='0', heading='PLAYER LEVEL'):
    root = ET.Element('node')
    xp_card = ET.SubElement(root, 'node', {'resource-id': 'progress-lifetime-xp',
                                          'bounds': '[40,300][600,420]'})
    for text in [heading, xp, 'TOTAL XP']:
        ET.SubElement(xp_card, 'node', {'text': text, 'bounds': '[40,300][600,360]'})
    record = ET.SubElement(root, 'node', {'resource-id': 'progress-record-sessions',
                                        'bounds': '[40,400][600,520]'})
    for text in ['RECORDED DRILLS', drills]:
        ET.SubElement(record, 'node', {'text': text, 'bounds': '[40,400][600,460]'})
    training_days = ET.SubElement(root, 'node', {'resource-id': 'progress-training-days',
                                                'bounds': '[40,540][600,660]'})
    for text in ['TRAINING DAYS', days]:
        ET.SubElement(training_days, 'node', {'text': text, 'bounds': '[40,540][600,600]'})
    return list(root.iter('node'))


class EmptyProgressTest(unittest.TestCase):
    def test_redesigned_record_requires_real_empty_values(self):
        assert_empty_progress(empty_progress())

    def test_offscreen_metrics_are_scrolled_into_view_before_asserting(self):
        complete = empty_progress()
        initial = [node for node in complete
                   if node.get('resource-id') == 'progress-lifetime-xp'
                   or node.get('text') in {'PLAYER LEVEL', 'TOTAL XP'}]
        visits = []

        def read_tag(tag):
            visits.append(tag)
            return complete

        assert_empty_progress(initial, read_tag)
        self.assertEqual(['progress-record-sessions', 'progress-training-days'], visits)

    def test_scrolled_nonzero_empty_metric_is_still_rejected(self):
        complete = empty_progress(drills='1')
        initial = [node for node in complete
                   if node.get('resource-id') == 'progress-lifetime-xp'
                   or node.get('text') in {'PLAYER LEVEL', 'TOTAL XP'}]
        with self.assertRaises(AssertionError):
            assert_empty_progress(initial, lambda _tag: complete)

    def test_nonempty_or_obsolete_record_is_rejected(self):
        for change in [dict(xp='100'), dict(drills='2'), dict(days='1'),
                       dict(heading='YOUR PLAYER LEVEL')]:
            with self.subTest(change=change), self.assertRaises(AssertionError):
                assert_empty_progress(empty_progress(**change))


if __name__ == '__main__':
    unittest.main()
