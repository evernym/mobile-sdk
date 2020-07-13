package me.connect.sdk.java.sample.connections;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.db.entity.Connection;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionsViewHolder> {

    private List<Connection> data; //todo implement

    public ConnectionsAdapter() {
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public ConnectionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.connection_list_item, parent, false);
        return new ConnectionsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionsViewHolder holder, int position) {
        holder.textView.setText(data.get(position).name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ConnectionsViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ConnectionsViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.textView);
        }
    }

    public void setData(List<Connection> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
}
