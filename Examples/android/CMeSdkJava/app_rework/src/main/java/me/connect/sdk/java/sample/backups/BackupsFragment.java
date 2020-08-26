package me.connect.sdk.java.sample.backups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import me.connect.sdk.java.sample.databinding.BackupsFragmentBinding;

public class BackupsFragment extends Fragment {

    private BackupsFragmentBinding binding;
    private BackupsViewModel model;

    public static BackupsFragment newInstance() {
        return new BackupsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BackupsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(BackupsViewModel.class);

        model.getLastBackup().observeOnce(getViewLifecycleOwner(), res -> {
            String text;
            if (res != null) {
                text = "Last backup: " + res;
            } else {
                text = "No backups yet";
            }
            binding.status.setText(text);
        });

        binding.buttonCreate.setOnClickListener(v -> {
            model.performBackup().observeOnce(getViewLifecycleOwner(), res -> {
                binding.status.setText(res);
            });
        });

        binding.buttonRestore.setOnClickListener(v -> {
            model.performRestore().observeOnce(getViewLifecycleOwner(), res -> {
                binding.status.setText(res);
            });
        });
    }
}
