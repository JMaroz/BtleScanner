package com.andreamarozzi.btle.scanner.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat

fun Context.hasPermission(vararg permission: String): Boolean {
    return checkPermission(permission).isEmpty()
}

/**
 * Controlla se la lista dei permessin in ingresso è stata concessa, in caso contrario restituisce
 * la lista dei permessi da chiedere
 *
 * @param permission permissi da controllare
 * @return la lista dei permessi da chiedere
 */
fun Context.checkPermission(permission: Array<out String>): Array<String> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pNotGranted = ArrayList<String>()
        for (i in permission.indices) {
            if (!isPermissionGranted(permission[i]))
                pNotGranted.add(permission[i])
        }
        return pNotGranted.toTypedArray()
    }

    return arrayOf()
}

/**
 * Controlla se il permesso in ingresso è stato concesso
 *
 * @param permission permesso da controllare.
 * Esempio Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS
 * @return true permesso concesso, false in caso contrario
 */
fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}