package ran.tmpTest.db.repositories;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ran.tmpTest.db.AppDatabase;
import ran.tmpTest.db.daos.GameDao;
import ran.tmpTest.db.entities.EventInGameEntity;
import ran.tmpTest.db.entities.GameEntity;
import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.EventInGame;

public class GameRepository
{

    public final GameDao gameDao;
    public List<String> gameNames;
    public List<Long> gameIds;

    public GameRepository(AppDatabase db)
    {
        gameDao = db.gameDao();
        Executors.newSingleThreadExecutor().execute(() ->
        {
            gameNames = gameDao.getAllGameNames();
            gameIds = gameDao.getAllGameIds();
        });
    }

    public void addGameAtEnd(String gameName)
    {
        gameNames.add(gameName);
        Executors.newSingleThreadExecutor().execute(() ->
        {
            long newOrderIndex;
            if (gameNames.size() == 1) // was empty
                newOrderIndex = 10;
            else
                newOrderIndex = gameDao.getMaxOrderIndex() + 10;
            GameEntity game = new GameEntity(gameName, newOrderIndex);
            long gameId = gameDao.insertGame(game);
            gameIds.add(gameId);
        });
    }


    public void addGameAtStart(String gameName)
    {
        gameNames.add(0,gameName);
        Executors.newSingleThreadExecutor().execute(() ->
        {
            long newOrderIndex;
            if (gameNames.size() == 1) // was empty
                newOrderIndex = 10;
            else
                newOrderIndex = gameDao.getMinOrderIndex() - 10;
            GameEntity game = new GameEntity(gameName, newOrderIndex);
            long gameId = gameDao.insertGame(game);
            gameIds.add(0,gameId);
        });
    }

    private void reindexGames()
    {
        List<GameEntity> games = gameDao.getAllGames();
        long index = 10;
        for (GameEntity gameEntity : games)
        {
            gameEntity.orderIndex = index;
            index += 10;
        }
        gameDao.updateGames(games);
    }

    public void moveGame(int fromIndex,int toIndex)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            long gameId = gameIds.get(fromIndex);
            GameEntity game = gameDao.getGameById(gameId);
            gameNames.remove(fromIndex);
            gameIds.remove(fromIndex);
            gameNames.add(toIndex, game.gameName);
            gameIds.add(toIndex,gameId);
            if (toIndex == 0)
                game.orderIndex = gameDao.getMinOrderIndex() - 10;
            else if (toIndex == gameNames.size()-1)
                game.orderIndex = gameDao.getMaxOrderIndex() + 10;
            else
            {
                long gameIndexBeforeMe = gameIds.get(toIndex-1);
                long gameIndexAfterMe = gameIds.get(toIndex);
                GameEntity before = gameDao.getGameById(gameIndexBeforeMe);
                GameEntity after = gameDao.getGameById(gameIndexAfterMe);
                long gap = after.orderIndex - before.orderIndex;
                if (gap > 1)
                    game.orderIndex = before.orderIndex + gap / 2;
                else
                {
                    reindexGames();
                    before = gameDao.getGameById(gameIndexBeforeMe);
                    after = gameDao.getGameById(gameIndexAfterMe);
                    gap = after.orderIndex - before.orderIndex;
                    game.orderIndex = before.orderIndex + gap / 2;
                }
            }
            gameDao.updateGame(game);
        });
    }


    public void deleteGame(int index)
    {
        long gameId = gameIds.get(index);
        gameIds.remove(index);
        gameNames.remove(index);
        Executors.newSingleThreadExecutor().execute(() -> gameDao.deleteGameById(gameId));
    }

    public void updateGameName(int index,String gameName)
    {
        gameNames.set(index,gameName);
        long gameId = gameIds.get(index);
        Executors.newSingleThreadExecutor().execute(() -> gameDao.updateGameName(gameId,gameName));
    }

}
