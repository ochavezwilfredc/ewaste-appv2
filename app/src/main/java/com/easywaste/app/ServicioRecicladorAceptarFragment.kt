package com.easywaste.app

import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.SupportMapFragment
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import java.lang.Exception

import java.util.ArrayList


class ServicioRecicladorAceptarFragment : Fragment() {

    var loc : ClsLocalizacion? =null
    var txtTiempoEstimado : TextView? =null
    var cardViewInfo : CardView? =null
    var SERVICIOID:Int = 0
    var parent:ServicioRecicladorOperacionFragment? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val acti = activity as AppCompatActivity
        parent = parentFragment as ServicioRecicladorOperacionFragment
        SERVICIOID = parent!!.SERVICIOID
        val servicio = parent?.SERVICIO
        val servicio_direccion = parent?.SERVICIO_DIRECCION

        val view = inflater.inflate(R.layout.servicio_reciclador_aceptar, container, false)
        val txtProveedor:TextView = view.findViewById(R.id.proveedor)
        txtProveedor.text = servicio?.proveedor
        val txtDireccion:TextView = view.findViewById(R.id.direccion)
        txtDireccion.text = servicio_direccion?.direccion
        txtTiempoEstimado = view.findViewById(R.id.tiempoestimado)
        val btnVolver:Button = view.findViewById(R.id.btnVolver)
        val btnCancelar:Button = view.findViewById(R.id.btnCancelar)
        val btnAceptar:Button = view.findViewById(R.id.btnAceptar)
        btnAceptar.setOnClickListener {
            atenderServicio()
        }
        btnCancelar.setOnClickListener {
            cancelarServicio()
        }
        btnVolver.setOnClickListener {
            parent!!.fragmentManager?.popBackStackImmediate()
        }

        Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()
        try {
            val locationAddress = ClsLocationAdress()
            locationAddress.getAddressFromLocation(
                servicio_direccion!!.posicion.latitude, servicio_direccion.posicion.longitude,
                context!!, ServicioProveedorReferenciaDialog.GeocoderHandler(txtDireccion)
            )
            parent!!.dibujarRuta(ClsLocalizacion.lastLatLong!!, servicio_direccion.posicion, txtTiempoEstimado)
        }catch (ex:Exception){
            Toast.makeText(context,  "Error al mostrar la ruta, intente nuevamente.", Toast.LENGTH_SHORT).show()
            fragmentManager?.popBackStackImmediate()
        }

        return view
    }

    fun cancelarServicio(){
        val activity = activity as MainActivity?
        Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()
        val params = HashMap<String,Any>()
        params["servicio_id"] = SERVICIOID
        params["reciclador_id"] = Prefs.pullId()

        val parameters = JSONObject(params as Map<String, Any>)
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_cancel"),parameters,
            Response.Listener { response ->
                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        AlertaMensaje.mostrarSuccess(activity!! ,response.getString("mensaje"))
                    }else{
                        AlertaMensaje.mostrarError(activity!!,response.getString("mensaje"))
                    }
                    activity.onBackPressed()
                }

            }, Response.ErrorListener{
                try {
                    if( it.networkResponse == null ||  it.networkResponse.statusCode == 203
                        || it.networkResponse.statusCode == 500){
                        Prefs.putServicioId(0)
                        activity!!.cambiarFragment(ServicioRecicladorSolicitudesFragment())
                        Toast.makeText(context,  "Servicio cancelado", Toast.LENGTH_SHORT).show()
                    }

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

    fun atenderServicio(){
        val params = HashMap<String,Any>()
        params["servicio_id"] =  SERVICIOID
        params["tiempo_aprox"] =  parent?.tiempoEstimado!!

        val parameters = JSONObject(params as Map<String, Any>)
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_atender"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        Prefs.putServicioRecicladorId(SERVICIOID)
                        Prefs.putString("SERVICIORECICLADOR_FECHA", "")
                        parent!!.cardViewInfo?.visibility = View.INVISIBLE
                        parent!!.childFragmentManager.beginTransaction().replace(R.id.subfragmento, ServicioRecicladorLlegoFragment()).commit()
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