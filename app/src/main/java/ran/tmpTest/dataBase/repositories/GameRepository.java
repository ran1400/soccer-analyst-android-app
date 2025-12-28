package ran.tmpTest.dataBase.repositories;

import java.util.List;
import java.util.concurrent.ExecutorService;

import ran.tmpTest.dataBase.AppDatabase;
import ran.tmpTest.dataBase.daos.GameDao;
import ran.tmpTest.dataBase.entities.GameEntity;

public class GameRepository
{

    private final GameDao gameDao;
    public List<String> gameNames;
    public List<Long> gameIds;
    private final ExecutorService executorService;

    public GameRepository(AppDatabase db, ExecutorService executorService)
    {
        gameDao = db.gameDao();
        this.executorService = executorService;
        executorService.execute(() ->
        {
            gameNames = gameDao.getAllGameNames();
            gameIds = gameDao.getAllGameIds();
        });
    }

    public void addGameAtEnd(String gameName)
    {
        gameNames.add(gameName);
        executorService.execute(() ->
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
        executorService.execute(() ->
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
        executorService.execute(() ->
        {
            long gameId = gameIds.get(fromIndex);
            GameEntity gameToMove = gameDao.getGameById(gameId);
            gameNames.remove(fromIndex);
            gameIds.remove(fromIndex);
            gameNames.add(toIndex, gameToMove.gameName);
            gameIds.add(toIndex,gameId);
            if (toIndex == 0)
                gameToMove.orderIndex = gameDao.getMinOrderIndex() - 10;
            else if (toIndex == gameNames.size()-1)
                gameToMove.orderIndex = gameDao.getMaxOrderIndex() + 10;
            else
            {
                long gameIndexBeforeMe = gameIds.get(toIndex-1);
                long gameIndexAfterMe = gameIds.get(toIndex);
                GameEntity gameBeforeMe = gameDao.getGameById(gameIndexBeforeMe);
                GameEntity gameAfterMe = gameDao.getGameById(gameIndexAfterMe);
                long gap = gameAfterMe.orderIndex - gameBeforeMe.orderIndex;
                if (gap > 1)
                    gameToMove.orderIndex = gameBeforeMe.orderIndex + gap / 2;
                else
                {
                    reindexGames();
                    gameBeforeMe = gameDao.getGameById(gameIndexBeforeMe);
                    gameAfterMe = gameDao.getGameById(gameIndexAfterMe);
                    gap = gameAfterMe.orderIndex - gameBeforeMe.orderIndex;
                    gameToMove.orderIndex = gameBeforeMe.orderIndex + gap / 2;
                }
            }
            gameDao.updateGame(gameToMove);
        });
    }


    public void deleteGame(int index)
    {
        long gameId = gameIds.get(index);
        gameIds.remove(index);
        gameNames.remove(index);
        executorService.execute(() -> gameDao.deleteGameById(gameId));
    }

    public void updateGameName(int index,String gameName)
    {
        gameNames.set(index,gameName);
        long gameId = gameIds.get(index);
        executorService.execute(() -> gameDao.updateGameName(gameId,gameName));
    }

}
