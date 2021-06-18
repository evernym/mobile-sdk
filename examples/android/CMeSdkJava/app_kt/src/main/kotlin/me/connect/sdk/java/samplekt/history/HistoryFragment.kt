package me.connect.sdk.java.samplekt.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.connect.sdk.java.samplekt.databinding.HistoryFragmentBinding
import me.connect.sdk.java.samplekt.db.entity.Action

class HistoryFragment : Fragment() {
    private lateinit var binding: HistoryFragmentBinding
    private val model: HistoryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = HistoryFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        binding.historyList.layoutManager = layoutManager
        val adapter = HistoryAdapter()
        binding.historyList.adapter = adapter

        model.getHistory().observe(
            viewLifecycleOwner,
            Observer<List<Action>> { data: List<Action> ->
                adapter.setData(
                    data
                )
            })
    }

    companion object {
        fun newInstance(): HistoryFragment = HistoryFragment()
    }
}
