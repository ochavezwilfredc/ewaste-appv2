package com.easywaste.app


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.easywaste.app.Clases.ClsLocationAdress
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.AlertaMensaje
import com.easywaste.app.Clases.Prefs
import com.easywaste.app.Clases.VAR
import org.json.JSONObject
import java.lang.Exception

class ServicioProveedorReferenciaDialog: DialogFragment() {

   var accion :String? = ""
   var bmap: Bitmap? = null
   var gmap: GoogleMap? = null
   var latlong:LatLng?= null
   var txtDireccion: TextView? = null
    var txtReferencia: TextView? = null
   var habilitarEdicion = false
   var cerroDialog:Int = 0
    var btnOperacion:Button?=null
    var RETORNA_IMAGEN:Int = 101
    var imageView:ImageView? = null
    var PERMISO_GALERIA = 1
    var imagenCodificada:String? = null
   override fun onCreateView(
       inflater: LayoutInflater,
       container: ViewGroup?,
       savedInstanceState: Bundle?
   ): View? {
       return  inflater.inflate(R.layout.dialog_agregar_referencia, container)
   }


   override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
       super.onViewCreated(v, savedInstanceState)
       txtDireccion = v.findViewById(R.id.direccion)
       btnOperacion = v.findViewById<Button>(R.id.btnConfirmar)
       val btnCancelar = v.findViewById<Button>(R.id.btnCancelar)
       val bundle = arguments
       txtReferencia = v.findViewById<TextView>(R.id.referencia)
       imageView = v.findViewById(R.id.seleccionar_imagen)
       val activity = activity as MainActivity?

       if (bundle != null) {
           accion = bundle.getString("accion")

           if(accion!=null && activity!=null){
               val lat = bundle.getDouble("lat")
               val long = bundle.getDouble("long")
               latlong = LatLng(lat,long)
               val locationAddress = ClsLocationAdress()
               locationAddress.getAddressFromLocation(
                   lat, long,
                   context!!, GeocoderHandler(txtDireccion!!)
               )
               btnOperacion?.setOnClickListener{
                       if(validarCampos()){
                           val ref = txtReferencia?.text.toString().trim()
                           registrarServicio(lat, long, ref)
                       }
               }
               btnCancelar.setOnClickListener{
                   dismiss()
               }
           }

       }

       imageView?.setOnClickListener{
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
               if (checkSelfPermission(activity!!,Manifest.permission.READ_EXTERNAL_STORAGE) ==
                   PermissionChecker.PERMISSION_DENIED){
                   val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                   requestPermissions(permissions, PERMISO_GALERIA);
               }
               else{
                   seleccionarImagen()
               }
           }
           else{
               seleccionarImagen()
           }

       }
   }
    fun seleccionarImagen(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity!!.startActivityForResult(intent, RETORNA_IMAGEN)
    }
    fun registrarServicio(lat:Double, long:Double, ref:String){
        val activity = activity as MainActivity?

        btnOperacion?.isEnabled = false
        Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()
        val params = HashMap<String,Any>()
        params["proveedor_id"] = Prefs.pullId()
        params["latitud"] = lat
        params["longitud"] = long
        params["referencia"] = ref

        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_create"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        AlertaMensaje.mostrarSuccess(activity!! ,response.getString("mensaje"))
                        val datos =  response.getJSONObject("datos")
                        val id = datos.getInt("id")
                        Prefs.putServicioId(id)
                        activity.cambiarFragment(ServicioProveedorEspereFragment())
                        dialog!!.dismiss()
                    }else{
                        AlertaMensaje.mostrarError(activity!!,response.getString("mensaje"))
                    }
                    btnOperacion?.isEnabled = true

                }

            }, Response.ErrorListener{
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    Log.e("registro",r)
                    val response=  JSONObject(r)
                    Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    Log.e("registro",ex.message)
                    Toast.makeText(context,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }
                btnOperacion?.isEnabled = true
                dialog!!.dismiss()

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


   fun validarCampos():Boolean{
       if(txtReferencia?.text.toString().trim().isEmpty()){
           txtReferencia?.error ="Ingrese una referencia."
           return false
       }

       return true
   }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISO_GALERIA -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    seleccionarImagen()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(activity, "Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
       super.onResume()
       val window = dialog?.window
       val size : Point = Point()

       window!!.windowManager.defaultDisplay.getSize(size)
       val width = size.x
       window.setLayout((width*0.9).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
       window.setGravity(Gravity.CENTER)

   }


    class GeocoderHandler(txt:TextView) : Handler() {

       var txtDirecion:TextView= txt


       override fun handleMessage(msg: Message) {
           val locationAddress: String?
           when (msg.what) {
               1 -> {
                   val bundle = msg.data
                   locationAddress = bundle.getString("address")

               }
               else -> locationAddress = null
           }
           txtDirecion.text = locationAddress
       }
   }

}
