import copy
import json
from pathlib import Path
import unittest
from check_local import check_modes

class ModeGuardTest(unittest.TestCase):
    def setUp(self):
        self.value = json.loads((Path(__file__).parent / 'operating-modes.json').read_bytes())

    def test_two_allowed_modes(self):
        check_modes(self.value)

    def test_general_scheduler_cannot_be_enabled(self):
        self.value['modes']['bounded_audio_candidate']['APP_BACKGROUND_TASKS_AUDIO_ONLY'] = 'false'
        with self.assertRaises(ValueError):
            check_modes(self.value)

    def test_no_seeding_or_secret_injection(self):
        for key, val in [('APP_BOOTSTRAP_TEST_USERS_ENABLED', 'true'), ('TOSS_SECRET_KEY', 'synthetic')]:
            candidate = copy.deepcopy(self.value)
            candidate['modes']['cold_preparation'][key] = val
            with self.assertRaises(ValueError):
                check_modes(candidate)

    def test_cold_cannot_accidentally_run_worker(self):
        self.value['modes']['cold_preparation']['APP_BACKGROUND_TASKS_ENABLED'] = 'true'
        with self.assertRaises(ValueError):
            check_modes(self.value)

if __name__ == '__main__':
    unittest.main()
