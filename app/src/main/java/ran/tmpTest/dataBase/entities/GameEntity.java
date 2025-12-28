package ran.tmpTest.dataBase.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game")
public class GameEntity
{
    @PrimaryKey(autoGenerate = true)
    public long gameId;

    @ColumnInfo(name = "game_name")
    public String gameName;

    @ColumnInfo(name = "order_index")
    public long orderIndex;

    public GameEntity(String gameName,long orderIndex)
    {
        this.orderIndex = orderIndex;
        this.gameName = gameName;
    }
}
