package me.connect.sdk.java.samplekt.credentials

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.connect.sdk.java.samplekt.R
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer


class CredentialOffersAdapter(private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<CredentialOffersAdapter.CredentialsViewHolder>() {
    private val data = mutableListOf<CredentialOffer>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialsViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.credentials_list_item, parent, false)
        return CredentialsViewHolder(v)
    }

    override fun onBindViewHolder(holder: CredentialsViewHolder, position: Int) {
        val credentialOffer= data[position]
        holder.text.text = credentialOffer.name
        holder.offers.text = credentialOffer.attributes
        val res: Int
        holder.accept.isEnabled = true
        res = when (credentialOffer.accepted) {
            null -> R.drawable.question
            true -> R.drawable.yes
            else -> R.drawable.no
        }
        holder.image.setImageResource(res)
        Glide.with(holder.logo.context).load(credentialOffer.attachConnectionLogo).into(holder.logo)
        holder.accept.visibility = if (credentialOffer.accepted == null) View.VISIBLE else View.GONE
        holder.accept.setOnClickListener {
            holder.accept.isEnabled = false
            itemClickListener.onItemClick(credentialOffer.id)
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(credentials: List<CredentialOffer>) {
        data.clear()
        data.addAll(credentials)
        notifyDataSetChanged()
    }

    class CredentialsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var text: TextView = v.findViewById(R.id.text)
        var image: ImageView = v.findViewById(R.id.image)
        var logo: ImageView = v.findViewById(R.id.logo)
        var offers: TextView = v.findViewById(R.id.offers)
        var accept: Button = v.findViewById(R.id.buttonaccept)
    }

    interface ItemClickListener {
        fun onItemClick(credOfferId: Int)
    }
}
