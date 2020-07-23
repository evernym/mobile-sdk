package me.connect.sdk.java.sample.connections;

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

import me.connect.sdk.java.sample.databinding.ConnectionsFragmentBinding;

public class ConnectionsFragment extends Fragment {

    private ConnectionsFragmentBinding binding;
    private ConnectionsViewModel model;

    public static ConnectionsFragment newInstance() {
        return new ConnectionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ConnectionsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.connectionList.setLayoutManager(layoutManager);
        ConnectionsAdapter adapter = new ConnectionsAdapter();
        binding.connectionList.setAdapter(adapter);

        model = new ViewModelProvider(requireActivity()).get(ConnectionsViewModel.class);
        model.getConnections().observe(getViewLifecycleOwner(), adapter::setData);

        binding.buttonAddConnection.setOnClickListener(v -> {
            String invite = binding.editTextConnection.getText().toString();
            performNewConnection(invite);
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
                binding.editTextConnection.setText(invite);
                performNewConnection(invite);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void performNewConnection(String invite) {
        binding.buttonAddConnection.setEnabled(false);
        binding.buttonQr.setEnabled(false);
        model.newConnection(invite).observeOnce(getViewLifecycleOwner(), status -> {
            Toast.makeText(getActivity(), "New connection processed", Toast.LENGTH_SHORT).show();
            if (status) {
                binding.editTextConnection.setText(null);
            }
            binding.buttonAddConnection.setEnabled(true);
            binding.buttonQr.setEnabled(true);
        });
    }
}
