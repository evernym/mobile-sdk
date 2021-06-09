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

import com.google.android.material.tabs.TabLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.databinding.ConnectionsFragmentBinding;

public class ConnectionsFragment extends Fragment {

    private ConnectionsFragmentBinding binding;
    private ConnectionsViewModel model;
    private TabLayout tabLayout;

    public static ConnectionsFragment newInstance() {
        return new ConnectionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ConnectionsFragmentBinding.inflate(inflater, container, false);

        View view = Objects.requireNonNull(getActivity()).findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tab_layout);

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

    public void selectActiveTab (Integer index) {
        tabLayout.selectTab(tabLayout.getTabAt(index));
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
            switch (status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), "Connection created", Toast.LENGTH_SHORT).show();
                    break;
                case REDIRECT:
                    Toast.makeText(getActivity(), "Connection redirected", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE:
                    Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_ATTACH:
                    selectActiveTab(1);
                    break;
                case PROOF_ATTACH:
                    selectActiveTab(2);
                    break;
            }
            if (status != ConnectionCreateResult.FAILURE) {
                binding.editTextConnection.setText(null);
            }
            binding.buttonAddConnection.setEnabled(true);
            binding.buttonQr.setEnabled(true);
        });
    }
}
