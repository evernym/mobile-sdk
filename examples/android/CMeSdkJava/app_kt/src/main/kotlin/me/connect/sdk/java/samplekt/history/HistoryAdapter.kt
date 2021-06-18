package me.connect.sdk.java.samplekt.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.connect.sdk.java.samplekt.R
import me.connect.sdk.java.samplekt.db.entity.Action

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private var data = mutableListOf<Action>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val action: Action = data[position]
        holder.name.text = action.name
        holder.description.text = action.description
        Glide.with(holder.image.context).load(action.icon).into(holder.image)
    }

    override fun getItemCount(): Int = data.size

    fun setData(action: List<Action>) {
        data.clear()
        data.addAll(action)
        notifyDataSetChanged()
    }

    class HistoryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.name)
        var description: TextView = v.findViewById(R.id.description)
        var image: ImageView = v.findViewById(R.id.image)
    }
}