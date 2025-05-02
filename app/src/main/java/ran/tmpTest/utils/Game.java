package ran.tmpTest.utils;

import java.util.ArrayList;
import java.util.List;

public class Game
{
    public  String gameName;
    public ArrayList<Event> events;
    public Game(String gameName)
    {
        this.gameName = gameName;
        events = new ArrayList<>();
    }

    public void removeLestEvent()
    {
        int lestEventIndex = events.size() -1;
        events.remove(lestEventIndex);
    }
}
