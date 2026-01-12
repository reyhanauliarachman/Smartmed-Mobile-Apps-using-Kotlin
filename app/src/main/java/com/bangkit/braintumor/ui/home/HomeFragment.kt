//HomeFragment
package com.bangkit.braintumor.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.braintumor.R
import com.bangkit.braintumor.data.ApiClient
import com.bangkit.braintumor.data.ArticleResponse
import com.bangkit.braintumor.data.ArticlesItem
import com.example.capstonefinal.ui.ArticleAdapter
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var titleHome: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        pieChart = rootView.findViewById(R.id.pieChart)
        titleHome = rootView.findViewById(R.id.title_home)
        recyclerView = rootView.findViewById(R.id.recyclerView)

        val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        titleHome.startAnimation(fadeInAnimation)

        loadPatientData()
        loadScanResults()
        fetchArticles()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        loadPatientData()
        loadScanResults()
    }

    private fun fetchArticles() {
        val apiService = ApiClient.instance
        val call = apiService.getArticles(
            query = "penyakit",
            language = "id",
            sortBy = "publishedAt",
            from = "2024-12-01",
            apiKey = "d326f680db32451e8d4580b4fb1cbf05"
        )

        call.enqueue(object : Callback<ArticleResponse> {
            override fun onResponse(call: Call<ArticleResponse>, response: Response<ArticleResponse>) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    setupRecyclerView(articles.filterNotNull())
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch articles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArticleResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching articles: ${t.message}")
                Toast.makeText(requireContext(), "Error fetching articles", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(articles: List<ArticlesItem>) {
        articleAdapter = ArticleAdapter(articles)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = articleAdapter
    }

    private fun loadPatientData() {
        val sharedPreferences = requireActivity().getSharedPreferences("PatientData", AppCompatActivity.MODE_PRIVATE)
        val patientName = sharedPreferences.getString("PATIENT_NAME", "Unknown") ?: "Unknown"
        val patientId = sharedPreferences.getString("PATIENT_ID", "Unknown") ?: "Unknown"
        val scanResult = sharedPreferences.getString("SCAN_RESULT", "No result found") ?: "No result found"

        // Tampilkan data pasien di UI
        val patientNameTextView = view?.findViewById<TextView>(R.id.patientname_home)
        val patientIdTextView = view?.findViewById<TextView>(R.id.idpatient_home)
        val scanResultTextView = view?.findViewById<TextView>(R.id.scanresult_home)

        patientNameTextView?.text = "Patient Name: $patientName"
        patientIdTextView?.text = "ID Patient: $patientId"
        scanResultTextView?.text = "Scan Result: $scanResult"
    }



    private fun loadScanResults() {
        val sharedPreferences = requireActivity().getSharedPreferences("PatientData", AppCompatActivity.MODE_PRIVATE)
        val processedOutput = sharedPreferences.getString("PROCESSED_OUTPUT", "") ?: ""
        Log.d("HomeFragment", "Processed Output: $processedOutput") // Tambahkan log
        setupPieChart(processedOutput)
    }


    private fun setupPieChart(processedOutput: String) {
        Log.d("ScanResultActivity", "Processed Output: $processedOutput")

        val pieEntries = mutableListOf<PieEntry>()
        val labels = listOf("Meningioma", "Glioma", "No Tumor", "Pituitary")
        val values = processedOutput.split(",").mapNotNull {
            val parts = it.split(":")
            parts.getOrNull(1)?.trim()?.toFloatOrNull()
        }

        // Cek apakah ada kategori dengan nilai 100%
        val hasFullValue = values.any { it == 1f }

        if (labels.size == values.size) {
            for (i in labels.indices) {
                // Sembunyikan label jika ada kategori yang 100%
                val entryLabel = if (hasFullValue && labels[i] != "No Tumor") {
                    "" // Sembunyikan label untuk kategori lain
                } else {
                    labels[i]
                }
                if (values[i] > 0) {
                    pieEntries.add(PieEntry(values[i] * 100, entryLabel)) // Ubah ke persentase
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
}
