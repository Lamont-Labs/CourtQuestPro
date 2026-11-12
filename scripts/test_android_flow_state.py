"""Readiness means the persisted rollover is complete, not that a delay elapsed."""
import unittest
from verify_android_game_flow import daily_reset_complete

class AndroidStateReadinessTest(unittest.TestCase):
    def reset(self):
        return dict(activeDate='2026-11-12', matchPlayer='', matchOpponent='', playerScore=0, opponentScore=0, pointHistory=[])
    def test_accepts_the_completed_persisted_reset(self):
        self.assertTrue(daily_reset_complete(self.reset(), '2026-11-12'))
    def test_rejects_the_previous_day_even_with_zero_scores(self):
        value=self.reset();value['activeDate']='2026-11-11'
        self.assertFalse(daily_reset_complete(value, '2026-11-12'))
    def test_a_new_date_cannot_hide_stale_identity_or_attempts(self):
        for key,value in [('matchPlayer','Jordan'),('matchOpponent','Casey'),('playerScore',5),('opponentScore',2),('pointHistory',[True])]:
            record=self.reset();record[key]=value
            self.assertFalse(daily_reset_complete(record,'2026-11-12'),key)
    def test_incomplete_persisted_fields_are_not_ready(self):
        self.assertFalse(daily_reset_complete({}, '2026-11-12'))

if __name__=='__main__':unittest.main()
