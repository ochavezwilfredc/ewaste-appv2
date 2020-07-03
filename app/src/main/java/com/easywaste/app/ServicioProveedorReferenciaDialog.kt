package com.easywaste.app


import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.AlertaMensaje
import com.easywaste.app.Clases.ClsLocationAdress
import com.easywaste.app.Clases.Prefs
import com.easywaste.app.Clases.VAR
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import es.dmoral.toasty.Toasty
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class ServicioProveedorReferenciaDialog: DialogFragment() {

   var accion :String? = ""
   var bmap: Bitmap? = null
   var gmap: GoogleMap? = null
   var latlong:LatLng?= null
   var txtDireccion: TextView? = null
    var txtReferencia: TextView? = null
    var btnOperacion:Button?=null
    var RETORNA_IMAGEN:Int = 101
    var imageView:ImageView? = null
    var encodedImage:String? = null
    var imageCapturadaUri:Uri? =null
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
           var options = arrayOf<CharSequence>("Tomar foto", "Galeria", "Borrar Imagen", "Cancelar")
           if(encodedImage==null){
                options = arrayOf<CharSequence>("Tomar foto", "Galeria", "Cancelar")
           }
           val builder: AlertDialog.Builder = AlertDialog.Builder(activity!!)
           builder.setTitle("Cargar imagen!")
           builder.setItems(options) { dialog, item ->
               if (options[item] == "Tomar foto") {
                   Dexter.withActivity(activity)
                       .withPermission(Manifest.permission.CAMERA)
                       .withListener(object : PermissionListener {
                           override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                               val fileName = "Camera_Example.jpg"

                               val values = ContentValues()

                               values.put(MediaStore.Images.Media.TITLE, fileName)

                               values.put(
                                   MediaStore.Images.Media.DESCRIPTION,
                                   "Imagen capturada por la camera"
                               )

                               imageCapturadaUri = activity.getContentResolver().insert(
                                   MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                               )
                               val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                               intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCapturadaUri)
                               startActivityForResult(intent, RC_CAPTURAR_IMAGEN)
                           }

                           override fun onPermissionDenied(response: PermissionDeniedResponse?) { /* ... */
                           }

                           override fun onPermissionRationaleShouldBeShown(
                               permission: PermissionRequest?,
                               token: PermissionToken?
                           ) {
                               /* ... */
                           }
                       }).check()

               } else if (options[item] == "Galeria") {
                   Dexter.withActivity(activity)
                       .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                       .withListener(object : PermissionListener {
                           override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                               val intent = Intent(Intent.ACTION_GET_CONTENT)
                                   .setType("image/*")
                                   .addCategory(Intent.CATEGORY_OPENABLE)

                               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                   val mimeTypes =
                                       arrayOf("image/jpeg", "image/png")
                                   intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                               }
                               startActivityForResult(
                                   Intent.createChooser(
                                       intent,"Seleccione una imagen")
                                   , RC_SELECCION_IMAGEN
                               )
                           }

                           override fun onPermissionDenied(response: PermissionDeniedResponse?) { /* ... */

                           }

                           override fun onPermissionRationaleShouldBeShown(
                               permission: PermissionRequest?,
                               token: PermissionToken?
                           ) {
                               /* ... */
                           }
                       }).check()
               }else if (options[item] == "Borrar Imagen") {
                   imageView?.setImageResource(R.drawable.upload)
                   encodedImage =null
                   dialog.dismiss()
               }
               else if (options[item] == "Cancelar") {
                   dialog.dismiss()
               }
           }
           builder.show()
       }
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
        if(encodedImage!=null){
            params["imagen"] = encodedImage!!
        }


        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("servicio_create"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){
                        AlertaMensaje.mostrarSuccess(activity!! ,response.getString("mensaje"))
                        Log.e("error", response.toString())
                        val datos =  response.getJSONArray("datos")
                        if(datos.length()>0){
                            var cont = 0
                            val noelegidos = JSONArray()
                            val id =  datos.getJSONObject(0).getInt("servicio_id")
                            for(i in 0..datos.length()-1){

                                val ele =  datos.getJSONObject(i)
                              //  if(ele.getInt("elegido") == 0){
                                    noelegidos.put(cont, ele)
                                    cont++
                                //   }
                            }
                            Prefs.putServicioId(id)
                            Prefs.putServicioRecicladoresCercanos(noelegidos.toString())
                        }
                        activity.cambiarFragment(ServicioProveedorEspereFragment())
                        dialog!!.dismiss()
                    }else{
                        AlertaMensaje.mostrarError(activity!!,response.getString("mensaje"))
                        dialog!!.dismiss()
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
                    Log.e("registro", ex.message.toString())
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
    companion object {
        const val RC_CAPTURAR_IMAGEN= 200

        const val RC_SELECCION_IMAGEN= 201
    }

    private fun getRealPathFromURI( contentURI:Uri):Long {
        val scheme = contentURI.scheme
        var size:Long = 0
        System.out.println("Scheme type " + scheme);
        if(scheme.equals(ContentResolver.SCHEME_CONTENT))
        {
            try {
                val fileInputStream= activity?.contentResolver?.openInputStream(contentURI)
                size = fileInputStream?.available()!!.toLong()
                return size
            } catch (e:Exception) {
                e.printStackTrace()
                return 0
            }
        }
        else if(scheme.equals(ContentResolver.SCHEME_FILE))
        {
            val path = contentURI.path
            try {
                val f =  File(path)
                return f.length()
            } catch ( e:Exception) {
                e.printStackTrace()
                return 0
            }

        }

        return 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SELECCION_IMAGEN){
            val uri = data!!.data
            if(uri !=null){
                try {
                    var valid = true
                    val size = getRealPathFromURI(uri)

                    if(size>0){
                        val nsize = size/(1024.0*1024)
                        Log.e("myerror", "tamanio $nsize")

                        if(nsize>10.0){
                            Toasty.warning(activity!!, "El archivo supera 10mb!!", Toast.LENGTH_LONG, true).show()
                            valid = false
                        }
                    }

                    if(valid){
                        var bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
                        bitmap = getResizedBitmap(bitmap, 500)
                        val baos =  ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos)
                        val decoded = BitmapFactory.decodeStream( ByteArrayInputStream(baos.toByteArray()))
                        imageView?.setImageBitmap(decoded)
                        val imageBytes = baos.toByteArray()
                        encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                    }


                }catch (ex:Exception){
                    encodedImage = null
                }

            }

        }  else if(requestCode == RC_CAPTURAR_IMAGEN){
            var bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageCapturadaUri)
            bitmap = getResizedBitmap(bitmap, 500)
            val baos =  ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos)
            val decoded = BitmapFactory.decodeStream( ByteArrayInputStream(baos.toByteArray()))
            imageView?.setImageBitmap(decoded)
            val imageBytes = baos.toByteArray()
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }

    }


    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

}
