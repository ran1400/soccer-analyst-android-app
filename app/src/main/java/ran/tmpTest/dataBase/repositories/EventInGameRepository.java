package ran.tmpTest.dataBase.repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;


import ran.tmpTest.dataBase.AppDatabase;
import ran.tmpTest.dataBase.daos.EventInGameDao;
import ran.tmpTest.dataBase.entities.EventInGameEntity;
import ran.tmpTest.utils.dataStructures.EventInGame;

public class EventInGameRepository
{
    private final EventInGameDao eventInGameDao;
    private final Map<Long, List<EventInGame>> eventsInGame; //gamedId -> list of events
    private final ExecutorService executorService;

    public EventInGameRepository(AppDatabase db, ExecutorService executorService)
    {
        eventInGameDao = db.eventInGameDao();
        eventsInGame = new HashMap<Long,List<EventInGame>>();
        this.executorService = executorService;
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

    public List<EventInGame> getEventsInGameWithBlockingUI(long gameId)// do not run on the main thread
    {
        List<EventInGame> events = eventsInGame.get(gameId);
        if (events != null)
            return events;
        List<EventInGameEntity> eventEntities = eventInGameDao.getEventsFromGame(gameId);
        List<EventInGame> res = new ArrayList<>();
        for(EventInGameEntity eventEntity : eventEntities)
            res.add(fromEntity(eventEntity));
        eventsInGame.put(gameId,res);
        return res;
    }

    public void addEventToGame(EventInGame eventInGame)
    {
        executorService.execute(() ->
        {
            List<EventInGame> gameEvents = getEventsInGameWithBlockingUI(eventInGame.gameId);
            eventInGame.eventId = eventInGameDao.insertEvent(toEntity(eventInGame));
            gameEvents.add(eventInGame);
        });
    }

    public void deleteEventInGame(EventInGame eventInGame)
    {
        executorService.execute(() ->
        {
            List<EventInGame> gameEvents = getEventsInGameWithBlockingUI(eventInGame.gameId);
            eventInGameDao.deleteEventById(eventInGame.eventId);
            gameEvents.remove(eventInGame);
        });
    }

    public void cancelDeleteEventInGame(EventInGame eventInGame)
    {
        executorService.execute(() ->
        {
            EventInGameEntity eventInGameEntity = toEntity(eventInGame);
            eventInGameEntity.eventId = eventInGame.eventId;
            eventInGameDao.insertEvent(eventInGameEntity);
        });
    }

    public void updateEventInGame(EventInGame newEventInGame)
    {
        executorService.execute(() ->
        {
            EventInGame oldEvent = fromEntity(eventInGameDao.getEventInGame(newEventInGame.gameId));
            oldEvent.eventName = newEventInGame.eventName;
            oldEvent.playerNum = newEventInGame.playerNum;
            oldEvent.team = newEventInGame.team;
            eventInGameDao.updateEvent(toEntity(oldEvent));
        });
    }
}
