package ran.tmpTest;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;


import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.EventInGame;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.lang.reflect.Type;


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
        saveDataToMemory();
        clockHandler.removeCallbacks(clockThread);
    }

    public void onResume()
    {
        super.onResume();
        clockCheck();
    }

    private void getDataFromMemory()
    {
        AppData.GameFragmentData.gameChosen = sharedPreferences.getInt("gameChosenGameFragment",-1);
        AppData.EventsFragmentsData.gameChosen = sharedPreferences.getInt("gameChosenEventsFragment",-1);
        Log.d("MyApp","event fragment - game chosen = " + AppData.EventsFragmentsData.gameChosen);
        EventInGame.GamePart gamePartChosen = getDataFromMemory("gamePartChosen", EventInGame.GamePart.class);
        if (gamePartChosen == null)
            AppData.GameFragmentData.gamePartChosen = EventInGame.GamePart.HALF_1;
        else
            AppData.GameFragmentData.gamePartChosen = gamePartChosen;
        EventInGame.Team teamChosen = getDataFromMemory("teamChosen", EventInGame.Team.class);
        if (teamChosen == null)
            AppData.GameFragmentData.teamChosen = EventInGame.Team.NON;
        else
            AppData.GameFragmentData.teamChosen = teamChosen;
        AppData.GameFragmentData.playerChosenDigit1 = sharedPreferences.getInt("playerChosenDigit1",0);
        AppData.GameFragmentData.playerChosenDigit2 = sharedPreferences.getInt("playerChosenDigit2",0);
    }

    public void saveDataToMemory()
    {
        saveDataToMemory("teamChosen", AppData.GameFragmentData.teamChosen);
        saveDataToMemory("gamePartChosen",AppData.GameFragmentData.gamePartChosen);
        editor.putInt("gameChosenGameFragment",AppData.GameFragmentData.gameChosen);
        editor.putInt("gameChosenEventsFragment",AppData.EventsFragmentsData.gameChosen);
        editor.putInt("playerChosenDigit1",AppData.GameFragmentData.playerChosenDigit1);
        editor.putInt("playerChosenDigit2",AppData.GameFragmentData.playerChosenDigit2);
        editor.apply();
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
            AppData.GameFragmentData.sec++;
            if (AppData.GameFragmentData.sec == 60)
            {
                AppData.GameFragmentData.min++;
                if (AppData.GameFragmentData.min > CLOCK_MAX_VALUE)
                {
                    stopClock();
                    return;
                }
                AppData.GameFragmentData.sec = 0;
            }
            if (AppData.gameFragment != null)
                if (AppData.gameFragment.isAdded() && AppData.gameFragment.isVisible())
                        AppData.gameFragment.updateClockText();
        }
    }

    public void startClock()
    {
        AppData.GameFragmentData.clockRun = true;
        clockThread.run();
        editor.putLong("clock", System.currentTimeMillis()).apply();
    }

    public void stopClock()
    {
        AppData.GameFragmentData.clockRun = false;
        editor.putLong("clock",0).apply();
        clockHandler.removeCallbacks(clockThread);
        AppData.GameFragmentData.min = 0;
        AppData.GameFragmentData.sec = -1;
        if (AppData.gameFragment != null)
            if (AppData.gameFragment.isAdded() && AppData.gameFragment.isVisible())
                AppData.gameFragment.resetClock();
    }



    private void clockCheck()
    {
        long clockTimeStamp = sharedPreferences.getLong("clock",0);
        AppData.GameFragmentData.clockRun = clockTimeStamp != 0;
        if (!AppData.GameFragmentData.clockRun)
            return;
        int timeToAdd = (int) ((System.currentTimeMillis() - clockTimeStamp) / 1000);
        AppData.GameFragmentData.sec = (timeToAdd % 60);
        AppData.GameFragmentData.min = (timeToAdd / 60) ;
        if (AppData.GameFragmentData.min > CLOCK_MAX_VALUE )
        {
            AppData.GameFragmentData.clockRun = false;
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
        editor.apply();
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