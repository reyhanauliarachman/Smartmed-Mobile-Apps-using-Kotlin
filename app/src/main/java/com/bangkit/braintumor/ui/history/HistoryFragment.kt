package com.bangkit.braintumor.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.braintumor.data.ApiConfig
import com.bangkit.braintumor.data.Patient
import com.bangkit.braintumor.data.PatientsResponse
import com.bangkit.braintumor.databinding.FragmentHistoryBinding
import com.example.capstonefinal.ui.HistoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter
    private var patientList: List<Patient> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = HistoryAdapter(patientList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = historyAdapter

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        fetchPatientData(it)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.searchView.requestFocus()

        binding.searchView.isIconified = false
    }


    private fun fetchPatientData(patientId: String) {
        val apiService = ApiConfig.instance
        val call = apiService.getPatientById(patientId)

        call.enqueue(object : Callback<PatientsResponse> {
            override fun onResponse(call: Call<PatientsResponse>, response: Response<PatientsResponse>) {
                if (response.isSuccessful) {
                    val patient = response.body()?.patient
                    if (patient != null) {
                        patientList = listOf(patient)
                        historyAdapter.updateData(patientList)
                    } else {
                        Toast.makeText(context, "Patient not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PatientsResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
