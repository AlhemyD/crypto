package com.example.crypto

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.crypto.MainViewModel.Companion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal


class RateCheckService : Service() {
    val handler = Handler(Looper.getMainLooper())
    lateinit var viewModel: MainViewModel
    var rateCheckAttempt = 0
    lateinit var startRate: BigDecimal
    lateinit var targetRate: BigDecimal
    val rateCheckInteractor = RateCheckInteractor()
    private val CHANNEL_ID = "MyNotificationChannel"
    private lateinit var rateCheckRunnable: Runnable
    var old_rate = BigDecimal("-1222")
    private lateinit var notificationManager: MyNotificationManager
    override fun onCreate() {
        super.onCreate()
        Log.d("onCreate: ", "Creating service")
        //sendNotification("CURRENT RATE", "Текущий курс: currentRate")
        notificationManager = MyNotificationManager(this)
    }
    private fun sendNotification(title: String, message: String){
        Log.d(title, " message: $message")
        notificationManager.sendNotification(title, message)
    }

    private fun requestAndCheckRate() {
        // Write your code here
        Log.d("RequestCheckService: ", "checking service")
        //viewModel.onRefreshClicked()

        GlobalScope.launch(Dispatchers.Main) {
            if (old_rate==BigDecimal("-1222")){
                old_rate=startRate
            }
            val rate = BigDecimal(rateCheckInteractor.requestRate())
            Log.d("RequestCheckService2: ", "new rate: $rate")
            var up_down=""
            when{
                old_rate<rate -> up_down = "^^^"
                old_rate == rate -> up_down="same"
                old_rate > rate -> up_down="VVV"
            }
            when{
                (up_down == "^^^" && rate.subtract(old_rate)>targetRate) -> sendNotification("CURRENT RATE", "Старый курс: $old_rate Текущий курс: $rate $up_down")
                (up_down == "VVV" && old_rate.subtract(rate)>targetRate) -> sendNotification("CURRENT RATE", "Старый курс: $old_rate Текущий курс: $rate $up_down")
                else -> Log.d("CURRENT RATE", "Старый курс: $old_rate Текущий курс: $rate $up_down")
            }
            old_rate=rate

        }


    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRate = BigDecimal(intent?.getStringExtra(ARG_START_RATE))
        targetRate = BigDecimal(intent?.getStringExtra(ARG_TARGET_RATE))

        Log.d("onStartCommand", "onStartCommand startRate = $startRate targetRate = $targetRate")
        //sendNotification("CURRENT RATE", "Текущий курс: $startRate")
        rateCheckRunnable = object : Runnable {

            // Write your code here. Check number of attempts and stop service if needed
            override fun run() {
                Log.d("RateCheckRunnable: ", "rateCheckRunnable function started")
                requestAndCheckRate()
                handler.postDelayed(this, 10000)
            }

        }
        handler.post(rateCheckRunnable)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onDestroy: ", "Destroying service")
        handler.removeCallbacks(rateCheckRunnable)
    }


    companion object {
        const val TAG = "RateCheckService"
        const val RATE_CHECK_INTERVAL = 5000L
        const val RATE_CHECK_ATTEMPTS_MAX = 100

        const val ARG_START_RATE = "ARG_START_RATE"
        const val ARG_TARGET_RATE = "ARG_TARGET_RATE"

        fun startService(context: Context, startRate: String, targetRate: String) {
            Log.d("StartService: ", "startService function started")

            context.startService(Intent(context, RateCheckService::class.java).apply {
                putExtra(ARG_START_RATE, startRate)
                putExtra(ARG_TARGET_RATE, targetRate)
            })
        }

        fun stopService(context: Context) {
            Log.d("StopService: ", "stopService function started")

            context.stopService(Intent(context, RateCheckService::class.java))
        }
    }
}