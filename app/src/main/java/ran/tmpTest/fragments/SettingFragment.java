package ran.tmpTest.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

import ran.tmpTest.MyApp;
import ran.tmpTest.R;

import ran.tmpTest.sharedData.EventsFragmentData;

import ran.tmpTest.sharedData.GameFragmentData;
import ran.tmpTest.utils.lists.DragAndDropList;

import java.util.List;


public class SettingFragment extends Fragment
{
    private RadioGroup selectListRadioGroup, addToTopOrBottomRadioGroup;
    private Button addBtn,deleteBtn,editBtn;
    private RecyclerView eventsListView,gamesListView;
    private DragAndDropList eventsDragAndDropList,gamesDragAndDropList;
    private TextView headerTextView;
    private EditText eventOrGameEditText;
    private List<String> listToShow;
    public int listChoosePosition = -1; // for drag and dop list

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
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
        eventsDragAndDropList = createDragAndDropList(MyApp.dbRepository.eventRepository.eventNames, eventsListView);
        gamesDragAndDropList = createDragAndDropList(MyApp.dbRepository.gameRepository.gameNames, gamesListView);
        listToShow = MyApp.dbRepository.eventRepository.eventNames;
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
        if(listChoosePosition != -1)
        {
            listChoosePosition = -1;
            if (listToShow == MyApp.dbRepository.eventRepository.eventNames)
                eventsDragAndDropList.notifyItemChanged(listChoosePosition);
            else //listToShow == MyApp.dbRepository.eventRepository.gameNames
                gamesDragAndDropList.notifyItemChanged(listChoosePosition);
        }
    }

    private void dismissKeyboard()
    {
        eventOrGameEditText.setEnabled(false);
        eventOrGameEditText.setEnabled(true);
    }

    private void onListChange(int checkedId)
    {
        if (checkedId == R.id.gamesList)
        {
            listToShow = MyApp.dbRepository.gameRepository.gameNames;
            eventsDragAndDropList.notifyItemChanged(listChoosePosition);
            gamesListView.setVisibility(View.VISIBLE);
            eventsListView.setVisibility(View.INVISIBLE);
            headerTextView.setText(getString(R.string.gamesList));
            eventOrGameEditText.setHint(getString(R.string.enterGameName));
            addBtn.setText(getString(R.string.addGame));
            editBtn.setText(getString(R.string.renameGame));
            deleteBtn.setText(getString(R.string.deleteGame));
        }
        else // (checkedId == R.id.eventsList)
        {
            listToShow = MyApp.dbRepository.eventRepository.eventNames;
            gamesDragAndDropList.notifyItemChanged(listChoosePosition);
            gamesListView.setVisibility(View.INVISIBLE);
            eventsListView.setVisibility(View.VISIBLE);
            headerTextView.setText(getString(R.string.eventsList));
            eventOrGameEditText.setHint(getString(R.string.enterEventName));
            addBtn.setText(getString(R.string.addEvent));
            editBtn.setText(getString(R.string.renameEvent));
            deleteBtn.setText(getString(R.string.deleteEvent));
        }
        changeToNoneChoseItemMode();
    }

    public void changeToUserChoseItemMode()
    {
        eventOrGameEditText.setText(listToShow.get(listChoosePosition));
        deleteBtn.setVisibility(View.VISIBLE);
        editBtn.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.INVISIBLE);
        addToTopOrBottomRadioGroup.setVisibility(View.INVISIBLE);
    }

    public void updateEventOrGameEditText()
    {
        eventOrGameEditText.setText(listToShow.get(listChoosePosition));
    }

    public void changeToNoneChoseItemMode()
    {
        listChoosePosition = -1;
        eventOrGameEditText.setText("");
        deleteBtn.setVisibility(View.INVISIBLE);
        editBtn.setVisibility(View.INVISIBLE);
        addBtn.setVisibility(View.VISIBLE);
        addToTopOrBottomRadioGroup.setVisibility(View.VISIBLE);

    }
    public DragAndDropList createDragAndDropList(List<String> list,RecyclerView recyclerView)
    {
        DragAndDropList recyclerAdapter = new DragAndDropList(list,this);
        recyclerView.setAdapter(recyclerAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0)
        {
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target)

            {
                if (recyclerView.getAdapter() == null) // broken list adapter(shouldn't happen)
                    return false;
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                changeChoosePosition(fromPosition,toPosition);
                if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList )
                {
                    changeSelectedGamePosition(fromPosition,toPosition);
                    MyApp.dbRepository.gameRepository.moveGame(fromPosition,toPosition);
                }
                else // selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
                    MyApp.dbRepository.eventRepository.moveEvent(fromPosition,toPosition);
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return true;
            }
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return recyclerAdapter;
    }


    public void changeSelectedGamePosition(int fromPosition,int toPosition)
    {
        if(GameFragmentData.gameChosen != -1)
        {
            if (GameFragmentData.gameChosen == fromPosition)
                GameFragmentData.gameChosen = toPosition;
            else if (fromPosition < GameFragmentData.gameChosen && toPosition >= GameFragmentData.gameChosen)
                GameFragmentData.gameChosen--;
            else if (fromPosition > GameFragmentData.gameChosen && toPosition <= GameFragmentData.gameChosen)
                GameFragmentData.gameChosen++;
        }
        if (EventsFragmentData.gameChosen != -1)
        {
            if (EventsFragmentData.gameChosen == fromPosition)
                EventsFragmentData.gameChosen = toPosition;
            else if (fromPosition < EventsFragmentData.gameChosen && toPosition >= EventsFragmentData.gameChosen)
                EventsFragmentData.gameChosen--;
            else if (fromPosition > EventsFragmentData.gameChosen && toPosition <= EventsFragmentData.gameChosen)
                EventsFragmentData.gameChosen++;
        }
    }

    public void changeChoosePosition(int fromPosition,int toPosition)
    {
        if (listChoosePosition == fromPosition)
            listChoosePosition = toPosition;
        else if (fromPosition < listChoosePosition && toPosition >= listChoosePosition)
            listChoosePosition--;
        else if (fromPosition > listChoosePosition && toPosition <= listChoosePosition)
            listChoosePosition++;
    }

    private void editBtn()
    {
        String changeToName = eventOrGameEditText.getText().toString();
        if (changeToName.isEmpty())
        {
            Toast.makeText(getActivity(),eventOrGameEditText.getHint(),Toast.LENGTH_SHORT).show();
            return;
        }
        eventOrGameEditText.setText("");
        if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList )
        {
            MyApp.dbRepository.eventRepository.updateEventName(listChoosePosition,changeToName);
            eventsDragAndDropList.notifyItemChanged(listChoosePosition);
        }
        else //selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList
        {
            MyApp.dbRepository.gameRepository.updateGameName(listChoosePosition,changeToName);
            gamesDragAndDropList.notifyItemChanged(listChoosePosition);
        }
        changeToNoneChoseItemMode(); // this fun change AppData.listChoosePosition to -1
    }

    private void deleteBtn()
    {
        if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList)
        {
            MyApp.dbRepository.gameRepository.deleteGame(listChoosePosition);
            if(GameFragmentData.gameChosen == listChoosePosition)
                GameFragmentData.gameChosen = -1;
            else if (GameFragmentData.gameChosen > listChoosePosition)
                GameFragmentData.gameChosen--;
            if(EventsFragmentData.gameChosen == listChoosePosition)
                EventsFragmentData.gameChosen = -1;
            else if (EventsFragmentData.gameChosen > listChoosePosition)
                EventsFragmentData.gameChosen--;
            gamesDragAndDropList.notifyItemRemoved(listChoosePosition);
        }
        else //selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
        {
            MyApp.dbRepository.eventRepository.deleteEvent(listChoosePosition);
            eventsDragAndDropList.notifyItemRemoved(listChoosePosition);
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
                MyApp.dbRepository.gameRepository.addGameAtStart(newName);
                if(GameFragmentData.gameChosen != -1)
                    GameFragmentData.gameChosen++;
                if(EventsFragmentData.gameChosen != -1)
                    EventsFragmentData.gameChosen++;
                gamesDragAndDropList.notifyItemInserted(0);
            }
            else // selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
            {
                MyApp.dbRepository.eventRepository.addEventAtStart(newName);
                eventsDragAndDropList.notifyItemInserted(0);
            }
        }
        else // whereToAdd.getCheckedRadioButtonId() == R.id.addToDown
        {
            if (selectListRadioGroup.getCheckedRadioButtonId() == R.id.gamesList)
            {
                MyApp.dbRepository.gameRepository.addGameAtEnd(newName);
                gamesDragAndDropList.notifyItemInserted(listToShow.size() -1);
            }
            else // selectListRadioGroup.getCheckedRadioButtonId() == R.id.eventsList
            {
                MyApp.dbRepository.eventRepository.addEventAtEnd(newName);
                eventsDragAndDropList.notifyItemInserted(listToShow.size() -1);
            }
        }
        eventOrGameEditText.setText("");
    }
}