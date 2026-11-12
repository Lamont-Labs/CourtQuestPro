import unittest
from xml.etree import ElementTree as ET
from verify_android_daily_flow import find


class AndroidFlowSelectorTest(unittest.TestCase):
    def node(self, bounds, label='Record successful attempt'):
        return ET.Element('node', {'content-desc': label, 'text': '', 'bounds': bounds})

    def test_offscreen_zero_area_node_cannot_be_a_tap_target(self):
        self.assertIsNone(find([self.node('[0,0][0,0]')], 'Record successful attempt'))

    def test_visible_target_wins_over_offscreen_semantic_duplicate(self):
        hidden = self.node('[0,0][0,0]')
        visible = self.node('[40,500][600,610]')
        self.assertIs(find([hidden, visible], 'Record successful attempt'), visible)

    def test_visible_navigation_label_is_preserved(self):
        node = ET.Element('node', {'text': 'Progress', 'bounds': '[320,1300][450,1420]'})
        self.assertIs(find([node], 'Progress'), node)


if __name__ == '__main__':
    unittest.main()
