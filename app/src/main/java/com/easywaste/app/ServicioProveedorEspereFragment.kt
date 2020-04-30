package com.easywaste.app

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import org.json.JSONObject
import java.lang.Exception

class ServicioProveedorEspereFragment : Fragment() {

    var OK = true
    var btnCancelarServicio:Button?= null
    var mainActivity:MainActivity?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.servicio_proveedor_espere, container, false)
        mainActivity = activity as MainActivity
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

        return view
    }

    fun buscarServicio(){


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


                        if(estado == "Abierto"){
                            btnCancelarServicio?.visibility = View.VISIBLE
                        }else if (estado == "En Camino" || estado == "En Atencion"){
                            OK = false
                            mainActivity?.cambiarFragment(ServicioProveedorEnCaminoFragment())
                            btnCancelarServicio?.visibility = View.GONE
                        }else if(estado == "Finalizado"){
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
                        Toast.makeText(context,  "Servicio finalizado.", Toast.LENGTH_SHORT).show()
                        mainActivity?.cambiarFragment(ServicioProveedorRegistrarFragment())
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
        params["id"] = Prefs.pullServicioId()
        params["parametro"] = 5

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