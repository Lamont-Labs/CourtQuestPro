import unittest

from phone_v22_release_gate import (
    ALLOWED_FILES,
    BASE,
    BRANCH,
    WORKFLOWS,
    latest_exact_run,
    validate_source,
)


class V22SourceGateTests(unittest.TestCase):
    def setUp(self):
        self.base_gradle = (
            'defaultConfig {\n'
            '    applicationId = "com.courtquestpro"\n'
            '    versionCode = 21\n'
            '    versionName = "1.0"\n'
            '}\n'
        )
        self.current_gradle = self.base_gradle.replace("versionCode = 21", "versionCode = 22")

    def test_approved_redesign_and_version_only_change_pass(self):
        validate_source(
            BASE,
            f"refs/heads/{BRANCH}",
            ALLOWED_FILES,
            self.base_gradle,
            self.current_gradle,
        )

    def test_wrong_parent_branch_extra_file_or_gradle_change_fails(self):
        changed = set(ALLOWED_FILES)
        cases = [
            ("wrong", f"refs/heads/{BRANCH}", changed, self.current_gradle),
            (BASE, "refs/heads/main", changed, self.current_gradle),
            (BASE, f"refs/heads/{BRANCH}", changed | {"app/src/main/AndroidManifest.xml"},
             self.current_gradle),
            (BASE, f"refs/heads/{BRANCH}", changed,
             self.current_gradle.replace('applicationId = "com.courtquestpro"',
                                         'applicationId = "com.other.app"')),
        ]
        for parent, ref, files, gradle in cases:
            with self.subTest(parent=parent, ref=ref, changed=len(files), package=gradle):
                with self.assertRaises(RuntimeError):
                    validate_source(parent, ref, files, self.base_gradle, gradle)


class ExactCommitGateTests(unittest.TestCase):
    def run_record(self, workflow, sha="new-sha", branch=BRANCH, status="completed",
                   conclusion="success", run_id=100):
        return {
            "id": run_id,
            "head_sha": sha,
            "head_branch": branch,
            "path": WORKFLOWS[workflow],
            "status": status,
            "conclusion": conclusion,
            "html_url": f"https://github.com/example/runs/{run_id}",
        }

    def test_only_latest_exact_branch_run_is_selected(self):
        workflow = "verify-native-library-flow.yml"
        stale = self.run_record(workflow, sha="old-sha", run_id=102)
        current = self.run_record(workflow, run_id=101)
        self.assertEqual(latest_exact_run(workflow, "new-sha", BRANCH, [stale, current])["id"], 101)
        self.assertIsNone(latest_exact_run(workflow, "new-sha", BRANCH, [stale]))

    def test_pending_waits_and_failure_blocks_release(self):
        workflow = "premium-flow-toolkit.yml"
        pending = self.run_record(workflow, status="in_progress", conclusion="")
        self.assertEqual(
            latest_exact_run(workflow, "new-sha", BRANCH, [pending])["status"],
            "in_progress",
        )
        failed = self.run_record(workflow, conclusion="failure")
        with self.assertRaisesRegex(RuntimeError, "Release blocked"):
            latest_exact_run(workflow, "new-sha", BRANCH, [failed])


if __name__ == "__main__":
    unittest.main()
