package com.example.focus

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class RecordsFragment : Fragment() {
    private lateinit var dbrw: SQLiteDatabase
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var recordsList: ArrayList<String>
    private var selectedItemPosition: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        dbrw = DBHelper(requireContext()).writableDatabase
        listView = view.findViewById(R.id.previousRecordsListView)

        listView.setOnItemLongClickListener { _, view, position, _ ->
            selectedItemPosition = position
            registerForContextMenu(view)
            false
        }
        displayPreviousRecords()

        return view
    }

    private fun displayPreviousRecords() {
        val cursor: Cursor = dbrw.rawQuery("SELECT * FROM myTable", null)
        recordsList = ArrayList()

        val timeIndex = cursor.getColumnIndex("time")
        val describeIndex = cursor.getColumnIndex("describe")

        while (cursor.moveToNext()) {
            val time = if (timeIndex != -1) cursor.getString(timeIndex) else ""
            val describe = if (describeIndex != -1) cursor.getString(describeIndex) else ""
            recordsList.add("時間: $time\t\t\t\t描述: $describe")
        }

        cursor.close()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, recordsList)
        listView.adapter = adapter
    }

    override fun onDestroy() {
        dbrw.close()
        super.onDestroy()
    }

    private fun refreshListView() {
        val cursor: Cursor = dbrw.rawQuery("SELECT * FROM myTable", null)
        recordsList.clear()
        val timeIndex = cursor.getColumnIndex("time")
        val describeIndex = cursor.getColumnIndex("describe")

        while (cursor.moveToNext()) {
            val time = if (timeIndex != -1) cursor.getString(timeIndex) else ""
            val describe = if (describeIndex != -1) cursor.getString(describeIndex) else ""
            recordsList.add("時間: $time\t\t\t\t描述: $describe")
        }

        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_description -> {
                editDescription(selectedItemPosition)
                true
            }
            R.id.delete_record -> {
                deleteRecord(selectedItemPosition)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun editDescription(position: Int) {
        val oldDescription = getDescriptionFromList(position)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("編輯描述")
        val input = EditText(requireContext())
        input.setText(oldDescription)
        builder.setView(input)
        builder.setPositiveButton("確定") { dialog, _ ->
            val newDescription = input.text.toString()
            updateDescription(position, newDescription)
            dialog.dismiss()
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun getDescriptionFromList(position: Int): String {
        val record = recordsList[position]
        val regex = Regex("描述: (.+)")
        val matchResult = regex.find(record)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    private fun updateDescription(position: Int, newDescription: String) {
        val record = recordsList[position]
        val regex = Regex("時間: (.+)\t\t\t\t描述: (.+)")
        val matchResult = regex.find(record)
        val time = matchResult?.groupValues?.get(1) ?: ""
        val oldDescription = matchResult?.groupValues?.get(2) ?: ""

        val modifiedRecord = regex.replace(record) {
            "時間: ${it.groupValues[1]}\t\t\t\t描述: $newDescription"
        }
        recordsList[position] = modifiedRecord
        adapter.notifyDataSetChanged()
        showToast("描述已更新")

        val values = ContentValues()
        values.put("describe", newDescription)
        val whereClause = "time = ? AND describe = ?"
        val whereArgs = arrayOf(time, oldDescription)
        val rowsAffected = dbrw.update("myTable", values, whereClause, whereArgs)
        if (rowsAffected > 0) {
            showToast("資料已更新")
        } else {
            showToast("資料更新失敗")
        }
    }


    private fun deleteRecord(position: Int) {
        val record = recordsList[position]
        val regex = Regex("時間: (.+)\t\t\t\t描述: (.+)")
        val matchResult = regex.find(record)
        val time = matchResult?.groupValues?.get(1) ?: ""
        val description = matchResult?.groupValues?.get(2) ?: ""

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("刪除紀錄")
        builder.setMessage("確定要刪除時間: $time, 描述: $description 的紀錄嗎？")
        builder.setPositiveButton("確定") { dialog, _ ->
            deleteRecordFromDatabase(time, description)
            dialog.dismiss()
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deleteRecordFromDatabase(time: String, description: String) {
        try {
            dbrw.execSQL(
                "DELETE FROM myTable WHERE time = ? AND describe = ?",
                arrayOf(time, description)
            )
            showToast("紀錄已刪除")
            refreshListView()
        } catch (e: Exception) {
            showToast("刪除失敗: $e")
        }
    }
}