package ran.tmpTest.utils;

import android.os.Handler;
import android.util.Log;

import ran.tmpTest.MyApp;
import ran.tmpTest.fragments.GameFragment;
import ran.tmpTest.sharedData.GameFragmentData;

public class ClockHandler
{
    private final int CLOCK_MAX_VALUE = 999; //in minutes
    private final ClockThread clockThread;
    private final Handler clockHandler;

    private final GameFragment gameFragment;

    public ClockHandler(GameFragment gameFragment)
    {
        this.gameFragment = gameFragment;
        clockHandler = new Handler();
        clockThread = new ClockThread();
    }


    public void startClock()
    {
        GameFragmentData.clockRun = true;
        clockThread.run();
        MyApp.sharedPreferencesHandler.editor.putLong("clock", System.currentTimeMillis()).apply();
    }

    public void stopClock()
    {
        GameFragmentData.clockRun = false;
        MyApp.sharedPreferencesHandler.editor.putLong("clock",0).apply();
        clockHandler.removeCallbacks(clockThread);
        GameFragmentData.min = 0;
        GameFragmentData.sec = -1;
        gameFragment.resetClock();
    }

    public void stopTicking()
    {
        Log.d("clockHandler","stopTicking");
        clockHandler.removeCallbacks(clockThread);
    }

    public void clockCheck() // call when the user return to the fragment
    {
        long clockTimeStamp = MyApp.sharedPreferencesHandler.sharedPreferences.getLong("clock",0);
        GameFragmentData.clockRun = (clockTimeStamp != 0);
        if (!GameFragmentData.clockRun)
            return;
        int timeToAdd = (int) ((System.currentTimeMillis() - clockTimeStamp) / 1000);
        GameFragmentData.sec = (timeToAdd % 60);
        GameFragmentData.min = (timeToAdd / 60) ;
        if (GameFragmentData.min > CLOCK_MAX_VALUE )
        {
            GameFragmentData.clockRun = false;
            stopClock();
        }
        else
        {
            clockThread.run();
        }
    }


    private class ClockThread implements java.lang.Runnable
    {
        public void run()
        {
            clockHandler.postDelayed(clockThread, 1000);
            GameFragmentData.sec++;
            if (GameFragmentData.sec == 60)
            {
                GameFragmentData.min++;
                if (GameFragmentData.min > CLOCK_MAX_VALUE)
                {
                    stopClock();
                    return;
                }
                GameFragmentData.sec = 0;
            }
            gameFragment.updateClockText();
        }
    }
}
