//ScanFragment
package com.bangkit.braintumor.ui.scan

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bangkit.braintumor.R
import com.bangkit.braintumor.databinding.FragmentScanBinding
import com.bangkit.braintumor.ml.BrainTumorModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ScanFragment : Fragment(R.layout.fragment_scan) {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null
    private val utils by lazy { Utils() }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScanBinding.bind(view)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.scanButton.setOnClickListener { uploadImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = utils.getImageUri(requireContext())
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        if (currentImageUri == null) {
            Toast.makeText(requireContext(), "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val inputStream = requireContext().contentResolver.openInputStream(currentImageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true)
            val byteBuffer = bitmapToByteBuffer(scaledBitmap)
            val model = BrainTumorModel.newInstance(requireContext())
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val result = outputFeature0.floatArray

            val meningiomaValue = result[0]
            val gliomaValue = result[1]
            val noTumorValue = result[2]
            val pituitaryValue = result[3]

            val processedOutput = "Meningioma:$meningiomaValue,Glioma:$gliomaValue,No Tumor:$noTumorValue,Pituitary:$pituitaryValue"

            val patientName = binding.scanName.text.toString()
            val patientAge = binding.scanAge.text.toString()
            val patientGender = binding.scanGender.text.toString()
            val patientEmail = binding.scanEmail.text.toString()
            val patientAddress = binding.scanAlamat.text.toString()
            val patientId = binding.scanId.text.toString()

            val intent = Intent(requireContext(), ScanResultActivity::class.java).apply {
                putExtra("SCAN_RESULT", getLabel(result.indices.maxByOrNull { result[it] } ?: -1))
                putExtra("PROCESSED_OUTPUT", processedOutput)
                putExtra("PATIENT_NAME", patientName)
                putExtra("PATIENT_AGE", patientAge)
                putExtra("PATIENT_GENDER", patientGender)
                putExtra("PATIENT_EMAIL", patientEmail)
                putExtra("PATIENT_ADDRESS", patientAddress)
                putExtra("PATIENT_ID", patientId)
                putExtra("IMAGE_URI", currentImageUri.toString())
            }
            startActivity(intent)

            model.close()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ScanFragment", "Error: ", e)
        }
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 150 * 150 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(150 * 150)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            byteBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        return byteBuffer
    }

    private fun getLabel(index: Int): String {
        val labels = listOf("Meningioma", "Glioma", "No Tumor", "Pituitary")
        return labels.getOrElse(index) { "Unknown" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
