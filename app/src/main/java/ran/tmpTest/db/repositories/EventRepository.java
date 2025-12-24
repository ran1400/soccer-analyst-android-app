package ran.tmpTest.db.repositories;

import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ran.tmpTest.db.AppDatabase;
import ran.tmpTest.db.daos.EventDao;
import ran.tmpTest.db.entities.EventEntity;
import ran.tmpTest.sharedData.AppData;

public class EventRepository
{
    public final EventDao eventDao;
    public List<String> eventNames;
    public List<Long> eventIds;

    public EventRepository(AppDatabase db)
    {
        eventDao = db.eventDao();
        Executors.newSingleThreadExecutor().execute(() ->
        {
            eventNames = eventDao.getAllEventNames();
            eventIds = eventDao.getAllEventIds();
        });
    }


    public void addEventAtStart(String eventName)
    {
        eventNames.add(0,eventName);
        Executors.newSingleThreadExecutor().execute(() ->
        {
            long newOrderIndex;
            if (eventNames.size() == 1) // was empty
                newOrderIndex = 10;
            else
                newOrderIndex = eventDao.getMinOrderIndex() - 10;
            EventEntity event = new EventEntity(eventName,newOrderIndex);
            long eventId = eventDao.insertEvent(event);
            eventIds.add(0,eventId);
        });

    }

    public void addEventAtEnd(String eventName)
    {
        eventNames.add(eventName);
        Executors.newSingleThreadExecutor().execute(() ->
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
        Executors.newSingleThreadExecutor().execute(() ->
        {
            long eventId = eventIds.get(fromIndex);
            EventEntity event = eventDao.getEventById(eventId);
            eventNames.remove(fromIndex);
            eventIds.remove(fromIndex);
            eventNames.add(toIndex,event.eventName);
            eventIds.add(toIndex,eventId);
            if (toIndex == 0)
                event.orderIndex = eventDao.getMinOrderIndex() - 10;
            else if (toIndex == eventNames.size()-1)
                event.orderIndex = eventDao.getMaxOrderIndex() + 10;
            else
            {
                long eventIndexBeforeMe = eventIds.get(toIndex-1);
                long eventIndexAfterMe = eventIds.get(toIndex);
                EventEntity before = eventDao.getEventById(eventIndexBeforeMe);
                EventEntity after = eventDao.getEventById(eventIndexAfterMe);
                long gap = after.orderIndex - before.orderIndex;
                if (gap > 1)
                    event.orderIndex = before.orderIndex + gap / 2;
                else
                {
                    reindexEvents();
                    before = eventDao.getEventById(eventIndexBeforeMe);
                    after = eventDao.getEventById(eventIndexAfterMe);
                    gap = after.orderIndex - before.orderIndex;
                    event.orderIndex = before.orderIndex + gap / 2;
                }
            }
            eventDao.updateEvent(event);
        });

    }

    public void updateEventName(int index,String newName)
    {
        eventNames.set(index,newName);
        Executors.newSingleThreadExecutor().execute(() ->
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
        Executors.newSingleThreadExecutor().execute(() -> eventDao.deleteEventById(eventId));
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
