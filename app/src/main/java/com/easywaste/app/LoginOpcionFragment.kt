package com.easywaste.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import com.easywaste.app.Clases.Prefs

class LoginOpcionFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.login_opcion, container, false)
        val activity = activity as AppCompatActivity?
        Prefs.getInstance(context!!)

        if (activity != null) {
            activity.title = "Registrar"
            activity.supportActionBar!!.hide()
        }

        val btnIngresar = v.findViewById<Button>(R.id.btnIngresar)
        val btnRegistrar = v.findViewById<Button>(R.id.btnRegistrar)

        btnIngresar.setOnClickListener {
            btnIngresar.isEnabled = false
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.fragmento, LoginProveedorFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            btnIngresar.isEnabled = true
        }
        btnRegistrar.setOnClickListener {
            btnRegistrar.isEnabled = false
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.fragmento, LoginRecicladorFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            btnRegistrar.isEnabled = true
        }

        return v
    }


}