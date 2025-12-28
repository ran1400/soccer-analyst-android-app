package ran.tmpTest.utils.lists;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ran.tmpTest.R;
import ran.tmpTest.fragments.EventsFragment;


public class SwipeToDeleteList extends RecyclerView.Adapter<SwipeToDeleteList.MyViewHolder>
{

    private List<String> list;
    private final EventsFragment eventsFragment;

    public SwipeToDeleteList(EventsFragment eventsFragment)
    {
        this.eventsFragment = eventsFragment;
        list = new ArrayList<String>();
    }

    public void removeItem(int position)
    {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void setNewList(List<String> list)
    {
        this.list = list;
        notifyDataSetChanged();
    }

    public void removeList()
    {
        list = new ArrayList<String>();
        notifyDataSetChanged();
    }

    public void insertItem(int position,String item)
    {
        list.add(position,item);
        notifyItemInserted(position);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swip_to_delete_list_item, parent, false);
        return new MyViewHolder(view,eventsFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
        holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView;

        public MyViewHolder(@NonNull View itemView,EventsFragment eventsFragment)
        {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(view ->
            {
                int position = getAdapterPosition();
                eventsFragment.userClickOnEvent(position);
            });
        }

    }

}
