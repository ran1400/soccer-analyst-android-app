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

import android.os.Handler;
import android.os.Looper;
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
import ran.tmpTest.utils.EventInGame;
import ran.tmpTest.utils.ExcelHandel;
import ran.tmpTest.utils.lists.SwipeToDeleteList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;


public class EventsFragment extends Fragment
{
    private List<EventInGame> listToShow;
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
        if (AppData.dbRepository.gameRepository.gameNames.isEmpty())
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
        if(AppData.EventsFragmentsData.gameChosen != -1)
            chooseGameDropDownList.setSelection(AppData.EventsFragmentsData.gameChosen);
    }

    private void showMsgToUser(String msg)
    {
        msgToUser.setText(msg);
        msgToUser.setVisibility(View.VISIBLE);
    }

    public void userClickOnItem(int position) // user click on event for edit him
    {
        long gameId = AppData.dbRepository.gameRepository.gameIds.get(AppData.EventsFragmentsData.gameChosen);
        EventAlertDialog eventAlertDialog = new EventAlertDialog(gameId,listToShow.get(position));
        eventAlertDialog.show(AppData.mainActivity.getSupportFragmentManager(),"");
    }

    private void userSwipeItemLeft(int position) // user swiped event to delete
    {
        EventInGame eventToRemove = listToShow.get(position);
        swipeToDeleteList.listData.remove(position);
        listToShow.remove(position);
        swipeToDeleteList.notifyItemRemoved(position);
        if(listToShow.isEmpty())
        {
            saveFileBtn.setVisibility(View.INVISIBLE);
            showMsgToUser(getString(R.string.noEventsHaveBeenRecordedYet));
        }
        AppData.dbRepository.eventInGameRepository.deleteEventInGame(eventToRemove);
        showEventRemoveSnackBar(position,eventToRemove);
    }

    private void showEventRemoveSnackBar(int position, EventInGame eventToRemove)
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
                AppData.dbRepository.eventInGameRepository.cancelDeleteEventInGame(eventToRemove);
            }
        }).setDuration(1000).show();
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


    private void saveFile(String gameName,long gameId, Uri uri)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            ExcelHandel excelHandel = new ExcelHandel(gameId,gameName,requireContext(),uri);
            boolean success =  excelHandel.makeEventsFile();
            if (success)
                AppData.mainActivity.showSnackBar(getString(R.string.theFileIsSaved),1000);
            else
                AppData.mainActivity.showSnackBar(getString(R.string.failedToSaveTheFile),1000);
        });
    }

    public void saveFileBtn(View view)
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE,makeFileName(AppData.dbRepository.gameRepository.gameNames.get(AppData.EventsFragmentsData.gameChosen)));
        createFileLauncher.launch(intent);
    }

    public String makeFileName(String gameName)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss", Locale.getDefault());
        String time = sdf.format(new Date());
        String fixedGameName = gameName.replaceAll("[\\\\/:*?\"<>|]", "-");
        return fixedGameName + "  " + time;
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
                        {
                            String gameName = AppData.dbRepository.gameRepository.gameNames.get(AppData.EventsFragmentsData.gameChosen);
                            long gameId = AppData.dbRepository.gameRepository.gameIds.get(AppData.EventsFragmentsData.gameChosen);
                            saveFile(gameName,gameId,uri); // do toasts
                        }
                    }
                }
        );
    }

    public void createChoseGameDropDownList()
    {
        ArrayAdapter<String>adapter = new ArrayAdapter(getActivity(),
                                                       android.R.layout.simple_spinner_item,AppData.dbRepository.gameRepository.gameNames);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        chooseGameDropDownList.setAdapter(adapter);
        chooseGameDropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l)
            {
                Executors.newSingleThreadExecutor().execute(() ->
                {
                    AppData.EventsFragmentsData.gameChosen = position;
                    long gameId = AppData.dbRepository.gameRepository.gameIds.get(AppData.EventsFragmentsData.gameChosen);
                    listToShow = AppData.dbRepository.eventInGameRepository.getEventsInGameWithBlocking(gameId);
                    new Handler(Looper.getMainLooper()).post(() -> showGameEvents());
                });
            }
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                AppData.EventsFragmentsData.gameChosen = -1;
                listToShow = null;
                saveFileBtn.setVisibility(View.INVISIBLE);
                swipeToDeleteList.listData = null;
                swipeToDeleteList.notifyDataSetChanged();
            }
        });
    }

    public void showGameEvents()
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
            for (EventInGame event : listToShow)
                swipeToDeleteList.listData.add(event.toString());
        }
        swipeToDeleteList.notifyDataSetChanged();
    }
}