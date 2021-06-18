package me.connect.sdk.java.sample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import me.connect.sdk.java.sample.backups.BackupsFragment;
import me.connect.sdk.java.sample.history.HistoryFragment;
import me.connect.sdk.java.sample.homepage.HomePageFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {
    public MainPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return HomePageFragment.newInstance();
            case 1:
                return HistoryFragment.newInstance();
            case 2:
                return BackupsFragment.newInstance();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        // todo should use resource strings
        switch (position) {
            case 0:
                return "Home";
            case 1:
                return "History";
            case 2:
                return "Wallet backups";
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return 3;
    }
}
