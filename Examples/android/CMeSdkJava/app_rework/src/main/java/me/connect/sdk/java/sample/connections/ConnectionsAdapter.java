package me.connect.sdk.java.sample.connections;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.db.entity.Connection;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionsViewHolder> {

    private List<Connection> data;

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
        Connection conn = data.get(position);
        holder.text.setText(conn.name);
        if (conn.icon != null) {
            Glide.with(holder.image.getContext()).load(conn.icon).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Connection> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    static class ConnectionsViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public ImageView image;

        public ConnectionsViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.text);
            image = v.findViewById(R.id.image);
        }
    }
}
