package com.easywaste.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.text.InputFilter
import android.app.DatePickerDialog
import android.app.Dialog
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.*
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.registrar_usuario.*
import org.angmarch.views.NiceSpinner
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class RegistrarFragment : Fragment(){

    var fechaNacimiento:String = ""
    var btnRegistrar:Button? =null
    var listaZonas:ArrayList<ClsZona> = ArrayList()
    var cZona:NiceSpinner? = null
    val zonaId:Int? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.registrar_usuario, container, false)
        val activity = activity as AppCompatActivity?
        /*
        if (activity != null) {
            activity.title = "Registrar"
            activity.supportActionBar!!.show()
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }*/

        val txtDNI = v.findViewById<EditText>(R.id.dni)
        val txtNombre = v.findViewById<EditText>(R.id.nombre)
        txtNombre.filters += InputFilter.AllCaps()
        val txtApellidoPaterno = v.findViewById<EditText>(R.id.apellido_paterno)
        txtApellidoPaterno.filters += InputFilter.AllCaps()
        val txtApellidoMaterno = v.findViewById<EditText>(R.id.apellido_materno)
        txtApellidoMaterno.filters += InputFilter.AllCaps()
        val txtTelefono = v.findViewById<EditText>(R.id.telefono)
        val txtEmail = v.findViewById<EditText>(R.id.correo)
        val txtDireccion = v.findViewById<EditText>(R.id.direccion)
        val sexoMasculino = v.findViewById<RadioButton>(R.id.sexoMasculino)
        val txtPass = v.findViewById<EditText>(R.id.password)
        val txtFechaNacimiento = v.findViewById<EditText>(R.id.fecha_nacimiento)
         cZona = v.findViewById(R.id.zonas)
        txtFechaNacimiento.setOnClickListener{
            val newFragment = DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val dia =  day.toString().padStart(2, '0')
                val mes = (month + 1).toString().padStart(2,'0')
                val selectedDate = dia+ " / " + mes + " / " + year
                fechaNacimiento = year.toString() +"-"+ mes +"-" + dia
                try {
                    txtFechaNacimiento.setError(null)

                }catch (ex:Exception){

                }
                txtFechaNacimiento.setText(selectedDate)
            })
            newFragment.show(fragmentManager!!, "datePicker")
        }

        btnRegistrar = v.findViewById<Button>(R.id.btnRegistrar)
        val btnLogin  = v.findViewById<Button>(R.id.btnLogin)
        btnRegistrar?.setOnClickListener{
            val zona_pos  = cZona?.selectedItem

            val zona_id = listaZonas.filter { s -> s.nombre == zona_pos }.single().id
            val sexo = if(sexoMasculino.isChecked) "M" else "F"
            txtFechaNacimiento.error = ""
            var valido = false
            if(Validar.vacio(txtDNI)) {
                Validar.txtErr(txtDNI, "Ingrese Número de DNI")
            }else if( ! Validar.strSize(txtDNI,   8)) {
                Validar.txtErr(txtDNI,  "El número de DNI debe tener 8 digitos")
            }else if(Validar.vacio(txtApellidoPaterno)){
                Validar.txtErr(txtApellidoPaterno,  "Ingrese Apellido Paterno")
            }else if(Validar.vacio(txtApellidoMaterno)){
                Validar.txtErr(txtApellidoMaterno,  "Ingrese Apellido Materno")
            }else if(Validar.vacio(txtNombre)) {
                Validar.txtErr(txtNombre, "Ingrese Nombre")
            }else if(Validar.vacio(txtEmail)){
                Validar.txtErr(txtEmail, "Ingrese correo electrónico" )
            }else if(! Validar.strEmail(txtEmail)){
                Validar.txtErr(txtEmail, "Correo inválido" )
            }else if(Validar.vacio(txtFechaNacimiento)){
                Validar.txtErr(txtFechaNacimiento, "Ingrese fecha nacimiento" )
            }
            /*
            else if(Validar.vacio(txtPass)){
                Validar.txtErr(txtPass, "Ingrese contraseña" )
            }else if(Validar.strMenorA(txtPass,6)){
                Validar.txtErr(txtPass, "Mínimo 6 caracteres" )
            }*/
            else{
                valido = true
            }
            if(valido){
                val proveedor = ClsPersona( -1,3, txtDNI.text.toString().trim(),
                    txtApellidoPaterno.text.toString().trim(), txtApellidoMaterno.text.toString().trim(),
                    txtNombre.text.toString().trim(), sexo, txtTelefono.text.toString().trim(),
                    txtDireccion.text.toString().trim(), txtEmail.text.toString().trim(),
                    fechaNacimiento.trim(), zona_id)
                registrarProveedor(proveedor)

            }else{
                AlertaMensaje.mostrarError(activity!!,"Complete correctamente los campos.")
                btnRegistrar?.isEnabled = true

            }
        }
        btnLogin.setOnClickListener{
            fragmentManager?.popBackStack()
        }


        buscarZonas()

        return v
    }


    fun registrarProveedor(proveedor:ClsPersona){
        val activity = activity as AppCompatActivity?
        val parameters = JSONObject(proveedor.registrarProveedor() as Map<String, String>)

        btnRegistrar?.isEnabled = false
        Toast.makeText(activity,  "Espere ...", Toast.LENGTH_SHORT).show()
        val request = JsonObjectRequest(
            Request.Method.POST, VAR.url("proveedor_create"), parameters,
            Response.Listener { response ->

                if(response!=null){
                    try {
                        if(response.getInt("estado")==200){
                            if(activity != null){
                                AlertaMensaje.mostrarSuccess(activity,  response.getString("mensaje"))
                            }
                            fragmentManager?.popBackStack()

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

            })


        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(request)

    }

    fun buscarZonas(){
            val request : JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, VAR.url("zonas_list"),null,
                Response.Listener { response ->

                    if(response!=null){
                        try {
                           val zonas = response.getJSONArray("datos")
                            listaZonas.clear()
                            if(zonas.length()>0){
                                for (i in 0 until zonas.length()) {
                                    val zona = zonas.getJSONObject(i)
                                    listaZonas.add(ClsZona(zona.getInt("id"), zona.getString("nombre")))
                                }
                            }
                            procesarZonas()


                        }catch (ex:Exception){
                            listaZonas.clear()
                        }
                    }

                },
                Response.ErrorListener{
                    try {
                        val nr = it.networkResponse
                        val r = String(nr.data)
                        val response=  JSONObject(r)
                        Toast.makeText(activity,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    }catch (ex: Exception){
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

    fun procesarZonas(){
        val lista = ArrayList<String>(listaZonas.size)
        listaZonas.forEach {
            lista.add(it.nombre)
        }
        cZona?.attachDataSource(lista)
        cZona?.setOnSpinnerItemSelectedListener { parent, view, position, id ->

        }
    }
    class DatePickerFragment : DialogFragment() {

        private var listener: DatePickerDialog.OnDateSetListener? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog  = DatePickerDialog(activity!!, listener, year, month, day)
            datePickerDialog.datePicker.maxDate = Date().time
            return datePickerDialog

        }

        companion object {
            fun newInstance(listener: DatePickerDialog.OnDateSetListener): DatePickerFragment {
                val fragment = DatePickerFragment()
                fragment.listener = listener
                return fragment
            }
        }

    }
}