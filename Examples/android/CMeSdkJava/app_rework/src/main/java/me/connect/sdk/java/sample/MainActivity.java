package me.connect.sdk.java.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import me.connect.sdk.java.sample.databinding.MainActivityBinding;
import me.connect.sdk.java.sample.ui.main.MainPagerAdapter;
import me.connect.sdk.java.sample.ui.main.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    private MainActivityBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewBinding.pager.setAdapter(adapter);
        viewBinding.tabLayout.setupWithViewPager(viewBinding.pager);
    }
}
