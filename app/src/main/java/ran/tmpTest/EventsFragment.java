package ran.tmpTest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import ran.tmpTest.alertDialogs.EventAlertDialog;
import ran.tmpTest.sharedData.AppData;
import ran.tmpTest.utils.Event;
import ran.tmpTest.utils.ExelHandel;
import ran.tmpTest.utils.Game;
import ran.tmpTest.utils.lists.SwipeToDeleteList;

import java.util.ArrayList;
import java.util.List;


public class EventsFragment extends Fragment
{
    public static int gameChosen;
    private List<Event> listToShow;
    private Button saveFileBtn;
    private Spinner chooseGameDropDownList;
    private RecyclerView eventsList;
    private SwipeToDeleteList swipeToDeleteList;

    private TextView msgToUser;
    private View view;

    ActivityResultLauncher<Intent> createFileLauncher;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_events, container, false);
        saveFileBtn = view.findViewById(R.id.saveFileBtn);
        AppData.eventsFragment = this;
        msgToUser = view.findViewById(R.id.msgToUser);
        if (AppData.gamesStringList.isEmpty())
        {
            showMsgToUser(getString(R.string.theGamesListIsEmptyAddGameInTheSettings));
            saveFileBtn.setVisibility(View.INVISIBLE);
            return view;
        }
        eventsList = view.findViewById(R.id.eventsList);
        chooseGameDropDownList = view.findViewById(R.id.choseGameDropDownList);
        saveFileBtn.setOnClickListener(this::saveFileBtn);
        swipeToDeleteList = new SwipeToDeleteList();
        eventsList.setAdapter(swipeToDeleteList);
        eventsList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        createEventsList();
        createChoseGameDropDownList();
        createFileLauncher = createFileLauncher();
        return view;
    }

    public void onResume()
    {
        super.onResume();
        if(gameChosen != -1)
            chooseGameDropDownList.setSelection(gameChosen);
    }

    private void showMsgToUser(String msg)
    {
        msgToUser.setText(msg);
        msgToUser.setVisibility(View.VISIBLE);
    }

    public void userClickOnItem(int position) // user click on event for edit him
    {
        EventAlertDialog eventAlertDialog = new EventAlertDialog(listToShow.get(position));
        eventAlertDialog.show(AppData.mainActivity.getSupportFragmentManager(),"");
    }

    private void userSwipeItemLeft(int position) // user swiped event to delete
    {
        Event eventToRemove = listToShow.get(position);
        swipeToDeleteList.listData.remove(position);
        listToShow.remove(position);
        swipeToDeleteList.notifyItemRemoved(position);
        if(listToShow.isEmpty())
        {
            saveFileBtn.setVisibility(View.INVISIBLE);
            showMsgToUser(getString(R.string.noEventsHaveBeenRecordedYet));
        }
        showEventRemoveSnackBar(position,eventToRemove);
    }

    private void showEventRemoveSnackBar(int position,Event eventToRemove)
    {
        Snackbar snackBar = Snackbar.make(AppData.mainActivity.getView(),R.string.theEventIsDeleted, Snackbar.LENGTH_LONG);
        snackBar.setAction(R.string.cancel, new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if (listToShow.isEmpty())
                    msgToUser.setVisibility(View.INVISIBLE); //remove the msg if the list is empty
                listToShow.add(position,eventToRemove); //user decide to cancel the delete event action
                swipeToDeleteList.listData.add(position,eventToRemove.toString());
                swipeToDeleteList.notifyDataSetChanged();
            }
        }).setDuration(1000).show();
    }

    public void notifyEventEditChanged()
    {
        showGameEvents();
        swipeToDeleteList.notifyDataSetChanged();
    }

    private void createEventsList() //swipe to delete list
    {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
        {
            private Paint paint = new Paint();
            private TextView swipeToDeleteText;
            public boolean onMove(RecyclerView recyclerView,RecyclerView.ViewHolder viewHolder,RecyclerView.ViewHolder target)
            {
                return false;
            }

            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
                userSwipeItemLeft(viewHolder.getAdapterPosition());
            }

            public void onChildDraw(Canvas canvas,RecyclerView recyclerView,RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive)
            {
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE)
                    return;
                View itemView = viewHolder.itemView;
                paint.setColor(Color.RED);
                canvas.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                if (swipeToDeleteText == null)
                {
                    swipeToDeleteText = new TextView(view.getContext());
                    swipeToDeleteText.setText(R.string.swipeTDelete);
                    swipeToDeleteText.setTextColor(Color.WHITE);
                    swipeToDeleteText.setBackgroundColor(Color.RED);
                    swipeToDeleteText.setPadding(16, 16, 16, 16);
                    swipeToDeleteText.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    swipeToDeleteText.layout(0, 0, swipeToDeleteText.getMeasuredWidth(), swipeToDeleteText.getMeasuredHeight());
                }
                float textX = itemView.getRight() - swipeToDeleteText.getWidth() - 16; // Adjust padding;
                float textY = itemView.getTop() + ((itemView.getBottom() - itemView.getTop() - swipeToDeleteText.getHeight()) / 2);
                canvas.save();
                canvas.translate(textX, textY);
                swipeToDeleteText.draw(canvas);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(eventsList);
    }

    private ActivityResultLauncher<Intent> createFileLauncher()
    {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->
                {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)
                    {
                        Uri uri = result.getData().getData();
                        if (uri != null)
                            saveFile(AppData.games.get(gameChosen),uri); // do toasts
                    }
                }
        );
    }

    private void saveFile(Game game, Uri uri)
    {
        ExelHandel exelHandel = new ExelHandel(game,requireContext(),uri);
        boolean success =  exelHandel.makeEventsFile();
        if (success)
            AppData.mainActivity.showSnackBar(getString(R.string.theFileIsSaved),1000);
        else
            AppData.mainActivity.showSnackBar(getString(R.string.failedToSaveTheFile),1000);
    }

    public void saveFileBtn(View view)
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE,AppData.games.get(gameChosen).makeFileName());
        createFileLauncher.launch(intent);
    }

    public void createChoseGameDropDownList()
    {
        ArrayAdapter<String>adapter = new ArrayAdapter(getActivity(),
                                                       android.R.layout.simple_spinner_item,AppData.gamesStringList);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        chooseGameDropDownList.setAdapter(adapter);
        chooseGameDropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l)
            {
                gameChosen = position;
                listToShow = AppData.games.get(position).events;
                showGameEvents();
                swipeToDeleteList.notifyDataSetChanged();
            }
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                gameChosen = -1;
                listToShow = null;
                saveFileBtn.setVisibility(View.INVISIBLE);
                swipeToDeleteList.listData = null;
                swipeToDeleteList.notifyDataSetChanged();
            }
        });
    }

    private void showGameEvents()
    {
        if(listToShow.isEmpty())
        {
            saveFileBtn.setVisibility(View.INVISIBLE);
            swipeToDeleteList.listData = null;
            showMsgToUser(getString(R.string.noEventsHaveBeenRecordedYet));
        }
        else
        {
            msgToUser.setVisibility(View.INVISIBLE);
            swipeToDeleteList.listData = new ArrayList<>();
            saveFileBtn.setVisibility(View.VISIBLE);
            for (Event event : listToShow)
                swipeToDeleteList.listData.add(event.toString());
        }
    }
}