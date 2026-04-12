package com.starter.app.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.starter.app.R
import androidx.core.graphics.drawable.toDrawable

object DialogUtils {

    /**
     * AlertDialog with Positive and Negative buttons.
     */
    fun showAlertDialog(
        context: Context,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveButtonClicked: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClicked: (dialog: DialogInterface) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveButtonClicked.invoke(dialog)
        }
        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            onNegativeButtonClicked.invoke(dialog)
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun showAlertDialog(
        context: Context,
        message: String,
        positiveButtonText: String,
        onPositiveButtonClicked: (dialog: DialogInterface) -> Unit,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveButtonClicked.invoke(dialog)

        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    fun showNotOutSideClickAlertDialog(
        context: Context,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveButtonClicked: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClicked: (dialog: DialogInterface) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveButtonClicked.invoke(dialog)
        }
        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            onNegativeButtonClicked.invoke(dialog)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun showPermissionAlertDialog(
        context: Context,
        message: String,
        positiveButtonText: String,
        onPositiveButtonClicked: (dialog: DialogInterface) -> Unit,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveButtonClicked.invoke(dialog)
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun showUpdateDialog(
        context: Context,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveButtonClicked: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClicked: (dialog: DialogInterface) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveButtonClicked.invoke(dialog)
        }

        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            onNegativeButtonClicked.invoke(dialog)
        }

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    fun setCustomeDialog(
        context: Context,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        isTitleHide: Boolean? = null,
        nevBtnHide: Boolean? = null,
        onPositiveButtonClicked: (dialog: DialogInterface) -> Unit,
        onNegativeButtonClicked: (dialog: DialogInterface) -> Unit,
    ) {


        val dialogBuilder = android.app.AlertDialog.Builder(context).create()
        val inflater = LayoutInflater.from(context)
        dialogBuilder.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val dialogView = inflater.inflate(R.layout.common_dialog, null)
        dialogBuilder.setView(dialogView)


        val textShow = dialogView.findViewById<AppCompatTextView>(R.id.textShow)
        val buttonNeg = dialogView.findViewById<AppCompatButton>(R.id.btnNegitive)
        val buttonPos = dialogView.findViewById<AppCompatButton>(R.id.btnPositive)

        // Uncomment this line if restrict outside click and dismiss dialog
        /*dialogBuilder.setCanceledOnTouchOutside(false)*/

        textShow.text = message
        buttonNeg.text = negativeButtonText
        buttonPos.text = positiveButtonText


        //mange hide show
        buttonNeg.setOnClickListener {
            onNegativeButtonClicked.invoke(dialogBuilder)
        }

        buttonPos.setOnClickListener {
            onPositiveButtonClicked.invoke(dialogBuilder)
        }

        dialogBuilder.show()
    }

}