package com.metehanbolat.teknofestegitim.view.mainviews.gift

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.metehanbolat.teknofestegitim.R
import com.metehanbolat.teknofestegitim.databinding.FragmentGiftBinding

class GiftFragment : Fragment() {

    private var _binding : FragmentGiftBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private lateinit var giftList : ArrayList<Gift>

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var docRef : DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiftBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = Firebase.firestore
        firebaseAuth = Firebase.auth

        docRef = firestore.collection(resources.getString(R.string.firebase_userData)).document(firebaseAuth.currentUser!!.email.toString())

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navController = findNavController()
                navController.navigate(R.id.action_giftFragment_to_mainFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.background_color)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        giftList = ArrayList()

        binding.floatingButton.setOnClickListener {

            navController = findNavController()
            val action = GiftFragmentDirections.actionGiftFragmentToGiftAwardsAddFragment(null)
            navController.navigate(action)

        }

        docRef.get().addOnSuccessListener { createControl ->
            if (createControl != null){
                val isCreate = createControl[resources.getString(R.string.firebase_sixthCoin)].toString()
                if (isCreate == resources.getString(R.string.zero)){
                    Snackbar.make(view, resources.getString(R.string.welcome_gift_fragment_snackbar), Snackbar.LENGTH_LONG).show()
                    docRef.update(resources.getString(R.string.firebase_sixthCoin), 1)
                }
            }
        }

        val listDocRef = docRef.collection(firebaseAuth.currentUser!!.email.toString())

        listDocRef.addSnapshotListener { value, error ->
            giftList.clear()
            if (error == null){
                if (value != null){
                    val documents = value.documents
                    for (document in documents){
                        val award = document[resources.getString(R.string.hash_award)] as String
                        val coin = document[resources.getString(R.string.hash_coin)] as String

                        val gift = Gift(award, coin)
                        giftList.add(gift)

                    }
                    binding.giftRecyclerView.layoutManager = StaggeredGridLayoutManager(2,GridLayoutManager.VERTICAL)
                    val giftAdapter = GiftRecyclerAdapter(resources.getString(R.string.firebase_userData), firebaseAuth.currentUser!!.email.toString(), resources.getString(R.string.firebase_userCoin), firestore, firebaseAuth, giftList)
                    binding.giftRecyclerView.adapter = giftAdapter
                }
            }
        }

        docRef.addSnapshotListener { value, error ->
            if (error == null){
                if (value != null){
                    binding.giftCoin.text = value[resources.getString(R.string.firebase_userCoin)].toString()
                }
            }
        }
    }
}