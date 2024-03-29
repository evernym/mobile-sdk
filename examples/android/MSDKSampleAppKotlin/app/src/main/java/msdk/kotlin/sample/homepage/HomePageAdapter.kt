package msdk.kotlin.sample.homepage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import msdk.kotlin.sample.R
import msdk.kotlin.sample.db.entity.Action
import org.json.JSONObject


class HomePageAdapter(private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<HomePageAdapter.HomePageViewHolder>() {
    private var data = mutableListOf<Action>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePageViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.action_item, parent, false)
        return HomePageViewHolder(v)
    }

    override fun onBindViewHolder(holder: HomePageViewHolder, position: Int) {
        val (id, name, _, description, details, icon, _, _, _, _, entryId, selectedAnswer, messageAnswers) = data[position]
        holder.name.text = name
        holder.description.text = description
        holder.details.text = details
        Glide.with(holder.image.context).load(icon)
            .into(holder.image)

        holder.buttonAccept.visibility = View.INVISIBLE
        holder.buttonReject.visibility = View.INVISIBLE
        holder.selectedAnswer.visibility = View.INVISIBLE
        holder.buttonHolder.visibility = View.INVISIBLE

        if (entryId == null && (messageAnswers == null || messageAnswers.isEmpty())) {
            holder.buttonHolder.removeAllViews()
            holder.buttonAccept.visibility = View.VISIBLE
            holder.buttonReject.visibility = View.VISIBLE
            holder.buttonAccept.isEnabled = true
            holder.buttonReject.isEnabled = true
            holder.buttonAccept.setOnClickListener { v ->
                holder.buttonAccept.isEnabled = false
                holder.buttonReject.isEnabled = false
                itemClickListener.onAcceptClick(id)
            }
            holder.buttonReject.setOnClickListener { v ->
                holder.buttonAccept.isEnabled = false
                holder.buttonReject.isEnabled = false
                itemClickListener.onRejectClick(id)
            }
        } else {
            holder.buttonHolder.removeAllViews()
            holder.buttonHolder.visibility = View.VISIBLE
            if (selectedAnswer != null) {
                holder.selectedAnswer.visibility = View.VISIBLE
                holder.selectedAnswer.text = "Selected answer: $selectedAnswer"
            } else {
                holder.selectedAnswer.visibility = View.GONE
                for (response in messageAnswers!!) {
                    val btn: Button = AppCompatButton(holder.itemView.context)
                    btn.text = response.text
                    holder.buttonHolder.addView(btn)
                    btn.setOnClickListener { v: View? ->
                        for (i in 0 until holder.buttonHolder.childCount) {
                            val view = holder.buttonHolder.getChildAt(i)
                            view.isEnabled = false
                        }
                        itemClickListener.onAnswerClick(id, response.response!!)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(action: List<Action>) {
        data.clear()
        data.addAll(action)
        notifyDataSetChanged()
    }

    class HomePageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.name)
        var description: TextView = v.findViewById(R.id.description)
        var details: TextView = v.findViewById(R.id.details)
        var image: ImageView = v.findViewById(R.id.image)
        var buttonAccept: Button = v.findViewById(R.id.buttonAccept)
        var buttonReject: Button = v.findViewById(R.id.buttonReject)
        var buttonHolder: LinearLayout = v.findViewById(R.id.buttonHolder)
        var selectedAnswer: TextView = v.findViewById(R.id.answer)
    }

    interface ItemClickListener {
        fun onAcceptClick(entryId: Int);

        fun onRejectClick(entryId: Int)

        fun onAnswerClick(entryId: Int, answer: JSONObject)
    }
}
