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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import org.json.JSONObject
import java.lang.Exception


class ServicioRecicladorAlmacenesFragment : Fragment() {

    var OK: Boolean = true

    class AlmacenViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_almacenlista, parent, false)) {
        private var nId: TextView? = null
        private var nCode: TextView? = null
        private var nCentro: TextView? = null
        private var nSector: TextView? = null
        private var nPeso: TextView? = null
        private var recyclerView:RecyclerView?=null

        var listadetalle = ArrayList<ClsAlmacenDetalle>()


        init {
            nId = itemView.findViewById(R.id.id)
            nCode    = itemView.findViewById(R.id.code)
            nCentro = itemView.findViewById(R.id.centro)
            nSector = itemView.findViewById(R.id.sector)
            nPeso = itemView.findViewById(R.id.peso)

        }

        fun bind(act: MainActivity, almacen: ClsAlmacen) {
            val headers = itemView.findViewById<ConstraintLayout>(R.id.headers)
            recyclerView =  itemView.findViewById(R.id.recyclerView)
            listadetalle = ArrayList<ClsAlmacenDetalle>()
            recyclerView?.apply {
                layoutManager = LinearLayoutManager(act)
                adapter = AlmacenDetalleListAdapter(act, listadetalle)
            }

            Log.e("myerror", almacen.detalle.toString())
            listadetalle.addAll(almacen.detalle)
            recyclerView?.visibility = View.GONE
            headers.visibility = View.GONE

            nId?.text = almacen.id.toString()
            nCode?.text = almacen.code
            nSector?.text = almacen.sector
            nCentro?.text = almacen.centro
            nPeso?.text = almacen.peso.toString()

            val btnVer = itemView.findViewById<Button>(R.id.btnVer)
            btnVer.setOnClickListener {
                if(recyclerView?.visibility == View.GONE){
                    recyclerView?.visibility = View.VISIBLE
                    headers.visibility = View.VISIBLE

                }else{
                    recyclerView?.visibility = View.GONE
                    headers.visibility = View.GONE


                }
                /*
                val args = Bundle()
                args.putString("detalle", almacen.detalle)
                val frag = ServicioRecicladorOperacionFragment()
                frag.arguments = args
                act.cambiarFragmentBackStack(frag)

                 */
            }
        }

    }

    class AlmacenListAdapter(val act: MainActivity, val list: List<ClsAlmacen>) :
        RecyclerView.Adapter<AlmacenViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlmacenViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return AlmacenViewHolder(inflater, parent)
        }

        override fun onBindViewHolder(holder: AlmacenViewHolder, position: Int) {
            val almacen: ClsAlmacen = list[position]
            holder.bind(act, almacen)
        }

        override fun getItemCount(): Int = list.size
    }

    class AlmacenDetalleViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_almacendetallelista, parent, false)) {
        private var nCantidad: TextView? = null
        private var nNombre: TextView? = null
        private var nPeso: TextView? = null


        init {
            nCantidad = itemView.findViewById(R.id.cantidad)
            nNombre    = itemView.findViewById(R.id.nombre)
          //  nPeso = itemView.findViewById(R.id.peso)
        }

        fun bind(act: MainActivity, almacen: ClsAlmacenDetalle) {
            nCantidad?.text = almacen.cantidad
            nNombre?.text = almacen.nombre
        }
    }

    class AlmacenDetalleListAdapter(val act: MainActivity, val list: List<ClsAlmacenDetalle>) :
        RecyclerView.Adapter<AlmacenDetalleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlmacenDetalleViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return AlmacenDetalleViewHolder(inflater, parent)
        }

        override fun onBindViewHolder(holder: AlmacenDetalleViewHolder, position: Int) {
            val almacen: ClsAlmacenDetalle = list[position]
            holder.bind(act, almacen)
        }

        override fun getItemCount(): Int = list.size
    }

    val listaAlmacenes = ArrayList<ClsAlmacen>()
    var recyclerView: RecyclerView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.servicio_reciclador_solicitudes, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        listaAlmacenes.clear()
        listaAlmacenes()
        
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if (OK) {
                    try {
                        listaAlmacenes()
                        mainHandler.postDelayed(this, 1000*60)
                    } catch (ex: Exception) {

                    }
                }

            }
        })
        listaAlmacenes()
        return view
    }

    fun actualizarServicios() {
        val activity = activity as MainActivity?

        recyclerView?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = AlmacenListAdapter(activity!!, listaAlmacenes)
        }


    }

    fun listaAlmacenes() {
        val activity = activity as MainActivity?
        // Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()
        val params = HashMap<String, Any>()
        params["reciclador_id"] = Prefs.pullId()

        val parameters = JSONObject(params as Map<String, Any>)

        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("recycle_almacen"), parameters,
            Response.Listener { response ->

                if (response != null) {
                    if (response.getInt("estado") == 200) {
                        val servicioArr = response.getJSONArray("datos")
                        if (servicioArr.length() > 0) {
                            listaAlmacenes.clear()
                            for (i in 0 until servicioArr.length()) {
                                val s = servicioArr.getJSONObject(i)

                                val detalle = s.getJSONArray("detalle")
                                val detalles = ArrayList<ClsAlmacenDetalle>()
                                for (j in 0 until detalle.length()) {
                                    val d = detalle.getJSONObject(j)
                                    detalles.add(ClsAlmacenDetalle(d.getInt("id"),
                                        d.getInt("residuo_id"),d.getString("nombre"),
                                        d.getString("cantidad")
                                    ))
                                }


                                val almacen = ClsAlmacen(
                                    s.getInt("id"), s.getString("code"),
                                    s.getString("centro_acopio"),
                                    s.getString("sector"), s.getString("total_peso"),
                                    detalles
                                )
                                listaAlmacenes.add(almacen)
                                actualizarServicios()
                            }

                        } else {
                        }
                        //     Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    }
                }

            }, Response.ErrorListener {

                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response = JSONObject(r)
                    //Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) {
                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }


            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> = HashMap()
                params["TOKEN"] = Prefs.pullToken()
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

}