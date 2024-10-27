package com.pam.gemastik_app.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.pam.gemastik_app.R
import com.pam.gemastik_app.model.UserCalorie
import com.pam.gemastik_app.thread.CalorieAccess

/**
 * A simple [Fragment] subclass.
 * Use the [ChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChartFragment : Fragment() {
    private lateinit var barChart: BarChart
    private var currentActivityName: String? = null
    private val calorieAccess = CalorieAccess()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentActivityName = it.getString(ARG_ACTIVITY_NAME)
        }
        calorieAccess.saveData("167", "60", "3500")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chart, container, false)
        barChart = view.findViewById(R.id.barchart)

        loadCalorieData()

        return view
    }

    private fun loadCalorieData() {
        calorieAccess.getCalorieData { calorieList ->
            if (calorieList.isNotEmpty()) {
                val entries = mutableListOf<BarEntry>()
                for ((index, userCalorie) in calorieList.withIndex()) {
                    val calorieValue = userCalorie.calorie?.toFloatOrNull() ?: 0f
                    entries.add(BarEntry(index.toFloat(), calorieValue))
                }
                displayChart(entries)
            } else {
                Log.d("Calorie Data", "No data available")
            }
        }
    }

    private fun displayChart(entries: List<BarEntry>) {
        val barDataSet = BarDataSet(entries, "Calories")
//        barDataSet.color = resources.getColor(R.color.colorAccent, null)
        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.description.text = "Daily Calorie Intake"
        barChart.setFitBars(true)
        barChart.animateY(1000)
        barChart.invalidate() // refresh chart
    }

    companion object {
        private const val ARG_ACTIVITY_NAME = "activity_name"

        fun newInstance(activityName: String): ChartFragment {
            return ChartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACTIVITY_NAME, activityName)
                }
            }
        }
    }
}
