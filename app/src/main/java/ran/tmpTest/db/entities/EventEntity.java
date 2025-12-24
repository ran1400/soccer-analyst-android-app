package ran.tmpTest.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "event")
public class EventEntity
{

    @PrimaryKey(autoGenerate = true)
    public long eventId;
    @ColumnInfo(name = "order_index")
    public long orderIndex;
    @ColumnInfo(name = "event_name")
    public String eventName;

    public EventEntity(String eventName,long orderIndex)
    {
        this.orderIndex = orderIndex;
        this.eventName = eventName;
    }

}


