package com.easywaste.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class ServicioProveedorEnCaminoFragment : Fragment() {

    var loc : ClsLocalizacion? =null
    var txtTiempoEstimado : TextView? =null
    var cardViewInfo : CardView? =null
    var SERVICIO_DIRECCION:ClsServicioDireccion? = null
    var SERVICIO:ClsServicio? = null
    var txtRecicladorDni:TextView? = null
    var txtReciclador:TextView? = null
    var OK = true
    val TIEMPO_ACTUALIZACION_RECICLADOR:Long = 1000*6
    var posicionProveedor:LatLng? =null
    var posicionReciclador:LatLng?= null
    var requestQueue:RequestQueue? =null
    var txtLLego:TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val acti = activity as AppCompatActivity
        loc = ClsLocalizacion(acti)
        val view = inflater.inflate(R.layout.servicio_proveedor_encaminor, container, false)

        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.frg) as SupportMapFragment
        mapFragment.getMapAsync(loc)


        loc!!.gmap?.clear()
        requestQueue = Volley.newRequestQueue(activity!!.applicationContext)

        txtReciclador = view.findViewById(R.id.reciclador)
        txtRecicladorDni= view.findViewById(R.id.dni)
        txtTiempoEstimado = view.findViewById(R.id.tiempoestimado)
        cardViewInfo = view.findViewById(R.id.infoserviciomapa)
        txtLLego = view.findViewById(R.id.txtLlego)
        cardViewInfo?.visibility = View.INVISIBLE

        val btnCancelar:Button = view.findViewById(R.id.btnCancelar)
        btnCancelar.setOnClickListener {

        }

        //  Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(OK){
                    try {
                        buscarServicioProveedorEstado()
                        mainHandler.postDelayed(this, 5000)
                    }catch (ex:Exception){

                    }
                }

            }
        })

        return view
    }

    fun buscarServicioProveedorEstado(){
        val params = HashMap<String,Any>()
        params["servicio_id"] =  Prefs.pullServicioId()
        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_info"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        val datos =  response.getJSONObject("datos")
                        val proveedor = datos.getString("proveedor")
                        val latitud = datos.getDouble("latitud")
                        val longitud = datos.getDouble("longitud")
                        val estado = datos.getString("estado")
                        val reciclador = datos.getString("reciclador")
                        val reciclador_dni = datos.getString("reciclador_dni")
                        val tiempo_aproximado = datos.getInt("tiempo_aprox_atencion")
                        SERVICIO = ClsServicio(Prefs.pullServicioId(), proveedor)
                        SERVICIO_DIRECCION = ClsServicioDireccion("" , LatLng(latitud,longitud))


                        if (estado == "En Camino"){
                            if(posicionProveedor==null){
                                posicionProveedor = LatLng(latitud,longitud)
                                ClsLocalizacion.lastLatLong = posicionProveedor
                            }
                            txtRecicladorDni?.text =reciclador_dni
                            txtReciclador?.text = reciclador
                            txtTiempoEstimado?.text= "Llega en $tiempo_aproximado minutos"
                            cardViewInfo?.visibility = View.VISIBLE
                            buscarPosicionReciclador()
                        }else if(estado=="En Atencion"){
                            txtLLego?.visibility = View.VISIBLE
                        }
                        else if(estado == "Finalizado"){

                            try {
                                OK = false
                                val mainActivity:MainActivity = activity as MainActivity
                                mainActivity.cambiarFragment(ServicioProveedorFinalizadoFragment())
                            }catch (ex:Exception){
                                //OK = false
                            }
                        }



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
                    Toast.makeText(context,  "Error de conexión", Toast.LENGTH_SHORT).show()
                  //  fragmentManager?.popBackStackImmediate()
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


    fun buscarPosicionReciclador(){
        val params = HashMap<String,Any>()
        params["servicio_id"] =  Prefs.pullServicioId()
        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("position_get"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        val datos =  response.getJSONObject("datos")
                        val latitud = datos.getDouble("lat_actual")
                        val longitud = datos.getDouble("lon_actual")
                        if(posicionReciclador==null && posicionProveedor!=null){
                            posicionReciclador = LatLng(latitud,longitud)
                            loc!!.gmap?.clear()
                            val markerproveedor = loc!!.markerProveedor(posicionProveedor!!)
                            loc!!.agregarMarcador(markerproveedor)
                            val markerpreciclador = loc!!.markerReciclador(posicionReciclador!!)
                            loc!!.agregarMarcador(markerpreciclador)
                            Timer().schedule(6000) {
                                try {
                                    dibujarRuta(posicionReciclador!!,posicionProveedor!!,null)
                                }catch (ex:Exception){

                                }
                            }

                            val data = JSONArray(Prefs.pullServicioRecicladoresCercanos())
                            for (i in 0 until data.length()){
                                val ele = data.getJSONObject(i)
                                if(ele.getString("elegido")=="0"){
                                    val posicion = LatLng(ele.getDouble("lat"),ele.getDouble("lng"))
                                    val marker = loc!!.markerNormal(posicion)
                                }
                            }

                        }

                        if(posicionReciclador!=null){
                            loc!!.markerReciclador?.position = LatLng(latitud,longitud)
                        }

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



    fun dibujarRuta(origin:LatLng, dest:LatLng, txtTiempoEstimado:TextView?){
        val url = urlMapsApi(origin, dest)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url,null,
            Response.Listener { response ->
                var routes: List<List<java.util.HashMap<String, String>>>? = null

                try {

                    val parser = DirectionsJSONParser()
                    routes = parser.parse(response)
                    if(txtTiempoEstimado!=null) txtTiempoEstimado.text = "Llega en "+ parser.parseTiempo()

                    var points: ArrayList<LatLng>? = null
                    var lineOptions: PolylineOptions? = null

                    for (i in routes.indices) {
                        points = ArrayList()
                        lineOptions = PolylineOptions()

                        // Fetching i-th route
                        val path = routes.get(i)

                        // Fetching all the points in i-th route
                        for (j in path.indices) {
                            val point = path.get(j)
                            val lat = java.lang.Double.parseDouble(point.get("lat")!!)
                            val lng = java.lang.Double.parseDouble(point.get("lng")!!)
                            val position = LatLng(lat, lng)
                            points.add(position)
                        }

                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points)
                        lineOptions.width(8f)
                        lineOptions.color(Color.RED)
                    }

                    loc?.gmap?.addPolyline(lineOptions)
                    cardViewInfo?.visibility = View.VISIBLE


                } catch (e: Exception) {
                    Log.e("eerr", e.message.toString())
                    Toast.makeText(context,  "Error de estimación", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }


            },
            Response.ErrorListener{
                //      Toast.makeText(activity,  "Error de conexión", Toast.LENGTH_SHORT).show()
            }) {}


        requestQueue?.add(request)

    }



    fun urlMapsApi(origin:LatLng, dest:LatLng):String{
        if(isAdded()) {
            val str_origin = "origin=" + origin.latitude + "," + origin.longitude

            val str_dest = "destination=" + dest.latitude + "," + dest.longitude

            val key = "key=" + getString(R.string.api_google)

            val parameters = str_origin + "&" + str_dest + "&" + key

            val output = "json?language=es&"

            val url = "https://maps.googleapis.com/maps/api/directions/" + output + parameters;
            return url
        }
      return ""
    }


}