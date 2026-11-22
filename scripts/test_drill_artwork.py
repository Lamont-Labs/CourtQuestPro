import json
import unittest

from scripts.verify_drill_artwork import (
    FALLBACK,
    MANIFEST_PATH,
    build_manifest,
    render_coverage,
    REPORT_PATH,
)


class DrillArtworkManifestTests(unittest.TestCase):
    def test_every_builtin_drill_has_an_offline_asset_and_sequence(self):
        manifest = build_manifest()
        committed = json.loads(MANIFEST_PATH.read_text(encoding="utf-8"))
        self.assertEqual(manifest, committed)
        self.assertEqual(18, len(manifest["drills"]))
        self.assertEqual(len(manifest["drills"]), len({entry["id"] for entry in manifest["drills"]}))
        for entry in manifest["drills"]:
            self.assertTrue(entry["assetPath"].endswith(f"/{entry['id']}.webp"))
            self.assertGreaterEqual(len(entry["sequence"]), 2)
            self.assertEqual(64, len(entry["sha256"]))

    def test_comparison_exclusions_explain_the_safety_or_accuracy_reason(self):
        manifest = build_manifest()
        for entry in manifest["drills"]:
            comparison = entry["comparison"]
            if not comparison["included"]:
                self.assertTrue(comparison.get("reason"), entry["id"])

    def test_custom_and_missing_art_fallbacks_are_explicit(self):
        self.assertIn("No drill-specific artwork is inferred", FALLBACK["customDrills"])
        self.assertIn("explicit offline-artwork notice", FALLBACK["missingBuiltInArtwork"])

    def test_coverage_report_lists_every_builtin_drill(self):
        manifest = build_manifest()
        report = render_coverage(manifest)
        committed = REPORT_PATH.read_text(encoding="utf-8")
        self.assertEqual(report, committed)
        for entry in manifest["drills"]:
            self.assertIn(f"| `{entry['id']}` | {entry['title']} |", report)


if __name__ == "__main__":
    unittest.main()
