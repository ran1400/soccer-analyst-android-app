package ran.tmpTest.dataBase.repositories;

import java.util.List;
import java.util.concurrent.ExecutorService;

import ran.tmpTest.dataBase.AppDatabase;
import ran.tmpTest.dataBase.daos.EventDao;
import ran.tmpTest.dataBase.entities.EventEntity;

public class EventRepository
{
    private final EventDao eventDao;
    public List<String> eventNames;
    private List<Long> eventIds;
    private final ExecutorService executorService;

    public EventRepository(AppDatabase db, ExecutorService executorService)
    {
        eventDao = db.eventDao();
        this.executorService = executorService;
        executorService.execute(() ->
        {
            eventNames = eventDao.getAllEventNames();
            eventIds = eventDao.getAllEventIds();
        });
    }


    public void addEventAtStart(String eventName)
    {
        eventNames.add(0,eventName);
        executorService.execute(() ->
        {
            long newOrderIndex;
            if (eventNames.size() == 1) // was empty
                newOrderIndex = 10;
            else
                newOrderIndex = eventDao.getMinOrderIndex() - 10;
            EventEntity event = new EventEntity(eventName,newOrderIndex);
            long eventId = eventDao.insertEvent(event);
            eventIds.add(0,eventId); //equal to add first
        });

    }

    public void addEventAtEnd(String eventName)
    {
        eventNames.add(eventName);
        executorService.execute(() ->
        {
            long newOrderIndex;
            if (eventNames.size() == 1) // was empty
                newOrderIndex = 10;
            else
                newOrderIndex = eventDao.getMaxOrderIndex() + 10;
            EventEntity event = new EventEntity(eventName,newOrderIndex);
            long eventId = eventDao.insertEvent(event);
            eventIds.add(eventId);
        });
    }



    public void moveEvent(int fromIndex,int toIndex)
    {
        executorService.execute(() ->
        {
            long eventId = eventIds.get(fromIndex);
            EventEntity eventToMove = eventDao.getEventById(eventId);
            eventNames.remove(fromIndex);
            eventIds.remove(fromIndex);
            eventNames.add(toIndex,eventToMove.eventName);
            eventIds.add(toIndex,eventId);
            if (toIndex == 0) //first
                eventToMove.orderIndex = eventDao.getMinOrderIndex() - 10;
            else if (toIndex == eventNames.size()-1) //last
                eventToMove.orderIndex = eventDao.getMaxOrderIndex() + 10;
            else
            {
                long eventIndexBeforeMe = eventIds.get(toIndex-1);
                long eventIndexAfterMe = eventIds.get(toIndex);
                EventEntity eventBeforeMe = eventDao.getEventById(eventIndexBeforeMe);
                EventEntity eventAfterMe = eventDao.getEventById(eventIndexAfterMe);
                long gap = eventAfterMe.orderIndex - eventBeforeMe.orderIndex;
                if (gap > 1)
                    eventToMove.orderIndex = eventBeforeMe.orderIndex + gap / 2;
                else
                {
                    reindexEvents();
                    eventBeforeMe = eventDao.getEventById(eventIndexBeforeMe);
                    eventAfterMe = eventDao.getEventById(eventIndexAfterMe);
                    gap = eventAfterMe.orderIndex - eventBeforeMe.orderIndex;
                    eventToMove.orderIndex = eventBeforeMe.orderIndex + gap / 2;
                }
            }
            eventDao.updateEvent(eventToMove);
        });
    }

    public void updateEventName(int index,String newName)
    {
        eventNames.set(index,newName);
        executorService.execute(() ->
        {
            long eventId = eventIds.get(index);
            eventDao.updateEventName(eventId,newName);
        });
    }

    public void deleteEvent(int index)
    {
        long eventId = eventIds.get(index);
        eventIds.remove(index);
        eventNames.remove(index);
        executorService.execute(() -> eventDao.deleteEventById(eventId));
    }

    private void reindexEvents()
    {
        List<EventEntity> events = eventDao.getAllEvents();
        long index = 10;
        for (EventEntity e : events)
        {
            e.orderIndex = index;
            index += 10;
        }
        eventDao.updateEvents(events);
    }

}
