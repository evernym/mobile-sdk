package msdk.java.sample.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import msdk.java.sample.R;
import msdk.java.sample.db.entity.Action;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<Action> data;

    public HistoryAdapter() {
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public HistoryAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new HistoryAdapter.HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        Action action = data.get(position);
        holder.name.setText(action.name);
        holder.description.setText(action.description);
        Glide.with(holder.image.getContext()).load(action.icon).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Action> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView description;
        public ImageView image;

        public HistoryViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            description = v.findViewById(R.id.description);
            image = v.findViewById(R.id.image);
        }
    }
}
