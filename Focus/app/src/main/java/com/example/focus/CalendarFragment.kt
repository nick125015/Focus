package com.example.focus

import android.app.DatePickerDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.*

class CalendarFragment : Fragment() {
    private lateinit var selectDateButton: Button
    private lateinit var descriptionEditText: EditText
    private lateinit var addButton: Button
    private lateinit var dbrw: SQLiteDatabase
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        dbrw = DBHelper(requireContext()).writableDatabase

        selectDateButton = view.findViewById(R.id.selectDateButton)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        addButton = view.findViewById(R.id.addButton)

        selectDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        addButton.setOnClickListener {
            val date = "${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(
                Calendar.DAY_OF_MONTH
            )}"
            val description = descriptionEditText.text.toString().trim()

            if (description.isNotEmpty()) {
                saveRecordToDatabase(date, description)
            } else {
                Toast.makeText(requireContext(), "請輸入描述", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(year, month, day)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun saveRecordToDatabase(date: String, description: String) {
        try {
            dbrw.execSQL(
                "INSERT INTO dateTable(date, describe) VALUES(?,?)",
                arrayOf(date, description)
            )
            showToast("新增日期: $date, 描述: $description")
        } catch (e: Exception) {
            showToast("新增失敗: $e")
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        dbrw.close()
        super.onDestroy()
    }
}
