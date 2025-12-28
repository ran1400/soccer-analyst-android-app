package ran.tmpTest.utils.lists;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ran.tmpTest.R;
import ran.tmpTest.fragments.SettingFragment;


import java.util.List;

public class DragAndDropList extends RecyclerView.Adapter<DragAndDropList.ViewHolder>
{

    List<String> listData;
    SettingFragment settingFragment;

    public DragAndDropList(List<String> listData, SettingFragment settingFragment)
    {
        this.listData = listData;
        this.settingFragment = settingFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.drag_and_drop_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.textView.setText(listData.get(position));
        if (settingFragment.listChoosePosition == position)
            holder.textView.setTextColor(Color.RED);
        else
            holder.textView.setTextColor(Color.BLACK);
    }

    @Override
    public int getItemCount()
    {
        return listData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView textView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (settingFragment.listChoosePosition == -1) // none choose item from the list before click
            {
                settingFragment.listChoosePosition = getAdapterPosition();
                notifyItemChanged(settingFragment.listChoosePosition);
                settingFragment.changeToUserChoseItemMode();
            }
            else if (settingFragment.listChoosePosition == getAdapterPosition()) // the item has clicked was choose already
            {
                settingFragment.changeToNoneChoseItemMode();
                notifyItemChanged(getAdapterPosition());
            }
            else
            {
                int prevChoosePosition = settingFragment.listChoosePosition;
                settingFragment.listChoosePosition = getAdapterPosition();
                notifyItemChanged(prevChoosePosition);
                notifyItemChanged(settingFragment.listChoosePosition);
                settingFragment.updateEventOrGameEditText();
            }
        }

    }
}

