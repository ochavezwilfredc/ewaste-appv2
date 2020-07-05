package com.easywaste.app.Clases

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.easywaste.app.R
import com.google.android.gms.maps.model.LatLng
import com.tapadoo.alerter.Alerter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


data class ClsUsuarioResumen(
    val id:Int,
    val token: String,
    val dni: String,
    val persona: String,
    val celular: String,
    val rol: String
) {

    fun guardarDatos() {
        Prefs.putId(id)
        Prefs.putToken(token)
        Prefs.putDNI(dni)
        Prefs.putString(Prefs.PERSONA, persona)
        Prefs.putString(Prefs.CELULAR, celular)
        Prefs.putString(Prefs.ROL, rol)

    }
}

 class ClsServicio(val id: Int, val proveedor:String) {
     var fecha:String = ""
     var hora :String = ""
     constructor(id:Int, proveedor:String, fecha:String, hora:String) : this(id,proveedor) {
         this.fecha = fecha
         this.hora = hora
     }

 }

class ClsAlmacen(val id: Int, val code:String, val centro:String, val sector:String, val peso:String,val fecha:String,
                 val detalle:List<ClsAlmacenDetalle>) {

}
class ClsAlmacenDetalle(val id: Int, val residuo_id:Int, val nombre:String, val cantidad:String) {
}
data class ClsServicioDireccion(val direccion: String, val posicion:LatLng) {
}

data class ClsZona(val id: Int, val nombre:String) {
}
class ClsPersona(val id: Int, val rolid:Int, val dni:String, val apellido_paterno:String,val apellido_materno:String,
                 val nombre:String, val sexo:String,val celular:String, val direccion: String, val correo:String, val fecha_nacimiento: String) {

    var zonaid:Int? = null
    var cambio_pass:Int? =null
    var pass:String?= null
    constructor( id: Int,  rolid:Int, dni:String, apellido_paterno:String, apellido_materno:String,
                 nombre:String,  sexo:String, celular:String,  direccion: String,  correo:String ,
                 fecha_nacimiento:String, zona:Int) : this(id,rolid, dni, apellido_paterno, apellido_materno,
                    nombre, sexo, celular, direccion, correo, fecha_nacimiento) {
        this.zonaid = zona

    }

    constructor( id: Int,  rolid:Int, dni:String, apellido_paterno:String, apellido_materno:String,
                 nombre:String,  sexo:String, celular:String,  direccion: String,  correo:String , fecha_nacimiento:String,
                 cambio_pass:Int, pass:String) : this(id,rolid, dni, apellido_paterno, apellido_materno,
        nombre, sexo, celular,  direccion, correo, fecha_nacimiento) {
        this.cambio_pass = cambio_pass
        this.pass = pass
    }


    fun registrarProveedor():HashMap<String,Any?>{
        val params = HashMap<String,Any?>()
        params["operation"] = "Nuevo"
        params["rol_id"] = this.rolid
        params["dni"] = this.dni
        params["nombres"] = this.nombre
        params["ap_paterno"] = this.apellido_paterno
        params["ap_materno"] = this.apellido_materno
        params["sexo"] = this.sexo
        params["fn"] = this.fecha_nacimiento
        params["celular"] = this.celular
        params["direccion"] = this.direccion
        params["correo"] = this.correo
        params["estado"] = "A"
        params["zona_id"] = this.zonaid
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha_registro = formatter.format(Calendar.getInstance().time)
        params["fecha_registro"] = fecha_registro
        return params
    }
    fun actualizarDatos():HashMap<String,Any?>{
        val params = HashMap<String,Any?>()
        params["id"] = this.id
        params["rol_id"] = this.rolid
        params["dni"] = this.dni
        params["nombres"] = this.nombre
        params["ap_paterno"] = this.apellido_paterno
        params["ap_materno"] = this.apellido_materno
        params["sexo"] = this.sexo
        params["fn"] = this.fecha_nacimiento
        params["celular"] = this.celular
        params["direccion"] = this.direccion
        params["correo"] = this.correo
        params["cambiar_password"] = this.cambio_pass
        params["password"] = this.pass

        return params
    }


}
data class Respuesta(val outstate: Boolean, val outid: Int, val outerrornumber: Int, val outdescription: String)
data class RespuestaWS(val estado: Int, val msg: String)


class Validar {
    companion object {
        fun vacio(txt: EditText): Boolean {
            return (txt.text.trim().isEmpty())
        }

        fun strMenorA(txt: EditText, t: Int): Boolean {
            return (txt.text.trim().length < t)
        }


        fun strSize(txt: EditText, t: Int): Boolean {
            return (txt.text.trim().length == t)
        }

        fun strEmail(txt: EditText): Boolean {
            val email = getString(txt)
            val pat = Patterns.EMAIL_ADDRESS
            return pat.matcher(email).matches()

        }

        fun getString(txt: EditText): String {
            return txt.text.trim().toString()
        }

        fun txtErr(txt: EditText, err: String) {
            txt.error = err
            txt.requestFocus()
        }


    }
}

