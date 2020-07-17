package me.connect.sdk.java.sample.credentials;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import me.connect.sdk.java.sample.databinding.CredentialsFragmentBinding;

public class CredentialOffersFragment extends Fragment {

    private CredentialsFragmentBinding binding;
    private CredentialOffersViewModel model;

    public static CredentialOffersFragment newInstance() {
        return new CredentialOffersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = CredentialsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.credentialsList.setLayoutManager(layoutManager);
        CredentialOffersAdapter adapter = new CredentialOffersAdapter(offerId -> {
            accept(offerId);
        });
        binding.credentialsList.setAdapter(adapter);


        model = new ViewModelProvider(requireActivity()).get(CredentialOffersViewModel.class);
        model.getCredentialOffers().observe(getViewLifecycleOwner(), adapter::setData);

        binding.buttonCheckOffers.setOnClickListener(v -> {
            binding.buttonCheckOffers.setEnabled(false);
            model.getNewCredentialOffers().observe(getViewLifecycleOwner(), ok -> {
                if (ok) {
                    binding.buttonCheckOffers.setEnabled(true);
                }
            });
        });
    }

    private void accept(int offerId) {
        model.acceptOffer(offerId); //todo UI updates
    }

}

