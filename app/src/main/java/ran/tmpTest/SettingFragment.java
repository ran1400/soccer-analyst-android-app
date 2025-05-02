package ran.tmpTest;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.lists.DragAndDropList;
import ran.tmpTest.utils.Game;

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
        eventsDragAndDropList = createDragAndDropList(AppData.events,eventsListView);
        gamesDragAndDropList = createDragAndDropList(AppData.gamesStringList,gamesListView);
        listToShow = AppData.events;
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
        if(AppData.listChoosePosition != -1)
        {
            AppData.listChoosePosition = -1;
            if (listToShow == AppData.events)
                eventsDragAndDropList.notifyItemChanged(AppData.listChoosePosition);
            else //listToShow == AppData.gamesStringList
                gamesDragAndDropList.notifyItemChanged(AppData.listChoosePosition);
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
                listToShow = AppData.gamesStringList;
                eventsDragAndDropList.notifyItemChanged(AppData.listChoosePosition);
                gamesListView.setVisibility(View.VISIBLE);
                eventsListView.setVisibility(View.INVISIBLE);
                headerTextView.setText(getString(R.string.gamesList));
                eventOrGameEditText.setHint(getString(R.string.enterGameName));
                addBtn.setText(getString(R.string.addGame));
                editBtn.setText(getString(R.string.renameGame));
                deleteBtn.setText(getString(R.string.deleteGame));
                break;
            case R.id.evetnsList :
                listToShow = AppData.events;
                gamesDragAndDropList.notifyItemChanged(AppData.listChoosePosition);
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
        eventOrGameEditText.setText(listToShow.get(AppData.listChoosePosition));
        deleteBtn.setVisibility(View.VISIBLE);
        editBtn.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.INVISIBLE);
        addToTopOrBottomRadioGroup.setVisibility(View.INVISIBLE);
    }

    public void updateEventOrGameEditText()
    {
        eventOrGameEditText.setText(listToShow.get(AppData.listChoosePosition));
    }

    public void changeToNoneChoseItemMode()
    {
        AppData.listChoosePosition = -1;
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
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
            {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(listToShow, fromPosition, toPosition);
                if (listToShow == AppData.gamesStringList )
                {
                    Collections.swap(AppData.games, fromPosition, toPosition);
                    changeSelectedGamePosition(fromPosition,toPosition);
                }
                changeChoosePosition(fromPosition,toPosition);
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return recyclerAdapter;
    }

    public void changeSelectedGamePosition(int fromPosition,int toPosition)
    {
        if(GameFragment.gameChosen != -1)
        {
            if (GameFragment.gameChosen == fromPosition)
                GameFragment.gameChosen = toPosition;
            else if (fromPosition < GameFragment.gameChosen && toPosition >= GameFragment.gameChosen)
                GameFragment.gameChosen--;
            else if (fromPosition > GameFragment.gameChosen && toPosition <= GameFragment.gameChosen)
                GameFragment.gameChosen++;
        }
        if (EventsFragment.gameChosen != -1)
        {
            if (EventsFragment.gameChosen == fromPosition)
                EventsFragment.gameChosen = toPosition;
            else if (fromPosition < EventsFragment.gameChosen && toPosition >= EventsFragment.gameChosen)
                EventsFragment.gameChosen--;
            else if (fromPosition > EventsFragment.gameChosen && toPosition <= EventsFragment.gameChosen)
                EventsFragment.gameChosen++;
        }
    }

    public void changeChoosePosition(int fromPosition,int toPosition)
    {
        if (AppData.listChoosePosition == fromPosition)
            AppData.listChoosePosition = toPosition;
        else if (fromPosition < AppData.listChoosePosition && toPosition >= AppData.listChoosePosition)
            AppData.listChoosePosition--;
        else if (fromPosition > AppData.listChoosePosition && toPosition <= AppData.listChoosePosition)
            AppData.listChoosePosition++;
    }

    private void editBtn()
    {
        String changeToName = eventOrGameEditText.getText().toString();
        if (changeToName.isEmpty())
        {
            Toast.makeText(getActivity(), eventOrGameEditText.getHint(), Toast.LENGTH_SHORT).show();
            return;
        }
        listToShow.set(AppData.listChoosePosition, changeToName);
        eventOrGameEditText.setText("");
        if (listToShow == AppData.events)
            eventsDragAndDropList.notifyItemChanged(AppData.listChoosePosition);
        else //listToShow == AppData.gamesStringList
        {
            AppData.games.get(AppData.listChoosePosition).gameName = changeToName;
            gamesDragAndDropList.notifyItemChanged(AppData.listChoosePosition);
        }
        changeToNoneChoseItemMode(); // this fun change AppData.listChoosePosition to -1
    }

    private void deleteBtn()
    {
        if (listToShow == AppData.gamesStringList)
        {
            AppData.games.remove(AppData.listChoosePosition);
            if(GameFragment.gameChosen == AppData.listChoosePosition)
                GameFragment.gameChosen = -1;
            else if (GameFragment.gameChosen > AppData.listChoosePosition)
                GameFragment.gameChosen--;
            if(EventsFragment.gameChosen == AppData.listChoosePosition)
                EventsFragment.gameChosen = -1;
            else if (EventsFragment.gameChosen > AppData.listChoosePosition)
                EventsFragment.gameChosen--;
            AppData.gamesStringList.remove(AppData.listChoosePosition);
            gamesDragAndDropList.notifyItemRemoved(AppData.listChoosePosition);
        }
        else //listToShow == AppData.events
        {
            AppData.events.remove(AppData.listChoosePosition);
            eventsDragAndDropList.notifyItemRemoved(AppData.listChoosePosition);
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
        DragAndDropList crntList ;
        if (listToShow == AppData.events)
            crntList = eventsDragAndDropList;
        else //listToShow == AppData.gamesStringList
            crntList = gamesDragAndDropList;
        if ( addToTopOrBottomRadioGroup.getCheckedRadioButtonId() == R.id.addToUp )
        {
            if (listToShow == AppData.gamesStringList)
            {
                AppData.games.add(0, new Game(newName));
                if(GameFragment.gameChosen != -1)
                    GameFragment.gameChosen++;
                if(EventsFragment.gameChosen != -1)
                    EventsFragment.gameChosen++;
            }
            listToShow.add(0, newName);
            crntList.notifyItemInserted(0);
        }
        else // whereToAdd.getCheckedRadioButtonId() == R.id.addToDown
        {
            if (listToShow == AppData.gamesStringList)
                AppData.games.add(new Game(newName));
            listToShow.add(newName);
            crntList.notifyItemInserted(listToShow.size() -1);
        }
        eventOrGameEditText.setText("");
    }
}