package com.storescanner.ui.sessions

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.storescanner.R
import com.storescanner.data.models.ScanSession
import com.storescanner.databinding.FragmentSessionListBinding
import com.storescanner.databinding.ItemSessionBinding
import com.storescanner.viewmodel.ScannerViewModel
import com.storescanner.viewmodel.UiState

class SessionListFragment : Fragment() {

    private var _binding: FragmentSessionListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSessionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SessionAdapter { session ->
            val args = Bundle().apply { putInt("sessionId", session.id) }
            findNavController().navigate(R.id.action_sessions_to_scanner, args)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnNewSession.setOnClickListener {
            findNavController().navigate(R.id.action_sessions_to_new_session)
        }

        viewModel.sessions.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.data
                    binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvSessionCount.text = "${list.size} visit${if (list.size == 1) "" else "s"}"
                    adapter.submitList(list)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.loadSessions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SessionAdapter(
    private val onScan: (ScanSession) -> Unit
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    private val items = mutableListOf<ScanSession>()

    fun submitList(list: List<ScanSession>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemSessionBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = items[position]
        holder.binding.apply {
            tvName.text = session.name
            val date = session.scannedOn ?: session.createdAt?.take(10) ?: ""
            val location = session.location ?: ""
            tvLocationDate.text = buildString {
                if (location.isNotEmpty()) append(location)
                if (location.isNotEmpty() && date.isNotEmpty()) append(" · ")
                if (date.isNotEmpty()) append(date)
            }
            tvItemCount.text = "${session.itemCount} items"
            btnScan.setOnClickListener { onScan(session) }
            root.setOnClickListener { onScan(session) }
        }
    }
}
