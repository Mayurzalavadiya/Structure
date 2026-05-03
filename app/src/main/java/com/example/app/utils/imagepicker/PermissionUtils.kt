package com.example.app.utils.imagepicker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Suppress("unused")
object PermissionUtils {

    /*check if permissions are there*/
    fun verifyPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.isEmpty()) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun hasPermission(permissionString: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionString
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun netPermissions(wantedPermissions: Array<String>, context: Context): Array<String> {
        val result = ArrayList<String>()
        for (permission in wantedPermissions) {
            if (!hasPermission(permission, context)) {
                result.add(permission)
            }
        }
        return result.toTypedArray()
    }


    fun canOpenCalender(context: Context): Boolean {
        return (hasPermission(Manifest.permission.WRITE_CALENDAR, context)
                && hasPermission(Manifest.permission.READ_CALENDAR, context))
    }


    fun ifPermissionGiven(context: Context): Boolean {
        return (hasPermission(Manifest.permission.WRITE_CALENDAR, context)
                && hasPermission(Manifest.permission.READ_CALENDAR, context)
                && hasPermission(Manifest.permission.INTERNET, context)
                && hasPermission(Manifest.permission.CAMERA, context)
                && hasPermission(Manifest.permission.RECORD_AUDIO, context)
                && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)
                && hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE, context))
    }



}