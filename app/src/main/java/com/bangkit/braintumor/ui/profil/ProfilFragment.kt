package com.bangkit.braintumor.ui.profil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bangkit.braintumor.databinding.FragmentProfilBinding
import com.bangkit.braintumor.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.profilName.setText(currentUser.displayName ?: "Tidak ada nama")
            binding.profilEmail.setText(currentUser.email ?: "Tidak ada email")
        } else {
            binding.profilName.setText("Pengguna tidak ditemukan")
            binding.profilEmail.setText("Silakan login kembali")
        }

        binding.profilLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
