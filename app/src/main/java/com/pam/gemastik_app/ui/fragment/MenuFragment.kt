package com.pam.gemastik_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment

import com.pam.gemastik_app.R
import com.pam.gemastik_app.ui.FoodDetailActivity
import com.pam.gemastik_app.ui.MainActivity
import com.pam.gemastik_app.ui.RecommendationActivity


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
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        val saladButton: ImageButton = view.findViewById(R.id.imageButtonSalad)
        saladButton.setOnClickListener {
            val intent = Intent(activity, RecommendationActivity::class.java)
            startActivity(intent)
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
