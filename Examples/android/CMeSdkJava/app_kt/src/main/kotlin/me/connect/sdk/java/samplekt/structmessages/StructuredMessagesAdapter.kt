package me.connect.sdk.java.samplekt.structmessages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import me.connect.sdk.java.samplekt.db.entity.StructuredMessage
import me.connect.sdk.java.samplekt.R


class StructuredMessagesAdapter(private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<StructuredMessagesAdapter.MessageViewHolder>() {
    private var data = mutableListOf<StructuredMessage>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.struct_messages_list_item, parent, false)
        return MessageViewHolder(v)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = data[position]
        holder.question.text = message.questionText
        holder.details.text = message.questionDetail
        holder.buttonHolder.removeAllViews()
        if (message.selectedAnswer != null) {
            holder.selectedAnswer.visibility = View.VISIBLE
            holder.selectedAnswer.text = "Selected answer: ${message.selectedAnswer}"
        } else {
            holder.selectedAnswer.visibility = View.GONE
            message.answers.forEach { response ->
                val btn = AppCompatButton(holder.itemView.context)
                btn.text = response.text
                holder.buttonHolder.addView(btn)
                btn.setOnClickListener { v ->
                    holder.buttonHolder.children.forEach { it.isEnabled = false }
                    itemClickListener.onAnswerClick(message.id, response.text)
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(messages: List<StructuredMessage>) {
        data.clear()
        data.addAll(messages)
        notifyDataSetChanged()
    }

    class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var question: TextView = v.findViewById(R.id.question)
        var details: TextView = v.findViewById(R.id.details)
        var buttonHolder: LinearLayout = v.findViewById(R.id.buttonHolder)
        var selectedAnswer: TextView = v.findViewById(R.id.answer)

    }

    interface ItemClickListener {
        fun onAnswerClick(entryId: Int, answer: String)
    }
}