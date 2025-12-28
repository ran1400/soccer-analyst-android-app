package ran.tmpTest.alertDialogs;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import ran.tmpTest.MyApp;
import ran.tmpTest.fragments.EventsFragment;
import ran.tmpTest.fragments.GameFragment;
import ran.tmpTest.R;
import ran.tmpTest.sharedData.GameFragmentData;
import ran.tmpTest.utils.dataStructures.EventInGame;


public class EventAlertDialogFragment extends AppCompatDialogFragment
{
    private EventsFragment eventsFragment = null;
    private GameFragment gameFragment = null;
    private final EventInGame eventToEdit; // if null -> user want create new event
    private NumberPicker playerDigit1NumberPicker, playerDigit2NumberPicker;
    private RadioGroup eventsRadioGroup;
    private RadioGroup choseTeamRadioGroup;
    private RadioButton customEventRadioButton;
    private EditText customEventEditText;
    private TextView clockTextView,gamePartTextView;
    private final int PERSONAL_EVENT = -1;
    private int getEventChosenHelper;
    private View view;
    private final long gameId;

    public EventAlertDialogFragment(GameFragment gameFragment,long gameId) //create new event
    {
        this.gameFragment = gameFragment;
        this.gameId = gameId;
        eventToEdit = null;
    }

