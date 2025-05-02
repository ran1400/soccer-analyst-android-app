package ran.tmpTest.utils.lists;


import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ran.tmpTest.R;
import ran.tmpTest.sharedData.AppData;


public class SwipeToDeleteList extends RecyclerView.Adapter<SwipeToDeleteList.MyViewHolder>
{

    public List<String> listData;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swip_to_delete_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
        holder.textView.setText(listData.get(position));
        holder.position = position;
    }

    @Override
    public int getItemCount()
    {
        if (listData == null)
            return 0;
        return listData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView;
        int position;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    AppData.eventsFragment.userClickOnItem(position);
                }
            });
        }

    }

}
