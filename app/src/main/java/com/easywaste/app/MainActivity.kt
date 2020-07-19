package com.easywaste.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.easywaste.app.Clases.ClsLocalizacion
import com.easywaste.app.Clases.Prefs
import com.easywaste.app.Clases.VAR
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.material.navigation.NavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.angmarch.views.NiceSpinner
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var toggle:ActionBarDrawerToggle? = null
    var  drawerLayout: DrawerLayout? = null
    var cantPintrash:TextView?=null
    var OK = false
    @SuppressLint("MissingPermission")

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Prefs.getInstance(applicationContext,true)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        /*
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }

        */
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val txtNombre = headerView.findViewById<TextView>(R.id.nombre)
        val txtDNI = headerView.findViewById<TextView>(R.id.dni)
        val txtCelular = headerView.findViewById<TextView>(R.id.celular)
        val txtRol = headerView.findViewById<TextView>(R.id.rol)
        txtNombre.text = Prefs.pullString(Prefs.PERSONA)
        txtDNI.text = Prefs.pullString(Prefs.DNI)
        txtCelular.text = Prefs.pullString(Prefs.CELULAR)
        txtRol.text = Prefs.pullString(Prefs.ROL)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )


        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.api_google))
        }

        drawerLayout!!.addDrawerListener(toggle!!)

        toggle!!.syncState()
        navView.setNavigationItemSelectedListener(this)
        val menu = navView.menu
        val rolid = Prefs.pullRolId()
        if(rolid==3){
            //PROVEEDOR
            menu.findItem(R.id.nav_servicio).setVisible(true)
            val contPintrash:CardView = headerView.findViewById(R.id.contenedorPintrash)
            contPintrash.visibility = View.VISIBLE

            cantPintrash =  headerView.findViewById(R.id.cantPintrash)
            buscarPintrash()
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post(object : Runnable {
                override fun run() {
                        try {
                            buscarPintrash()
                            mainHandler.postDelayed(this, 5000)
                        }catch (ex:Exception){

                        }

                }
            })
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) { /* ... */
                    }
                }).check()


        }else if(rolid==2){
            //RECICLADOR
            menu.findItem(R.id.nav_solicitud).setVisible(true)
            menu.findItem(R.id.nav_almacen).setVisible(true)

            val estadoReciclador : NiceSpinner = headerView.findViewById(R.id.estado_reciclador)
            estadoReciclador.visibility = View.VISIBLE
            val dataset = listOf("Disponible", "No Disponible", "Ocupado")
            estadoReciclador.attachDataSource(dataset)
            estadoReciclador.setOnSpinnerItemSelectedListener { parent, view, position, id ->
                actualizarEstadoReciclador(parent,true)
            }
            var estado_reciclador = Prefs.pullInt(Prefs.RECICLADOR_ESTADO)
            if(estado_reciclador<1){ estado_reciclador = 1 }
            estadoReciclador.selectedIndex = estado_reciclador - 1
            actualizarEstadoReciclador(estadoReciclador)
            if(Prefs.pullServicioRecicladorId() !=0){
                val frag = ServicioRecicladorOperacionFragment()
                cambiarFragment(frag)
            }else{
                val frag = ServicioRecicladorSolicitudesFragment()
                cambiarFragment(frag)
            }


            OK = true

            val locManager:LocationManager? = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            try {
                // Request location updates
                locManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0f, locationListener)
            } catch(ex: SecurityException) {

            }

            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) { /* ... */
                    }
                }).check()

        }


    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            ClsLocalizacion.lastLatLong = LatLng(location.latitude, location.longitude)
            actualizarPosicionReciclador()
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    fun buscarPintrash(){
        //  cantPintrash?.visibility = View.INVISIBLE
        val params = HashMap<String,Any>()
        params["proveedor_id"] =  Prefs.pullId()
        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("proveedor_pintrash"),parameters,
            Response.Listener { response ->

                if(response!=null){

                    try {
                        cantPintrash?.visibility = View.VISIBLE

                        if(response.getInt("estado") == 200){

                            cantPintrash?.setText( response.getInt("datos").toString()+ " pintrash")

                        }else{
                            cantPintrash?.setText("0 pintrash")

                        }
                    }catch (ex:Exception){
                        cantPintrash?.setText("-")
                        ex.printStackTrace()
                        Log.e("error", ex.message.toString())
                    }
                }

            },
            Response.ErrorListener{
                cantPintrash?.visibility = View.INVISIBLE
                try {
                    cantPintrash?.setText("-")
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Toast.makeText(this,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    ex.printStackTrace()
                    Toast.makeText(this,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }

            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> =HashMap()
                params["TOKEN"] =  Prefs.pullToken()
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

    fun actualizarEstadoReciclador(parent: NiceSpinner, update:Boolean = false){
        val  item = parent.getItemAtPosition( parent.selectedIndex)
        parent.setTextColor(resources.getColor(R.color.textWhite))
        var estado:Int= 0
        when(item) {
            "Disponible" -> {
                parent.setBackgroundColor(resources.getColor(R.color.primaryColor))
                estado = 1
            }
            "No Disponible" ->{
                parent.setBackgroundColor(resources.getColor(R.color.colorRed))
                estado = 2
            }
            "Ocupado" -> {
                parent.setBackgroundColor(resources.getColor(R.color.colorOrange))
                estado = 3
            }

        }
        if(update){
            val params = HashMap<String,Any>()
            params["id"] =  Prefs.pullId()
            params["status"] = estado

            val parameters = JSONObject(params as Map<String, Any>)

            val request : JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, VAR.url("reciclador_status"),parameters,
                Response.Listener { response ->

                    if(response!=null){
                        if(response.getInt("estado") == 200 ){
                            Prefs.putInt(Prefs.RECICLADOR_ESTADO, estado)
                            Toast.makeText(applicationContext,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                        }else{
                            Prefs.putInt(Prefs.RECICLADOR_ESTADO, estado)

                            var estadoReciclador = Prefs.pullInt(Prefs.RECICLADOR_ESTADO)
                            if(estadoReciclador<1){ estadoReciclador = 1 }
                            actualizarEstadoReciclador(parent)
                            Toast.makeText(applicationContext,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                        }
                    }

                },
                Response.ErrorListener{
                    try {
                        val nr = it.networkResponse
                        val r = String(nr.data)
                        val response=  JSONObject(r)
                        Prefs.putInt(Prefs.RECICLADOR_ESTADO, estado)
                        var estadoReciclador = Prefs.pullInt(Prefs.RECICLADOR_ESTADO)
                        if(estadoReciclador<1){ estadoReciclador = 1 }
                        actualizarEstadoReciclador(parent)
                        Toast.makeText(applicationContext,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    }catch (ex: Exception){
                        Log.e("error", ex.message.toString())
                        ex.printStackTrace()
                        Toast.makeText(applicationContext,  "Error de conexión", Toast.LENGTH_SHORT).show()
                    }

                }) {
                override fun getHeaders(): Map<String, String> {
                    var params: MutableMap<String, String> =HashMap()
                    params["TOKEN"] =  Prefs.pullToken()
                    return params
                }
            }

            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(request)
        }

    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.



        when (item.itemId) {
            R.id.action_settings -> return true
            R.id.home  -> {
                supportFragmentManager.popBackStack()
                return true
            }


            else -> return super.onOptionsItemSelected(item)
        }

    }


    fun actualizarPosicionReciclador(){

        val params = HashMap<String,Any>()
        params["reciclador_id"] =  Prefs.pullId()
        params["latitud"] =  ClsLocalizacion.lastLatLong!!.latitude
        params["longitud"] =  ClsLocalizacion.lastLatLong!!.longitude

        val parameters = JSONObject(params as Map<String, Any>)

        val request : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, VAR.url("reciclador_posicion_actual"),parameters,
            Response.Listener { response ->

                if(response!=null){
                  //  Toast.makeText(this,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener{
                try {
                    val nr = it.networkResponse
                    val r = String(nr.data)
                    val response=  JSONObject(r)
                    Toast.makeText(this,  response.getString("mensaje"), Toast.LENGTH_SHORT).show()
                }catch (ex: Exception){
                    ex.printStackTrace()
                    Toast.makeText(this,  "Error de conexión", Toast.LENGTH_SHORT).show()
                }

            }) {
            override fun getHeaders(): Map<String, String> {
                var params: MutableMap<String, String> =HashMap()
                params["TOKEN"] =  Prefs.pullToken()
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_cuenta -> {
                val frag = PerfilPersonaFragment()
                cambiarFragment(frag)
            }

            R.id.nav_informacion->{
                Prefs.getInstance(this)
                val url = String.format(VAR.urlInformacion() + "?persona=%s",Prefs.pullId())
                Log.e("url", url)
                val intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            R.id.nav_servicio -> {
                if(Prefs.pullServicioId() == 0){
                    val frag = ServicioProveedorRegistrarFragment()
                    cambiarFragment(frag)
                }else{
                    val frag = ServicioProveedorEspereFragment()
                    cambiarFragment(frag)
                }
            }
            R.id.nav_almacen -> {
                val frag = ServicioRecicladorAlmacenesFragment()
                cambiarFragment(frag)
            }
            R.id.nav_solicitud -> {
                val frag = ServicioRecicladorSolicitudesFragment()
                cambiarFragment(frag)
            }
            R.id.nav_logout  -> {
                Prefs.destroy()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun cambiarFragment(frag:Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmento, frag)
        fragmentTransaction.commit()
    }
    fun cambiarFragmentBackStack(frag:Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmento, frag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
    fun habilitarNavDrawer(habilitado:Boolean){

        if (!habilitado) {
            toggle!!.isDrawerIndicatorEnabled = false
            drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            toggle?.setToolbarNavigationClickListener { onBackPressed() }
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            toggle?.isDrawerIndicatorEnabled = true
            toggle?.toolbarNavigationClickListener = null
            toggle?.syncState()
        }
    }

    fun mostrarBotonMenu(){
        supportActionBar!!.show()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    fun mostrarBotonAtras(){
        supportActionBar!!.show()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

         when(requestCode) {

            ServicioProveedorRegistrarFragment.RETORNA_IMAGEN -> {
                if (resultCode == 1) {
                    /*
                    if( ServicioProveedorRegistrarFragment.fragReferenciaDialog!=null){
                        Log.e("error", "mostrando data 2")
                        ServicioProveedorRegistrarFragment.fragReferenciaDialog?.imageView?.setImageURI(data?.data)
                    }

                     */
                }

            }
        }

    }
}
