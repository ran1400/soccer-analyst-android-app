package ran.tmpTest.db;

import android.content.Context;

import java.util.List;

import ran.tmpTest.db.repositories.EventInGameRepository;
import ran.tmpTest.db.repositories.EventRepository;
import ran.tmpTest.db.repositories.GameRepository;


public class DbRepository
{

    public EventInGameRepository eventInGameRepository;
    public EventRepository eventRepository;
    public GameRepository gameRepository;

    public DbRepository(Context context)
    {
        AppDatabase db = AppDatabase.getInstance(context);
        eventInGameRepository = new EventInGameRepository(db);
        eventRepository = new EventRepository(db);
        gameRepository = new GameRepository(db);
    }

    public static <G> String printList(List<G> l,String listName)
    {
        String res = listName + " : " ;
        if(l.isEmpty())
            return res + " list is empty";
        for (G item : l)
            res += item + "\n";
        return res;
    }



}
