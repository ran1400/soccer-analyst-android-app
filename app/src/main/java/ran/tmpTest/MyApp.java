package ran.tmpTest;

import android.app.Application;

import ran.tmpTest.db.DbRepository;
import ran.tmpTest.sharedData.AppData;

public class MyApp extends Application
{
    public void onCreate()
    {
        super.onCreate();
        AppData.dbRepository = new DbRepository(this);
    }
}
