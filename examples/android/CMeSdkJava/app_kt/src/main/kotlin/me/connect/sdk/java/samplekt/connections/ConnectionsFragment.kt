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
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import me.connect.sdk.java.samplekt.R
import me.connect.sdk.java.samplekt.connections.ConnectionCreateResult.*
import me.connect.sdk.java.samplekt.databinding.ConnectionsFragmentBinding
import me.connect.sdk.java.samplekt.db.entity.Connection


class ConnectionsFragment : Fragment() {
    private lateinit var binding: ConnectionsFragmentBinding
    private val model: ConnectionsViewModel by viewModels()
    private var tabLayout: TabLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = ConnectionsFragmentBinding.inflate(inflater, container, false)
        val view = activity?.findViewById<View>(R.id.pager)
        tabLayout = view?.findViewById(R.id.tab_layout)
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

    private fun selectActiveTab(index: Int?) {
        tabLayout!!.selectTab(tabLayout!!.getTabAt(index!!))
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
            when (status) {
                SUCCESS -> Toast.makeText(activity, "Connection created", Toast.LENGTH_SHORT).show()
                REDIRECT -> Toast.makeText(activity, "Connection redirected", Toast.LENGTH_SHORT).show()
                FAILURE -> Toast.makeText(activity, "Connection failed", Toast.LENGTH_SHORT).show()
                REQUEST_ATTACH -> selectActiveTab(1)
                PROOF_ATTACH -> selectActiveTab(2)
            }
            if (status != FAILURE) {
                binding.editTextConnection.text = null
            }
            binding.buttonAddConnection.isEnabled = true
            binding.buttonQr.isEnabled = true
        })
    }

    companion object {
        fun newInstance(): ConnectionsFragment = ConnectionsFragment()
    }
}