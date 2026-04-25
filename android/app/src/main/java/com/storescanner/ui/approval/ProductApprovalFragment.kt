package com.storescanner.ui.approval

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.storescanner.data.models.ProductSaveRequest
import com.storescanner.databinding.FragmentProductApprovalBinding
import com.storescanner.viewmodel.ScannerViewModel
import com.storescanner.viewmodel.UiState

class ProductApprovalFragment : Fragment() {

    private var _binding: FragmentProductApprovalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()

    private val categories = listOf("Paint", "Hardware", "Cement", "Electrical", "Plumbing", "Tools", "Adhesive", "Other")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductApprovalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        arguments?.let { args ->
            binding.etBarcode.setText(args.getString("barcode", ""))
            binding.etName.setText(args.getString("name", ""))
            binding.etBrand.setText(args.getString("brand", ""))
            binding.etDescription.setText(args.getString("description", ""))
            binding.etUnit.setText(args.getString("unit", ""))

            val cat = args.getString("category", "")
            val catIndex = categories.indexOfFirst { it.equals(cat, ignoreCase = true) }
            if (catIndex >= 0) binding.spinnerCategory.setSelection(catIndex)

            val source = args.getString("source", "manual")
            binding.tvSource.text = "Source: $source"

            // Show OCR prompt when product wasn't found in any database
            if (args.getBoolean("needsOcr", false)) {
                binding.ocrTrigger.visibility = View.VISIBLE
            }
        }

        binding.btnOcr.setOnClickListener {
            // Signal back to scanner to capture OCR photo
            findNavController().navigateUp()
            // The scanner observes ocrResult; user will re-trigger from there.
            // For now, show a toast pointing back to scanner's OCR button.
            Snackbar.make(binding.root, "Go back and use the camera to capture the label", Snackbar.LENGTH_LONG).show()
        }

        binding.btnClose.setOnClickListener { findNavController().navigateUp() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { saveProduct() }

        viewModel.saveResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    findNavController().navigateUp()
                }
                is UiState.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                null -> {}
            }
        }
    }

    private fun saveProduct() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = "Product name required"
            return
        }

        val product = ProductSaveRequest(
            barcode      = binding.etBarcode.text.toString().trim(),
            barcodeType  = arguments?.getString("barcodeType") ?: "",
            name         = name,
            brand        = binding.etBrand.text.toString().trim(),
            category     = binding.spinnerCategory.selectedItem.toString(),
            subCategory  = binding.etSubCategory.text.toString().trim(),
            description  = binding.etDescription.text.toString().trim(),
            unit         = binding.etUnit.text.toString().trim(),
            imageUrl     = arguments?.getString("imageUrl") ?: "",
            source       = arguments?.getString("source") ?: "manual"
        )

        val sessionId = arguments?.getInt("sessionId") ?: return
        val quantity  = binding.etQuantity.text.toString().toIntOrNull() ?: 1

        viewModel.saveProduct(product, sessionId, quantity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
