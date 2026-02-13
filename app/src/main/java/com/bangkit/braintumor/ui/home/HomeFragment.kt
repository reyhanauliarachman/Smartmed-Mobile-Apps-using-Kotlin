// HomeFragment.kt
package com.bangkit.braintumor.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bangkit.braintumor.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class HomeFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var titleHome: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        pieChart = rootView.findViewById(R.id.pieChart)
        titleHome = rootView.findViewById(R.id.title_home)

        val fadeInAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        titleHome.startAnimation(fadeInAnimation)

        loadPatientData(rootView)
        loadScanResults()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        loadPatientData(requireView())
        loadScanResults()
    }

    private fun loadPatientData(view: View) {
        val sharedPreferences =
            requireActivity().getSharedPreferences("PatientData", AppCompatActivity.MODE_PRIVATE)

        val patientName = sharedPreferences.getString("PATIENT_NAME", "Unknown")
        val patientId = sharedPreferences.getString("PATIENT_ID", "Unknown")
        val scanResult = sharedPreferences.getString("SCAN_RESULT", "No result")

        view.findViewById<TextView>(R.id.patientname_home).text =
            "Patient Name: $patientName"
        view.findViewById<TextView>(R.id.idpatient_home).text =
            "ID Patient: $patientId"
        view.findViewById<TextView>(R.id.scanresult_home).text =
            "Scan Result: $scanResult"
    }

    private fun loadScanResults() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("PatientData", AppCompatActivity.MODE_PRIVATE)
        val processedOutput = sharedPreferences.getString("PROCESSED_OUTPUT", "") ?: ""
        setupPieChart(processedOutput)
    }

    private fun setupPieChart(processedOutput: String) {
        val pieEntries = mutableListOf<PieEntry>()
        val labels = listOf("Meningioma", "Glioma", "No Tumor", "Pituitary")

        val values = processedOutput.split(",").mapNotNull {
            it.split(":").getOrNull(1)?.trim()?.toFloatOrNull()
        }

        if (labels.size == values.size) {
            for (i in labels.indices) {
                if (values[i] > 0) {
                    pieEntries.add(PieEntry(values[i] * 100, labels[i]))
                }
            }
        }

        if (pieEntries.isEmpty()) return

        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
        dataSet.valueTextSize = 12f

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            animateY(1200)
            invalidate()
        }
    }
}
