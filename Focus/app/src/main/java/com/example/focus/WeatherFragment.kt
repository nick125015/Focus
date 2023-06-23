package com.example.focus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class WeatherFragment : Fragment() {
    private lateinit var locationSpinner: Spinner
    private lateinit var button: Button

    private val cities = arrayOf(
        "臺北市", "新北市", "桃園市", "臺中市", "臺南市", "高雄市", "基隆市", "新竹縣", "新竹市", "苗栗縣",
        "彰化縣", "南投縣", "雲林縣", "嘉義縣", "嘉義市", "屏東縣", "宜蘭縣", "花蓮縣", "台東縣", "澎湖縣",
        "金門縣", "連江縣"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)

        locationSpinner = view.findViewById(R.id.locationSpinner)
        button = view.findViewById(R.id.button)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = adapter

        button.setOnClickListener {
            makeApiCall()
        }

        return view
    }

    private fun makeApiCall() {
        val client = OkHttpClient()
        val url =
            "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-4DAAE268-702B-4649-A3E3-AD474B4A8CA3&sort=time"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    showToast("網路連接錯誤")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val recordObject = jsonObject.getJSONObject("records")
                    val locationArray = recordObject.getJSONArray("location")
                    var i = 0

                    val whereLive = locationSpinner.selectedItem.toString()

                    while (i < locationArray.length()) {
                        val locationObject = locationArray.getJSONObject(i)
                        val locationName = locationObject.getString("locationName")

                        if (locationName == whereLive) {
                            requireActivity().runOnUiThread {
                                view?.findViewById<TextView>(R.id.textView4)?.text = "$whereLive 的天氣"
                            }

                            val weatherElementArray =
                                locationObject.getJSONArray("weatherElement")
                            val timeArray = weatherElementArray.getJSONObject(0)
                                .getJSONArray("time")

                            for (j in 0 until 3) {
                                val timeObject = timeArray.getJSONObject(j)
                                val startTime = timeObject.getString("startTime")
                                val parameterObject = timeObject.getJSONObject("parameter")
                                val parameterName = parameterObject.getString("parameterName")

                                val textViewId =
                                    resources.getIdentifier("Time$j", "id", requireActivity().packageName)
                                requireActivity().runOnUiThread {
                                    view?.findViewById<TextView>(textViewId)?.text =
                                        "時間:$startTime  天氣狀況:$parameterName"
                                }
                            }

                            return
                        }

                        i++
                    }

                    requireActivity().runOnUiThread {
                        showToast("找不到相應的地區")
                    }
                } else {
                    requireActivity().runOnUiThread {
                        showToast("伺服器請求錯誤")
                    }
                }
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
