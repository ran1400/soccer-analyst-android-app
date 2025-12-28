package ran.tmpTest.dataBase;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Database;

import ran.tmpTest.dataBase.daos.EventDao;
import ran.tmpTest.dataBase.daos.EventInGameDao;
import ran.tmpTest.dataBase.daos.GameDao;
import ran.tmpTest.dataBase.entities.EventEntity;
import ran.tmpTest.dataBase.entities.EventInGameEntity;
import ran.tmpTest.dataBase.entities.GameEntity;

@Database(entities = {GameEntity.class, EventInGameEntity.class, EventEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{

    private static volatile AppDatabase INSTANCE;

    public abstract GameDao gameDao();
    public abstract EventInGameDao eventInGameDao();

    public abstract EventDao eventDao();

    public static AppDatabase getInstance(Context context)
    {
        if (INSTANCE == null)
        {
            synchronized (AppDatabase.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "game_database"
                            ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
