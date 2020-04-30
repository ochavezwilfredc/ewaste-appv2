package com.easywaste.app.Clases
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.util.Locale

class ClsLocationAdress {
    private val TAG = "LocationAddress"

    fun getAddressFromLocation(
        latitude: Double, longitude: Double,
        context: Context, handler: Handler
    ) {
        val thread = object : Thread() {
            override fun run() {
                val geocoder = Geocoder(context, Locale.getDefault())
                var result: String? = null
                try {
                    val addressList = geocoder.getFromLocation(
                        latitude, longitude, 1
                    )
                    if (addressList != null && addressList.size > 0) {
                        val ad = addressList.get(0)
                        val direcArr = ad.getAddressLine(0).toString(). split(",")

                        result = direcArr[0]+ " , " + ad.locality
                    }

                } catch (e: IOException) {
                    Log.e(TAG, "Unable connect to Geocoder", e)
                } finally {
                    val message = Message.obtain()
                    message.target = handler
                    if (result != null) {
                        message.what = 1
                        val bundle = Bundle()
                        bundle.putString("address", result)
                        message.data = bundle
                    } else {
                        message.what = 1
                        val bundle = Bundle()
                        result = "-"
                        bundle.putString("address", result)
                        message.data = bundle
                    }


                    Log.e("direccion", result.toString())
                    message.sendToTarget()
                }
            }
        }
        thread.start()
    }
}