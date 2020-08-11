package me.connect.sdk.java.samplekt.proofs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.samplekt.R


class ProofRequestsAdapter(private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<ProofRequestsAdapter.ProofViewHolder>() {
    private val data = mutableListOf<ProofRequest>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProofViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.proofs_list_item, parent, false)
        return ProofViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProofViewHolder, position: Int) {
        val proofRequest = data[position]
        var acceptVisible = false
        var rejectVisible = false
        val res = when (proofRequest.accepted) {
            null -> {
                acceptVisible = true
                rejectVisible = true
                R.drawable.question
            }
            true -> R.drawable.yes
            false -> R.drawable.no

        }
        holder.image.setImageResource(res)
        holder.accept.visibility = if (acceptVisible) View.VISIBLE else View.GONE
        holder.reject.visibility = if (rejectVisible) View.VISIBLE else View.GONE
        holder.accept.isEnabled = true
        holder.reject.isEnabled = true
        holder.name.text = proofRequest.name
        holder.attributes.text = proofRequest.attributes
        holder.accept.setOnClickListener {
            holder.accept.isEnabled = false
            holder.reject.isEnabled = false
            itemClickListener.onAcceptClick(proofRequest.id)
        }
        holder.reject.setOnClickListener {
            holder.accept.isEnabled = false
            holder.reject.isEnabled = false
            itemClickListener.onRejectClick(proofRequest.id)
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(proofRequests: List<ProofRequest>) {
        data.clear()
        data.addAll(proofRequests)
        notifyDataSetChanged()
    }

    class ProofViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.name)
        var attributes: TextView = v.findViewById(R.id.attributes)
        var accept: Button = v.findViewById(R.id.buttonAccept)
        var reject: Button = v.findViewById(R.id.buttonReject)
        var image: ImageView = v.findViewById(R.id.image)

    }

    interface ItemClickListener {
        fun onAcceptClick(entryId: Int)
        fun onRejectClick(entryId: Int)
    }
}