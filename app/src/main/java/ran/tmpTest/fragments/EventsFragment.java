package ran.tmpTest.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import ran.tmpTest.sharedData.EventsFragmentData;
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

import ran.tmpTest.MyApp;
import ran.tmpTest.R;
import ran.tmpTest.alertDialogs.EventAlertDialogFragment;
import ran.tmpTest.utils.dataStructures.EventInGame;
import ran.tmpTest.utils.ExcelHandler;
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
    private ActivityResultLauncher<Intent> createFileLauncher;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_events, container, false);
        saveFileBtn = view.findViewById(R.id.saveFileBtn);
        msgToUser = view.findViewById(R.id.msgToUser);
        eventsList = view.findViewById(R.id.eventsList);
        chooseGameDropDownList = view.findViewById(R.id.choseGameDropDownList);
        if (MyApp.dbRepository.gameRepository.gameNames.isEmpty())
        {
            showMsgToUser(getString(R.string.theGamesListIsEmptyAddGameInTheSettings));
            saveFileBtn.setVisibility(View.INVISIBLE);
            return view;
        }
        saveFileBtn.setOnClickListener(this::saveFileBtn);
        swipeToDeleteList = new SwipeToDeleteList(this);
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
        if(EventsFragmentData.gameChosen != -1)
            chooseGameDropDownList.setSelection(EventsFragmentData.gameChosen);
    }

    private void showMsgToUser(String msg)
    {
        msgToUser.setText(msg);
        msgToUser.setVisibility(View.VISIBLE);
    }

    public void userClickOnEvent(int position) // user click on event to edit him
    {
        long gameId = MyApp.dbRepository.gameRepository.gameIds.get(EventsFragmentData.gameChosen);
        EventAlertDialogFragment eventAlertDialogFragment = new EventAlertDialogFragment(this,gameId,listToShow.get(position));
        eventAlertDialogFragment.show(requireActivity().getSupportFragmentManager(),"");
    }

    private void userSwipeItemLeft(int position) // user swiped event to delete
    {
        EventInGame eventToRemove = listToShow.get(position);
        listToShow.remove(position);
        swipeToDeleteList.removeItem(position);
        if(listToShow.isEmpty())
        {
            saveFileBtn.setVisibility(View.INVISIBLE);
            showMsgToUser(getString(R.string.noEventsHaveBeenRecordedYet));
        }
        MyApp.dbRepository.eventInGameRepository.deleteEventInGame(eventToRemove);
        showEventRemoveSnackBar(position,eventToRemove);
    }

    private void showEventRemoveSnackBar(int position, EventInGame eventToRemove)
    {
        View myView = getView();
        if (myView == null) // if the fragment is not on the screen
            return;
        Snackbar snackBar = Snackbar.make(myView,R.string.theEventIsDeleted, Snackbar.LENGTH_LONG);
        snackBar.setAction(R.string.cancel, view ->
        {
            if (listToShow.isEmpty())
                msgToUser.setVisibility(View.INVISIBLE); //remove the msg if the list is empty
            listToShow.add(position,eventToRemove); //user decide to cancel the delete event action
            swipeToDeleteList.insertItem(position,eventToRemove.toString());
            MyApp.dbRepository.eventInGameRepository.cancelDeleteEventInGame(eventToRemove);
        }).setDuration(1000).show();
    }



    private void createEventsList() //swipe to delete list
    {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
        {
            private final Paint paint = new Paint();
            private TextView swipeToDeleteText;
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target)
            {
                return false;
            }

            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
                userSwipeItemLeft(viewHolder.getAdapterPosition());
            }


            public void onChildDraw(@NonNull Canvas canvas,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
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
                float textY = itemView.getTop() + ((float) (itemView.getBottom() - itemView.getTop() - swipeToDeleteText.getHeight()) / 2);
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
            ExcelHandler excelHandler = new ExcelHandler(gameId,gameName,requireContext(),uri);
            boolean success =  excelHandler.makeEventsFile();
            View myView = getView();
            if (myView == null) // if the fragment is not on the screen
                return;
            if (success)
                Snackbar.make(myView,R.string.theFileIsSaved,1000).show();
            else
                Snackbar.make(myView,R.string.failedToSaveTheFile,1000).show();
        });
    }

    public void saveFileBtn(View view)
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE,makeFileName(MyApp.dbRepository.gameRepository.gameNames.get(EventsFragmentData.gameChosen)));
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
                            String gameName = MyApp.dbRepository.gameRepository.gameNames.get(EventsFragmentData.gameChosen);
                            long gameId = MyApp.dbRepository.gameRepository.gameIds.get(EventsFragmentData.gameChosen);
                            saveFile(gameName,gameId,uri); // do toasts
                        }
                    }
                }
        );
    }

    public void createChoseGameDropDownList()
    {
        ArrayAdapter<String>adapter = new ArrayAdapter<>(requireActivity(),
                                                         android.R.layout.simple_spinner_item,
                                                         MyApp.dbRepository.gameRepository.gameNames);

        adapter.setDropDownViewResource(R.layout.spinner_item);
        chooseGameDropDownList.setAdapter(adapter);
        chooseGameDropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l)
            {
                Executors.newSingleThreadExecutor().execute(() ->
                {
                    EventsFragmentData.gameChosen = position;
                    long gameId = MyApp.dbRepository.gameRepository.gameIds.get(EventsFragmentData.gameChosen);
                    listToShow = MyApp.dbRepository.eventInGameRepository.getEventsInGameWithBlockingUI(gameId);
                    new Handler(Looper.getMainLooper()).post(() -> showGameEvents());
                });
            }
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                EventsFragmentData.gameChosen = -1;
                listToShow = null;
                saveFileBtn.setVisibility(View.INVISIBLE);
                swipeToDeleteList.removeList();
            }
        });
    }

    public void showGameEvents()
    {
        if(listToShow.isEmpty())
        {
            saveFileBtn.setVisibility(View.INVISIBLE);
            swipeToDeleteList.removeList();
            showMsgToUser(getString(R.string.noEventsHaveBeenRecordedYet));
        }
        else
        {
            msgToUser.setVisibility(View.INVISIBLE);
            ArrayList<String> swipeToDeleteData = new ArrayList<>();
            for (EventInGame event : listToShow)
                swipeToDeleteData.add(event.toString());
            swipeToDeleteList.setNewList(swipeToDeleteData);
            saveFileBtn.setVisibility(View.VISIBLE);
        }
    }
}