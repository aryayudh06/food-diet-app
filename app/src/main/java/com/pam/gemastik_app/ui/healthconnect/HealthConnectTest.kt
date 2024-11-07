import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pam.gemastik_app.R
import com.pam.gemastik_app.model.healthconnect.HealthConnectManager
import kotlinx.coroutines.launch

class HealthConnectTest : AppCompatActivity() {
    private lateinit var healthConnectManager: HealthConnectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_connect_test)

        healthConnectManager = HealthConnectManager(this)

        val caloriesTextView = findViewById<TextView>(R.id.tvCalBurned)

        lifecycleScope.launch {
            val caloriesBurned = healthConnectManager.readCaloriesBurnedToday()
            caloriesTextView.text = "Calories Burned Today: ${caloriesBurned ?: "No data"} kcal"
        }
    }
}
