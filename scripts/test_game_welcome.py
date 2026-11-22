"""Legacy game verification enters the current welcome once, without replaying game actions."""
import sys
import unittest
from pathlib import Path
from unittest.mock import patch,call
sys.path.insert(0,str(Path(__file__).resolve().parent))
import verify_android_game_flow as game

class GameWelcomeTest(unittest.TestCase):
    def testCurrentWelcomeAndGuideAreDismissedBeforeGameActions(self):
        welcome={"resource-id":"welcome-swipe-ball"};skip={"resource-id":"tour-skip"}
        with patch.object(game.ui,"open_app") as opened,patch.object(game.ui,"nodes",return_value=[]),\
             patch.object(game.ui,"tagged",side_effect=[welcome,skip]),\
             patch.object(game.ui,"tap") as tap,patch.object(game.ui,"wait_tagged") as wait:
            game.open_game_app()
            opened.assert_called_once()
            self.assertEqual([call(welcome),call(skip)],tap.call_args_list)
            wait.assert_called_once_with("dock-home",attempts=20)
    def testOldAlreadyOpenCourtDoesNotReplayAnyNavigation(self):
        with patch.object(game.ui,"open_app"),patch.object(game.ui,"nodes",return_value=[]),\
             patch.object(game.ui,"tagged",return_value=None),patch.object(game.ui,"tap") as tap:
            game.open_game_app();tap.assert_not_called()

if __name__=="__main__":unittest.main()
