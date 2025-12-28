package ran.tmpTest.sharedData;

import android.content.SharedPreferences;

import ran.tmpTest.MyApp;
import ran.tmpTest.utils.dataStructures.EventInGame;

public class GameFragmentData
{
    public static int min,sec;
    public static boolean clockRun;
    public static int gameChosen;
    public static EventInGame.GamePart gamePartChosen;
    public static EventInGame.Team teamChosen;
    public static int playerChosenDigit1;
    public static int playerChosenDigit2;

    public static void initValues(SharedPreferences sharedPreferences)
    {
        gameChosen = sharedPreferences.getInt("gameChosenGameFragment",-1);
        gamePartChosen = MyApp.sharedPreferencesHandler.getData("gamePartChosen", EventInGame.GamePart.class,EventInGame.GamePart.HALF_1);
        teamChosen = MyApp.sharedPreferencesHandler.getData("teamChosen", EventInGame.Team.class,EventInGame.Team.NON);
        playerChosenDigit1 = sharedPreferences.getInt("playerChosenDigit1",0);
        playerChosenDigit2 = sharedPreferences.getInt("playerChosenDigit2",0);
    }

    public static void saveInMemory(SharedPreferences.Editor editor,boolean toApply)
    {
        MyApp.sharedPreferencesHandler.saveData("teamChosen",teamChosen,false);
        MyApp.sharedPreferencesHandler.saveData("gamePartChosen",gamePartChosen,false);
        editor.putInt("gameChosenGameFragment",gameChosen);
        editor.putInt("playerChosenDigit1",playerChosenDigit1);
        editor.putInt("playerChosenDigit2",playerChosenDigit2);
        if(toApply)
            editor.apply();
    }
}

