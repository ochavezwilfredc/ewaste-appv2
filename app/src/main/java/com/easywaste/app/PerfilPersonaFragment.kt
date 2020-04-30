package com.easywaste.app

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.registrar_usuario.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception

class PerfilPersonaFragment : Fragment() {

    var contenedor:LinearLayout? = null
    var txtDNI:EditText? = null
    var txtNombre:EditText? = null
    var txtApellidoPaterno:EditText? = null
    var txtApellidoMaterno:EditText? = null
    var txtTelefono:EditText? = null
    var txtEmail:EditText? = null
    var txtDireccion:EditText? = null
    var txtPass:EditText? = null
    var sexoMasculino:RadioButton?= null
    var sexoFemenino:RadioButton?=null
    var contenedorPass:TextInputLayout? =null
    var btnRegistrar:Button? =null
    var fechaNacimiento:String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.registrar_usuario, container, false)
        contenedor = view.findViewById(R.id.contenedorDatos)
        contenedor?.visibility = View.GONE
        val txtTitulo: TextView = view.findViewById(R.id.txttitulo)
        txtTitulo.text = "ACTUALIZAR DATOS"
        val txtSubtitulo:TextView = view .findViewById(R.id.txtsubtitulo)
        txtSubtitulo.visibility = View.GONE

        txtDNI = view.findViewById<EditText>(R.id.dni)
        txtNombre = view.findViewById<EditText>(R.id.nombre)
        txtNombre!!.filters += (InputFilter.AllCaps())
        txtApellidoPaterno = view.findViewById<EditText>(R.id.apellido_paterno)
        txtApellidoPaterno!!.filters += InputFilter.AllCaps()
        txtApellidoMaterno = view.findViewById<EditText>(R.id.apellido_materno)
        txtApellidoMaterno!!.filters += InputFilter.AllCaps()
        txtTelefono = view.findViewById<EditText>(R.id.telefono)
        txtEmail = view.findViewById<EditText>(R.id.correo)
        txtDireccion = view.findViewById<EditText>(R.id.direccion)
        txtPass = view.findViewById<EditText>(R.id.password)
        sexoMasculino = view.findViewById<RadioButton>(R.id.sexoMasculino)
        sexoFemenino = view.findViewById<RadioButton>(R.id.sexoFemenino)

        contenedorPass = view.findViewById(R.id.til_password)
        val contenedorFecha = view.findViewById<TextInputLayout>(R.id.til_fecha_nacimiento)
        contenedorFecha.visibility = View.GONE
        val contenedorZona = view.findViewById<LinearLayout>(R.id.contenedorZona)
        contenedorZona.visibility = View.GONE
        val contenedorCambioPass = view.findViewById<LinearLayout>(R.id.contenedorCambioPass)
        contenedorCambioPass.visibility = View.VISIBLE


        val btnLogin:Button = view.findViewById(R.id.btnLogin)
         btnRegistrar = view.findViewById(R.id.btnRegistrar)
        btnLogin.visibility = View.GONE
        btnRegistrar?.setText("ACTUALIZAR")
        buscarDatos()

        val cambiarPass:Switch = view.findViewById(R.id.cambiarPass)
        cambiarPass.setOnCheckedChangeListener { buttonView, isChecked ->
            txtPass?.setText("")
            contenedorPass?.visibility = if(isChecked) View.VISIBLE else View.GONE
        }

        btnRegistrar?.setOnClickListener {

            val sexo = if(sexoMasculino!!.isChecked) "M" else "F"

            var valido = false
            if(Validar.vacio(txtDNI!!)) {
                Validar.txtErr(txtDNI!!, "Ingrese Número de DNI")
            }else if( ! Validar.strSize(txtDNI!!,   8)) {
                Validar.txtErr(txtDNI!!,  "El número de DNI debe tener 8 digitos")
            }else if(Validar.vacio(txtApellidoPaterno!!)){
                Validar.txtErr(txtApellidoPaterno!!,  "Ingrese Apellido Paterno")
            }else if(Validar.vacio(txtApellidoMaterno!!)){
                Validar.txtErr(txtApellidoMaterno!!,  "Ingrese Apellido Materno")
            }else if(Validar.vacio(txtNombre!!)) {
                Validar.txtErr(txtNombre!!, "Ingrese Nombre")
            }else if(Validar.vacio(txtEmail!!)){
                Validar.txtErr(txtEmail!!, "Ingrese correo electrónico" )
            }else if(! Validar.strEmail(txtEmail!!)){
                Validar.txtErr(txtEmail!!, "Correo inválido" )
            }
            else if( cambiarPass.isChecked && Validar.vacio(txtPass!!)){
                Validar.txtErr(txtPass!!, "Ingrese contraseña" )
            }else if(cambiarPass.isChecked && Validar.strMenorA(txtPass!!,6)){
                Validar.txtErr(txtPass!!, "Mínimo 6 caracteres" )
            }
            else{
                valido = true
            }
            if(valido){
                val cambioPass = if(cambiarPass.isChecked) 1 else 0
                val data = ClsPersona( Prefs.pullId(),Prefs.pullRolId(), txtDNI?.text.toString().trim(),
                    txtApellidoPaterno?.text.toString().trim(), txtApellidoMaterno?.text.toString().trim(),
                    txtNombre?.text.toString().trim(), sexo, txtTelefono?.text.toString().trim(),
                    txtDireccion?.text.toString().trim(), txtEmail?.text.toString().trim(),
                    fechaNacimiento, cambioPass, txtPass?.text.toString().trim() )
                actualizarDatos(data)
            }else{
                AlertaMensaje.mostrarError(activity!!,"Complete correctamente los campos.")
                btnRegistrar?.isEnabled = true

            }
        }

        return view
    }

    fun actualizarDatos(persona:ClsPersona){
        val activity = activity as AppCompatActivity?
        val parameters = JSONObject(persona.actualizarDatos() as Map<String, String>)

        btnRegistrar?.isEnabled = false
        Toast.makeText(activity,  "Espere ...", Toast.LENGTH_SHORT).show()
        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("perfil_update"), parameters,
            Response.Listener { response ->
                if(response!=null){
                    try {
                        if(response.getInt("estado")==200){
                            if(activity != null){
                                AlertaMensaje.mostrarSuccess(activity,  response.getString("mensaje"))
                                buscarDatos()
                            }
                        }else{
                            if(activity != null){
                                AlertaMensaje.mostrarError(activity,  response.getString("mensaje"))
                            }
                        }
                        btnRegistrar?.isEnabled = true

                    }catch (ex:Exception){
                        ex.printStackTrace()
                        Log.e("error", ex.message.toString())
                        if(activity != null){
                            AlertaMensaje.mostrarError(activity, "Error de conexión")
                            btnRegistrar?.isEnabled = true
                        }
                    }
                }

            },
            Response.ErrorListener{
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    Log.e("error", r)
                    val response=  JSONObject(r)
                    Toast.makeText(activity,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    btnRegistrar?.isEnabled = true

                }catch (ex: Exception){
                    Log.e("error", ex.message.toString())
                    ex.printStackTrace()
                    Toast.makeText(activity,  "Error de conexión", Toast.LENGTH_SHORT).show()
                    btnRegistrar?.isEnabled = true
                }

            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> =HashMap()
                params["TOKEN"] =  Prefs.pullToken()
                return params
            }
        }


        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(request)
    }
    fun buscarDatos(){
        val params = HashMap<String,Any>()
        params["id"] =  Prefs.pullId()
        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("persona_read"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    contenedor?.visibility = View.GONE

                    try {
                        if(response.getInt("estado") == 200){

                            val datos = response.getJSONObject("datos")
                            txtDNI?.setText(datos.getString("dni"))
                            txtApellidoPaterno?.setText(datos.getString("ap_paterno"))
                            txtApellidoMaterno?.setText(datos.getString("ap_materno"))
                            txtTelefono?.setText(datos.getString("celular"))
                            txtDireccion?.setText(datos.getString("direccion"))
                            txtEmail?.setText(datos.getString("correo"))
                            txtNombre?.setText(datos.getString("nombres"))
                            fechaNacimiento = datos.getString("fecha_nac")
                            if(datos.getString("sexo")=="M") sexoMasculino?.isChecked = true
                            else sexoFemenino?.isChecked = true
                            contenedor?.visibility = View.VISIBLE

                        }else{
                            if(activity != null){
                                AlertaMensaje.mostrarError(activity!!, response.getString("mensaje"))
                            }
                        }

                    }catch (ex:Exception){
                        ex.printStackTrace()
                        Log.e("error", ex.message.toString())
                        if(activity != null){
                            AlertaMensaje.mostrarError(activity!!, "Error de conexión")
                        }
                    }
                }

            },
            Response.ErrorListener{
                contenedor?.visibility = View.GONE
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Toast.makeText(activity,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    ex.printStackTrace()
                    Toast.makeText(activity,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }

            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> =HashMap()
                params["TOKEN"] =  Prefs.pullToken()
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(request)
    }

}