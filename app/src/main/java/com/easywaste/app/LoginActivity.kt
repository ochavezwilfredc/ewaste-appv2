
package com.easywaste.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.easywaste.app.Clases.Prefs


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)
        Prefs.getInstance(applicationContext,true)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        accederSistema()
        cambiarFragment(LoginOpcionFragment())

    }

    fun cambiarFragment(frag: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmento, frag)
        fragmentTransaction.commit()
    }
    fun estaLogeado(){
        Prefs.putString(Prefs.LOGIN,"1")
    }
    fun accederSistema(){
        if(Prefs.isLogin()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                cambiarFragment(LoginOpcionFragment())
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}