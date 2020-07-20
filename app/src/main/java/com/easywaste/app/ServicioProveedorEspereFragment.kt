package com.easywaste.app

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class ServicioProveedorEspereFragment : Fragment() {

    var OK = true
    var btnCancelarServicio:Button?= null
    var mainActivity:MainActivity?=null
    var loc : ClsLocalizacion? =null
    var posicionProveedor : LatLng? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.servicio_proveedor_espere, container, false)
        mainActivity = activity as MainActivity
        loc = ClsLocalizacion(mainActivity)

        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.frg) as SupportMapFragment
        mapFragment.getMapAsync(loc)

        btnCancelarServicio= view.findViewById<Button>(R.id.btnCancelar)
        btnCancelarServicio?.setOnClickListener {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Cancelar servicio")
            builder.setMessage("Está seguro que desea cancelar el servicio?")

            val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
                when(which){
                    DialogInterface.BUTTON_POSITIVE -> cancelarServicio()
                }
            }
            builder.setPositiveButton("SI",dialogClickListener)
            builder.setNegativeButton("NO",dialogClickListener)
            val dialog = builder.create()
            dialog.show()
        }

        buscarServicio()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(OK){
                    try {
                        buscarServicio()
                        mainHandler.postDelayed(this, 5000)
                    }catch (ex:Exception){

                    }
                }

            }
        })
        loc!!.gmap?.clear()
        return view
    }

    fun buscarServicio(){


        val params = JSONObject()
        params.put("servicio_id",  Prefs.pullServicioId())
        Log.d("parametrosServicio", params.toString())
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_info"),params,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){

                        val datos =  response.getJSONObject("datos")
                        val estado = datos.getString("estado")
                        val latitud = datos.getDouble("latitud")
                        val longitud = datos.getDouble("longitud")
                        if(posicionProveedor==null){
                            loc!!.gmap?.clear()
                            posicionProveedor = LatLng(latitud,longitud)
                            val markerproveedor = loc!!.markerProveedor(posicionProveedor!!)
                            loc!!.agregarMarcador(markerproveedor)
                            val reciclado = Prefs.pullServicioRecicladoresCercanos()
                            if(reciclado !=""){
                                val data = JSONArray(Prefs.pullServicioRecicladoresCercanos())


                                for (i in 0 until data.length()){
                                    val ele = data.getJSONObject(i)
                                    val posicion = LatLng(ele.getDouble("lat"),ele.getDouble("lng"))
                                    val marker = loc!!.markerNormal(posicion)
                                    marker.title =  ele.getString("reciclador_name")
                                    //val posicionReciclador = LatLng(ele.getDouble("lat"),ele.getDouble("lng"))
                                    //val markerpreciclador = loc!!.markerReciclador(posicionReciclador)
                                    //loc!!.agregarMarcador(markerpreciclador)
//v
                                }
                            }

                        }



                        if(estado == "Abierto"){
                            btnCancelarServicio?.visibility = View.VISIBLE
                        }else if (estado == "En Camino" || estado == "En Atencion"){
                            OK = false
                            mainActivity?.cambiarFragment(ServicioProveedorEnCaminoFragment())
                            btnCancelarServicio?.visibility = View.GONE
                         }else if(estado=="Cancelado"){

                                try {
                                    OK = false
                                    if(!datos.isNull("nuevo_id")){
                                        val  nuevo_id = datos.getInt("nuevo_id")
                                        Prefs.putServicioId(nuevo_id)
                                        Prefs.putServicioRecicladoresCercanos("")
                                        Log.d("servicioIDCambio", nuevo_id.toString())
                                        val mainActivity:MainActivity = activity as MainActivity
                                        mainActivity.cambiarFragment(ServicioProveedorEspereFragment())
                                    }
                                }catch (ex:Exception){
                                    //OK = false
                                }
                            }

                        else if(estado == "Finalizado"){
                            OK = false
                            mainActivity?.cambiarFragment(ServicioProveedorFinalizadoFragment())

                        }else{
                            Prefs.putServicioId(0)
                        }

                    }else{
                        Prefs.putServicioId(0)
                    }

                    if(Prefs.pullServicioId() == 0 ){
                        OK = false
                        btnCancelarServicio?.visibility = View.GONE
                        try {

                        }catch (ex:Exception){
                            Toast.makeText(requireActivity(),  "Servicio finalizado.", Toast.LENGTH_SHORT).show()
                            mainActivity?.cambiarFragment(ServicioProveedorRegistrarFragment())
                        }

                    }
                }

            },
            Response.ErrorListener{
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Prefs.putServicioId(0)
                    Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    ex.printStackTrace()
                    Toast.makeText(context,  "Error de conexión", Toast.LENGTH_SHORT).show()
                 //   fragmentManager?.popBackStackImmediate()
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
    fun cancelarServicio(){
        val activity = activity as MainActivity?
        Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()
        val params = HashMap<String,Any>()
        params["servicio_id"] = Prefs.pullServicioId()
        params["parametro"] = 5
        params["reciclador_id"] = Prefs.pullId()

        val parameters = JSONObject(params as Map<String, Any>)
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_update_estado"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        AlertaMensaje.mostrarSuccess(activity!! ,response.getString("mensaje"))
                        Prefs.putServicioId(0)
                        activity.cambiarFragment(ServicioProveedorRegistrarFragment())
                    }else{
                        AlertaMensaje.mostrarError(activity!!,response.getString("mensaje"))
                    }
                }

            }, Response.ErrorListener{

                try {

                    if( it.networkResponse == null ||  it.networkResponse.statusCode == 203
                        || it.networkResponse.statusCode == 500){
                        Prefs.putServicioId(0)
                        activity!!.cambiarFragment(ServicioProveedorRegistrarFragment())
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

}