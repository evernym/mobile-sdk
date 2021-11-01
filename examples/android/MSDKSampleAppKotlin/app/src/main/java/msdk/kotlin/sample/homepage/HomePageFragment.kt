package msdk.kotlin.sample.homepage

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
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import msdk.kotlin.sample.db.entity.Action
import msdk.kotlin.sample.homepage.Results.*
import msdk.kotlin.sample.databinding.HomePageFragmentBinding
import org.json.JSONObject

class HomePageFragment: Fragment() {
    private lateinit var binding: HomePageFragmentBinding
    private val model: HomePageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomePageFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        binding.actionsList.layoutManager = layoutManager
        val listener = object : HomePageAdapter.ItemClickListener {
            override fun onAcceptClick(entryId: Int) = accept(entryId)

            override fun onRejectClick(entryId: Int) = reject(entryId)

            override fun onAnswerClick(entryId: Int, answer: JSONObject) = answer(entryId, answer)
        }
        val adapter = HomePageAdapter(listener)
        binding.actionsList.adapter = adapter
        binding.actionsList.adapter = adapter

        model.getActions().observe(
            viewLifecycleOwner,
            Observer<List<Action>> { data: List<Action> ->
                adapter.setData(
                    data
                )
            })

        binding.checkMessages.setOnClickListener { performCheckMessages() }

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
                performNewConnection(invite)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun accept(proofId: Int) {
        model.accept(proofId).observeOnce(
            viewLifecycleOwner,
            Observer { ok: Results ->
                when (ok) {
                    CONNECTION_SUCCESS -> Toast.makeText(activity, "Connection created", Toast.LENGTH_SHORT).show()
                    CONNECTION_REDIRECT -> Toast.makeText(activity, "Connection reused", Toast.LENGTH_SHORT).show()
                    CONNECTION_FAILURE -> Toast.makeText(activity, "Connection failed", Toast.LENGTH_SHORT).show()
                    OFFER_SUCCESS -> Toast.makeText(activity, "Credential received", Toast.LENGTH_SHORT).show()
                    OFFER_FAILURE -> Toast.makeText(activity, "Credential issuance failed", Toast.LENGTH_SHORT).show()
                    PROOF_SUCCESS -> Toast.makeText(activity, "Proof shared", Toast.LENGTH_SHORT).show()
                    PROOF_MISSED -> Toast.makeText(activity, "Credentials missed", Toast.LENGTH_SHORT).show()
                    PROOF_FAILURE -> Toast.makeText(activity, "Failed to share proof", Toast.LENGTH_SHORT).show()
                    else -> return@Observer
                }
            })
    }

    private fun reject(proofId: Int) {
        model.reject(proofId).observeOnce(
            viewLifecycleOwner,
            Observer { ok: Results ->
                when (ok) {
                    CONNECTION_SUCCESS -> Toast.makeText(activity, "Connection rejected", Toast.LENGTH_SHORT).show()
                    CONNECTION_FAILURE -> Toast.makeText(activity, "Connection reject failure", Toast.LENGTH_SHORT).show()
                    OFFER_SUCCESS -> Toast.makeText(activity, "Offer request rejected", Toast.LENGTH_SHORT).show()
                    OFFER_FAILURE -> Toast.makeText(activity, "Offer request reject failure", Toast.LENGTH_SHORT).show()
                    PROOF_SUCCESS -> Toast.makeText(activity, "Proof request rejected", Toast.LENGTH_SHORT).show()
                    PROOF_FAILURE -> Toast.makeText(activity, "Proof request reject failure", Toast.LENGTH_SHORT).show()
                    REJECT -> Toast.makeText(activity, "Rejected", Toast.LENGTH_SHORT).show()
                    FAILURE -> Toast.makeText(activity, "FAILURE", Toast.LENGTH_SHORT).show()
                    else -> return@Observer
                }
            })
    }

    private fun performNewConnection(invite: String) {
        binding.buttonQr.isEnabled = false
        binding.checkMessages.isEnabled = false
        model.newAction(invite).observeOnce(
            viewLifecycleOwner,
            Observer { status: Results ->
                when (status) {
                    ACTION_SUCCESS -> Toast.makeText(activity, "QR code is handled", Toast.LENGTH_SHORT).show()
                    ACTION_FAILURE -> Toast.makeText(activity, "QR code is handle failure", Toast.LENGTH_SHORT).show()
                    else -> return@Observer
                }
                binding.checkMessages.isEnabled = true
                binding.buttonQr.isEnabled = true
            })
    }

    private fun performCheckMessages() {
        binding.buttonQr.isEnabled = false
        binding.checkMessages.isEnabled = false
        model.checkMessages().observeOnce(
            viewLifecycleOwner,
            Observer {
                binding.checkMessages.isEnabled = true
                binding.buttonQr.isEnabled = true
            })
    }

    private fun answer(entryId: Int, answer: JSONObject) {
        model.answerMessage(entryId, answer).observeOnce(
            viewLifecycleOwner,
            Observer {
                Toast.makeText(
                    activity,
                    "Struct message processed",
                    Toast.LENGTH_SHORT
                ).show()
            })
    }

    companion object {
        fun newInstance(): HomePageFragment = HomePageFragment()
    }
}