    public EventAlertDialogFragment(EventsFragment eventsFragment,long gameId, EventInGame eventToEdit) // edit event
    {
        this.eventsFragment = eventsFragment;
        this.gameId = gameId;
        this.eventToEdit = eventToEdit;
    }


    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.alert_dialog_event_fragment, null);
        clockTextView = view.findViewById(R.id.clockText);
        Button saveBtn = view.findViewById(R.id.saveButton);
        Button cancelBtn = view.findViewById(R.id.cancelButton);
        gamePartTextView = view.findViewById(R.id.gamePartText);
        customEventRadioButton = view.findViewById(R.id.specialEventBtn);
        customEventEditText = view.findViewById(R.id.specialEventText);
        playerDigit1NumberPicker = view.findViewById(R.id.playerDigit1);
        playerDigit2NumberPicker = view.findViewById(R.id.playerDigit2);
        eventsRadioGroup = view.findViewById(R.id.eventsRadioGroup);
        choseTeamRadioGroup = view.findViewById(R.id.selectTeam);
        eventsRadioGroup.setOnCheckedChangeListener(this::eventsRadioGroupOnCheckedChanged);
        customEventRadioButton.setOnClickListener(this::showKeyboard);
        customEventEditText.setOnClickListener(this::customEventTextCLicked);
        cancelBtn.setOnClickListener(this::cancelBtn);
        saveBtn.setOnClickListener(this::saveBtn);
        setPlayerNumPickers0To9();
        getEventChosenHelper = addEvents(); //addEvents return the button Id of the first event in list
        if (eventToEdit == null) //create new event
            setDefaultClockAndPickers();
        else
        {
            setTeamChosen(eventToEdit.team);
            setClockTextView(eventToEdit.time);
            setGamePartChosen(eventToEdit.gamePart);
            setSpecialEventText(eventToEdit.eventName);
            setPlayerDigitNumbers(eventToEdit.playerNum/10,eventToEdit.playerNum%10);
        }
        builder.setView(view);
        return builder.create();
    }

    private void customEventTextCLicked(View view)
    {
        eventsRadioGroup.check(customEventRadioButton.getId());
        showKeyboard();
    }

    private void eventsRadioGroupOnCheckedChanged(RadioGroup group, int checkedId)
    {
        if ( checkedId != customEventRadioButton.getId() )
        {
            customEventEditText.setFocusable(false);
            hideKeyboard();
        }
        else
            showKeyboard();
    }

    private EventInGame.Team getTeamChosen()
    {
        int checkedId = choseTeamRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.noTeam)
            return EventInGame.Team.NON;
        else if (checkedId == R.id.home_team)
            return EventInGame.Team.HOME_TEAM;
        else // (checkedId == R.id.away_team)
            return EventInGame.Team.AWAY_TEAM;
    }

    private void cancelBtn(View view)
    {
        dismiss();
    }

    private void saveBtn(View view)
    {
        int eventChosen = getEventChosen();
        if (eventChosen == PERSONAL_EVENT && customEventEditText.getText().toString().isEmpty())
        {
            Toast.makeText(getActivity(),getString(R.string.enterEventName),Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventToEdit != null) // user want to edit event
        {
            eventToEdit.playerNum = getPlayerNumber();
            eventToEdit.team = getTeamChosen();
            if ( eventChosen == PERSONAL_EVENT)
                eventToEdit.eventName = customEventEditText.getText().toString();
            else
                eventToEdit.eventName = MyApp.dbRepository.eventRepository.eventNames.get(eventChosen);
            eventsFragment.showGameEvents();
            MyApp.dbRepository.eventInGameRepository.updateEventInGame(eventToEdit);
        }
        else //user want to make new event
        {
            EventInGame eventInGame = makeEvent(eventChosen);
            MyApp.dbRepository.eventInGameRepository.addEventToGame(eventInGame);
            gameFragment.showEventAddedSnackBar(eventInGame);
        }
        dismiss();
    }

    private EventInGame makeEvent(int eventChosen)
    {
        int playerNum = getPlayerNumber();
        EventInGame.GamePart gamePart = GameFragmentData.gamePartChosen;
        EventInGame.Team team = getTeamChosen();
        String eventName;
        if ( eventChosen == PERSONAL_EVENT)
            eventName = customEventEditText.getText().toString();
        else
            eventName = MyApp.dbRepository.eventRepository.eventNames.get(eventChosen);
        if (GameFragmentData.clockRun)
        {
            int min = GameFragmentData.min;
            int sec = GameFragmentData.sec;
            return new EventInGame(gameId,gamePart,team,min,sec,playerNum,eventName);
        }
        else
            return new EventInGame(gameId,gamePart,team,0,0,playerNum,eventName);
    }

    private int getEventChosen()
    {
        int checkedEvent = eventsRadioGroup.getCheckedRadioButtonId();
        if ( checkedEvent == R.id.specialEventBtn)
            return PERSONAL_EVENT;
        return checkedEvent - getEventChosenHelper;
    }

    private int getPlayerNumber()
    {
        return playerDigit1NumberPicker.getValue() * 10 + playerDigit2NumberPicker.getValue();
    }

    private void setClockTextView(String text)
    {
        clockTextView.setText(text);
    }

    private void setTeamChosen(EventInGame.Team teamChosen)
    {
        switch (teamChosen)
        {
            case NON ->
                    ((RadioButton) view.findViewById(R.id.noTeam)).setChecked(true);
            case AWAY_TEAM ->
                    ((RadioButton) view.findViewById(R.id.away_team)).setChecked(true);
            case HOME_TEAM ->
                    ((RadioButton) view.findViewById(R.id.home_team)).setChecked(true);
        }
    }

    private void setGamePartChosen(EventInGame.GamePart gamePartChosen)
    {
        switch( gamePartChosen )
        {
            case HALF_1:
                gamePartTextView.setText(getString(R.string.half1));
                break;
            case HALF_2:
                gamePartTextView.setText(getString(R.string.half2));
                break;
            case EXTRA_TIME_1:
                gamePartTextView.setText(getString(R.string.extraTime1));
                break;
            case EXTRA_TIME_2:
                gamePartTextView.setText(getString(R.string.extraTime2));
        }
    }

    private void setSpecialEventText(String text)
    {
        customEventEditText.setText(text);
    }

    private void setPlayerDigitNumbers(int digit1 , int digit2)
    {
        playerDigit1NumberPicker.setValue(digit1);
        playerDigit2NumberPicker.setValue(digit2);
    }

    private void setDefaultClockAndPickers()
    {
        if(GameFragmentData.clockRun)
        {
            String clockText = GameFragment.makeClockText();
            clockTextView.setText(clockText);
        }
        else
            clockTextView.setText("00:00");
        setGamePartChosen(GameFragmentData.gamePartChosen);
        setTeamChosen(GameFragmentData.teamChosen);
        setPlayerDigitNumbers(GameFragmentData.playerChosenDigit1,GameFragmentData.playerChosenDigit2);
    }

    private void showKeyboard(View view)
    {
        showKeyboard();
    }

    private void showKeyboard()
    {
        customEventEditText.setFocusableInTouchMode(true);
        customEventEditText.requestFocus();
        Activity activity = getActivity();
        if (activity == null)
            return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void hideKeyboard()
    {
        customEventEditText.setEnabled(false);
        customEventEditText.setEnabled(true);
    }

    private int addEvents()
    {
        if (MyApp.dbRepository.eventRepository.eventNames.isEmpty())
            return -1;
        RadioButton button = null;
        for(String eventName : MyApp.dbRepository.eventRepository.eventNames)
        {
            button = new RadioButton(getActivity());
            button.setText(eventName);
            eventsRadioGroup.addView(button);
        }
        return button.getId() - MyApp.dbRepository.eventRepository.eventNames.size() + 1;
    }

    private void setPlayerNumPickers0To9()
    {
        playerDigit1NumberPicker.setMinValue(0);
        playerDigit2NumberPicker.setMinValue(0);
        playerDigit1NumberPicker.setMaxValue(9);
        playerDigit2NumberPicker.setMaxValue(9);
    }

}