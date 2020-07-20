package me.connect.sdk.java.sample.credentials;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;

public class CredentialOffersAdapter extends RecyclerView.Adapter<CredentialOffersAdapter.CredentialsViewHolder> {

    private List<CredentialOffer> data;
    private ItemClickListener itemClickListener;

    public CredentialOffersAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public CredentialsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.credentials_list_item, parent, false);
        return new CredentialsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CredentialsViewHolder holder, int position) {
        CredentialOffer credentialOffer = data.get(position);
        holder.text.setText(credentialOffer.name);
        holder.offers.setText(credentialOffer.attributes);
        int res;
        if (credentialOffer.accepted == null)
            res = R.drawable.question;
        else if (credentialOffer.accepted)
            res = R.drawable.yes;
        else
            res = R.drawable.no;
        holder.image.setImageResource(res);
        holder.accept.setVisibility(credentialOffer.accepted == null ? View.VISIBLE : View.GONE);
        holder.accept.setOnClickListener(view -> {
            holder.accept.setEnabled(false);
            itemClickListener.onItemClick(credentialOffer.id);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<CredentialOffer> credentials) {
        data = credentials;
        notifyDataSetChanged();
    }

    static class CredentialsViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public ImageView image;
        public TextView offers;
        public Button accept;

        public CredentialsViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.text);
            image = v.findViewById(R.id.image);
            offers = v.findViewById(R.id.offers);
            accept = v.findViewById(R.id.buttonaccept);
        }
    }

    public interface ItemClickListener {
        void onItemClick(int credOfferId);
    }
}
