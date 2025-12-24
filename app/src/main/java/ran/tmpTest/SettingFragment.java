package ran.tmpTest;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.lists.DragAndDropList;

import java.util.Collections;
import java.util.List;


public class SettingFragment extends Fragment
{
    private View view;
    private RadioGroup selectListRadioGroup, addToTopOrBottomRadioGroup;
    private Button addBtn,deleteBtn,editBtn;
    private RecyclerView eventsListView,gamesListView;
    private DragAndDropList eventsDragAndDropList, gamesDragAndDropList;//,crntList;
    private TextView headerTextView;
    private EditText eventOrGameEditText;
    private List<String> listToShow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        AppData.settingFragment = this;
        selectListRadioGroup = view.findViewById(R.id.selectList);
        addToTopOrBottomRadioGroup = view.findViewById(R.id.whereToAdd);
        headerTextView = view.findViewById(R.id.topHeader);
        eventOrGameEditText = view.findViewById(R.id.eventOrGameEditText);
        deleteBtn = view.findViewById(R.id.deleteBtn);
        addBtn = view.findViewById(R.id.addBtn);
        editBtn = view.findViewById(R.id.editBtn);
        eventsListView = view.findViewById(R.id.dragAndDropListEvents);
        gamesListView = view.findViewById(R.id.dragAndDropListGames);
        deleteBtn.setOnClickListener((View) -> deleteBtn());
        editBtn.setOnClickListener((View) -> editBtn());
        addBtn.setOnClickListener((View) -> addBtn());
        eventsDragAndDropList = createDragAndDropList(AppData.dbRepository.eventRepository.eventNames, eventsListView);
        gamesDragAndDropList = createDragAndDropList(AppData.dbRepository.gameRepository.gameNames, gamesListView);
        listToShow = AppData.dbRepository.eventRepository.eventNames;
        selectListRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> onListChange(checkedId));
        return view;
    }

    public void onResume()
    {
        super.onResume();
        eventOrGameEditText.setText("");
    }

    public void onStop()
    {
        super.onStop();
        dismissKeyboard();
        if(AppData.SettingFragmentData.listChoosePosition != -1)
        {
            AppData.SettingFragmentData.listChoosePosition = -1;
            if (listToShow == AppData.dbRepository.eventRepository.eventNames)
                eventsDragAndDropList.notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
            else //listToShow == AppData.gamesStringList
                gamesDragAndDropList.notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
        }
    }

    private void dismissKeyboard()
    {
        eventOrGameEditText.setEnabled(false);
        eventOrGameEditText.setEnabled(true);
    }

    private void onListChange(int checkedId)
    {
        switch(checkedId)
        {
            case R.id.gamesList:
                listToShow = AppData.dbRepository.gameRepository.gameNames;
                eventsDragAndDropList.notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
                gamesListView.setVisibility(View.VISIBLE);
                eventsListView.setVisibility(View.INVISIBLE);
                headerTextView.setText(getString(R.string.gamesList));
                eventOrGameEditText.setHint(getString(R.string.enterGameName));
                addBtn.setText(getString(R.string.addGame));
                editBtn.setText(getString(R.string.renameGame));
                deleteBtn.setText(getString(R.string.deleteGame));
                break;
            case R.id.eventsList :
                listToShow = AppData.dbRepository.eventRepository.eventNames;
                gamesDragAndDropList.notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
                gamesListView.setVisibility(View.INVISIBLE);
                eventsListView.setVisibility(View.VISIBLE);
                headerTextView.setText(getString(R.string.eventsList));
                eventOrGameEditText.setHint(getString(R.string.enterEventName));
                addBtn.setText(getString(R.string.addEvent));
                editBtn.setText(getString(R.string.renameEvent));
                deleteBtn.setText(getString(R.string.deleteEvent));
                break;
        }
        changeToNoneChoseItemMode();
    }

    public void changeToUserChoseItemMode()
    {
        eventOrGameEditText.setText(listToShow.get(AppData.SettingFragmentData.listChoosePosition));
        deleteBtn.setVisibility(View.VISIBLE);
        editBtn.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.INVISIBLE);
        addToTopOrBottomRadioGroup.setVisibility(View.INVISIBLE);
    }

    public void updateEventOrGameEditText()
    {
        eventOrGameEditText.setText(listToShow.get(AppData.SettingFragmentData.listChoosePosition));
    }

    public void changeToNoneChoseItemMode()
    {
        AppData.SettingFragmentData.listChoosePosition = -1;
        eventOrGameEditText.setText("");
        deleteBtn.setVisibility(View.INVISIBLE);
        editBtn.setVisibility(View.INVISIBLE);
        addBtn.setVisibility(View.VISIBLE);
        addToTopOrBottomRadioGroup.setVisibility(View.VISIBLE);

    }
    public DragAndDropList createDragAndDropList(List<String> list,RecyclerView recyclerView)
    {
        DragAndDropList recyclerAdapter = new DragAndDropList(list);
        recyclerView.setAdapter(recyclerAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(AppData.mainActivity, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0)
        {
            public boolean onMove(RecyclerView recyclerView,RecyclerView.ViewHolder viewHolder,RecyclerView.ViewHolder target)
            {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                changeChoosePosition(fromPosition,toPosition);
                if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList )
                {
                    changeSelectedGamePosition(fromPosition,toPosition);
                    AppData.dbRepository.gameRepository.moveGame(fromPosition,toPosition);
                }
                else // selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
                    AppData.dbRepository.eventRepository.moveEvent(fromPosition,toPosition);
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return false;
            }
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return recyclerAdapter;
    }


    public void changeSelectedGamePosition(int fromPosition,int toPosition)
    {
        if(AppData.GameFragmentData.gameChosen != -1)
        {
            if (AppData.GameFragmentData.gameChosen == fromPosition)
                AppData.GameFragmentData.gameChosen = toPosition;
            else if (fromPosition < AppData.GameFragmentData.gameChosen && toPosition >= AppData.GameFragmentData.gameChosen)
                AppData.GameFragmentData.gameChosen--;
            else if (fromPosition > AppData.GameFragmentData.gameChosen && toPosition <= AppData.GameFragmentData.gameChosen)
                AppData.GameFragmentData.gameChosen++;
        }
        if (AppData.EventsFragmentsData.gameChosen != -1)
        {
            if (AppData.EventsFragmentsData.gameChosen == fromPosition)
                AppData.EventsFragmentsData.gameChosen = toPosition;
            else if (fromPosition < AppData.EventsFragmentsData.gameChosen && toPosition >= AppData.EventsFragmentsData.gameChosen)
                AppData.EventsFragmentsData.gameChosen--;
            else if (fromPosition > AppData.EventsFragmentsData.gameChosen && toPosition <= AppData.EventsFragmentsData.gameChosen)
                AppData.EventsFragmentsData.gameChosen++;
        }
    }

    public void changeChoosePosition(int fromPosition,int toPosition)
    {
        if (AppData.SettingFragmentData.listChoosePosition == fromPosition)
            AppData.SettingFragmentData.listChoosePosition = toPosition;
        else if (fromPosition < AppData.SettingFragmentData.listChoosePosition && toPosition >= AppData.SettingFragmentData.listChoosePosition)
            AppData.SettingFragmentData.listChoosePosition--;
        else if (fromPosition > AppData.SettingFragmentData.listChoosePosition && toPosition <= AppData.SettingFragmentData.listChoosePosition)
            AppData.SettingFragmentData.listChoosePosition++;
    }

    private void editBtn()
    {
        String changeToName = eventOrGameEditText.getText().toString();
        if (changeToName.isEmpty())
        {
            Toast.makeText(getActivity(), eventOrGameEditText.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        eventOrGameEditText.setText("");
        if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList )
        {
            AppData.dbRepository.eventRepository.updateEventName(AppData.SettingFragmentData.listChoosePosition,changeToName);
            eventsDragAndDropList.notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
        }
        else //selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList
        {
            AppData.dbRepository.gameRepository.updateGameName(AppData.SettingFragmentData.listChoosePosition,changeToName);
            gamesDragAndDropList.notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
        }
        changeToNoneChoseItemMode(); // this fun change AppData.listChoosePosition to -1
    }

    private void deleteBtn()
    {
        if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList)
        {
            AppData.dbRepository.gameRepository.deleteGame(AppData.SettingFragmentData.listChoosePosition);
            if(AppData.GameFragmentData.gameChosen == AppData.SettingFragmentData.listChoosePosition)
                AppData.GameFragmentData.gameChosen = -1;
            else if (AppData.GameFragmentData.gameChosen > AppData.SettingFragmentData.listChoosePosition)
                AppData.GameFragmentData.gameChosen--;
            if(AppData.EventsFragmentsData.gameChosen == AppData.SettingFragmentData.listChoosePosition)
                AppData.EventsFragmentsData.gameChosen = -1;
            else if (AppData.EventsFragmentsData.gameChosen > AppData.SettingFragmentData.listChoosePosition)
                AppData.EventsFragmentsData.gameChosen--;
            gamesDragAndDropList.notifyItemRemoved(AppData.SettingFragmentData.listChoosePosition);
        }
        else //selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
        {
            AppData.dbRepository.eventRepository.deleteEvent(AppData.SettingFragmentData.listChoosePosition);
            eventsDragAndDropList.notifyItemRemoved(AppData.SettingFragmentData.listChoosePosition);
        }
        eventOrGameEditText.setText("");
        changeToNoneChoseItemMode();
    }
    private void addBtn()
    {
        String newName = eventOrGameEditText.getText().toString();
        if (newName.isEmpty())
        {
            Toast.makeText(getActivity(), eventOrGameEditText.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }

        if ( addToTopOrBottomRadioGroup.getCheckedRadioButtonId() == R.id.addToUp )
        {
            if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList)
            {
                AppData.dbRepository.gameRepository.addGameAtStart(newName);
                if(AppData.GameFragmentData.gameChosen != -1)
                    AppData.GameFragmentData.gameChosen++;
                if(AppData.EventsFragmentsData.gameChosen != -1)
                    AppData.EventsFragmentsData.gameChosen++;
                gamesDragAndDropList.notifyItemInserted(0);
            }
            else // selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
            {
                AppData.dbRepository.eventRepository.addEventAtStart(newName);
                eventsDragAndDropList.notifyItemInserted(0);
            }
        }
        else // whereToAdd.getCheckedRadioButtonId() == R.id.addToDown
        {
            if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList)
            {
                AppData.dbRepository.gameRepository.addGameAtEnd(newName);
                gamesDragAndDropList.notifyItemInserted(listToShow.size() -1);
            }
            else // selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
            {
                AppData.dbRepository.eventRepository.addEventAtEnd(newName);
                eventsDragAndDropList.notifyItemInserted(listToShow.size() -1);
            }
        }
        eventOrGameEditText.setText("");
    }
}