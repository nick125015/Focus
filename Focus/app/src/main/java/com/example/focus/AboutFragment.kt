package com.example.focus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        val courseButton: Button = view.findViewById(R.id.courseButton)
        courseButton.setOnClickListener {
            showConfirmationDialog("將引導至本課程網站", "http://120.107.155.68/~cmchen/index.html")
        }

        val mathButton: Button = view.findViewById(R.id.mathButton)
        mathButton.setOnClickListener {
            showConfirmationDialog("將引導至彰師大數學系網站", "http://www.math.ncue.edu.tw/")
        }

        return view
    }

    private fun showConfirmationDialog(message: String, url: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(message)
            .setPositiveButton("確定") { dialog, _ ->
                openWebsite(url)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
