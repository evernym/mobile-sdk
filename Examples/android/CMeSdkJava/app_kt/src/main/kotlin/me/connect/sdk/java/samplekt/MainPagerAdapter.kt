package me.connect.sdk.java.samplekt

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.connect.sdk.java.samplekt.connections.ConnectionsFragment
import me.connect.sdk.java.samplekt.credentials.CredentialOffersFragment
import me.connect.sdk.java.samplekt.backups.BackupsFragment
import me.connect.sdk.java.samplekt.proofs.ProofRequestsFragment
import me.connect.sdk.java.samplekt.structmessages.StructuredMessagesFragment


class MainPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> ConnectionsFragment.newInstance()
        1 -> CredentialOffersFragment.newInstance()
        2 -> ProofRequestsFragment.newInstance()
        3 -> StructuredMessagesFragment.newInstance()
        4 -> BackupsFragment.newInstance()
        else -> ConnectionsFragment.newInstance()
    }


    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> "Connections"
        1 -> "Credentials Offers"
        2 -> "Proof requests"
        3 -> "Structured messages"
        4 -> "Wallet backups"
        else -> null
    }

    override fun getCount(): Int = 5
}
