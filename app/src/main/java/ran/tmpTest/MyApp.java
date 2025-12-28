package ran.tmpTest;

import android.app.Application;
import android.content.Context;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ran.tmpTest.dataBase.DbRepository;
import ran.tmpTest.utils.SharedPreferencesHandler;


public class MyApp extends Application
{
    private static MyApp instance;
    public static DbRepository dbRepository;
    public static SharedPreferencesHandler sharedPreferencesHandler;

    public void onCreate()
    {
        super.onCreate();
        instance = this;
        ExecutorService executor = Executors.newSingleThreadExecutor(); //will shut down when the app is killed
        dbRepository = new DbRepository(this, executor);
        sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        sharedPreferencesHandler.initValues();
    }


    public static Context getContext()
    {
        return instance;
    }

}
