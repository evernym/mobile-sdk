package msdk.java.samplekt.backups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import msdk.java.samplekt.databinding.BackupsFragmentBinding


class BackupsFragment : Fragment() {
    private lateinit var binding: BackupsFragmentBinding
    private val model: BackupsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BackupsFragmentBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.getLastBackup().observeOnce(viewLifecycleOwner, Observer {
            val text = if (it != null) {
                "Last backup: $it"
            } else {
                "No backups yet"
            }
            binding.status.text = text
        })
        binding.buttonCreate.setOnClickListener {
            val backupKey = binding.backupKey.text.toString()
            model.performBackup(backupKey).observeOnce(viewLifecycleOwner, Observer { binding.status.text = it })
        }
        binding.buttonRestore.setOnClickListener {
            val backupKey = binding.backupKey.text.toString()
            model.performRestore(backupKey).observeOnce(viewLifecycleOwner, Observer { binding.status.text = it })
        }
    }

    companion object {
        fun newInstance(): BackupsFragment {
            return BackupsFragment()
        }
    }
}