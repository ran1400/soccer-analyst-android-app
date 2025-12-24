package ran.tmpTest.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "eventInGame",
        foreignKeys = @ForeignKey(
                entity = GameEntity.class,
                parentColumns = "gameId",
                childColumns = "game_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("game_id")})

public class EventInGameEntity
{
        @PrimaryKey(autoGenerate = true)
        public long eventId;

        @ColumnInfo(name = "game_id")
        public long gameId;

        @ColumnInfo(name = "game_part")
        public String gamePart;

        public String team;
        public String time;

        @ColumnInfo(name = "player_num")
        public int playerNum;

        @ColumnInfo(name = "event_name")
        public String eventName;

        public EventInGameEntity(long gameId, String gamePart, String team, String time, int playerNum, String eventName)
        {
                this.gameId = gameId;
                this.gamePart = gamePart;
                this.team = team;
                this.time = time;
                this.playerNum = playerNum;
                this.eventName = eventName;
        }

}
