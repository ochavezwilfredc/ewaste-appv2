package com.easywaste.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import org.json.JSONObject
import java.lang.Exception

class LoginRecicladorFragment : Fragment(){

    var btnIngresar:Button? = null
    var cbGuardar:CheckBox? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.login_reciclador, container, false)
        Prefs.getInstance(context!!)
        val activity = activity as AppCompatActivity?
        /*
        if (activity != null) {
            activity.title = "Ingreso Reciclador"
            activity.supportActionBar!!.show()
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        */
        btnIngresar = v.findViewById<Button>(R.id.btnIngresar)
        cbGuardar = v.findViewById<CheckBox>(R.id.guardar)

        val txtCodigo = v.findViewById<EditText>(R.id.codigo)
        val txtPass = v.findViewById<EditText>(R.id.password)
        val btnAyuda = v.findViewById<CardView>(R.id.btnAyuda)
        val guardoPass = Prefs.guardoPass()
        if( Prefs.pullRolId() == 2 && guardoPass!=null){
            txtCodigo.setText(Prefs.pullString(Prefs.USUARIOLOGIN))
            if(guardoPass) txtPass.setText( Prefs.pullPass() )
            cbGuardar?.isChecked = guardoPass
        }


        btnIngresar?.setOnClickListener {
            btnIngresar?.isEnabled = false
            var valido = false
            if(Validar.vacio(txtCodigo)) {
                Validar.txtErr(txtCodigo, "Ingrese su código de registro.")
            }
            else if(Validar.vacio(txtPass)) {
                Validar.txtErr(txtPass,  "Ingrese su contraseña.")
            }
            else if(Validar.strMenorA(txtPass,6)) {
                Validar.txtErr(txtPass,   "Mínimo 6 caracteres.")
            }else{
                valido = true
            }
            if(valido){
                val codigo = Validar.getString(txtCodigo)
                val pass = Validar.getString(txtPass)
                validarPersona(codigo,pass)
            }else{
                AlertaMensaje.mostrarError(activity!!,"Complete correctamente los campos.")
                btnIngresar?.isEnabled = true
            }


        }
        btnAyuda.setOnClickListener {
            val frag = RecicladorCodigoDialog()
            frag.show(fragmentManager!!,"dialogfrag")
        }

        return v
    }

    fun leerDatosPersona(datos:JSONObject){
        try {
            val id = datos.getInt("id")
            val token = datos.getString("token")
            val dni = datos.getString("dni")
            val persona = datos.getString("persona")
            val celular = datos.getString("celular")
            val rol = datos.getString("rol")
            val us = ClsUsuarioResumen (id, token, dni, persona, celular ,rol)
            us.guardarDatos()
        }catch (ex:Exception){

        }
    }
    fun validarPersona(codigo:String, pass:String){
        val activity = activity as LoginActivity?

        btnIngresar?.isEnabled = false
        Toast.makeText(context,  "Espere ...", Toast.LENGTH_SHORT).show()
        val params = HashMap<String,String>()
        params["p_rol"] = "2"
        params["p_dni"] = codigo
        params["p_clave"] = pass
        val parameters = JSONObject(params as Map<String, String>)

        val request = JsonObjectRequest(
            Request.Method.POST, VAR.url("uservalidar"),parameters,
            Response.Listener { response ->

                if(response!=null){
                    if(response.getInt("estado") == 200 ){

                        AlertaMensaje.mostrarSuccess(activity!! ,response.getString("mensaje"))
                        Prefs.putString(Prefs.USUARIOLOGIN, codigo)
                        Prefs.putRolId(2)
                        val datos = response.getJSONObject("datos")
                        leerDatosPersona(datos)
                        btnIngresar?.isEnabled = true
                        Prefs.putId(datos.getInt("id"))

                        if(cbGuardar!!.isChecked ){
                            Prefs.putPass(pass)
                        }
                        activity.estaLogeado()
                        activity.accederSistema()

                    }else{
                        Prefs.putPass("")
                        AlertaMensaje.mostrarError(activity!!,response.getString("mensaje"))
                        btnIngresar?.isEnabled = true

                    }


                }


            }, Response.ErrorListener{
                btnIngresar?.isEnabled = true

                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Toast.makeText(context,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    Prefs.putDNI("")
                    btnIngresar?.isEnabled = true
                }catch (ex: Exception){
                    Toast.makeText(context,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }

            })
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }




}