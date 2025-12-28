package ran.tmpTest.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.snackbar.Snackbar;

import java.util.List;


import ran.tmpTest.MyApp;
import ran.tmpTest.R;
import ran.tmpTest.alertDialogs.EventAlertDialogFragment;

import ran.tmpTest.sharedData.GameFragmentData;
import ran.tmpTest.utils.ClockHandler;
import ran.tmpTest.utils.dataStructures.EventInGame;


public class GameFragment extends Fragment
{
    private TextView clockTextView, msgToUserTextView;
    private ImageButton playBtn;
    private RadioGroup choseGamePartRadioGroup, choseTeamRadioGroup;
    private NumberPicker playerDigit1NumberPicker, playerDigit2NumberPicker;
    private Spinner choseGameDropDownList;
    private ClockHandler clockHandler;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View gameFragmentView = inflater.inflate(R.layout.fragment_game, container, false);
        choseGameDropDownList = gameFragmentView.findViewById(R.id.choseGameDropDownList);
        msgToUserTextView = gameFragmentView.findViewById(R.id.msgToUser);
        msgToUserTextView.setVisibility(View.INVISIBLE);
        playerDigit1NumberPicker = gameFragmentView.findViewById(R.id.playerDigit1);
        playerDigit2NumberPicker = gameFragmentView.findViewById(R.id.playerDigit2);
        clockTextView = gameFragmentView.findViewById(R.id.clockText);
        playBtn = gameFragmentView.findViewById(R.id.playBtn);
        ImageButton stopBtn = gameFragmentView.findViewById(R.id.stopBtn);
        choseGamePartRadioGroup = gameFragmentView.findViewById(R.id.gamePart);
        choseTeamRadioGroup = gameFragmentView.findViewById(R.id.selectTeam);
        Button specialEventBtn = gameFragmentView.findViewById(R.id.specialEventBtn);
        playBtn.setOnClickListener((View)->playBtn());
        stopBtn.setOnClickListener((View)-> stopBtn());
        specialEventBtn.setOnClickListener((View)-> specialEventBtn());
        choseGamePartRadioGroup.setOnCheckedChangeListener((group, checkedId) -> setGamePartValue(checkedId));
        choseTeamRadioGroup.setOnCheckedChangeListener((group, checkedId) -> SetTeamValue(checkedId));
        setPlayerNumPickers();
        createGamesDropDownList();
        createEventButtons(gameFragmentView,MyApp.dbRepository.eventRepository.eventNames);
        playerDigit1NumberPicker.setValue(GameFragmentData.playerChosenDigit1);
        playerDigit2NumberPicker.setValue(GameFragmentData.playerChosenDigit2);
        setGamePart();
        setTeamChosen();
        if (!MyApp.dbRepository.gameRepository.gameNames.isEmpty() &&
            !MyApp.dbRepository.eventRepository.eventNames.isEmpty())
        {
            msgToUserTextView.setVisibility(View.INVISIBLE);
        }
        clockHandler = new ClockHandler(this);
        return gameFragmentView;
    }

    public void onPause()
    {
        super.onPause();
        GameFragmentData.playerChosenDigit1 = playerDigit1NumberPicker.getValue();
        GameFragmentData.playerChosenDigit2 = playerDigit2NumberPicker.getValue();
        clockHandler.stopTicking();
    }

    public void onResume()
    {
        super.onResume();
        if(GameFragmentData.gameChosen != -1)
            choseGameDropDownList.setSelection(GameFragmentData.gameChosen);
        clockHandler.clockCheck();
        if (GameFragmentData.clockRun)
        {
            playBtn.setVisibility(View.INVISIBLE);
            updateClockText();
        }
        else
            clockTextView.setText("00:00");
    }

    private AdapterView.OnItemSelectedListener onSelectGameDropDownList()
    {
        return new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                GameFragmentData.gameChosen = position;
            }
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                GameFragmentData.gameChosen = -1;
            }
        };
    }
    public void specialEventBtn()
    {
        if(MyApp.dbRepository.gameRepository.gameNames.isEmpty())
        {
            Toast.makeText(getActivity(), R.string.addGameInTheSettings, Toast.LENGTH_SHORT).show();
            return;
        }
        GameFragmentData.playerChosenDigit1 = playerDigit1NumberPicker.getValue();
        GameFragmentData.playerChosenDigit2 = playerDigit2NumberPicker.getValue();
        long gameId = MyApp.dbRepository.gameRepository.gameIds.get(GameFragmentData.gameChosen);
        EventAlertDialogFragment eventAlertDialogFragment = new EventAlertDialogFragment(this,gameId);
        eventAlertDialogFragment.show(getParentFragmentManager(),"");
    }


    public void showMsgToUser(String text)
    {
        msgToUserTextView.setText(text);
        msgToUserTextView.setVisibility(View.VISIBLE);
    }

    public void SetTeamValue(int checkedId)
    {
        if (checkedId == R.id.away_team)
            GameFragmentData.teamChosen = EventInGame.Team.AWAY_TEAM;
        else if (checkedId == R.id.home_team)
            GameFragmentData.teamChosen = EventInGame.Team.HOME_TEAM;
        else // (checkedId == R.id.noTeam)
            GameFragmentData.teamChosen = EventInGame.Team.NON;

    }

    public void setGamePartValue(int checkedId) //when user click of radio btn
    {
        if (checkedId == R.id.half1)
            GameFragmentData.gamePartChosen = EventInGame.GamePart.HALF_1;
        else if (checkedId == R.id.half2)
            GameFragmentData.gamePartChosen = EventInGame.GamePart.HALF_2;
        else if (checkedId == R.id.et1)
            GameFragmentData.gamePartChosen = EventInGame.GamePart.EXTRA_TIME_1;
        else // (checkedId == R.id.et2)
            GameFragmentData.gamePartChosen = EventInGame.GamePart.EXTRA_TIME_2;
    }

    public void setGamePart()
    {
        switch(GameFragmentData.gamePartChosen)
        {
            case HALF_1:
                choseGamePartRadioGroup.check(R.id.half1);
                break;
            case HALF_2:
                choseGamePartRadioGroup.check(R.id.half2);
                break;
            case EXTRA_TIME_1:
                 choseGamePartRadioGroup.check(R.id.et1);
                 break;
            case EXTRA_TIME_2:
                 choseGamePartRadioGroup.check(R.id.et2);
        }
    }

    public void setTeamChosen()
    {
        switch (GameFragmentData.teamChosen)
        {
            case NON :
                choseTeamRadioGroup.check(R.id.noTeam);
                break;
            case HOME_TEAM:
                choseTeamRadioGroup.check(R.id.home_team);
                break;
            case AWAY_TEAM:
                choseTeamRadioGroup.check(R.id.away_team);
        }
    }

    public int getPlayerNumber()
    {
        return playerDigit1NumberPicker.getValue() * 10 + playerDigit2NumberPicker.getValue();
    }

    public void setPlayerNumPickers()
    {
        playerDigit1NumberPicker.setMinValue(0);
        playerDigit2NumberPicker.setMinValue(0);
        playerDigit1NumberPicker.setMaxValue(9);
        playerDigit2NumberPicker.setMaxValue(9);
    }

    public void createGamesDropDownList()
    {
        ArrayAdapter<String>adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item,MyApp.dbRepository.gameRepository.gameNames);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        choseGameDropDownList.setAdapter(adapter);
        choseGameDropDownList.setOnItemSelectedListener(onSelectGameDropDownList());
    }


    public void createEventButtons(View view , List<String> list)
    {
        if (MyApp.dbRepository.gameRepository.gameNames.isEmpty())
        {
            showMsgToUser(getString(R.string.addGameInTheSettings));
            return;
        }
        else if (list.isEmpty())
        {
            showMsgToUser(getString(R.string.addEventsInTheSettings));
            return;
        }

        LinearLayout linearLayout = view.findViewById(R.id.buttonList);
        linearLayout.removeAllViewsInLayout();
        final Button[] buttons = new Button[list.size()];
        for (int i = 0; i < buttons.length ; i++)
        {
            int buttonStyle = R.style.buttonStyle;
            buttons[i]  = new Button(new ContextThemeWrapper(getActivity(), buttonStyle), null, buttonStyle);
            buttons[i].setMinHeight(170);
            buttons[i].setOnClickListener(new eventListener(i));
            buttons[i].setText(list.get(i));
            linearLayout.addView(buttons[i]);
        }
    }

    private class eventListener implements View.OnClickListener //events buttons
    {
        int eventNum;
        public eventListener(int eventNum)
        {
            this.eventNum = eventNum ;
        }
        public void onClick(View v) //buttonListClick
        {
            long gameId = MyApp.dbRepository.gameRepository.gameIds.get(GameFragmentData.gameChosen);
            EventInGame eventInGame = makeEvent(eventNum,gameId);
            MyApp.dbRepository.eventInGameRepository.addEventToGame(eventInGame);
            showEventAddedSnackBar(eventInGame);
        }
    }

    public void showEventAddedSnackBar(EventInGame eventInGame)
    {
        View myView = getView();
        if (myView == null) // if the fragment is not on the screen
            return;
        Snackbar snackBar = Snackbar.make(getView(),R.string.theEventWasRecorded, Snackbar.LENGTH_SHORT);
        snackBar.setAction(R.string.cancel, view ->
        {
            MyApp.dbRepository.eventInGameRepository.deleteEventInGame(eventInGame);
            View myView1 = getView();
            if (myView1 == null) // if the fragment is not on the screen
                return;
            Snackbar.make(myView1,R.string.theEventIsDeleted,700).show();
        }).setDuration(1000).show();
    }


    public EventInGame makeEvent(int eventNum,long gameId)
    {
        int playerNum = getPlayerNumber();
        EventInGame.GamePart gamePart = GameFragmentData.gamePartChosen;
        EventInGame.Team team = GameFragmentData.teamChosen;
        String eventName = MyApp.dbRepository.eventRepository.eventNames.get(eventNum);
        if (GameFragmentData.clockRun)
            return new EventInGame(gameId,gamePart,team,GameFragmentData.min,GameFragmentData.sec,playerNum,eventName);
        else
            return new EventInGame(gameId,gamePart,team,0,0,playerNum,eventName);
    }

    public void playBtn()
    {
        clockHandler.startClock();
        playBtn.setVisibility(View.INVISIBLE);
    }

    public void stopBtn()
    {
        clockHandler.stopClock();
    }

    public void resetClock()
    {
        playBtn.setVisibility(View.VISIBLE);
        clockTextView.setText("00:00");
    }


    public void updateClockText()
    {
        clockTextView.setText(makeClockText());
    }

    public static String makeClockText()
    {
        String minText,secText;
        if (GameFragmentData.min  > 9)
            minText = String.valueOf(GameFragmentData.min);
        else
            minText = "0" + GameFragmentData.min;
        if (GameFragmentData.sec > 9)
            secText = String.valueOf(GameFragmentData.sec);
        else
            secText = "0" + GameFragmentData.sec;
        return minText + ":" + secText;
    }
}