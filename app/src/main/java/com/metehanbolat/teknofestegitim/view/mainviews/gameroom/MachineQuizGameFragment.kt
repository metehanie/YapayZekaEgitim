package com.metehanbolat.teknofestegitim.view.mainviews.gameroom

import android.content.res.ColorStateList
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.metehanbolat.teknofestegitim.R
import com.metehanbolat.teknofestegitim.databinding.FragmentMachineQuizGameBinding
import com.metehanbolat.teknofestegitim.utils.UserFirebaseProcess
import com.metehanbolat.teknofestegitim.view.mainviews.machinelearning.model.QuestionAPI
import com.metehanbolat.teknofestegitim.view.mainviews.machinelearning.model.QuestionAPIEnglish
import com.metehanbolat.teknofestegitim.view.mainviews.machinelearning.model.QuestionModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class MachineQuizGameFragment : Fragment() {

    private var _binding : FragmentMachineQuizGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController : NavController

    private lateinit var baseUrl : String

    private var questionModels : ArrayList<QuestionModel>? = null
    private var questionNumber = 1

    private var chosen = 0
    private var choosable = 1

    private lateinit var questionsRightAnswer : String

    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var auth : FirebaseAuth

    private lateinit var answerOne : String
    private lateinit var answerTwo : String
    private lateinit var answerThree : String
    private lateinit var answerFour : String

    private lateinit var call : Call<List<QuestionModel>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMachineQuizGameBinding.inflate(inflater, container,false)
        val view = binding.root

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.quiz_game_background)

        baseUrl = resources.getString(R.string.baseUrl)
        answerOne = resources.getString(R.string.answer_one)
        answerTwo = resources.getString(R.string.answer_two)
        answerThree = resources.getString(R.string.answer_three)
        answerFour = resources.getString(R.string.answer_four)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Snackbar.make(view, resources.getString(R.string.are_u_sure_quit_game), Snackbar.LENGTH_INDEFINITE).setAction(resources.getString(R.string.exit)){
                    navController = findNavController()
                    navController.navigate(R.id.action_machineQuizGameFragment_to_machineGameListFragment)
                }.show()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

        loadData()

        firestore = Firebase.firestore
        firebaseUser= FirebaseAuth.getInstance().currentUser!!
        auth = Firebase.auth

        val colorIdGreen = android.R.color.holo_green_dark
        val colorIdRed = android.R.color.holo_red_dark
        val colorIdQuizGameAnswers = R.color.quiz_game_answer_background

        binding.nextButton.setOnClickListener {

            val color1Answer = binding.answer1CardView.context.getColor(colorIdQuizGameAnswers)
            binding.answer1CardView.backgroundTintList = ColorStateList.valueOf(color1Answer)
            val color2Answer = binding.answer2CardView.context.getColor(colorIdQuizGameAnswers)
            binding.answer2CardView.backgroundTintList = ColorStateList.valueOf(color2Answer)
            val color3Answer = binding.answer3CardView.context.getColor(colorIdQuizGameAnswers)
            binding.answer3CardView.backgroundTintList = ColorStateList.valueOf(color3Answer)
            val color4Answer = binding.answer4CardView.context.getColor(colorIdQuizGameAnswers)
            binding.answer4CardView.backgroundTintList = ColorStateList.valueOf(color4Answer)

            if (questionNumber < (questionModels!!.size)){
                if (chosen == 1){
                    binding.questionTextView.text = questionModels!![questionNumber].question
                    binding.answer1TextView.text = questionModels!![questionNumber].answer1
                    binding.answer2TextView.text = questionModels!![questionNumber].answer2
                    binding.answer3TextView.text = questionModels!![questionNumber].answer3
                    binding.answer4TextView.text = questionModels!![questionNumber].answer4
                    questionsRightAnswer=questionModels!![questionNumber].rightAnswer
                    questionNumber += 1
                }else{
                    Snackbar.make(it, resources.getString(R.string.please_answer), Snackbar.LENGTH_SHORT).show()
                }
            }else{
                val empty : String = resources.getString(R.string.empty)
                Snackbar.make(it, resources.getString(R.string.all_question_answered), Snackbar.LENGTH_SHORT).show()
                increaseCoin(5)
                binding.questionTextView.text = resources.getString(R.string.all_question_answered)
                binding.answer1TextView.text = empty
                binding.answer2TextView.text = empty
                binding.answer3TextView.text = empty
                binding.answer4TextView.text = empty
                choosable = 0
            }
            chosen = 0
        }

        binding.answer1CardView.setOnClickListener {
            if (chosen == 0 && choosable == 1 && questionsRightAnswer == answerOne){
                val colorGreen = binding.answer1CardView.context.getColor(colorIdGreen)
                binding.answer1CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                increaseCoin(2)
            }else if (chosen == 0 && choosable == 1 && questionsRightAnswer != answerOne){
                val colorRed = binding.answer1CardView.context.getColor(colorIdRed)
                binding.answer1CardView.backgroundTintList = ColorStateList.valueOf(colorRed)
                decreaseCoin()

                when(questionsRightAnswer){
                    resources.getString(R.string.answer_two) -> {
                        val colorGreen = binding.answer2CardView.context.getColor(colorIdGreen)
                        binding.answer2CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_three) -> {
                        val colorGreen = binding.answer3CardView.context.getColor(colorIdGreen)
                        binding.answer3CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_four) -> {
                        val colorGreen = binding.answer4CardView.context.getColor(colorIdGreen)
                        binding.answer4CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                }
            }
            chosen = 1
        }

        binding.answer2CardView.setOnClickListener {
            if (chosen == 0 && choosable == 1 && questionsRightAnswer == answerTwo){
                val colorGreen = binding.answer2CardView.context.getColor(colorIdGreen)
                binding.answer2CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                increaseCoin(2)
            }else if (chosen == 0 && choosable == 1 && questionsRightAnswer != answerTwo){
                val colorRed = binding.answer2CardView.context.getColor(colorIdRed)
                binding.answer2CardView.backgroundTintList = ColorStateList.valueOf(colorRed)
                decreaseCoin()

                when(questionsRightAnswer){
                    resources.getString(R.string.answer_one) -> {
                        val colorGreen = binding.answer1CardView.context.getColor(colorIdGreen)
                        binding.answer1CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_three) -> {
                        val colorGreen = binding.answer3CardView.context.getColor(colorIdGreen)
                        binding.answer3CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_four) -> {
                        val colorGreen = binding.answer4CardView.context.getColor(colorIdGreen)
                        binding.answer4CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                }
            }
            chosen = 1
        }

        binding.answer3CardView.setOnClickListener {
            if (chosen == 0 && choosable == 1 && questionsRightAnswer == answerThree){
                val colorGreen = binding.answer3CardView.context.getColor(colorIdGreen)
                binding.answer3CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                increaseCoin(2)
            }else if (chosen == 0 && choosable == 1 && questionsRightAnswer != answerThree){
                val colorRed = binding.answer3CardView.context.getColor(colorIdRed)
                binding.answer3CardView.backgroundTintList = ColorStateList.valueOf(colorRed)
                decreaseCoin()

                when(questionsRightAnswer){
                    resources.getString(R.string.answer_one) -> {
                        val colorGreen = binding.answer1CardView.context.getColor(colorIdGreen)
                        binding.answer1CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_two) -> {
                        val colorGreen = binding.answer2CardView.context.getColor(colorIdGreen)
                        binding.answer2CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_four) -> {
                        val colorGreen = binding.answer4CardView.context.getColor(colorIdGreen)
                        binding.answer4CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                }
            }
            chosen = 1
        }

        binding.answer4CardView.setOnClickListener {
            if (chosen == 0 && choosable == 1 && questionsRightAnswer == answerFour){
                val colorGreen = binding.answer4CardView.context.getColor(colorIdGreen)
                binding.answer4CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                increaseCoin(2)
            }else if (chosen == 0 && choosable == 1 && questionsRightAnswer != answerFour){
                val colorRed = binding.answer4CardView.context.getColor(colorIdRed)
                binding.answer4CardView.backgroundTintList = ColorStateList.valueOf(colorRed)
                decreaseCoin()

                when(questionsRightAnswer){
                    resources.getString(R.string.answer_one) -> {
                        val colorGreen = binding.answer1CardView.context.getColor(colorIdGreen)
                        binding.answer1CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_two) -> {
                        val colorGreen = binding.answer2CardView.context.getColor(colorIdGreen)
                        binding.answer2CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                    resources.getString(R.string.answer_three) -> {
                        val colorGreen = binding.answer3CardView.context.getColor(colorIdGreen)
                        binding.answer3CardView.backgroundTintList = ColorStateList.valueOf(colorGreen)
                    }
                }
            }
            chosen = 1
        }
    }

    private fun loadData(){

        val retrofit = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build()
        call = if (Locale.getDefault().language.toString() == resources.getString(R.string.tr)){
            val service = retrofit.create(QuestionAPI::class.java)
            service.getData()
        }else{
            val service = retrofit.create(QuestionAPIEnglish::class.java)
            service.getData()
        }

        call.enqueue(object : Callback<List<QuestionModel>>{
            override fun onResponse(
                call : Call<List<QuestionModel>>,
                response : Response<List<QuestionModel>>
            ) {
                if (response.isSuccessful){
                    response.body()?.let {
                        questionModels = ArrayList(it)

                        binding.questionTextView.text = questionModels!![0].question
                        binding.answer1TextView.text = questionModels!![0].answer1
                        binding.answer2TextView.text = questionModels!![0].answer2
                        binding.answer3TextView.text = questionModels!![0].answer3
                        binding.answer4TextView.text = questionModels!![0].answer4
                        questionsRightAnswer = questionModels!![0].rightAnswer

                    }
                }
            }

            override fun onFailure(call : Call<List<QuestionModel>>, t : Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun increaseCoin(amountOfIncrease : Int){
        val earnGold = UserFirebaseProcess(firestore, resources.getString(R.string.firebase_userData), auth.currentUser!!.email.toString())
        earnGold.getCoin(resources.getString(R.string.firebase_userCoin)){
            earnGold.userCoinIncrease(resources.getString(R.string.firebase_userCoin), it!!, amountOfIncrease)
        }
    }

    private fun decreaseCoin(){
        val earnGold = UserFirebaseProcess(firestore, resources.getString(R.string.firebase_userData), auth.currentUser!!.email.toString())
        earnGold.getCoin(resources.getString(R.string.firebase_userCoin)){
            earnGold.userCoinDecrease(resources.getString(R.string.firebase_userCoin), it!!, 2)
        }
    }

}