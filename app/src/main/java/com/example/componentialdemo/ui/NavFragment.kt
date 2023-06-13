package com.example.componentialdemo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.componentialdemo.R
import com.example.componentialdemo.databinding.FragmentHomeBinding
import com.example.componentialdemo.databinding.FragmentNavBinding
import com.example.componentialdemo.ui.home.HomeViewModel

class NavFragment : Fragment() {
    private var _binding: FragmentNavBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentNavBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnNav.setOnClickListener {
            findNavController().navigate(R.id.action_navFragment_to_navigation_home)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}