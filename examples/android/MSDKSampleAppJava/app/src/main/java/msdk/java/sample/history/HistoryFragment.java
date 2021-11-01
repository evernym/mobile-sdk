package msdk.java.sample.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import msdk.java.sample.databinding.HistoryFragmentBinding;

public class HistoryFragment extends Fragment {

    private HistoryFragmentBinding binding;
    private HistoryViewModel model;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = HistoryFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.historyList.setLayoutManager(layoutManager);
        HistoryAdapter adapter = new HistoryAdapter();
        binding.historyList.setAdapter(adapter);

        model = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        model.getHistory().observe(getViewLifecycleOwner(), adapter::setData);
    }
}
