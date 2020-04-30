package com.easywaste.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class RecicladorCodigoDialog: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.dialog_recicladorcodigo, container)
    }
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val btnCerrar = v.findViewById<Button>(R.id.btnCerrar)
        btnCerrar.setOnClickListener {
            dismiss()
        }
    }
}