import copy
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import phone_release_gate as gate
from verify_apk import EXPECTED_CERTIFICATE
from verify_phone_update import compatible, identity, saved_values_preserved


class PhoneUpdateContractTest(unittest.TestCase):
    def setUp(self):
        self.old = dict(package_id='com.courtquestpro', version_code=20,
                        certificate_sha256=EXPECTED_CERTIFICATE)
        self.new = dict(self.old, version_code=21)

    def test_original_identity_and_monotonic_increment_pass(self):
        compatible(self.old, self.new)

    def test_equal_downgraded_or_wrong_version_fails(self):
        for version in [19, 20, 22]:
            with self.subTest(version=version), self.assertRaises(RuntimeError):
                compatible(self.old, dict(self.new, version_code=version))

    def test_wrong_previous_release_fails(self):
        with self.assertRaises(RuntimeError):
            compatible(dict(self.old, version_code=9), self.new)

    def test_new_key_is_not_an_upgrade(self):
        with self.assertRaises(RuntimeError):
            compatible(self.old, dict(self.new, certificate_sha256='0' * 64))

    def test_two_matching_replacement_keys_still_fail(self):
        with self.assertRaises(RuntimeError):
            compatible(dict(self.old, certificate_sha256='0' * 64),
                       dict(self.new, certificate_sha256='0' * 64))

    def test_package_change_fails(self):
        with self.assertRaises(RuntimeError):
            compatible(self.old, dict(self.new, package_id='com.courtquestpro.replacement'))

    def test_all_existing_saved_values_must_survive(self):
        old = dict(playerName='QA', xp=100, drafts=[dict(successful=3)], history=[1, 2])
        self.assertTrue(saved_values_preserved(old, copy.deepcopy(old)))
        self.assertTrue(saved_values_preserved(old, dict(old, newDefault=[])))
        for key, value in [('playerName', ''), ('xp', 0), ('drafts', []), ('history', [2, 1])]:
            changed = dict(old)
            changed[key] = value
            self.assertFalse(saved_values_preserved(old, changed), key)
        self.assertFalse(saved_values_preserved(old, {}))
        self.assertFalse(saved_values_preserved(dict(xp=0), dict(xp=False)))

    def test_invalid_or_multiple_signatures_are_rejected(self):
        signatures = ['invalid signature', 'Verified using v2 scheme (APK Signature Scheme v2): true\n' +
                      '\n'.join(f'Signer #{i} certificate SHA-256 digest: {EXPECTED_CERTIFICATE}' for i in [1, 2])]
        for signature in signatures:
            with patch('verify_phone_update.run', return_value=signature):
                with self.assertRaises(RuntimeError):
                    identity(Path('missing.apk'), Path('tools'), Path('output'), 'test')


class ExactCommitGateTest(unittest.TestCase):
    def test_failed_or_wrong_source_baseline_is_rejected(self):
        for run in [dict(head_sha='wrong', head_branch=gate.BRANCH, conclusion='success',
                         path='.github/workflows/verify-native-library-flow.yml'),
                    dict(head_sha=gate.BASE, head_branch=gate.BRANCH, conclusion='failure',
                         path='.github/workflows/verify-native-library-flow.yml')]:
            with patch.object(gate, 'api', return_value=run), self.assertRaises(RuntimeError):
                gate.source()

    def test_current_commit_failure_blocks_publication(self):
        run = dict(id=1, head_sha='current', head_branch=gate.BRANCH,
                   status='completed', conclusion='failure', html_url='https://github.com/example')
        with patch.dict('os.environ', {'GITHUB_SHA': 'current'}), \
                patch.object(gate, 'api', return_value={'workflow_runs': [run]}):
            with self.assertRaisesRegex(RuntimeError, 'Release blocked'):
                gate.wait_for_ci(Path('must-not-be-written.json'))

    def test_all_current_commit_checks_exclude_stale_results(self):
        run = dict(id=1, head_sha='current', head_branch=gate.BRANCH,
                   status='completed', conclusion='success', html_url='https://github.com/example')
        stale = dict(run, id=2, head_sha='old')
        with tempfile.TemporaryDirectory() as tmp, patch.dict('os.environ', {'GITHUB_SHA': 'current'}), \
                patch.object(gate, 'api', return_value={'workflow_runs': [stale, run]}):
            target = Path(tmp) / 'ci.json'
            gate.wait_for_ci(target)
            self.assertIn('"head_sha": "current"', target.read_text())
            self.assertNotIn('"head_sha": "old"', target.read_text())


if __name__ == '__main__':
    unittest.main()
