package me.connect.sdk.java.samplekt.proofs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import me.connect.sdk.java.samplekt.proofs.ProofCreateResult.*
import me.connect.sdk.java.samplekt.databinding.ProofsFragmentBinding


class ProofRequestsFragment : Fragment() {
    private lateinit var binding: ProofsFragmentBinding
    private val model: ProofRequestsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ProofsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        binding.requestsList.layoutManager = layoutManager
        val listener = object : ProofRequestsAdapter.ItemClickListener {
            override fun onAcceptClick(entryId: Int) = accept(entryId)

            override fun onRejectClick(entryId: Int) = reject(entryId)
        }
        val adapter = ProofRequestsAdapter(listener)
        binding.requestsList.adapter = adapter
        model.getProofRequests().observe(viewLifecycleOwner, Observer { adapter.setData(it) })
        binding.buttonCheckRequests.setOnClickListener {
            binding.buttonCheckRequests.isEnabled = false
            model.getNewProofRequests().observeOnce(viewLifecycleOwner, Observer { binding.buttonCheckRequests.isEnabled = true })
        }
    }

    private fun accept(proofId: Int) {
        model.acceptProofRequest(proofId).observeOnce(viewLifecycleOwner, Observer { status ->
            when (status) {
                SUCCESS -> Toast.makeText(activity, "Proof request accepted", Toast.LENGTH_SHORT).show()
                FAILURE -> Toast.makeText(activity, "Proof request accept failure", Toast.LENGTH_SHORT).show()
                SUCCESS_CONNECTION -> Toast.makeText(activity, "Connection created", Toast.LENGTH_SHORT).show()
                FAILURE_CONNECTION -> Toast.makeText(activity, "Connection failed", Toast.LENGTH_SHORT).show()
                MISSED -> Toast.makeText(activity, "Credentials missed", Toast.LENGTH_SHORT).show()
                else -> return@Observer
            }
        })
    }

    private fun reject(proofId: Int) {
        model.rejectProofRequest(proofId).observeOnce(viewLifecycleOwner,
                Observer { Toast.makeText(activity, "Proof request rejected", Toast.LENGTH_SHORT).show() })
    }

    companion object {
        fun newInstance(): ProofRequestsFragment = ProofRequestsFragment()
    }
}
