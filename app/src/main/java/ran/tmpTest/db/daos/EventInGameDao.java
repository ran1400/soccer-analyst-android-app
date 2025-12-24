package ran.tmpTest.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ran.tmpTest.db.entities.EventInGameEntity;

@Dao
public interface EventInGameDao
{

    @Insert
    long insertEvent(EventInGameEntity event);

    @Query("SELECT * FROM EventInGame WHERE game_id = :gameId ORDER BY eventId ASC")
    List<EventInGameEntity> getEventsFromGame(Long gameId);

    @Query("DELETE FROM EventInGame WHERE eventId = :eventId")
    void deleteEventById(Long eventId);

    @Update
    void updateEvent(EventInGameEntity event);

    @Query("SELECT * FROM EventInGame WHERE eventId = :eventId LIMIT 1")
    EventInGameEntity getEventInGame(Long eventId);

}
