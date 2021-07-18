package com.example.pdcast.ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pdcast.R
import com.example.pdcast.databinding.FragmentAccountBinding


class AccountFragment : Fragment() {

    private var _binding:FragmentAccountBinding? = null

    private val binding: FragmentAccountBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentAccountBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

}