class VAR {
    companion object {
       // val url: String = "http://192.168.18.57/www/muni_api/webservice/"
        val url: String = "http://192.168.1.107/www/"
        var ext: String = ".php"
        fun url(m: String): String {
            return url + "muni_api/webservice/"+ m + ext
        }
        fun urlInformacion(): String {
            return url + "muni_web/Vista/sensibilizacion_info.php"
        }
        fun LeerRespuesta(s: String): RespuestaWS? {

            try {
                val x = JSONObject(s)
                return RespuestaWS(x.getInt("estado"), x.getString("mensaje"))

            } catch (e: JSONException) {
                return null
            }

        }


        fun wsResponse(s: String): Respuesta? {

            try {

                val jo = JSONObject(s)
                return null
            } catch (e: JSONException) {
                return null
            }

        }
    }
}

class AlertaMensaje {
    companion object {
        fun mostrarSuccess(a: Activity, m: String) {
            Alerter.create(a)
                .setIcon(R.drawable.check)
                .setBackgroundColorRes(R.color.colorGreen)
                .setText(m)
                .setDuration(3000)
                .show()
        }

        fun mostrarError(a: Activity, m: String) {
            Alerter.create(a)
                .setIcon(R.drawable.exclamation)
                .setBackgroundColorRes(R.color.colorRed)
                .setText(m)
                .setDuration(3000)
                .show()
        }

        fun mostrarInfo(a: Activity, m: String) {
            Alerter.create(a)
                .setIcon(R.drawable.check)
                .setBackgroundColorRes(R.color.colorBlue)
                .setText(m)
                .setDuration(3000)
                .show()
        }
    }

}


class Prefs {
    companion object {
        var instance: CryptoPrefs? = null
        val PASS = "pass"
        val PERSONA = "persona"
        val DNI = "dni"
        val CELULAR = "celular"
        val ROL = "rol"
        val ROL_ID = "rolid"
        val LOGIN = "login"
        val USUARIOLOGIN = "usuariologin"
        val ID = "id"
        val SERVICIOID = "servicioid"
        val SERVICIORECICLADORID = "serviciorecicladorid"
        val RECICLADOR_ESTADO = "reciclador_estado"
        val TOKEN = "token"

        val posiciones_recicladores = "posicionesrecicladores"
        fun getInstance(context: Context, change: Boolean = false) {
            if (change || instance == null) {
                instance = CryptoPrefs(context, "easywaste", "EasyWaste!=?")
            }
        }
        fun putId(id: Int) {
            if (instance != null) {
                instance?.put(ID, id)
            }
        }
        fun pullId(): Int {
            if (instance != null) {
                return instance?.get(ID, "0")!!.toInt()
            }
            return 0
        }
        fun putServicioId(id: Int) {
            if (instance != null) {
                instance?.put(SERVICIOID, id)
            }
        }

        fun putServicioRecicladoresCercanos(str: String) {
            if (instance != null) {
                instance?.put(posiciones_recicladores, str)
            }
        }
        fun pullServicioRecicladoresCercanos(): String {
            if (instance != null) {
                return instance?.get(posiciones_recicladores, "")!!
            }
            return ""
        }
        fun pullServicioId(): Int {
            if (instance != null) {
                return instance?.get(SERVICIOID, "0")!!.toInt()
            }
            return 0
        }
        fun putServicioRecicladorId(id: Int) {
            if (instance != null) {
                instance?.put(SERVICIORECICLADORID, id)
            }
        }
        fun pullServicioRecicladorId(): Int {
            if (instance != null) {
                return instance?.get(SERVICIORECICLADORID, "0")!!.toInt()
            }
            return 0
        }
        fun putDNI(dni: String) {
            if (instance != null) {
                instance?.put(DNI, dni)
            }
        }
        fun putRolId(rolid: Int) {
            if (instance != null) {
                instance?.put(ROL_ID, rolid)
            }
        }
        fun pullDNI(): String {
            if (instance != null) {
                return instance?.get(DNI, "")!!
            }
            return ""
        }
        fun pullRolId(): Int {
            if (instance != null) {
                    return instance?.get(ROL_ID, "0")!!.toInt()
            }
            return 0
        }
        fun putPass(pass: String) {
            if (instance != null) {
                instance?.put(PASS, pass)
            }
        }

        fun pullPass(): String {
            if (instance != null) {
                return instance?.get(PASS, "")!!
            }
            return ""
        }
        fun putToken(pass: String) {
            if (instance != null) {
                instance?.put(TOKEN, pass)
            }
        }

        fun pullToken(): String {
            if (instance != null) {
                return instance?.get(TOKEN, "")!!
            }
            return ""
        }
        fun putString(tipo: String, s: String) {
            if (instance != null) {
                instance?.put(tipo, s)
            }
        }

        fun pullString(tipo: String): String {
            if (instance != null) {
                return instance?.get(tipo, "")!!
            }
            return ""
        }
        fun putInt(tipo: String, s: Int) {
            if (instance != null) {
                instance?.put(tipo, s)
            }
        }

        fun pullInt(tipo: String): Int {
            if (instance != null) {
                return instance?.get(tipo, "0")!!.toInt()
            }
            return 0
        }
        fun guardoPass(): Boolean? {
            if (pullDNI() == "") {
                return null
            } else if (pullDNI() != "" && pullPass() != "") {
                return true
            }
            return false
        }
        fun isLogin():Boolean{
            if (instance != null) {
                val l =instance?.get(LOGIN,"")
                if (l == "1"){
                    return true
                }
            }
            return false
        }

        fun destroy(){
            if (instance != null) {
                instance?.put(LOGIN, "")
            }
        }


    }
}