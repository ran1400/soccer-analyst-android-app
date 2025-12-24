package ran.tmpTest.db.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

import ran.tmpTest.db.entities.EventEntity;

@Dao
public interface EventDao
{

    @Insert
    long insertEvent(EventEntity event);

    @Query("SELECT * FROM event ORDER BY order_index ASC")
    List<EventEntity> getAllEvents();

    @Query("SELECT * FROM event WHERE eventId = :id LIMIT 1")
    EventEntity getEventById(long id);

    @Query("SELECT eventId FROM event ORDER BY order_index ASC")
    List<Long> getAllEventIds();

    @Query("SELECT * FROM event WHERE order_index = :orderIndex LIMIT 1")
    EventEntity getEventByOrderIndex(long orderIndex);

    @Query("SELECT event_name FROM event ORDER BY order_index ASC")
    List<String> getAllEventNames();

    @Update
    void updateEvent(EventEntity event);

    @Update
    void updateEvents(List<EventEntity> events);

    @Delete
    void deleteEvent(EventEntity event);

    @Query("DELETE FROM event WHERE eventId = :eventId")
    void deleteEventById(long eventId);

    @Query("UPDATE event SET event_name = :newName WHERE eventId = :eventId")
    void updateEventName(long eventId, String newName);

    @Query("SELECT MAX(order_index) FROM event")
    Long getMaxOrderIndex();

    @Query("SELECT MIN(order_index) FROM event")
    Long getMinOrderIndex();

}
