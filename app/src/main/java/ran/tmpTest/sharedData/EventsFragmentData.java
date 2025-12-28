package ran.tmpTest.sharedData;

import android.content.SharedPreferences;

public class EventsFragmentData
{
    public static int gameChosen;

    public static void initValues(SharedPreferences sharedPreferences)
    {
        gameChosen = sharedPreferences.getInt("gameChosenEventsFragment",-1);
    }

    public static void saveInMemory(SharedPreferences.Editor editor,boolean toApply)
    {
        editor.putInt("gameChosenEventsFragment",gameChosen);
        if (toApply)
            editor.apply();
    }
}
