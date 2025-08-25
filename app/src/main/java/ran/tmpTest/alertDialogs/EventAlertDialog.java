package ran.tmpTest.alertDialogs;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import ran.tmpTest.GameFragment;
import ran.tmpTest.R;
import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.EventInGame;


public class EventAlertDialog extends AppCompatDialogFragment
{
    private EventInGame eventToEdit; // if null -> user want create new event
    private NumberPicker playerDigit1NumberPicker, playerDigit2NumberPicker;
    private ConstraintLayout eventsScrollView;
    private RadioGroup eventsRadioGroup;
    private RadioButton specialEventRadioButton;
    private EditText specialEventEditText;
    private TextView clockTextView,gamePartTextView;
    private EventInGame.Team teamChosen;
    private final int PERSONAL_EVENT = -1;
    private int getEventChosenHelper;
    private View view;
    private Button saveBtn,cancelBtn;

    long gameId;

    public EventAlertDialog(long gameId) //create new event
    {
        this.gameId = gameId;
        eventToEdit = null;
    }

    public EventAlertDialog(long gameId,EventInGame eventToEdit) // edit event
    {
        this.gameId = gameId;
        this.eventToEdit = eventToEdit;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.alert_dialog_event, null);
        clockTextView = view.findViewById(R.id.clockText);
        saveBtn = view.findViewById(R.id.saveButton);
        cancelBtn = view.findViewById(R.id.cancelButton);
        gamePartTextView = view.findViewById(R.id.gamePartText);
        specialEventRadioButton = view.findViewById(R.id.specialEvent);
        specialEventEditText = view.findViewById(R.id.specialEventText);
        playerDigit1NumberPicker = view.findViewById(R.id.playerDigit1);
        playerDigit2NumberPicker = view.findViewById(R.id.playerDigit2);
        eventsRadioGroup = view.findViewById(R.id.eventsRadioGroup);
        eventsScrollView = view.findViewById(R.id.scrollViewLayout);
        RadioGroup choseTeamRadioGroup = view.findViewById(R.id.selectTeam);
        eventsRadioGroup.setOnCheckedChangeListener(this::eventsRadioGroupOnCheckedChanged);
        specialEventRadioButton.setOnClickListener(this::showKeyboard);
        specialEventEditText.setOnClickListener(this::specialEventTextOnCLicked);
        choseTeamRadioGroup.setOnCheckedChangeListener(this::chooseTeamRadioBtnOnCheckedChanged);
        cancelBtn.setOnClickListener(this::cancelBtn);
        saveBtn.setOnClickListener(this::saveBtn);
        setPlayerNumPickers0To9();
        GameFragment.setLayoutSize(eventsScrollView,50);
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

    public void specialEventTextOnCLicked(View view)
    {
        eventsRadioGroup.check(specialEventRadioButton.getId());
        showKeyboard();
    }

    public void eventsRadioGroupOnCheckedChanged(RadioGroup group, int checkedId)
    {
        if ( checkedId != specialEventRadioButton.getId() )
        {
            specialEventEditText.setText("");
            specialEventEditText.setFocusable(false);
            hideKeyboard();
        }
        else
            showKeyboard();
    }

    public void chooseTeamRadioBtnOnCheckedChanged(RadioGroup group, int checkedId)
    {
        switch(checkedId)
        {
            case R.id.noTeam :
                teamChosen = EventInGame.Team.NON;
                break;
            case R.id.home_team:
                teamChosen = EventInGame.Team.HOME_TEAM;
                break;
            case R.id.away_team:
                teamChosen = EventInGame.Team.AWAY_TEAM;
        }
    }

    public void cancelBtn(View view)
    {
        dismiss();
    }

