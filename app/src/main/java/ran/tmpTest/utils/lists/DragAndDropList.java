package ran.tmpTest.utils.lists;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ran.tmpTest.R;
import ran.tmpTest.sharedData.AppData;

import java.util.List;

public class DragAndDropList extends RecyclerView.Adapter<DragAndDropList.ViewHolder>
{

    List<String> listData;

    public DragAndDropList(List<String> listData)
    {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.drag_and_drop_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.textView.setText(listData.get(position));
        if (AppData.SettingFragmentData.listChoosePosition == position)
            holder.textView.setTextColor(Color.RED);
        else
            holder.textView.setTextColor(Color.BLACK);
    }

    @Override
    public int getItemCount()
    {
        return listData.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
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
            if (AppData.SettingFragmentData.listChoosePosition == -1) // none choose item from the list before click
            {
                AppData.SettingFragmentData.listChoosePosition = getAdapterPosition();
                notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
                AppData.settingFragment.changeToUserChoseItemMode();
            }
            else if (AppData.SettingFragmentData.listChoosePosition == getAdapterPosition()) // the item has clicked was choose already
            {
                AppData.settingFragment.changeToNoneChoseItemMode();
                notifyItemChanged(getAdapterPosition());
            }
            else
            {
                int prevChoosePosition = AppData.SettingFragmentData.listChoosePosition;
                AppData.SettingFragmentData.listChoosePosition = getAdapterPosition();
                notifyItemChanged(prevChoosePosition);
                notifyItemChanged(AppData.SettingFragmentData.listChoosePosition);
                AppData.settingFragment.updateEventOrGameEditText();
            }
        }

    }
}

