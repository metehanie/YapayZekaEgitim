package com.metehanbolat.teknofestegitim.view.mainviews.artificialintelligence.aieducation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.metehanbolat.teknofestegitim.R
import com.metehanbolat.teknofestegitim.databinding.FragmentEducationOneBinding

class EducationOne : Fragment() {

    private var _binding : FragmentEducationOneBinding? = null
    private val binding get() = _binding!!
    private var eduControl : Int = 0
    private lateinit var navController: NavController
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private var userCoin : Any? = null
    private var learningOne : Any? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEducationOneBinding.inflate(inflater,container,false)
        val view = binding.root

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.fragment_education_background)

        auth = Firebase.auth
        firestore = Firebase.firestore

        getDataFun(auth.currentUser!!.email.toString(), view)

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore

        binding.educationOneButtonTwo.setOnClickListener {
            when(eduControl){
                0 -> {
                    binding.educationOneImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.father_ai))
                    binding.educationOneInfoTwo.alpha = 1f
                    eduControl = 1
                }
                1 -> {
                    binding.educationOneImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ai_data))
                    binding.educationOneInfoThree.alpha = 1f
                    eduControl = 2
                }
                2 -> {
                    binding.educationOneImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ai_skills))
                    binding.educationOneInfoFour.alpha = 1f
                    binding.educationOneButtonTwo.text = resources.getString(R.string.back)
                    eduControl = 3
                }
                3 -> {
                    if (learningOne!! == resources.getString(R.string.true_ai)) {
                        Snackbar.make(it, resources.getString(R.string.awards_already_collected_ai), Snackbar.LENGTH_LONG).show()
                    }else{
                        val getData = firestore.collection(resources.getString(R.string.firebase_userData)).document(auth.currentUser!!.email.toString())
                        val userCoinInt : Int = userCoin.toString().toInt()
                        val newCoin : Int = userCoinInt + 10
                        getData.update(resources.getString(R.string.ai_learning_one),resources.getString(R.string.true_ai),resources.getString(R.string.firebase_userCoin),newCoin)
                        Snackbar.make(it, resources.getString(R.string.earn_ten_gold), Snackbar.LENGTH_LONG).show()
                    }
                    navController = findNavController()
                    navController.navigate(R.id.action_educationOne_to_mainEducation)
                }
            }

        }

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navController = findNavController()
                navController.navigate(R.id.action_educationOne_to_mainEducation)

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

    }

    private fun getDataFun(mail : String, view : View){

        val getUserData = firestore.collection(resources.getString(R.string.firebase_userData)).document(mail)
        getUserData.get().addOnSuccessListener { document ->
            if (document != null){
                if (document.data != null){
                    learningOne = document.data!![resources.getString(R.string.firebase_aiLearningOne)]
                    userCoin = document.data!![resources.getString(R.string.firebase_userCoin)]
                }else{
                    Snackbar.make(view, resources.getString(R.string.no_user_data), Snackbar.LENGTH_LONG).show()
                }
            }
        }.addOnFailureListener {
            Snackbar.make(view, it.toString(), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}