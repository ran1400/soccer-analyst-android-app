package ran.tmpTest.db.repositories;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ran.tmpTest.db.AppDatabase;
import ran.tmpTest.db.daos.EventInGameDao;
import ran.tmpTest.db.entities.EventInGameEntity;
import ran.tmpTest.utils.EventInGame;

public class EventInGameRepository
{
    public final EventInGameDao eventInGameDao;
    private Map<Long, List<EventInGame>> eventsInGame; //gamedId -> list of events

    public EventInGameRepository(AppDatabase db)
    {
        eventInGameDao = db.eventInGameDao();
        eventsInGame = new HashMap<Long,List<EventInGame>>();
    }
    private EventInGameEntity toEntity(EventInGame event) {
        return new EventInGameEntity(
                event.gameId,
                event.gamePart.toString(),
                event.team.toString(),
                event.time,
                event.playerNum,
                event.eventName
        );
    }

    private EventInGame fromEntity(EventInGameEntity entity) {
        return new EventInGame(
                entity.gameId,
                entity.eventId,
                EventInGame.GamePart.valueOf(entity.gamePart),
                EventInGame.Team.valueOf(entity.team),
                entity.time,
                entity.playerNum,
                entity.eventName
        );
    }

    public List<EventInGame> getEventsInGameWithBlocking(long gameId) // do not run on the main thread
    {
        List<EventInGame> events = eventsInGame.get(gameId);
        if (events != null)
            return events;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<EventInGame>> future = executor.submit(() ->
        {
            List<EventInGameEntity> eventEntities = eventInGameDao.getEventsFromGame(gameId);
            List<EventInGame> res = new ArrayList<>();
            for(EventInGameEntity eventEntity : eventEntities)
                res.add(fromEntity(eventEntity));
            eventsInGame.put(gameId,res);
            return res;
        });
        try {return future.get();}
        catch(Exception e) {return null;} //TODO make error msg
    }

    public void addEventToGame(EventInGame eventInGame)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            List<EventInGame> gameEvents = getEventsInGameWithBlocking(eventInGame.gameId);
            long eventId = eventInGameDao.insertEvent(toEntity(eventInGame));
            eventInGame.eventId = eventId;
            gameEvents.add(eventInGame);
        });

    }

    public void deleteEventInGame(EventInGame eventInGame)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            List<EventInGame> gameEvents = getEventsInGameWithBlocking(eventInGame.gameId);
            eventInGameDao.deleteEventById(eventInGame.eventId);
            gameEvents.remove(eventInGame);
        });
    }

    public void cancelDeleteEventInGame(EventInGame eventInGame)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            List<EventInGame> gameEvents = getEventsInGameWithBlocking(eventInGame.gameId);
            EventInGameEntity eventInGameEntity = toEntity(eventInGame);
            eventInGameEntity.eventId = eventInGame.eventId;
            eventInGameDao.insertEvent(eventInGameEntity);
        });
    }

    public void updateEventInGame(EventInGame newEventInGame)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            EventInGame oldEvent = fromEntity(eventInGameDao.getEventInGame(newEventInGame.gameId));
            oldEvent.eventName = newEventInGame.eventName;
            oldEvent.playerNum = newEventInGame.playerNum;
            oldEvent.team = newEventInGame.team;
            eventInGameDao.updateEvent(toEntity(oldEvent));
        });
    }
}
