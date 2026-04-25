package com.storescanner.ui.sessions

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.storescanner.R
import com.storescanner.databinding.FragmentNewSessionBinding
import com.storescanner.viewmodel.ScannerViewModel
import java.text.SimpleDateFormat
import java.util.*

class NewSessionFragment : Fragment() {

    private var _binding: FragmentNewSessionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Pre-fill today's date
        binding.etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        binding.btnCreate.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etName.error = "Session name required"
                return@setOnClickListener
            }
            val location = binding.etLocation.text.toString().trim()
            val notes    = binding.etNotes.text.toString().trim()

            binding.btnCreate.isEnabled = false
            viewModel.createSession(name, location, notes) { session ->
                // Go directly to scanner for this session
                val args = Bundle().apply { putInt("sessionId", session.id) }
                findNavController().navigate(R.id.action_new_session_to_scanner, args)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
