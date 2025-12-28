package ran.tmpTest.dataBase.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ran.tmpTest.dataBase.entities.GameEntity;

@Dao
public interface GameDao
{

    @Insert
    long insertGame(GameEntity game);

    @Delete
    void deleteGame(GameEntity game);

    @Query("DELETE FROM game WHERE gameId = :gameId")
    void deleteGameById(long gameId);

    @Query("SELECT game_name FROM game ORDER BY order_index ASC")
    List<String> getAllGameNames();

    @Query("SELECT gameId FROM game ORDER BY order_index ASC")
    List<Long> getAllGameIds();

    @Query("SELECT * FROM game ORDER BY order_index ASC")
    List<GameEntity> getAllGames();

    @Query("SELECT MAX(order_index) FROM game")
    Long getMaxOrderIndex();

    @Query("SELECT MIN(order_index) FROM game")
    Long getMinOrderIndex();

    @Update
    void updateGame(GameEntity game);

    @Query("UPDATE game SET game_name = :newName WHERE gameId = :gameId")
    void updateGameName(long gameId, String newName);

    @Update
    void updateGames(List<GameEntity> games);

    @Query("SELECT * FROM game WHERE gameId = :id LIMIT 1")
    GameEntity getGameById(long id);
}
