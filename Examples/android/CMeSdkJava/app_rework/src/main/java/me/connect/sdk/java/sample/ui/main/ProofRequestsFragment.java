package me.connect.sdk.java.sample.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.connect.sdk.java.sample.R;

public class ProofRequestsFragment extends Fragment {

    public static ProofRequestsFragment newInstance() {
        return new ProofRequestsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.temp_fragment, container, false);
    }
}

