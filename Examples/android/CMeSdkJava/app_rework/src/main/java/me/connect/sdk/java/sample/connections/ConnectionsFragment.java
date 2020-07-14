package me.connect.sdk.java.sample.connections;

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

import java.util.ArrayList;

import me.connect.sdk.java.sample.databinding.ConnectionsFragmentBinding;

public class ConnectionsFragment extends Fragment {

    private ConnectionsFragmentBinding binding;

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

        ConnectionsViewModel model = new ViewModelProvider(requireActivity()).get(ConnectionsViewModel.class);
        model.getConnections().observe(getViewLifecycleOwner(), adapter::setData);

        binding.buttonAddConnection.setOnClickListener(v -> {
            binding.buttonAddConnection.setEnabled(false);
            String connString = binding.editTextConnection.getText().toString();

            model.newConnection(connString).observe(requireActivity(), status -> {
                binding.buttonAddConnection.setEnabled(true);
                Toast.makeText(requireContext(), "Connection processing: " + status, Toast.LENGTH_SHORT).show();
            });
        });
    }
}


