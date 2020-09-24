package me.connect.sdk.java.samplekt.connections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.R

class ConnectionsAdapter : RecyclerView.Adapter<ConnectionsAdapter.ConnectionsViewHolder>() {
    private var data = mutableListOf<Connection>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.connection_list_item, parent, false)
        return ConnectionsViewHolder(v)
    }

    override fun onBindViewHolder(holder: ConnectionsViewHolder, position: Int) {
        val conn = data[position]
        holder.text.text = conn.name
        if (conn.icon != null) {
            Glide.with(holder.image.context).load(conn.icon).into(holder.image)
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(connections: List<Connection>) {
        data.clear()
        data.addAll(connections)
        notifyDataSetChanged()
    }

    class ConnectionsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var text: TextView = v.findViewById(R.id.text)
        var image: ImageView = v.findViewById(R.id.image)
    }

}