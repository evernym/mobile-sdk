package me.connect.sdk.java.samplekt.connections

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import me.connect.sdk.java.samplekt.connections.ConnectionCreateResult.*
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.databinding.ConnectionsFragmentBinding


class ConnectionsFragment : Fragment() {
    private lateinit var binding: ConnectionsFragmentBinding
    private val model: ConnectionsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = ConnectionsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        val adapter = ConnectionsAdapter()
        model.getConnections().observe(viewLifecycleOwner, Observer<List<Connection>> { adapter.setData(it) })
        binding.connectionList.layoutManager = layoutManager
        binding.connectionList.adapter = adapter
        binding.buttonAddConnection.setOnClickListener {
            val invite: String = binding.editTextConnection.text.toString()
            performNewConnection(invite)
        }
        binding.buttonQr.setOnClickListener {
            IntentIntegrator
                    .forSupportFragment(this)
                    .setPrompt("Scan invitation QR code")
                    .setOrientationLocked(true)
                    .initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val invite = result.contents
                binding.editTextConnection.setText(invite)
                performNewConnection(invite)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun performNewConnection(invite: String) {
        binding.buttonAddConnection.isEnabled = false
        binding.buttonQr.isEnabled = false
        model.newConnection(invite).observeOnce(viewLifecycleOwner, Observer { status ->
            val msg = when (status) {
                SUCCESS -> "Connection created"
                REDIRECT -> "Connection redirected"
                FAILURE -> "Connection failed"
            }
            if (status != FAILURE) {
                binding.editTextConnection.text = null
            }
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
            binding.buttonAddConnection.isEnabled = true
            binding.buttonQr.isEnabled = true
        })
    }

    companion object {
        fun newInstance(): ConnectionsFragment = ConnectionsFragment()
    }
}