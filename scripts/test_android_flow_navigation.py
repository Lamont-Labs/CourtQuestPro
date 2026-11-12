"""Drawer selectors must retain real labels, geometry and actionable semantics."""
import unittest
from unittest.mock import patch
from xml.etree import ElementTree as ET

import verify_android_daily_flow as ui
import verify_android_navigation_flow as navigation
import verify_android_library_flow as library
import verify_android_mission_flow as mission


def node(tag='', label='', bounds='[0,0][168,160]', **attrs):
    return ET.Element('node', {
        'resource-id': tag, 'text': label, 'bounds': bounds,
        'clickable': 'true', 'enabled': 'true', **attrs,
    })


def drawer(*children):
    root = node('player-navigation-drawer', bounds='[0,0][240,680]')
    root.extend(children)
    return list(root.iter('node'))


class DrawerSelectorTest(unittest.TestCase):
    def setUp(self):
        sleep = patch.object(ui.time, 'sleep')
        sleep.start()
        self.addCleanup(sleep.stop)

    def test_leaf_element_is_a_valid_target_not_a_false_boolean(self):
        leaf = node('nav-home', 'Court')
        self.assertIs(ui.tagged([leaf], 'nav-home'), leaf)
        with patch.object(ui, 'nodes', return_value=drawer(leaf)):
            self.assertIs(ui.drawer_target('Court'), leaf)

    def test_accepts_accessible_name_on_merged_descendant(self):
        target = node('com.courtquestpro:id/nav-home')
        target.append(node(label='', **{'content-desc': 'Court'}))
        with patch.object(ui, 'nodes', return_value=drawer(target)):
            self.assertIs(ui.drawer_target('Court'), target)

    def test_accepts_text_or_exact_line_description(self):
        for attrs in ({'text': 'Court'}, {'content-desc': 'Court'}, {'content-desc': 'Court\nSelected'}):
            with self.subTest(attrs=attrs):
                target = node('nav-home', **attrs)
                with patch.object(ui, 'nodes', return_value=drawer(target)):
                    self.assertIs(ui.drawer_target('Court'), target)

    def test_wrong_missing_or_partial_labels_fail(self):
        for label in ('', 'Train', 'Court settings', 'Courtyard'):
            with self.subTest(label=label):
                with patch.object(ui, 'nodes', return_value=drawer(node('nav-home', label))):
                    with self.assertRaises(AssertionError):
                        ui.drawer_target('Court')

    def test_label_elsewhere_cannot_name_an_unlabeled_target(self):
        with patch.object(ui, 'nodes', return_value=drawer(node('nav-home'), node(label='Court'))):
            with self.assertRaises(AssertionError):
                ui.drawer_target('Court')

    def test_disabled_and_nonclickable_destinations_fail(self):
        for attrs in ({'enabled': 'false'}, {'clickable': 'false'}):
            with patch.object(ui, 'nodes', return_value=drawer(node('nav-home', 'Court', **attrs))):
                with self.assertRaises(AssertionError):
                    ui.drawer_target('Court')

    def test_selected_tab_may_be_nonclickable_in_android_accessibility_export(self):
        target = node('nav-home', clickable='false', selected='true', focusable='true')
        target.append(node(**{'content-desc': 'Court'}))
        with patch.object(ui, 'nodes', return_value=drawer(target)):
            self.assertIs(ui.drawer_target('Court'), target)

    def test_selected_tab_still_requires_focusable_enabled_semantics(self):
        for attrs in ({'focusable': 'false'}, {'enabled': 'false'}):
            values = dict(clickable='false', selected='true', focusable='true')
            values.update(attrs)
            with patch.object(ui, 'nodes', return_value=drawer(node('nav-home', 'Court', **values))):
                with self.assertRaises(AssertionError):
                    ui.drawer_target('Court')

    def test_waits_for_drawer_to_finish_opening(self):
        opener = node('nav-open-handle')
        target = node('nav-home', 'Court')
        with patch.object(ui, 'nodes', side_effect=[[opener], [], drawer(target)]), patch.object(ui, 'tap'):
            self.assertIs(ui.drawer_target('Court'), target)

    def test_zero_area_and_similar_tag_are_not_destinations(self):
        for target in (node('nav-home', 'Court', '[0,0][0,0]'), node('other-nav-home', 'Court')):
            self.assertIsNone(ui.tagged([target], 'nav-home'))
            with patch.object(ui, 'nodes', return_value=drawer(target)):
                with self.assertRaises(AssertionError):
                    ui.drawer_target('Court')

    def test_opens_drawer_before_selecting_destination(self):
        opener = node('nav-open-handle')
        target = node('nav-career', 'Progress')
        with patch.object(ui, 'nodes', side_effect=[[opener], drawer(target)]), patch.object(ui, 'tap') as tap:
            self.assertIs(ui.drawer_target('Progress'), target)
            tap.assert_called_once_with(opener)

    def test_closed_drawer_without_opener_fails(self):
        with patch.object(ui, 'nodes', return_value=[]):
            with self.assertRaises(AssertionError):
                ui.drawer_target('Court')

    def test_short_drawer_scrolls_to_lazy_destination(self):
        region = node(bounds='[0,20][192,680]', scrollable='true')
        initial = drawer(region)
        target = node('nav-me', 'Me')
        with patch.object(ui, 'nodes', side_effect=[initial, initial, drawer(target)]), patch.object(ui, 'shell') as shell:
            self.assertIs(ui.drawer_target('Me'), target)
            shell.assert_called_once_with('input', 'swipe', '96', '515', '96', '185', '250')

    def test_scrolled_drawer_can_search_back_toward_start(self):
        initial = drawer(node(scrollable='true'))
        target = node('nav-home', 'Court')
        with patch.object(ui, 'nodes', side_effect=[initial, initial, drawer(target)]), patch.object(ui, 'shell') as shell:
            self.assertIs(ui.drawer_target('Court', limit=1), target)
            shell.assert_called_once()

    def test_row_outside_scroll_viewport_must_scroll_before_tapping(self):
        # Android may export an entire 80dp row even when its bottom is clipped.
        viewport = node(bounds='[12,64][180,608]', scrollable='true')
        clipped = node('nav-career', 'Progress', '[12,560][180,720]')
        initial = drawer(viewport, clipped)
        complete = node('nav-career', 'Progress', '[12,400][180,560]')
        ready = drawer(viewport, complete)
        with patch.object(ui, 'nodes', side_effect=[initial, initial, ready]), patch.object(ui, 'shell') as shell:
            self.assertIs(ui.drawer_target('Progress', minimum_px=96), complete)
            shell.assert_called_once_with('input', 'swipe', '96', '472', '96', '200', '250')

    def test_unreachable_destination_fails_after_bounded_scroll(self):
        initial = drawer(node(scrollable='true'))
        with patch.object(ui, 'nodes', return_value=initial), patch.object(ui, 'shell') as shell:
            with self.assertRaisesRegex(AssertionError, 'unreachable'):
                ui.drawer_target('Court', limit=1)
            self.assertEqual(shell.call_count, 2)

    def test_partial_target_is_scrolled_until_minimum_hit_area_is_visible(self):
        clipped = node('nav-me', 'Me', '[0,600][168,660]')
        initial = drawer(node(bounds='[0,0][192,680]', scrollable='true'), clipped)
        complete = node('nav-me', 'Me', '[0,400][168,560]')
        with patch.object(ui, 'nodes', side_effect=[initial, initial, drawer(complete)]), patch.object(ui, 'shell') as shell:
            self.assertIs(ui.drawer_target('Me', minimum_px=96), complete)
            shell.assert_called_once()

    def test_navigation_must_close_drawer(self):
        target = node('nav-home', 'Court')
        with patch.object(ui, 'drawer_target', return_value=target), patch.object(ui, 'tap') as tap:
            with patch.object(ui, 'nodes', return_value=[node('nav-open-handle')]):
                ui.navigate('Court')
            tap.assert_called_once_with(target)
            with patch.object(ui, 'nodes', return_value=drawer(target)):
                with self.assertRaisesRegex(AssertionError, 'did not become ready'):
                    ui.navigate('Court')

    def test_all_candidate_flow_click_helpers_use_drawer_navigation(self):
        for flow in (navigation, library, mission):
            with self.subTest(flow=flow.__name__), patch.object(ui, 'navigate') as navigate:
                flow.click('Me')
                navigate.assert_called_once_with('Me')


class CandidateVersionTest(unittest.TestCase):
    def test_source_version_is_authoritative_not_obsolete_environment(self):
        with patch('pathlib.Path.read_text', return_value='    versionCode = 10\n'):
            with patch.dict('os.environ', {'COURTQUEST_VERSION_CODE': '9'}):
                self.assertEqual(ui.expected_version_code(), 10)

    def test_missing_dynamic_or_ambiguous_source_version_fails_explicitly(self):
        for source in ('', 'versionCode = providers.environmentVariable("VERSION")',
                       'versionCode = 9\nversionCode = 10\n'):
            with self.subTest(source=source), patch('pathlib.Path.read_text', return_value=source):
                with self.assertRaises(AssertionError):
                    ui.expected_version_code()


if __name__ == '__main__':
    unittest.main()
