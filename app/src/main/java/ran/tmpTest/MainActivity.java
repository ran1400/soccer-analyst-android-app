package ran.tmpTest;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.Event;
import ran.tmpTest.utils.saveInMemoryLists.EventsList;
import ran.tmpTest.utils.saveInMemoryLists.GamesList;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity
{
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private View mainActivityView;
    private ClockThread clockThread;
    private Handler clockHandler;
    private final int CLOCK_MAX_VALUE = 999; //in minutes
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityView = findViewById(R.id.mainActivity);
        AppData.mainActivity = this;
        sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        getDataFromMemory();
        clockHandler = new Handler();
        clockThread = new ClockThread();
        AppData.gameFragment = new GameFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,AppData.gameFragment).commit();
    }


    protected void onPause()
    {
        super.onPause();
        saveDataToMemory("events",new EventsList(AppData.events));
        saveDataToMemory("games",new GamesList(AppData.games));
        editor.putBoolean("clockRun",AppData.clockRun);
        if (AppData.clockRun)
        {
            editor.putInt("min", AppData.min);
            editor.putInt("sec", AppData.sec);
            editor.putLong("time", System.currentTimeMillis());
        }
        saveDataToMemory("teamChosen", AppData.teamChosen);
        saveDataToMemory("gamePartChosen",AppData.gamePartChosen);
        editor.putInt("gameChosenGameFragment",GameFragment.gameChosen);
        editor.putInt("gameChosenEventsFragment",EventsFragment.gameChosen);
        editor.putInt("playerChosenDigit1",AppData.playerChosenDigit1);
        editor.putInt("playerChosenDigit2",AppData.playerChosenDigit2);
        editor.commit();
        clockHandler.removeCallbacks(clockThread);
    }

    public void onResume()
    {
        super.onResume();
        clockCheck();
    }

    public View getView()
    {
        return mainActivityView;
    }

    private class ClockThread implements java.lang.Runnable
    {
        public void run()
        {
            clockHandler.postDelayed(clockThread, 1000);
            AppData.sec++;
            if (AppData.sec == 60)
            {
                AppData.min++;
                if (AppData.min > CLOCK_MAX_VALUE)
                {
                    stopClock();
                    return;
                }
                AppData.sec = 0;
            }
            if (AppData.gameFragment != null)
                if (AppData.gameFragment.isAdded() && AppData.gameFragment.isVisible())
                        AppData.gameFragment.updateClockText();
        }
    }

    public void startClock()
    {
        AppData.clockRun = true;
        clockThread.run();
    }

    public void stopClock()
    {
        AppData.clockRun = false;
        clockHandler.removeCallbacks(clockThread);
        AppData.min = 0;
        AppData.sec = -1;
        if (AppData.gameFragment != null)
            if (AppData.gameFragment.isAdded() && AppData.gameFragment.isVisible())
                AppData.gameFragment.resetClock();
    }

    private void getDataFromMemory()
    {
        EventsList events = getDataFromMemory("events",EventsList.class);
        if (events == null)
            AppData.events = new ArrayList<>();
        else
            AppData.events = events.list;
        GamesList games = getDataFromMemory("games",GamesList.class);
        if ( games == null)
            AppData.games = new ArrayList<>();
        else
            AppData.games = games.list;
        AppData.makeGamesStringList();
        GameFragment.gameChosen = sharedPreferences.getInt("gameChosenGameFragment",-1);
        EventsFragment.gameChosen = sharedPreferences.getInt("gameChosenEventsFragment",-1);
        AppData.clockRun = sharedPreferences.getBoolean("clockRun",false);
        Event.GamePart gamePartChosen = getDataFromMemory("gamePartChosen", Event.GamePart.class);
        if (gamePartChosen == null)
            AppData.gamePartChosen = Event.GamePart.HALF_1;
        else
            AppData.gamePartChosen = gamePartChosen;
        Event.Team teamChosen = getDataFromMemory("teamChosen", Event.Team.class);
        if (teamChosen == null)
            AppData.teamChosen = Event.Team.NON;
        else
            AppData.teamChosen = teamChosen;
        AppData.playerChosenDigit1 = sharedPreferences.getInt("playerChosenDigit1",0);
        AppData.playerChosenDigit2 = sharedPreferences.getInt("playerChosenDigit2",0);
    }

    private void clockCheck()
    {
        if ( AppData.clockRun )
        {
            AppData.min = sharedPreferences.getInt("min", 0);
            AppData.sec = sharedPreferences.getInt("sec", 0);
            long pastTime = sharedPreferences.getLong("time", 0);
            int timeToAdd = (int) ((System.currentTimeMillis() - pastTime) / 1000);
            AppData.sec += (timeToAdd % 60);
            if (AppData.sec >= 60)
            {
                AppData.sec -= 60;
                AppData.min +=  (timeToAdd / 60) + 1;
            }
            else
                AppData.min += (timeToAdd / 60) ;
            if (AppData.min > CLOCK_MAX_VALUE )
            {
                AppData.clockRun = false;
                stopClock();
            }
            else
            {
                if (AppData.gameFragment != null)
                    if (AppData.gameFragment.isAdded() && AppData.gameFragment.isVisible())
                        AppData.gameFragment.updateClockText();
                clockThread.run();
            }
        }
    }
    public void showSnackBar(String msg, int time)
    {
        Snackbar snackBar = Snackbar.make(mainActivityView, msg, Snackbar.LENGTH_SHORT);
        snackBar.setAction("", null).setDuration(time).show();
    }




    public void gameBtn(View view)
    {
        if (AppData.gameFragment == null)
            AppData.gameFragment = new GameFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,AppData.gameFragment).commit();
    }

    public void eventsBtn(View view)
    {
        if (AppData.eventsFragment == null)
            AppData.eventsFragment = new EventsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,AppData.eventsFragment).commit();
    }

    public void settingBtn(View view)
    {
        if (AppData.settingFragment == null)
            AppData.settingFragment = new SettingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,AppData.settingFragment).commit();
    }

    public <T> void saveDataToMemory(String key,T data)
    {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        editor.putString(key, json);
        editor.commit();
    }

    public <T> T getDataFromMemory(String key,Type type)
    {
        T data = null;
        String serializedObject = sharedPreferences.getString(key, null);
        if (serializedObject != null)
        {
            Gson gson = new Gson();
            data = gson.fromJson(serializedObject, type);
        }
        return data;
    }
}