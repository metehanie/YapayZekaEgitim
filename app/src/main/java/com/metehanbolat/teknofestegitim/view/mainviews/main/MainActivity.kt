package com.metehanbolat.teknofestegitim.view.mainviews.main

import  androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.metehanbolat.teknofestegitim.R
import com.metehanbolat.teknofestegitim.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_TeknofestEgitim)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }

}