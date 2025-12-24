package ran.tmpTest.utils;

import ran.tmpTest.R;
import ran.tmpTest.sharedData.AppData;

public class EventInGame
{
    public enum GamePart {HALF_1,HALF_2,EXTRA_TIME_1,EXTRA_TIME_2}
    public enum Team {HOME_TEAM,NON,AWAY_TEAM}
    public GamePart gamePart;
    public Team team;
    public String time;
    public int playerNum;
    public String eventName;
    public long gameId;

    public long eventId;

    public EventInGame(long gameId, GamePart gamePart, Team team, int min, int sec, int playerNum, String eventName)
    {
        this.gameId = gameId;
        this.gamePart = gamePart;
        this.team = team;
        this.playerNum = playerNum;
        this.eventName = eventName;
        if (min  > 9)
            time = min + ":";
        else
            time = "0" + min + ":";
        if (sec > 9)
            time += sec;
        else
            time += "0" + sec;
    }

    public EventInGame(long gameId,long eventId, GamePart gamePart, Team team, String time, int playerNum, String eventName)
    {
        this.gameId = gameId;
        this.eventId = eventId;
        this.gamePart = gamePart;
        this.team = team;
        this.time = time;
        this.playerNum = playerNum;
        this.eventName = eventName;
    }

    public String toString()
    {
        String res;
        if (gamePart == GamePart.EXTRA_TIME_2)
            res = "ET2 ";
        else if (gamePart == GamePart.HALF_2)
            res = "H2 ";
        else if (gamePart == GamePart.EXTRA_TIME_1)
            res = "ET1 ";
        else // (gamePart == GamePart.half1)
            res = "H1 ";
        res += time;

        if (team == null && playerNum == 0) //team and player are null
            return res + ":" + eventName;
        if (team != Team.NON)
        {
            if (this.team == Team.HOME_TEAM)
                res += " (" + AppData.mainActivity.getString(R.string.homeTeam);
            else //(team == Team.AWAY_TEAM)
                res += " (" + AppData.mainActivity.getString(R.string.awayTeam);
            if (playerNum == 0) //team is not null and player is null
                return res + ") : " + eventName;
            else
                return res + " P" + playerNum + ") : " + eventName;
        }
        else if (playerNum != 0) //team is null and player is not 0
            return res + " (P" + playerNum + ")" + " : " + eventName;
        //team and player are not null
        return res + " : " + eventName;
    }
}
