package com.easywaste.app

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.Rating
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception

class ServicioProveedorFinalizadoFragment : Fragment() {


    var txtNombre:TextView? = null
    var txtDNI:TextView? = null
    var barraPuntaje:AppCompatRatingBar? = null
    var contenedor:LinearLayout? = null
    var requestQueue:RequestQueue? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.servicio_proveedor_finalizado, container, false)
        requestQueue = Volley.newRequestQueue(context)
        val btnFinalizar:Button = view.findViewById(R.id.btnFinalizar)
        barraPuntaje = view.findViewById(R.id.ratingBar)
        txtNombre = view.findViewById(R.id.nombre)
        txtDNI = view.findViewById(R.id.dni)
        buscarServicio()
        btnFinalizar.setOnClickListener {
            finalizarServicio()
        }
         contenedor = view.findViewById(R.id.contenedor)
        return view
    }
    fun buscarServicio(){

        val act = activity as MainActivity

        val params = HashMap<String,Any>()
        params["servicio_id"] =  Prefs.pullServicioId()
        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_info"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        val datos =  response.getJSONObject("datos")
                        val estado = datos.getString("estado")
                        val reciclador = datos.getString("reciclador")
                        val reciclador_dni = datos.getString("reciclador_dni")
                        txtDNI?.text = reciclador_dni
                        txtNombre?.text = reciclador
                        contenedor?.visibility = View.VISIBLE

                    }

                }

            },
            Response.ErrorListener{
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    ex.printStackTrace()
                    Toast.makeText(context,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }

            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> =HashMap()
                params["TOKEN"] =  Prefs.pullToken()
                return params
            }
        }

        requestQueue?.add(request)
    }

    fun finalizarServicio(){


        val params = HashMap<String,Any>()
        params["servicio_id"] =  Prefs.pullServicioId()
        params["calificacion"] =  barraPuntaje?.rating!!.toInt()

        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_calificacion"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){

                        AlertaMensaje.mostrarSuccess(activity!!,response.getString("mensaje"))
                        asignarPinTrash()

                    }else{
                        AlertaMensaje.mostrarError(activity!!,response.getString("mensaje"))

                    }

                }

            },
            Response.ErrorListener{
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    ex.printStackTrace()
                    Log.e("error",ex.message)
                    Toast.makeText(context,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }

            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> =HashMap()
                params["TOKEN"] =  Prefs.pullToken()
                return params
            }
        }

        requestQueue?.add(request)
    }


    fun asignarPinTrash(){

        val mainActivity = activity as MainActivity
        val params = HashMap<String,Any>()
        params["proveedor_id"] =  Prefs.pullId()

        val parameters = JSONObject(params as Map<String, Any>)
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("proveedor_pintrash"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                        Prefs.putServicioId(0)
                        mainActivity.cambiarFragment(ServicioProveedorRegistrarFragment())
                    }else{
                        Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    }
                }

            },
            Response.ErrorListener{
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    Toast.makeText(context,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }

            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> =HashMap()
                params["TOKEN"] =  Prefs.pullToken()
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }
}