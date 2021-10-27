package me.connect.sdk.java.sample.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import me.connect.sdk.java.sample.databinding.HomePageFragmentBinding;
import me.connect.sdk.java.sample.homepage.Results.*;

public class HomePageFragment extends Fragment {
    private HomePageFragmentBinding binding;
    private HomePageViewModel model;

    public static HomePageFragment newInstance() {
        return new HomePageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = HomePageFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.actionsList.setLayoutManager(layoutManager);
        HomePageAdapter adapter = new HomePageAdapter(new HomePageAdapter.ItemClickListener() {
            @Override
            public void onAcceptClick(int entryId) {
                accept(entryId);
            }

            @Override
            public void onRejectClick(int entryId) {
                reject(entryId);
            }

            @Override
            public void onAnswerClick(int entryId, JSONObject answer) {
                answer(entryId, answer);
            }
        });
        binding.actionsList.setAdapter(adapter);

        model = new ViewModelProvider(requireActivity()).get(HomePageViewModel.class);
        model.getActions().observe(getViewLifecycleOwner(), adapter::setData);

        binding.checkMessages.setOnClickListener(v -> {
            performCheckMessages();
        });

        binding.buttonQr.setOnClickListener(v -> {
            IntentIntegrator
                    .forSupportFragment(this)
                    .setPrompt("Scan invitation QR code")
                    .setOrientationLocked(true)
                    .initiateScan();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String invite = result.getContents();
                performNewConnection(invite);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void accept(int proofId) {
        model.accept(proofId).observeOnce(getViewLifecycleOwner(), ok -> {
            switch (ok) {
                case CONNECTION_SUCCESS:
                    Toast.makeText(getActivity(), "Connection created", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTION_REDIRECT:
                    Toast.makeText(getActivity(), "Connection reused", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTION_FAILURE:
                    Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
                    break;

                case OFFER_SUCCESS:
                    Toast.makeText(getActivity(), "Credential received", Toast.LENGTH_SHORT).show();
                    break;
                case OFFER_FAILURE:
                    Toast.makeText(getActivity(), "Credential issuance failed", Toast.LENGTH_SHORT).show();
                    break;

                case PROOF_SUCCESS:
                    Toast.makeText(getActivity(), "Proof shared", Toast.LENGTH_SHORT).show();
                    break;
                case PROOF_MISSED:
                    Toast.makeText(getActivity(), "Credentials missed", Toast.LENGTH_SHORT).show();
                    break;
                case PROOF_FAILURE:
                    Toast.makeText(getActivity(), "Failed to share proof", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void reject(int proofId) {
        model.reject(proofId).observeOnce(getViewLifecycleOwner(), ok -> {
            switch (ok) {
                case CONNECTION_SUCCESS:
                    Toast.makeText(getActivity(), "Connection rejected", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTION_FAILURE:
                    Toast.makeText(getActivity(), "Connection reject failure", Toast.LENGTH_SHORT).show();
                    break;

                case OFFER_SUCCESS:
                    Toast.makeText(getActivity(), "Offer request rejected", Toast.LENGTH_SHORT).show();
                    break;
                case OFFER_FAILURE:
                    Toast.makeText(getActivity(), "Offer request reject failure", Toast.LENGTH_SHORT).show();
                    break;

                case PROOF_SUCCESS:
                    Toast.makeText(getActivity(), "Proof request rejected", Toast.LENGTH_SHORT).show();
                    break;
                case PROOF_FAILURE:
                    Toast.makeText(getActivity(), "Proof request reject failure", Toast.LENGTH_SHORT).show();
                    break;

                case REJECT:
                    Toast.makeText(getActivity(), "Rejected", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE:
                    Toast.makeText(getActivity(), "FAILURE", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void performNewConnection(String invite) {
        binding.buttonQr.setEnabled(false);
        binding.checkMessages.setEnabled(false);
        model.newAction(invite).observeOnce(getViewLifecycleOwner(), status -> {
            switch (status) {
                case ACTION_SUCCESS:
                    Toast.makeText(getActivity(), "QR code is handled", Toast.LENGTH_SHORT).show();
                    break;
                case ACTION_FAILURE:
                    Toast.makeText(getActivity(), "QR code is handle failure", Toast.LENGTH_SHORT).show();
                    break;
            }
            binding.checkMessages.setEnabled(true);
            binding.buttonQr.setEnabled(true);
        });
    }

    private void performCheckMessages() {
        binding.buttonQr.setEnabled(false);
        binding.checkMessages.setEnabled(false);
        model.checkMessages().observeOnce(getViewLifecycleOwner(), status -> {
            binding.checkMessages.setEnabled(true);
            binding.buttonQr.setEnabled(true);
        });
    }

    private void answer(int entryId, JSONObject answer) {
        model.answerMessage(entryId, answer).observeOnce(getViewLifecycleOwner(), ok -> {
            Toast.makeText(getActivity(), "Struct message processed", Toast.LENGTH_SHORT).show();
        });
    }
}
