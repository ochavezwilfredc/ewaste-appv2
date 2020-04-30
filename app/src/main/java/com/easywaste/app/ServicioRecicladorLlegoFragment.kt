package com.easywaste.app

import android.content.DialogInterface
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap


class ServicioRecicladorLlegoFragment : Fragment() {

    var OK = true
    var parent:ServicioRecicladorOperacionFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val acti = activity as AppCompatActivity
        parent = parentFragment as ServicioRecicladorOperacionFragment
        val view = inflater.inflate(R.layout.servicio_reciclador_llego, container, false)
        parent!!.cardViewInfo?.visibility = View.VISIBLE
        val txtDireccion:TextView = view.findViewById(R.id.direccion)
        val contenedorLlego:LinearLayout = view.findViewById(R.id.contenedorLlego)
        contenedorLlego.visibility = View.GONE

        val btnLlego:Button = view.findViewById(R.id.btnLlego)
        val txtEspera:TextView = view.findViewById(R.id.txtEspera)
        val btnAusente:Button = view.findViewById(R.id.btnAusente)
        val btnProveedorSalio:Button = view.findViewById(R.id.btnSalioProveedor)
        val servicio = parent?.SERVICIO
        val servicio_direccion = parent?.SERVICIO_DIRECCION
        var txtSalioProveedor =
        try {
            val locationAddress = ClsLocationAdress()
            locationAddress.getAddressFromLocation(
                servicio_direccion!!.posicion.latitude, servicio_direccion.posicion.longitude,
                context!!, ServicioProveedorReferenciaDialog.GeocoderHandler(txtDireccion)
            )
            parent!!.dibujarRuta(ClsLocalizacion.lastLatLong!!, servicio_direccion.posicion, null)
        }catch (ex:Exception){
            Toast.makeText(context,  "Error al mostrar la ruta, intente nuevamente.", Toast.LENGTH_SHORT).show()
            try{
                fragmentManager?.popBackStackImmediate()
            }catch(ex:Exception){

            }
        }

        btnLlego.setOnClickListener {

            val params = HashMap<String,Any>()
            params["parametro"] =  3
            params["id"] =  Prefs.pullServicioRecicladorId()

            val parameters = JSONObject(params as Map<String, Any>)
            val request : JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, VAR.url("servicio_update_estado"),parameters,
                Response.Listener { response ->

                    if(response!=null){
                        if(response.getInt("estado") == 200 ){
                            contenedorLlego.visibility = View.VISIBLE
                            val time = Calendar.getInstance().timeInMillis.toString()
                            Prefs.putString("SERVICIORECICLADOR_FECHA", time)
                            btnLlego.visibility = View.GONE
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
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(OK){
                    try {
                        mostrarBtnEspere(contenedorLlego,btnLlego,btnAusente)
                        mainHandler.postDelayed(this, 3000)
                    }catch (ex:Exception){

                    }
                }

            }
        })
        mostrarBtnEspere(contenedorLlego,btnLlego,btnAusente)

        btnProveedorSalio.setOnClickListener {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Confirmar servicio")
            builder.setMessage("Desea finalizar el servicio?")

            val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
                when(which){
                    DialogInterface.BUTTON_POSITIVE -> {
                        confirmarServicio()
                    }
                }
            }

            builder.setPositiveButton("SI",dialogClickListener)
            builder.setNegativeButton("NO",dialogClickListener)
            val dialog = builder.create()
            dialog.show()
        }
        btnAusente.setOnClickListener {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Cancelar servicio")
            builder.setMessage("Confirmar que el proveedor no se encuentra.")

            val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
                when(which){
                    DialogInterface.BUTTON_POSITIVE -> {
                        cancelarServicio()
                    }
                }
            }

            builder.setPositiveButton("SI",dialogClickListener)
            builder.setNegativeButton("NO",dialogClickListener)
            val dialog = builder.create()
            dialog.show()
        }
        return view
    }

    fun mostrarBtnEspere( contenedorLLego:LinearLayout, btnLLego:Button, btnAusente:Button){
        if(Prefs.pullString("SERVICIORECICLADOR_FECHA")=="0") {
            contenedorLLego.visibility = View.GONE
            btnLLego.visibility = View.VISIBLE
            OK = false
        }else if(Prefs.pullString("SERVICIORECICLADOR_FECHA")!=""){
            btnLLego.visibility = View.GONE
            contenedorLLego.visibility = View.VISIBLE
            val ahora = Calendar.getInstance().timeInMillis
            val tiempo = Prefs.pullString("SERVICIORECICLADOR_FECHA").toLong()
            val diff = ((ahora - tiempo)/1000).toInt()
            if(diff>10*5){
               // btnAusente.visibility = View.VISIBLE
                OK = false
            }
        }
    }
    fun confirmarServicio(){
        val mainActivity:MainActivity = activity as MainActivity

        val params = HashMap<String,Any>()
        params["parametro"] =  4
        params["id"] =  Prefs.pullServicioRecicladorId()

        val parameters = JSONObject(params as Map<String, Any>)
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_update_estado"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        Prefs.putServicioRecicladorId(0)

                        AlertaMensaje.mostrarSuccess(activity!!,response.getString("mensaje"))
                        val frag = ServicioRecicladorSolicitudesFragment()
                        mainActivity.cambiarFragment(frag)
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

    fun proveedorPintrash(){

    }

    fun cancelarServicio(){
        val mainActivity:MainActivity = activity as MainActivity

        val params = HashMap<String,Any>()
        params["parametro"] =  5
        params["id"] =  Prefs.pullServicioRecicladorId()

        val parameters = JSONObject(params as Map<String, Any>)
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_update_estado"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        AlertaMensaje.mostrarSuccess(activity!!,response.getString("mensaje"))
                        val frag = ServicioRecicladorSolicitudesFragment()
                        mainActivity.cambiarFragment(frag)
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