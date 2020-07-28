package me.connect.sdk.java.sample.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import me.connect.sdk.java.sample.connections.ConnectionsFragment;
import me.connect.sdk.java.sample.credentials.CredentialOffersFragment;
import me.connect.sdk.java.sample.proofs.ProofRequestsFragment;
import me.connect.sdk.java.sample.structmessages.StructuredMessagesFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {
    public MainPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ConnectionsFragment.newInstance();
            case 1:
                return CredentialOffersFragment.newInstance();
            case 2:
                return ProofRequestsFragment.newInstance();
            case 3:
                return StructuredMessagesFragment.newInstance();
            case 4:
                return WalletBackupsFragment.newInstance();
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
                return "Connections";
            case 1:
                return "Credentials Offers";
            case 2:
                return "Proof requests";
            case 3:
                return "Structured messages";
            case 4:
                return "Wallet backups";
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return 5;
    }
}
