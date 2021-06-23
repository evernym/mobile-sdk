package me.connect.sdk.java.sample.homepage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.connect.sdk.java.message.StructuredMessageHolder;
import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.db.entity.Action;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.HomePageViewHolder> {
    private List<Action> data;
    private HomePageAdapter.ItemClickListener itemClickListener;

    public HomePageAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public HomePageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.action_item, parent, false);
        return new HomePageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HomePageAdapter.HomePageViewHolder holder, int position) {
        Action action = data.get(position);
        holder.name.setText(action.name);
        holder.description.setText(action.description);
        holder.details.setText(action.details);
        Glide.with(holder.image.getContext()).load(action.icon).into(holder.image);

        holder.buttonAccept.setVisibility(View.INVISIBLE);
        holder.buttonReject.setVisibility(View.INVISIBLE);
        holder.selectedAnswer.setVisibility(View.INVISIBLE);
        holder.buttonHolder.setVisibility(View.INVISIBLE);

        if (action.entryId == null && action.messageAnswers == null) {
            holder.buttonHolder.removeAllViews();
            holder.buttonAccept.setVisibility(View.VISIBLE);
            holder.buttonReject.setVisibility(View.VISIBLE);
            holder.buttonAccept.setEnabled(true);
            holder.buttonReject.setEnabled(true);
            holder.buttonAccept.setOnClickListener(v -> {
                holder.buttonAccept.setEnabled(false);
                holder.buttonReject.setEnabled(false);
                itemClickListener.onAcceptClick(action.id);
            });
            holder.buttonReject.setOnClickListener(v -> {
                holder.buttonAccept.setEnabled(false);
                holder.buttonReject.setEnabled(false);
                itemClickListener.onRejectClick(action.id);
            });
        } else {
            holder.buttonHolder.removeAllViews();
            holder.buttonHolder.setVisibility(View.VISIBLE);
            if (action.selectedAnswer != null) {
                holder.selectedAnswer.setVisibility(View.VISIBLE);
                holder.selectedAnswer.setText("Selected answer: " + action.selectedAnswer);
            } else {
                holder.selectedAnswer.setVisibility(View.GONE);
                for (StructuredMessageHolder.Response response : action.messageAnswers) {
                    Button btn = new AppCompatButton(holder.itemView.getContext());
                    btn.setText(response.getText());
                    holder.buttonHolder.addView(btn);
                    btn.setOnClickListener(v -> {
                        for (int i = 0; i < holder.buttonHolder.getChildCount(); i++) {
                            View view = holder.buttonHolder.getChildAt(i);
                            view.setEnabled(false);
                        }
                        itemClickListener.onAnswerClick(action.id, response.getResponse());
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Action> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    static class HomePageViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView description;
        public TextView details;
        public ImageView image;
        public Button buttonAccept;
        public Button buttonReject;
        public LinearLayout buttonHolder;
        public TextView selectedAnswer;

        public HomePageViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            description = v.findViewById(R.id.description);
            details = v.findViewById(R.id.details);
            image = v.findViewById(R.id.image);
            buttonAccept = v.findViewById(R.id.buttonAccept);
            buttonReject = v.findViewById(R.id.buttonReject);
            buttonHolder = v.findViewById(R.id.buttonHolder);
            selectedAnswer = v.findViewById(R.id.answer);
        }
    }

    public interface ItemClickListener {
        void onAcceptClick(int entryId);

        void onRejectClick(int entryId);

        void onAnswerClick(int entryId, JSONObject answer);
    }
}
