//ScanResultActivity
package com.bangkit.braintumor.ui.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.braintumor.MainActivity
import com.bangkit.braintumor.R
import com.bangkit.braintumor.data.ApiConfig
import com.bangkit.braintumor.data.UploadResponse
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

class ScanResultActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        pieChart = findViewById(R.id.pieChart_result)

        val scanResult = intent.getStringExtra("SCAN_RESULT") ?: "No result found"
        val processedOutput = intent.getStringExtra("PROCESSED_OUTPUT") ?: "No output available"

        val tvScanResult = findViewById<TextView>(R.id.tv_scan_result)
        val tvProcessedOutput = findViewById<TextView>(R.id.tv_processed_output)

        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientAge = intent.getStringExtra("PATIENT_AGE") ?: "Unknown"
        val patientGender = intent.getStringExtra("PATIENT_GENDER") ?: "Unknown"
        val patientEmail = intent.getStringExtra("PATIENT_EMAIL") ?: "Unknown"
        val patientAddress = intent.getStringExtra("PATIENT_ADDRESS") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "Unknown"

        findViewById<TextView>(R.id.tv_patient_name).text = "$patientName"
        findViewById<TextView>(R.id.tv_patient_id).text = "$patientId"
        findViewById<TextView>(R.id.tv_patient_age).text = "$patientAge"
        findViewById<TextView>(R.id.tv_patient_gender).text = "$patientGender"
        findViewById<TextView>(R.id.tv_patient_email).text = "$patientEmail"
        findViewById<TextView>(R.id.tv_patient_address).text = "$patientAddress"
        findViewById<TextView>(R.id.tv_patient_complications).text = "$scanResult"


        tvProcessedOutput.text = getHighestPercent(processedOutput)
        tvScanResult.text = scanResult

        setupPieChart(processedOutput)

        val btnSave = findViewById<Button>(R.id.btn_save)
        val btnRescan = findViewById<Button>(R.id.btn_rescan)

        btnSave.setOnClickListener {
            val imageUriString = intent.getStringExtra("IMAGE_URI")
            if (imageUriString != null) {
                val imageUri = Uri.parse(imageUriString)
                uploadPatientData(
                    patientName,
                    patientId,
                    patientGender,
                    patientAddress,
                    patientEmail,
                    scanResult,
                    patientAge,
                    imageUri
                )
            } else {
                Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this@ScanResultActivity, MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO", "HOME")
            startActivity(intent)
            finish()
        }


        btnRescan.setOnClickListener {
            finish()
        }

        savePatientData(patientName, patientId, scanResult, processedOutput)

        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val imageView = findViewById<ImageView>(R.id.imageView_scan_result)

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
        } else {
            Log.e("ScanResultActivity", "No image URI received")
        }
    }

    private fun setupPieChart(processedOutput: String) {
        Log.d("ScanResultActivity", "Processed Output: $processedOutput")

        val pieEntries = mutableListOf<PieEntry>()
        val labels = listOf("Meningioma", "Glioma", "No Tumor", "Pituitary")
        val values = processedOutput.split(",").mapNotNull {
            val parts = it.split(":")
            parts.getOrNull(1)?.trim()?.toFloatOrNull()
        }

        val hasFullValue = values.any { it == 1f }

        if (labels.size == values.size) {
            for (i in labels.indices) {
                val entryLabel = if (hasFullValue && labels[i] != "No Tumor") {
                    ""
                } else {
                    labels[i]
                }
                if (values[i] > 0) {
                    pieEntries.add(PieEntry(values[i] * 100, entryLabel))
                }
            }
        }

        if (pieEntries.isEmpty()) {
            Log.d("ScanResultActivity", "No entries for pie chart")
            return
        }

        val dataSet = PieDataSet(pieEntries, "Scan Results")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
        dataSet.valueTextSize = 10f
        pieChart.setEntryLabelTextSize(10f)

        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(android.R.color.transparent)
        pieChart.setUsePercentValues(true)
        pieChart.animateY(1500)

        pieChart.invalidate()
    }

    private fun uploadPatientData(
        name: String,
        id: String,
        gender: String,
        address: String,
        email: String,
        complications: String,
        age: String,
        imageUri: Uri
    ) {
        val nameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val idBody = RequestBody.create("text/plain".toMediaTypeOrNull(), id)
        val genderBody = RequestBody.create("text/plain".toMediaTypeOrNull(), gender)
        val addressBody = RequestBody.create("text/plain".toMediaTypeOrNull(), address)
        val emailBody = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
        val complicationsBody = RequestBody.create("text/plain".toMediaTypeOrNull(), complications)
        val ageBody = RequestBody.create("text/plain".toMediaTypeOrNull(), age)

        val file = File(getRealPathFromURI(imageUri))
        val imageBody = MultipartBody.Part.createFormData(
            "image",
            file.name,
            RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        )

        ApiConfig.instance.uploadPatientData(
            nameBody,
            idBody,
            genderBody,
            addressBody,
            emailBody,
            complicationsBody,
            ageBody,
            imageBody
        ).enqueue(object : retrofit2.Callback<UploadResponse> { // Ensure retrofit2.Callback is used
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ScanResultActivity, "Upload successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("UploadError", "Response Code: ${response.code()}, Error Body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ScanResultActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Toast.makeText(this@ScanResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return path ?: ""
    }


    private fun getHighestPercent(output: String): String {
        val results = output.split(",").mapNotNull { part ->
            val (label, value) = part.split(":").map { it.trim() }
            val percentValue = value.toFloatOrNull()?.times(100) ?: return@mapNotNull null
            label to percentValue
        }

        val highest = results.maxByOrNull { it.second }
        return highest?.let { "${it.first}: %.2f%%".format(it.second) } ?: "No data available"
    }

    //hanya sharedpreference di home fragment
    private fun savePatientData(patientName: String, patientId: String, scanResult: String, processedOutput: String) {
        val sharedPreferences = getSharedPreferences("PatientData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("PATIENT_NAME", patientName)
        editor.putString("PATIENT_ID", patientId)
        editor.putString("SCAN_RESULT", scanResult)
        editor.putString("PROCESSED_OUTPUT", processedOutput)
        editor.apply()

        Log.d("ScanResultActivity", "Saved Patient Data: Name=$patientName, ID=$patientId, Result=$scanResult")
    }
}
