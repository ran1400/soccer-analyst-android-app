package ran.tmpTest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;


import ran.tmpTest.fragments.EventsFragment;
import ran.tmpTest.fragments.GameFragment;
import ran.tmpTest.fragments.SettingFragment;




public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameBtn(null);
    }


    protected void onPause()
    {
        super.onPause();
        MyApp.sharedPreferencesHandler.saveDataInMemory();
    }


    public void gameBtn(View view)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment gameFragment = fragmentManager.findFragmentByTag("gameFragment");

        if (gameFragment == null)
            gameFragment = new GameFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,gameFragment,"gameFragment")
                .commit();
    }

    public void eventsBtn(View view)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment eventsFragment = fragmentManager.findFragmentByTag("eventsFragment");

        if (eventsFragment == null)
            eventsFragment = new EventsFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,eventsFragment,"eventsFragment")
                .commit();
    }


    public void settingBtn(View view)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment settingFragment = fragmentManager.findFragmentByTag("settingFragment");

        if (settingFragment == null)
            settingFragment = new SettingFragment();

        fragmentManager.beginTransaction()
                       .replace(R.id.fragmentContainer,settingFragment,"settingFragment")
                       .commit();
    }

}