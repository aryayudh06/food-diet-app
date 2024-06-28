package com.pam.gemastik_app.ui.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.pam.gemastik_app.R
import com.pam.gemastik_app.ui.FoodDetailActivity
import com.pam.gemastik_app.ui.FoodTrackingActivity
import com.pam.gemastik_app.ui.HomeActivity
import com.pam.gemastik_app.ui.MainActivity
import com.pam.gemastik_app.ui.RecommendationActivity
import com.pam.gemastik_app.ui.photoutil.CameraActivity


/**
 * A simple [Fragment] subclass.
 * Use the [MenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 */class MenuFragment : Fragment() {
    private var currentActivityName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentActivityName = it.getString(ARG_ACTIVITY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        // Log the name of the current activity
        Log.d("MenuFragment", "Current Activity: $currentActivityName")


        val homeButton: ImageButton = view.findViewById(R.id.imageButtonHome)
        homeButton.setOnClickListener {
            val intent = Intent(activity, HomeActivity::class.java)
            startActivity(intent)
        }

        val camButton: ImageButton = view.findViewById(R.id.imageButtonCamera)
        camButton.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }

        val saladButton: ImageButton = view.findViewById(R.id.imageButtonSalad)
        saladButton.setOnClickListener {
            val intent = Intent(activity, RecommendationActivity::class.java)
            startActivity(intent)
        }

        val colorOff = ContextCompat.getColor(requireContext(), R.color.ibOff)
        val colorOn = ContextCompat.getColor(requireContext(), R.color.ibOn)


        if (currentActivityName == "MainActivity") {
            saladButton.imageTintList = ColorStateList.valueOf(colorOff)
            camButton.imageTintList = ColorStateList.valueOf(colorOff)
            homeButton.imageTintList = ColorStateList.valueOf(colorOn)
        } else if(currentActivityName == "FoodDetailActivity" || currentActivityName == "RecommendationActivity"){
            homeButton.imageTintList = ColorStateList.valueOf(colorOff)
            camButton.imageTintList = ColorStateList.valueOf(colorOff)
            saladButton.imageTintList = ColorStateList.valueOf(colorOn)
        } else if(currentActivityName == "FoodTrackingActivity") {
            homeButton.imageTintList = ColorStateList.valueOf(colorOff)
            saladButton.imageTintList = ColorStateList.valueOf(colorOff)
            camButton.imageTintList = ColorStateList.valueOf(colorOn)
        }

        return view
    }

    companion object {
        private const val ARG_ACTIVITY_NAME = "activity_name"

        fun newInstance(activityName: String): MenuFragment {
            val fragment = MenuFragment()
            val args = Bundle()
            args.putString(ARG_ACTIVITY_NAME, activityName)
            fragment.arguments = args
            return fragment
        }
    }
}