    public void saveBtn(View view)
    {
        int eventChosen = getEventChosen();
        if (eventChosen == PERSONAL_EVENT && specialEventEditText.getText().toString().isEmpty())
        {
            Toast.makeText(getActivity(),getString(R.string.enterEventName),Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventToEdit != null) // user want to edit event
        {
            eventToEdit.playerNum = getPlayerNumber();
            eventToEdit.team = teamChosen;
            if ( eventChosen == PERSONAL_EVENT)
                eventToEdit.eventName = specialEventEditText.getText().toString();
            else
                eventToEdit.eventName = AppData.dbRepository.eventRepository.eventNames.get(eventChosen);
            AppData.eventsFragment.showGameEvents();
            AppData.dbRepository.eventInGameRepository.updateEventInGame(eventToEdit);
        }
        else //user want to make new event
        {
            EventInGame eventInGame = makeEvent(eventChosen);
            AppData.dbRepository.eventInGameRepository.addEventToGame(eventInGame);
            AppData.gameFragment.showEventAddedSnackBar(eventInGame);
        }
        dismiss();
    }

    private EventInGame makeEvent(int eventChosen)
    {
        int playerNum = getPlayerNumber();
        EventInGame.GamePart gamePart = AppData.GameFragmentData.gamePartChosen;
        EventInGame.Team team = teamChosen;
        String eventName;
        if ( eventChosen == PERSONAL_EVENT)
            eventName = specialEventEditText.getText().toString();
        else
            eventName = AppData.dbRepository.eventRepository.eventNames.get(eventChosen);
        if (AppData.GameFragmentData.clockRun)
            return new EventInGame(gameId,gamePart,team,AppData.GameFragmentData.min,AppData.GameFragmentData.sec,playerNum,eventName);
        else
            return new EventInGame(gameId,gamePart,team,0,0,playerNum,eventName);
    }

    private int getEventChosen()
    {
        int checkedEvent = eventsRadioGroup.getCheckedRadioButtonId();
        if ( checkedEvent == R.id.specialEvent)
            return PERSONAL_EVENT;
        return checkedEvent - getEventChosenHelper;
    }



    public int getPlayerNumber()
    {
        return playerDigit1NumberPicker.getValue() * 10 + playerDigit2NumberPicker.getValue();
    }

    public void setClockTextView(String text)
    {
        clockTextView.setText(text);
    }

    public void setTeamChosen(EventInGame.Team teamChosen)
    {
        this.teamChosen = teamChosen;
        RadioButton teamChosenRadioBtnToCheck;
        if (teamChosen == EventInGame.Team.NON)
            teamChosenRadioBtnToCheck = view.findViewById(R.id.noTeam);
        else if (teamChosen == EventInGame.Team.HOME_TEAM)
            teamChosenRadioBtnToCheck = view.findViewById(R.id.home_team);
        else // (teamChosen == Event.Team.AWAY_TEAM)
            teamChosenRadioBtnToCheck = view.findViewById(R.id.away_team);
        teamChosenRadioBtnToCheck.setChecked(true);
    }

    public void setGamePartChosen(EventInGame.GamePart gamePartChosen)
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

    public void setSpecialEventText(String text)
    {
        specialEventEditText.setText(text);
    }

    public void setPlayerDigitNumbers(int digit1 , int digit2)
    {
        playerDigit1NumberPicker.setValue(digit1);
        playerDigit2NumberPicker.setValue(digit2);
    }

    public void setDefaultClockAndPickers()
    {
        if(AppData.GameFragmentData.clockRun)
        {
            String clockText = AppData.gameFragment.makeClockText();
            clockTextView.setText(clockText);
        }
        else
            clockTextView.setText("00:00");
        setGamePartChosen(AppData.GameFragmentData.gamePartChosen);
        setTeamChosen(AppData.GameFragmentData.teamChosen);
        setPlayerDigitNumbers(AppData.GameFragmentData.playerChosenDigit1,AppData.GameFragmentData.playerChosenDigit2);
    }
    private void showKeyboard(View view)
    {
        showKeyboard();
    }

    public void showKeyboard()
    {
        specialEventEditText.setFocusableInTouchMode(true);
        specialEventEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void hideKeyboard()
    {
        specialEventEditText.setEnabled(false);
        specialEventEditText.setEnabled(true);
    }

    private int addEvents()
    {
        if (AppData.dbRepository.eventRepository.eventNames.size() == 0 )
            return -1;
        RadioButton button = null;
        for(String eventName : AppData.dbRepository.eventRepository.eventNames)
        {
            button = new RadioButton(getActivity());
            button.setText(eventName);
            eventsRadioGroup.addView(button);
        }
        return button.getId() - AppData.dbRepository.eventRepository.eventNames.size() + 1;
    }

    public void setPlayerNumPickers0To9()
    {
        playerDigit1NumberPicker.setMinValue(0);
        playerDigit2NumberPicker.setMinValue(0);
        playerDigit1NumberPicker.setMaxValue(9);
        playerDigit2NumberPicker.setMaxValue(9);
    }

}