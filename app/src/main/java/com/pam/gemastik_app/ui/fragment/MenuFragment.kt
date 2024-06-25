package com.pam.gemastik_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.pam.gemastik_app.ARG_PARAM1
import com.pam.gemastik_app.ARG_PARAM2
import com.pam.gemastik_app.R
import com.pam.gemastik_app.ui.activity.HomeActivity
import com.pam.gemastik_app.ui.activity.CameraActivity
import com.pam.gemastik_app.ui.activity.SaladActivity
import com.pam.gemastik_app.ui.activity.UserActivity

/**
 * A simple [Fragment] subclass.
 * Use the [MenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MenuFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        // Set click listeners for each ImageButton
        val homeButton: ImageButton = view.findViewById(R.id.imageButtonHome)
        homeButton.setOnClickListener {
            val intent = Intent(activity, HomeActivity::class.java)
            startActivity(intent)
        }

        val cameraButton: ImageButton = view.findViewById(R.id.imageButtonCamera)
        cameraButton.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }

        val saladButton: ImageButton = view.findViewById(R.id.imageButtonSalad)
        saladButton.setOnClickListener {
            val intent = Intent(activity, SaladActivity::class.java)
            startActivity(intent)
        }

        val userButton: ImageButton = view.findViewById(R.id.imageButtonUser)
        userButton.setOnClickListener {
            val intent = Intent(activity, UserActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MenuFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
