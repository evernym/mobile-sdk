package me.connect.sdk.java.sample.proofs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.db.entity.ProofRequest;

public class ProofRequestsAdapter extends RecyclerView.Adapter<ProofRequestsAdapter.ProofViewHolder> {
    private List<ProofRequest> data;
    private ItemClickListener itemClickListener;


    public ProofRequestsAdapter(ItemClickListener acceptListener) {
        this.itemClickListener = acceptListener;
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public ProofViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.proofs_list_item, parent, false);
        return new ProofViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProofViewHolder holder, int position) {
        ProofRequest proofRequest = data.get(position);
        int res;
        boolean acceptVisible = false;
        boolean rejectVisible = false;
        if (proofRequest.accepted == null) {
            res = R.drawable.question;
            acceptVisible = true;
            rejectVisible = true;
        } else if (proofRequest.accepted) {
            res = R.drawable.yes;
        } else {
            res = R.drawable.no;
        }
        holder.image.setImageResource(res);
        Glide.with(holder.logo.getContext()).load(proofRequest.attachConnectionLogo).into(holder.logo);
        holder.accept.setVisibility(acceptVisible ? View.VISIBLE : View.GONE);
        holder.reject.setVisibility(rejectVisible ? View.VISIBLE : View.GONE);
        holder.accept.setEnabled(true);
        holder.reject.setEnabled(true);
        holder.name.setText(proofRequest.name);
        holder.attributes.setText(proofRequest.attributes);
        holder.accept.setOnClickListener(v -> {
            holder.accept.setEnabled(false);
            holder.reject.setEnabled(false);
            itemClickListener.onAcceptClick(proofRequest.id);
        });
        holder.reject.setOnClickListener(v -> {
            holder.accept.setEnabled(false);
            holder.reject.setEnabled(false);
            itemClickListener.onRejectClick(proofRequest.id);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<ProofRequest> proofRequests) {
        data = proofRequests;
        notifyDataSetChanged();
    }

    static class ProofViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView attributes;
        public Button accept;
        public Button reject;
        public ImageView image;
        public ImageView logo;


        public ProofViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            attributes = v.findViewById(R.id.attributes);
            accept = v.findViewById(R.id.buttonAccept);
            reject = v.findViewById(R.id.buttonReject);
            image = v.findViewById(R.id.image);
            logo = v.findViewById(R.id.logo);
        }
    }

    public interface ItemClickListener {
        void onAcceptClick(int entryId);

        void onRejectClick(int entryId);
    }
}
