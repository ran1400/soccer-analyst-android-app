package ran.tmpTest.dataBase;

import android.content.Context;

import java.util.concurrent.ExecutorService;

import ran.tmpTest.dataBase.repositories.EventInGameRepository;
import ran.tmpTest.dataBase.repositories.EventRepository;
import ran.tmpTest.dataBase.repositories.GameRepository;


public class DbRepository
{

    public final EventInGameRepository eventInGameRepository;
    public final EventRepository eventRepository;
    public final GameRepository gameRepository;


    public DbRepository(Context context, ExecutorService executorService)
    {
        AppDatabase db = AppDatabase.getInstance(context);
        eventInGameRepository = new EventInGameRepository(db,executorService);
        eventRepository = new EventRepository(db,executorService);
        gameRepository = new GameRepository(db,executorService);
    }

}
