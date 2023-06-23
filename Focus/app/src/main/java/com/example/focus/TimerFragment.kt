package com.example.focus

import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {
    private lateinit var chronometer: Chronometer
    private lateinit var startPauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var describe: String
    private lateinit var dbrw: SQLiteDatabase
    private var isRunning: Boolean = false
    private var pausedTime: Long = 0
    private var isFullScreen = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        dbrw = DBHelper(requireContext()).writableDatabase

        chronometer = view.findViewById(R.id.chronometer)
        startPauseButton = view.findViewById(R.id.button_start_pause)
        stopButton = view.findViewById(R.id.button_stop)

        startPauseButton.setOnClickListener {
            if (isRunning) {
                pauseChronometer()
            } else {
                startChronometer()
            }
        }

        stopButton.setOnClickListener {
            showDescriptionDialog()
        }

        return view
    }

    override fun onDestroy() {
        dbrw.close()
        super.onDestroy()
    }

    private fun startChronometer() {
        if (pausedTime == 0L) {
            chronometer.base = SystemClock.elapsedRealtime()
        } else {
            val elapsedTime = SystemClock.elapsedRealtime() - pausedTime
            chronometer.base += elapsedTime
            pausedTime = 0
        }
        chronometer.start()
        isRunning = true
        startPauseButton.text = "暫停"
        toFullScreenMode()
    }

    private fun pauseChronometer() {
        chronometer.stop()
        pausedTime = SystemClock.elapsedRealtime()
        isRunning = false
        startPauseButton.text = "繼續"
        toFullScreenMode()
    }

    private fun showDescriptionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("輸入描述")
        val input = EditText(requireContext())
        builder.setView(input)
        builder.setPositiveButton("確定") { dialog, _ ->
            describe = input.text.toString()
            if (describe.isNotEmpty()) {
                pauseChronometer() // 暂停计时器
                saveRecordToDatabase()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "請輸入描述", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()

        pauseChronometer()
    }

    private fun saveRecordToDatabase() {
        val time = chronometer.text.toString()
        try {
            dbrw.execSQL(
                "INSERT INTO myTable(time, describe) VALUES(?,?)",
                arrayOf(time, describe)
            )
            showToast("新增時間: $time, 描述: $describe")
        } catch (e: Exception) {
            showToast("新增失敗: $e")
        }
        chronometer.base = SystemClock.elapsedRealtime()
        pausedTime = 0
        outFullScreenMode()
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }
    private fun toFullScreenMode() {
        val window: Window = activity?.window ?: return

        // 進入全螢幕模式
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        isFullScreen = true
    }
    private fun outFullScreenMode() {
        val window: Window = activity?.window ?: return

        // 退出全螢幕模式
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        isFullScreen = false

    }

}