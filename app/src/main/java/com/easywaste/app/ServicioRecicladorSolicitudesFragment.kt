package com.easywaste.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.AlertaMensaje
import com.easywaste.app.Clases.ClsServicio
import com.easywaste.app.Clases.Prefs
import com.easywaste.app.Clases.VAR
import org.json.JSONObject
import java.lang.Exception

class ServicioRecicladorSolicitudesFragment : Fragment() {

    var OK:Boolean = true

    class ServicioViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
            RecyclerView.ViewHolder(inflater.inflate(R.layout.item_serviciolista, parent, false)) {
        private var nId: TextView? = null
        private var nProveedor: TextView? = null
        private var nFecha: TextView? = null
        private var nHora: TextView? = null


        init {
            nId  = itemView.findViewById(R.id.id)
            nProveedor = itemView.findViewById(R.id.proveedor)
            nFecha = itemView.findViewById(R.id.fecha)
            nHora = itemView.findViewById(R.id.hora)

        }

        fun bind(act:MainActivity ,servicio: ClsServicio) {
            nId?.text = servicio.id.toString()
            nProveedor?.text = servicio.proveedor
            nFecha?.text = servicio.fecha
            nHora?.text = servicio.hora
            val btnAtender = itemView.findViewById<Button>(R.id.btnAtender)
            btnAtender.setOnClickListener {
                val args = Bundle()
                args.putInt("servicio_id",servicio.id)
                val frag = ServicioRecicladorOperacionFragment()
                frag.arguments = args
                act.cambiarFragmentBackStack(frag)
            }
        }

    }

    class ServicioListAdapter(val act :MainActivity, val list: List<ClsServicio>)
        : RecyclerView.Adapter<ServicioViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ServicioViewHolder(inflater, parent)
        }

        override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
            val servicio:ClsServicio = list[position]
            holder.bind(act,servicio)
        }

        override fun getItemCount(): Int = list.size
    }

    val listaServicios = ArrayList<ClsServicio>()
    var recyclerView:RecyclerView?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.servicio_reciclador_solicitudes, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        listaServicios.clear()
        listarServicios()


        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(OK){
                    try {
                        listarServicios()
                        mainHandler.postDelayed(this, 4000)
                    }catch (ex:Exception){

                    }
                }

            }
        })
        listarServicios()
        return view
    }

    fun actualizarServicios(){
        val activity = activity as MainActivity?

        recyclerView?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ServicioListAdapter(activity!!,listaServicios)
        }


    }

    fun listarServicios(){

        val activity = activity as MainActivity?
       // Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()
        val params = HashMap<String, Any>()
        params["reciclador_id"] =  Prefs.pullId()

        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_list_prov"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        val servicioArr =  response.getJSONArray("datos")
                        if(servicioArr.length()>0){
                            listaServicios.clear()
                            for (i in 0 until servicioArr.length()) {
                                val s = servicioArr.getJSONObject(i)
                                val servicio = ClsServicio(s.getInt("id"), s.getString("proveedor"),
                                    s.getString("fecha"), s.getString("hora"))
                                listaServicios.add(servicio)
                            }
                            actualizarServicios()
                        }


                    }else{
                   //     Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    }
                }

            }, Response.ErrorListener{

                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    //Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex:Exception){
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