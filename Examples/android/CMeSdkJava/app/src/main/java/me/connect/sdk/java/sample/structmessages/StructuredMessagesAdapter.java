package me.connect.sdk.java.sample.structmessages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.connect.sdk.java.message.StructuredMessageHolder.Response;
import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.db.entity.StructuredMessage;

public class StructuredMessagesAdapter extends RecyclerView.Adapter<StructuredMessagesAdapter.MessageViewHolder> {
    private List<StructuredMessage> data;
    private ItemClickListener itemClickListener;

    public StructuredMessagesAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        this.data = new ArrayList<>();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.struct_messages_list_item, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        StructuredMessage message = data.get(position);
        holder.question.setText(message.questionText);
        holder.details.setText(message.questionDetail);
        holder.buttonHolder.removeAllViews();
        if (message.selectedAnswer != null) {
            holder.selectedAnswer.setVisibility(View.VISIBLE);
            holder.selectedAnswer.setText("Selected answer: " + message.selectedAnswer);
        } else {
            holder.selectedAnswer.setVisibility(View.GONE);
            for (Response response : message.answers) {
                Button btn = new AppCompatButton(holder.itemView.getContext());
                btn.setText(response.getText());
                holder.buttonHolder.addView(btn);
                btn.setOnClickListener(v -> {
                    for (int i = 0; i < holder.buttonHolder.getChildCount(); i++) {
                        View view = holder.buttonHolder.getChildAt(i);
                        view.setEnabled(false);
                    }
                    itemClickListener.onAnswerClick(message.id, response.getText());
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<StructuredMessage> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView question;
        public TextView details;
        public LinearLayout buttonHolder;
        public TextView selectedAnswer;

        public MessageViewHolder(View v) {
            super(v);
            question = v.findViewById(R.id.question);
            details = v.findViewById(R.id.details);
            buttonHolder = v.findViewById(R.id.buttonHolder);
            selectedAnswer = v.findViewById(R.id.answer);
        }
    }

    public interface ItemClickListener {
        void onAnswerClick(int entryId, String answer);
    }
}
