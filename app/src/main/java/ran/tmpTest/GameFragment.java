package ran.tmpTest;

import android.content.Context;
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

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import ran.tmpTest.alertDialogs.EventAlertDialog;
import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.Event;
import ran.tmpTest.utils.Game;


public class GameFragment extends Fragment
{
    public static int gameChosen;
    private View view;
    private TextView clockTextView, msgToUserTextView;
    private ImageButton playBtn,stopBtn;
    private RadioGroup choseGamePartRadioGroup, choseTeamRadioGroup;
    private NumberPicker playerDigit1NumberPicker, playerDigit2NumberPicker;
    private Button specialEventBtn;
    private ConstraintLayout eventsScrollView;
    private Spinner choseGameDropDownList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_game, container, false);
        AppData.gameFragment = this;
        choseGameDropDownList = view.findViewById(R.id.choseGameDropDownList);
        eventsScrollView = view.findViewById(R.id.scrollViewLayout);
        msgToUserTextView = view.findViewById(R.id.msgToUser);
        msgToUserTextView.setVisibility(View.INVISIBLE);
        playerDigit1NumberPicker = view.findViewById(R.id.playerDigit1);
        playerDigit2NumberPicker = view.findViewById(R.id.playerDigit2);
        clockTextView = view.findViewById(R.id.clock);
        playBtn = view.findViewById(R.id.playBtn);
        stopBtn = view.findViewById(R.id.stopBtn);
        choseGamePartRadioGroup = view.findViewById(R.id.gamePart);
        choseTeamRadioGroup = view.findViewById(R.id.selectTeam);
        specialEventBtn = view.findViewById(R.id.specialEvent);
        playBtn.setOnClickListener((View)->playBtn());
        stopBtn.setOnClickListener((View)-> stopBtn());
        specialEventBtn.setOnClickListener((View)-> specialEventBtn());
        choseGamePartRadioGroup.setOnCheckedChangeListener((group, checkedId) -> setGamePartValue(checkedId));
        choseTeamRadioGroup.setOnCheckedChangeListener((group, checkedId) -> SetTeamValue(checkedId));
        setPlayerNumPickers();
        createGamesDropDownList();
        setLayoutSize(eventsScrollView,35);
        createEventButtons(view,AppData.events);
        playerDigit1NumberPicker.setValue(AppData.playerChosenDigit1);
        playerDigit2NumberPicker.setValue(AppData.playerChosenDigit2);
        setGamePart();
        setTeamChosen();
        if (AppData.games.isEmpty() == false && AppData.events.isEmpty() == false)
            msgToUserTextView.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        AppData.playerChosenDigit1 = playerDigit1NumberPicker.getValue();
        AppData.playerChosenDigit2 = playerDigit2NumberPicker.getValue();
    }

    public void onResume()
    {
        super.onResume();
        if(GameFragment.gameChosen != -1)
        {
            choseGameDropDownList.setSelection(GameFragment.gameChosen);
        }
        if (AppData.clockRun)
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
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                GameFragment.gameChosen = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                GameFragment.gameChosen = -1;
            }
        };
    }
    public void specialEventBtn()
    {
        if(AppData.games.isEmpty())
        {
            Toast.makeText(getActivity(), R.string.addGameInTheSettings, Toast.LENGTH_SHORT).show();
            return;
        }
        AppData.playerChosenDigit1 = playerDigit1NumberPicker.getValue();
        AppData.playerChosenDigit2 = playerDigit2NumberPicker.getValue();
        EventAlertDialog eventAlertDialog = new EventAlertDialog();
        eventAlertDialog.show(AppData.mainActivity.getSupportFragmentManager(),"");
    }


    public void showMsgToUser(String text)
    {
        msgToUserTextView.setText(text);
        msgToUserTextView.setVisibility(View.VISIBLE);
    }

    public void SetTeamValue(int checkedId)
    {
        switch(checkedId)
        {
            case R.id.noTeam :
                AppData.teamChosen = Event.Team.NON;
                break;
            case R.id.home_team:
                AppData.teamChosen = Event.Team.HOME_TEAM;
                break;
            case R.id.away_team:
                AppData.teamChosen = Event.Team.AWAY_TEAM;
        }
    }

    public void setGamePart()
    {
        switch(AppData.gamePartChosen)
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
        switch (AppData.teamChosen)
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

    public void setGamePartValue(int checkedId)
    {
        switch(checkedId)
        {
            case R.id.half1:
                AppData.gamePartChosen = Event.GamePart.HALF_1;
                break;
            case R.id.half2:
                AppData.gamePartChosen = Event.GamePart.HALF_2;
                break;
            case R.id.et1 :
                AppData.gamePartChosen = Event.GamePart.EXTRA_TIME_1;
                break;
            case R.id.et2 :
                AppData.gamePartChosen = Event.GamePart.EXTRA_TIME_2;
                break;
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
        ArrayAdapter<String>adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,AppData.gamesStringList);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        choseGameDropDownList.setAdapter(adapter);
        choseGameDropDownList.setOnItemSelectedListener(onSelectGameDropDownList());
    }


    public static int dpToPx(int dp, Context context)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static void setLayoutSize(ConstraintLayout layout,int percentOfTheScreen)
    {
        final int screenHeight = AppData.mainActivity.getResources().getConfiguration().screenHeightDp;
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = dpToPx((int) (screenHeight * percentOfTheScreen * 0.01),AppData.mainActivity);
        layout.setLayoutParams(params);
    }

    public void createEventButtons(View view , List<String> list)
    {
        if (AppData.gamesStringList.size() == 0)
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
        @Override
        public void onClick(View v) //buttonListClick
        {
            Event event = makeEvent(eventNum);
            Game crntGame = AppData.games.get(GameFragment.gameChosen);
            crntGame.events.add(event);
            showEventAddedSnackBar(crntGame);
        }
    }

    public void showEventAddedSnackBar(Game game)
    {
        Snackbar snackBar = Snackbar.make(AppData.mainActivity.getView(),R.string.theEventWasRecorded, Snackbar.LENGTH_SHORT);
        snackBar.setAction(R.string.cancel, new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                game.removeLestEvent();
                AppData.mainActivity.showSnackBar(getString(R.string.theEventIsDeleted),700);
            }
        }).setDuration(1000).show();
    }


    public Event makeEvent(int eventNum)
    {
        int playerNum = getPlayerNumber();
        Event.GamePart gamePart = AppData.gamePartChosen;
        Event.Team team = AppData.teamChosen;
        String eventName = AppData.events.get(eventNum);
        if (AppData.clockRun)
            return new Event(gamePart,team,AppData.min,AppData.sec,playerNum,eventName);
        else
            return new Event(gamePart,team,0,0,playerNum,eventName);
    }

    public void playBtn()
    {
        AppData.mainActivity.startClock();
        playBtn.setVisibility(View.INVISIBLE);
    }

    public void stopBtn()
    {
        AppData.mainActivity.stopClock();
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
        if (AppData.min  > 9)
            minText = String.valueOf(AppData.min);
        else
            minText = "0" + AppData.min;
        if (AppData.sec > 9)
            secText = String.valueOf(AppData.sec);
        else
            secText = "0" + AppData.sec;
        return minText + ":" + secText;
    }
}