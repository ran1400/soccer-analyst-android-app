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
import ran.tmpTest.utils.Event;
import ran.tmpTest.utils.Game;


public class EventAlertDialog extends AppCompatDialogFragment
{
    private Event eventToEdit; // if null -> user want create new event
    private NumberPicker playerDigit1NumberPicker, playerDigit2NumberPicker;
    private ConstraintLayout eventsScrollView;
    private RadioGroup eventsRadioGroup;
    private RadioButton specialEventRadioButton;
    private EditText specialEventEditText;
    private TextView clockTextView,gamePartTextView;
    private Event.Team teamChosen;
    private final int PERSONAL_EVENT = -1;
    private int getEventChosenHelper;
    private View view;
    private Button saveBtn,cancelBtn;

    public EventAlertDialog() //create new event
    {
        eventToEdit = null;
    }

    public EventAlertDialog(Event eventToEdit) // edit event
    {
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
                teamChosen = Event.Team.NON;
                break;
            case R.id.home_team:
                teamChosen = Event.Team.HOME_TEAM;
                break;
            case R.id.away_team:
                teamChosen = Event.Team.AWAY_TEAM;
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
                eventToEdit.eventName = AppData.events.get(eventChosen);
            AppData.eventsFragment.notifyEventEditChanged();
        }
        else
        {
            Event event = makeEvent(eventChosen);
            Game crntGame = AppData.games.get(GameFragment.gameChosen);
            crntGame.events.add(event);
            AppData.gameFragment.showEventAddedSnackBar(crntGame);
        }
        dismiss();
    }

    private Event makeEvent(int eventChosen)
    {
        int playerNum = getPlayerNumber();
        Event.GamePart gamePart = AppData.gamePartChosen;
        Event.Team team = teamChosen;
        String eventName;
        if ( eventChosen == PERSONAL_EVENT)
            eventName = specialEventEditText.getText().toString();
        else
            eventName = AppData.events.get(eventChosen);
        if (AppData.clockRun)
            return new Event(gamePart,team,AppData.min,AppData.sec,playerNum,eventName);
        else
            return new Event(gamePart,team,0,0,playerNum,eventName);
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

    public void setTeamChosen(Event.Team teamChosen)
    {
        this.teamChosen = teamChosen;
        RadioButton teamChosenRadioBtnToCheck;
        if (teamChosen == Event.Team.NON)
            teamChosenRadioBtnToCheck = view.findViewById(R.id.noTeam);
        else if (teamChosen == Event.Team.HOME_TEAM)
            teamChosenRadioBtnToCheck = view.findViewById(R.id.home_team);
        else // (teamChosen == Event.Team.AWAY_TEAM)
            teamChosenRadioBtnToCheck = view.findViewById(R.id.away_team);
        teamChosenRadioBtnToCheck.setChecked(true);
    }

    public void setGamePartChosen(Event.GamePart gamePartChosen)
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
        if(AppData.clockRun)
        {
            String clockText = AppData.gameFragment.makeClockText();
            clockTextView.setText(clockText);
        }
        else
            clockTextView.setText("00:00");
        setGamePartChosen(AppData.gamePartChosen);
        setTeamChosen(AppData.teamChosen);
        setPlayerDigitNumbers(AppData.playerChosenDigit1,AppData.playerChosenDigit2);
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
        if (AppData.events.size() == 0 )
            return -1;
        RadioButton button = null;
        for (int i = 0; i < AppData.events.size() ; i++)
        {
            button = new RadioButton(getActivity());
            button.setText(AppData.events.get(i));
            eventsRadioGroup.addView(button);
        }
        return button.getId() - AppData.events.size() + 1;
    }

    public void setPlayerNumPickers0To9()
    {
        playerDigit1NumberPicker.setMinValue(0);
        playerDigit2NumberPicker.setMinValue(0);
        playerDigit1NumberPicker.setMaxValue(9);
        playerDigit2NumberPicker.setMaxValue(9);
    }

}