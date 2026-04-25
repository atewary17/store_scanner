package com.storescanner.ui.scanner

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.storescanner.R
import com.storescanner.data.models.LookupResponse
import com.storescanner.data.models.ScanItem
import com.storescanner.databinding.FragmentScannerBinding
import com.storescanner.databinding.ItemScanItemBinding
import com.storescanner.viewmodel.ScannerViewModel
import com.storescanner.viewmodel.UiState
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var sessionId: Int = -1
    private var lastBarcode: String? = null
    private var scanningPaused = false
    private var scanLineAnimator: ObjectAnimator? = null

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else showError("Camera permission required")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionId = arguments?.getInt("sessionId") ?: -1
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Load session info for header
        viewModel.loadSession(sessionId)
        viewModel.currentSession.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                val session = state.data
                binding.tvSessionName.text = session.name
                binding.tvSessionLocation.text = session.location ?: ""
                binding.tvItemCount.text = "${session.itemCount} items"
                val items = session.items ?: emptyList()
                sessionItemAdapter.submitList(items)
            }
        }

        // Session items list
        sessionItemAdapter = ScanItemAdapter()
        binding.rvSessionItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessionItems.adapter = sessionItemAdapter

        binding.btnExit.setOnClickListener { findNavController().navigateUp() }

        binding.btnScan.setOnClickListener {
            scanningPaused = false
            lastBarcode = null
            viewModel.resetLookup()
            binding.tvScanStatus.text = "Point camera at barcode or QR"
        }

        binding.btnStop.setOnClickListener {
            scanningPaused = true
            binding.tvScanStatus.text = "Scanning paused"
        }

        binding.btnManual.setOnClickListener {
            openApprovalSheet(null)
        }

        startScanLineAnimation()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }

        observeLookupResult()
        observeSaveResult()
    }

    private lateinit var sessionItemAdapter: ScanItemAdapter

    private fun startScanLineAnimation() {
        binding.scanLine.post {
            val height = (binding.scanLine.parent as View).height.toFloat()
            scanLineAnimator = ObjectAnimator.ofFloat(binding.scanLine, "translationY", 0f, height).apply {
                duration = 2000
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val barcodeScanner = BarcodeScanning.getClient()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!scanningPaused) processImageForBarcode(imageProxy, barcodeScanner)
                        else imageProxy.close()
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, imageAnalysis)
            } catch (e: Exception) {
                showError("Camera init failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageForBarcode(imageProxy: ImageProxy, scanner: com.google.mlkit.vision.barcode.BarcodeScanner) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull { it.rawValue != null } ?: run { imageProxy.close(); return@addOnSuccessListener }
                val value = barcode.rawValue!!
                val format = barcodeFormatName(barcode.format)

                if (value != lastBarcode) {
                    lastBarcode = value
                    scanningPaused = true
                    requireActivity().runOnUiThread {
                        binding.tvScanStatus.text = "Found: $value — looking up..."
                        addApiLog("🔍 Barcode: $value ($format)")
                        viewModel.lookupBarcode(value, format)
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun capturePhotoForOcr() {
        val imageCapture = imageCapture ?: return
        scanningPaused = true

        val file = File(requireContext().cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    requireActivity().runOnUiThread {
                        binding.tvScanStatus.text = "Running OCR..."
                        addApiLog("📷 OCR photo captured")
                        viewModel.ocrLookup(file, lastBarcode)
                    }
                }
                override fun onError(e: ImageCaptureException) {
                    requireActivity().runOnUiThread { showError("Capture failed: ${e.message}") }
                    scanningPaused = false
                }
            }
        )
    }

    private fun addApiLog(message: String) {
        binding.apiLogPanel.visibility = View.VISIBLE
        val tv = TextView(requireContext()).apply {
            text = message
            textSize = 11f
            setTextColor(ContextCompat.getColor(requireContext(), com.storescanner.R.color.dark_600))
        }
        binding.apiLogContainer.addView(tv)
        // Keep max 5 log entries
        while (binding.apiLogContainer.childCount > 5) {
            binding.apiLogContainer.removeViewAt(0)
        }
    }

    private fun observeLookupResult() {
        viewModel.lookupResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.tvScanStatus.text = "Looking up barcode..."
                is UiState.Success -> {
                    val result = state.data
                    val source = result.product?.source ?: result.apiSuggestion?.source ?: "unknown"
                    addApiLog("✅ Found via $source")
                    if (result.needsManual) {
                        binding.tvScanStatus.text = "Not found — scan via OCR or enter manually"
                        addApiLog("⚠️ Not found in any database")
                        openApprovalSheet(result)
                    } else {
                        openApprovalSheet(result)
                    }
                }
                is UiState.Error -> {
                    scanningPaused = false
                    addApiLog("❌ Lookup error")
                    showError(state.message)
                }
                null -> {}
            }
        }
    }

    private fun observeSaveResult() {
        viewModel.saveResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val name = state.data.scanItem?.product?.name ?: "Item"
                    Snackbar.make(binding.root, "✓ $name saved!", Snackbar.LENGTH_SHORT).show()
                    binding.tvScanStatus.text = "Point camera at barcode or QR"
                    scanningPaused = false
                    lastBarcode = null
                    viewModel.resetLookup()
                    // Reload session to refresh item count + list
                    viewModel.loadSession(sessionId)
                    binding.tvItemCount.text = "${(state.data.scanItem?.let { 1 } ?: 0)} items"
                }
                is UiState.Error -> showError(state.message)
                else -> {}
            }
        }
    }

    private fun openApprovalSheet(lookup: LookupResponse?) {
        val product = lookup?.product ?: lookup?.apiSuggestion
        val args = Bundle().apply {
            putInt("sessionId", sessionId)
            putString("barcode", lookup?.barcode ?: lastBarcode ?: "")
            putString("barcodeType", lookup?.barcodeType ?: "")
            putString("name", product?.name ?: "")
            putString("brand", product?.brand ?: "")
            putString("category", product?.category ?: "")
            putString("description", product?.description ?: "")
            putString("unit", product?.unit ?: "")
            putString("imageUrl", product?.imageUrl ?: "")
            putString("source", product?.source ?: "manual")
            putBoolean("needsOcr", lookup?.needsManual == true)
        }
        findNavController().navigate(R.id.action_scanner_to_approval, args)
    }

    private fun barcodeFormatName(format: Int) = when (format) {
        Barcode.FORMAT_EAN_13   -> "EAN13"
        Barcode.FORMAT_EAN_8    -> "EAN8"
        Barcode.FORMAT_UPC_A    -> "UPC_A"
        Barcode.FORMAT_UPC_E    -> "UPC_E"
        Barcode.FORMAT_CODE_128 -> "CODE128"
        Barcode.FORMAT_QR_CODE  -> "QR_CODE"
        else                    -> "UNKNOWN"
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanLineAnimator?.cancel()
        cameraExecutor.shutdown()
        _binding = null
    }
}

class ScanItemAdapter : RecyclerView.Adapter<ScanItemAdapter.ViewHolder>() {

    private val items = mutableListOf<ScanItem>()

    fun submitList(list: List<ScanItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemScanItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemScanItemBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvName.text = item.product?.name ?: "Unknown"
            tvBrand.text = item.product?.brand ?: item.product?.category ?: ""
            val barcode = item.product?.barcode ?: ""
            tvBarcodeShort.text = if (barcode.length > 6) "…${barcode.takeLast(6)}" else barcode
        }
    }
}
