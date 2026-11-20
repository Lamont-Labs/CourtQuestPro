import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch
from xml.etree import ElementTree as ET

PATH=Path(__file__).resolve().parents[1]/"tools/jersey/verify_device.py"
spec=importlib.util.spec_from_file_location("jersey_fixture_wait",PATH)
journey=importlib.util.module_from_spec(spec)
spec.loader.exec_module(journey)

class FixtureWaitTest(unittest.TestCase):
    def testV56CanonicalFixtureIncludesPrimitiveDefaultsWithoutChangingExistingValues(self):
        raw={"drillResults":[{"drillId":"s1","completed":8},{"drillId":"b1","practice":True}],
             "totalXp":100,"matches":[{"xp":0}]}
        fixture=journey.v56_fixture(raw)
        self.assertNotIn("practice",raw["drillResults"][0])
        self.assertIs(False,fixture["drillResults"][0]["practice"])
        self.assertIs(True,fixture["drillResults"][1]["practice"])
        self.assertEqual(raw["matches"],fixture["matches"])
        self.assertEqual(raw["totalXp"],fixture["totalXp"])
        with patch.object(journey,"state",return_value=fixture):
            self.assertEqual(fixture,journey.wait_for_fixture(fixture))

    def testPartialXmlAndStaleValuesMustNotPassTheFixtureGate(self):
        expected={"jerseyDoubleZero":True,"totalXp":100}
        with patch.object(journey,"state",side_effect=[ET.ParseError("in progress"),
            {"jerseyDoubleZero":False,"totalXp":0},dict(expected)]),patch.object(journey.time,"sleep"):
            self.assertEqual(expected,journey.wait_for_fixture(expected))

    def testMissingOrPersistentlyWrongDataStillFailsWithABoundedDeadline(self):
        with patch.object(journey,"state",return_value={"totalXp":0}), \
             patch.object(journey.time,"monotonic",side_effect=[0,0,31]), \
             patch.object(journey.time,"sleep"):
            with self.assertRaisesRegex(AssertionError,"did not yet match"):
                journey.wait_for_fixture({"totalXp":100},timeout=30)

if __name__=="__main__":unittest.main()
