package me.connect.sdk.java.samplekt.credentials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import me.connect.sdk.java.samplekt.credentials.CredentialOffersAdapter.ItemClickListener
import me.connect.sdk.java.samplekt.databinding.CredentialsFragmentBinding
import me.connect.sdk.java.samplekt.credentials.CredentialCreateResult.*

class CredentialOffersFragment : Fragment() {
    private lateinit var binding: CredentialsFragmentBinding
    private val model: CredentialOffersViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = CredentialsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        binding.credentialsList.layoutManager = layoutManager
        val listener = object : ItemClickListener {
            override fun onItemClick(credOfferId: Int) = accept(credOfferId)
        }
        val adapter = CredentialOffersAdapter(listener)
        binding.credentialsList.adapter = adapter
        model.getCredentialOffers().observe(viewLifecycleOwner, Observer { adapter.setData(it) })
        binding.buttonCheckOffers.setOnClickListener {
            binding.buttonCheckOffers.isEnabled = false
            model.getNewCredentialOffers().observeOnce(viewLifecycleOwner, Observer { binding.buttonCheckOffers.isEnabled = true })
        }
    }

    private fun accept(offerId: Int) {
        model.acceptOffer(offerId).observeOnce(
            viewLifecycleOwner,
            Observer<CredentialCreateResult> { ok: CredentialCreateResult? ->
                when (ok) {
                    SUCCESS -> Toast.makeText(
                        activity,
                        "Accept offer processed",
                        Toast.LENGTH_SHORT
                    ).show()
                    FAILURE -> Toast.makeText(
                        activity,
                        "Accept offer failure",
                        Toast.LENGTH_SHORT
                    ).show()
                    SUCCESS_CONNECTION -> Toast.makeText(
                        activity,
                        "Connection created",
                        Toast.LENGTH_SHORT
                    ).show()
                    FAILURE_CONNECTION -> Toast.makeText(
                        activity,
                        "Connection failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    companion object {
        fun newInstance(): CredentialOffersFragment = CredentialOffersFragment()
    }
}