package me.connect.sdk.java.sample.structmessages;

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

import me.connect.sdk.java.sample.R;
import me.connect.sdk.java.sample.databinding.StructMessagesFragmentBinding;

public class StructuredMessagesFragment extends Fragment {

    private StructMessagesFragmentBinding binding;
    private StructuredMessagesViewModel model;

    public static StructuredMessagesFragment newInstance() {
        return new StructuredMessagesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = StructMessagesFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.messagesList.setLayoutManager(layoutManager);
        StructuredMessagesAdapter adapter = new StructuredMessagesAdapter((entryId, nonce) -> {
            answer(entryId, nonce);
        });
        binding.messagesList.setAdapter(adapter);

        model = new ViewModelProvider(requireActivity()).get(StructuredMessagesViewModel.class);
        model.getStructuredMessages().observe(getViewLifecycleOwner(), adapter::setData);

        binding.buttonCheckMessages.setOnClickListener(v -> {
            binding.buttonCheckMessages.setEnabled(false);
            model.getNewStructuredMessages().observeOnce(getViewLifecycleOwner(), ok -> {
                binding.buttonCheckMessages.setEnabled(true);
            });
        });
    }

    private void answer(int entryId, String nonce) {
        model.answerMessage(entryId, nonce).observeOnce(getViewLifecycleOwner(), ok -> {
            Toast.makeText(getActivity(), "Struct message processed", Toast.LENGTH_SHORT).show();
        });
    }

}
