package com.easywaste.app.Clases

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.easywaste.app.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.collections.ArrayList


class ClsLocalizacion : OnMapReadyCallback, OnRequestPermissionsResultCallback,  EasyPermissions.PermissionCallbacks{


    var gmap:GoogleMap? = null
    var permisoLocalizacion: Boolean = false
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION= 1
    val LOCATION_PERMISSION_REQUEST_CODE = 1
    var ultimaLocalizacion: Location?=null
    val DEFAULT_ZOOM = 17f
    var locationClient:FusedLocationProviderClient?= null
    var activity:AppCompatActivity? = null
     var defaultClick:Boolean = true
     var markers = ArrayList<Marker>()
    var clickMarker:Boolean = false
    var markerProveedor:Marker?=null
    var markerReciclador:Marker?=null
    constructor(activity: AppCompatActivity?) {
         this.activity = activity
         activarLocalizacion()
         val locationManager:LocationManager? = activity?.getSystemService(LOCATION_SERVICE) as LocationManager?
         try {
                 // Request location updates
             locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
         } catch(ex: SecurityException) {

         }
     }


     companion object {
         var lastLatLong :LatLng? = null
         const val RC_FINELOCATION = 201

     }

     override fun onMapReady(gmaps: GoogleMap) {
        gmap =gmaps
        gmap?.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        gmap?.clear()
         activarFineLccation()
         gmap?.setOnMapClickListener {
             if(clickMarker){
                 gmap!!.clear()
                 lastLatLong = it
                 markerProveedor(lastLatLong!!)
             }

         }

         if(lastLatLong!=null){
             gmap!!.clear()
             gmap!!.addMarker( MarkerOptions()
                 .position(lastLatLong!!))
             gmap!!.moveCamera(
                 CameraUpdateFactory.newLatLngZoom(lastLatLong, DEFAULT_ZOOM))

         }


     }

     fun agregarMarcador( marker:Marker){
         markers.add(marker)
         val builder = LatLngBounds.Builder()
         for (m in markers) {
             builder.include(m.position)
         }
         val bounds = builder.build()
         val padding = 20
         val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
         gmap!!.moveCamera(cu)
     }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
           ClsLocalizacion.lastLatLong = LatLng(location.latitude, location.longitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun buscarGeoLocalizacion(){

        if (gmap != null) {
            gmap?.clear()

            val act : AppCompatActivity = activity as AppCompatActivity
            locationClient = LocationServices.getFusedLocationProviderClient(act)

            locationClient!!.lastLocation
                .addOnCompleteListener(act) { task ->
                    if (task.isSuccessful && task.result != null) {
                        ultimaLocalizacion = task.result
                        val long = ultimaLocalizacion?.longitude
                        val lat = ultimaLocalizacion?.latitude
                        val latlong = LatLng(lat!!,long!!)
                        lastLatLong = latlong



                        if(Prefs.pullRolId() == 3){
                            markers.add(markerProveedor(latlong))
                            gmap!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(latlong, DEFAULT_ZOOM))
                        }else if (Prefs.pullRolId() == 2){
                            markers.add(markerReciclador(latlong))
                            gmap!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(latlong, DEFAULT_ZOOM))
                        }


                    } else {
                        //no se encontro
                    }
                }
            // Access to the location has been granted to the app.
            gmap!!.setMyLocationEnabled(true)
        }
    }
     fun markerProveedor(coor:LatLng):Marker{
         markerProveedor =  gmap!!.addMarker(MarkerOptions()
                 .position(coor)
                 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
         return markerProveedor!!
     }
     fun markerReciclador(coor:LatLng):Marker{
         markerReciclador =  gmap!!.addMarker(MarkerOptions()
                 .position(coor)
                 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
         return markerReciclador!!
     }
    fun markerNormal(coor:LatLng):Marker{
        val rec =  gmap!!.addMarker(MarkerOptions()
            .position(coor)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        return  rec
    }
    fun activarLocalizacion(){
        activarFineLccation()

        /*
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.

            PermissionUtils.requestPermission( act  , LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        }
         */
    }

    @AfterPermissionGranted(RC_FINELOCATION)
    private fun activarFineLccation() {
        if (EasyPermissions.hasPermissions(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.e("error", "acceso fine locatio")
            permisoLocalizacion = true
            buscarGeoLocalizacion()
        } else {
            // Request one permission
            EasyPermissions.requestPermissions(activity!!, activity!!.getString(R.string.rationale_gps), RC_FINELOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    fun habilitarLocalizacion(){
        activarLocalizacion()
        buscarGeoLocalizacion()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.e("error", "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    @Override
   override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                           grantResults: IntArray) {
        // Forward results to EasyPermissions
       EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }



    /*
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (PermissionUtils.isPermissionGranted(
                permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
           // buscarGeoLocalizacion()
        } else {
            permisoLocalizacion = true
        }
    }
*/


}