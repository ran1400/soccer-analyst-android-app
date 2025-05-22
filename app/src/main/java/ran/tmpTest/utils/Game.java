package ran.tmpTest.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public String makeFileName()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss", Locale.getDefault());
        String time = sdf.format(new Date());
        String fixedGameName = gameName.replaceAll("[\\\\/:*?\"<>|]", "-");
        return fixedGameName + "  " + time;
    }
}
