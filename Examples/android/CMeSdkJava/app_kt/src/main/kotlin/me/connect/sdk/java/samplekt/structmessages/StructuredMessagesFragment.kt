package me.connect.sdk.java.samplekt.structmessages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import me.connect.sdk.java.samplekt.structmessages.StructuredMessagesAdapter.ItemClickListener
import me.connect.sdk.java.samplekt.databinding.StructMessagesFragmentBinding


class StructuredMessagesFragment : Fragment() {
    private lateinit var binding: StructMessagesFragmentBinding
    private val model: StructuredMessagesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = StructMessagesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        binding.messagesList.layoutManager = layoutManager
        val listener = object : ItemClickListener {
            override fun onAnswerClick(entryId: Int, answer: String) = answer(entryId, answer)
        }
        val adapter = StructuredMessagesAdapter(listener)
        binding.messagesList.adapter = adapter
        model.getStructuredMessages().observe(viewLifecycleOwner, Observer { adapter.setData(it) })
        binding.buttonCheckMessages.setOnClickListener {
            binding.buttonCheckMessages.isEnabled = false
            model.getNewStructuredMessages().observeOnce(viewLifecycleOwner, Observer { binding.buttonCheckMessages.isEnabled = true })
        }
    }

    private fun answer(entryId: Int, nonce: String) {
        model.answerMessage(entryId, nonce).observeOnce(viewLifecycleOwner,
                Observer { Toast.makeText(activity, "Struct message processed", Toast.LENGTH_SHORT).show() })
    }

    companion object {
        fun newInstance(): StructuredMessagesFragment = StructuredMessagesFragment()
    }
}
