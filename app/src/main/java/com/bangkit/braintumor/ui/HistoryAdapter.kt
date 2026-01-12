package com.example.capstonefinal.ui


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.braintumor.R
import com.bangkit.braintumor.data.Patient

class HistoryAdapter(private var patients: List<Patient>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.his_name)
        val age: TextView = itemView.findViewById(R.id.his_age)
        val address: TextView = itemView.findViewById(R.id.his_address)
        val komplikasi: TextView = itemView.findViewById(R.id.his_complications)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patient = patients[position]
        holder.name.text = patient.name
        holder.age.text = "Age: ${patient.age}"
        holder.address.text = "Address: ${patient.address}"
        holder.komplikasi.text = "Komplikasi: ${patient.komplikasi}"
    }

    override fun getItemCount() = patients.size

    fun updateData(newPatients: List<Patient>) {
        patients = newPatients
        notifyDataSetChanged()
    }
}