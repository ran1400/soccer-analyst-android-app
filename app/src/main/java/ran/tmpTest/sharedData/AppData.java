package ran.tmpTest.sharedData;

import ran.tmpTest.EventsFragment;
import ran.tmpTest.GameFragment;
import ran.tmpTest.MainActivity;
import ran.tmpTest.SettingFragment;
import ran.tmpTest.db.DbRepository;
import ran.tmpTest.utils.EventInGame;

public class AppData
{
    public static DbRepository dbRepository;
    public static MainActivity mainActivity;
    public static GameFragment gameFragment;
    public static SettingFragment settingFragment;
    public static EventsFragment eventsFragment;

    public class EventsFragmentsData
    {
        public static int gameChosen;
    }
    public class SettingFragmentData
    {
        public static int listChoosePosition = -1; // for drag and dop list
    }

    public class GameFragmentData
    {
        public static int min,sec;
        public static boolean clockRun;
        public static int gameChosen;
        public static EventInGame.GamePart gamePartChosen;
        public static EventInGame.Team teamChosen;
        public static int playerChosenDigit1;
        public static int playerChosenDigit2;
    }

}