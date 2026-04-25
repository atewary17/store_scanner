package com.storescanner.ui.dashboard

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.storescanner.R
import com.storescanner.databinding.FragmentDashboardBinding
import com.storescanner.viewmodel.ScannerViewModel
import com.storescanner.viewmodel.UiState

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnNewScan.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_new_session)
        }
        binding.btnProducts.setOnClickListener {
            Toast.makeText(requireContext(), "Products catalogue coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.btnSessions.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_sessions)
        }
        binding.btnExport.setOnClickListener {
            Toast.makeText(requireContext(), "Export feature — open in browser", Toast.LENGTH_SHORT).show()
        }

        viewModel.dashboard.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val d = state.data
                    binding.tvTotalProducts.text = d.totals.products.toString()
                    binding.tvTotalScans.text = d.totals.scans.toString()
                    binding.tvScannedToday.text = d.totals.scansToday.toString()
                    binding.tvStoreVisits.text = d.totals.sessions.toString()
                    binding.tvSourceApi.text = d.bySource["upcitemdb"]?.toString() ?: "0"
                    binding.tvSourceOcr.text = d.bySource["google_vision"]?.toString() ?: "0"
                    binding.tvSourceManual.text = d.bySource["manual"]?.toString() ?: "0"
                }
                is UiState.Error -> { /* silent fail — stats stay at — */ }
                else -> {}
            }
        }

        viewModel.loadDashboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
