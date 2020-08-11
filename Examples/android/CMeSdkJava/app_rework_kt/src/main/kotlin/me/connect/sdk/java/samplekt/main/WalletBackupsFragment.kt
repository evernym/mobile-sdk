package me.connect.sdk.java.samplekt.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.connect.sdk.java.samplekt.R


class WalletBackupsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.temp_fragment, container, false)
    }

    companion object {
        fun newInstance(): WalletBackupsFragment = WalletBackupsFragment()
    }
}