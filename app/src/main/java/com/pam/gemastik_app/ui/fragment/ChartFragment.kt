package com.pam.gemastik_app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.pam.gemastik_app.R
import com.pam.gemastik_app.ui.fragment.MenuFragment.Companion
import java.security.KeyStore.Entry

/**
 * A simple [Fragment] subclass.
 * Use the [ChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChartFragment : Fragment() {
    private lateinit var barChart: BarChart
    private var currentActivityName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentActivityName = it.getString(com.pam.gemastik_app.ui.fragment.ChartFragment.ARG_ACTIVITY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chart, container, false)
        barChart = view.findViewById(R.id.barchart)

        setupBarChart()

        return view
    }

    private fun setupBarChart() {
        // Example data
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 10f))
        entries.add(BarEntry(2f, 20f))
        entries.add(BarEntry(3f, 30f))
        entries.add(BarEntry(4f, 40f))
        entries.add(BarEntry(5f, 50f))

        val barDataSet = BarDataSet(entries, "Your BMI")
        barChart.description.text = "Date"
        val data = BarData(barDataSet)
        barChart.data = data
        barChart.invalidate() // refresh
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