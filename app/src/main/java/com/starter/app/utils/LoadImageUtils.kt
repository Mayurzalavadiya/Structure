package com.starter.app.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.starter.app.R
import java.io.ByteArrayOutputStream
import java.io.File

fun isValidContextForGlide(context: Context?): Boolean {
    if (context == null) {
        return false
    }
    if (context is Activity) {
        val activity = context as Activity?
        if (activity!!.isDestroyed || activity.isFinishing) {
            return false
        }
    }
    return true
}

fun AppCompatImageView.load(url: File, isCenterCrop: Boolean = true) {
    if (isValidContextForGlide(this.context)) {

        if (isCenterCrop) {

            Glide.with(this.context)
                .asDrawable()
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .apply(RequestOptions().centerCrop())
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(this)

        } else {

            Glide.with(this.context)
                .asDrawable()
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(this)

        }

    }
}

fun AppCompatImageView.loadDrawable(drawableRes: Int, isCenterCrop: Boolean = true) {
    if (isValidContextForGlide(this.context)) {

        if (isCenterCrop) {
            Glide.with(this.context)
                .asDrawable()
                .load(drawableRes)
                .apply(RequestOptions().centerCrop())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(this)
        } else {
            Glide.with(this.context)
                .asDrawable()
                .load(drawableRes)
                .apply(RequestOptions().centerInside())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(this)
        }

    }
}

fun AppCompatImageView.load(
    strImage: String,
    isCenterCrop: Boolean = true,
    loaderColor: Int = R.color.colorPrimary,
    isShowLoader: Boolean = true,
    placeHolder: Int = R.mipmap.ic_launcher,
) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    if (isShowLoader) {
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.setColorSchemeColors(resources.getColor(loaderColor, null))
        circularProgressDrawable.start()
    } else {
        circularProgressDrawable.stop()
    }
    if (isValidContextForGlide(this.context)) {

        if (isCenterCrop) {
            Glide.with(this.context)
                .asDrawable()
                .load(strImage)
                .placeholder(circularProgressDrawable)
                .error(placeHolder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions().centerCrop())
//                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(this)
        } else {
            Glide.with(this.context)
                .asDrawable()
                .load(strImage)
                .placeholder(circularProgressDrawable)
                .error(placeHolder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
//                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(this)
        }

    }
}


fun AppCompatImageView.loadUrl(
    url: String,
    @DrawableRes placeHolder: Int = R.mipmap.ic_launcher,/*, width: Int, height: Int*/
    isCenterCrop: Boolean = true,
) {

    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.setColorSchemeColors(
//        resources.getColor(R.color.colorBlack)
        ContextCompat.getColor(this.context,R.color.colorAccent)
        /*,
            resources.getColor(R.color.redLight),resources.getColor(R.color.yellow)*/
    )
    circularProgressDrawable.start()

    /*if (placeHolder == 0)

        GlideApp.with(this)
                .load(url)
//                .override(dpToPx(width), dpToPx(height))
                .centerCrop()
                .into(this)
    else*/
    if (isCenterCrop) {
        Glide.with(this)
            .load(url)
//                .override(dpToPx(width), dpToPx(height))
            .centerCrop()
            //  .placeholder(circularProgressDrawable)
            .error(placeHolder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    } else {
        Glide.with(this)
            .load(url)
//                .override(dpToPx(width), dpToPx(height))
            // .placeholder(circularProgressDrawable)
            .error(placeHolder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
}

fun AppCompatImageView.loadRounded(strImageUrl: String, radius: Int, isCenterCrop: Boolean = true) {
    if (isValidContextForGlide(this.context)) {

        if (isCenterCrop) {

            val multiTransformation = MultiTransformation(CenterCrop(), RoundedCorners(radius))

            Glide.with(this.context)
                .load(
                    if (strImageUrl.isEmpty() || strImageUrl == "null")
                        R.mipmap.ic_launcher
                    else
                        strImageUrl
                )
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(this)

        } else {

            val multiTransformation = MultiTransformation(RoundedCorners(radius))

            Glide.with(this.context)
                .load(
                    if (strImageUrl.isEmpty() || strImageUrl == "null")
                        R.mipmap.ic_launcher
                    else
                        strImageUrl
                )
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(this)
        }
    }
}

fun AppCompatImageView.loadDrawableRounded(
    drawableRes: Int,
    radius: Int,
    isCenterCrop: Boolean = true,
) {
    if (isValidContextForGlide(this.context)) {

        if (isCenterCrop) {

            val multiTransformation = MultiTransformation(CenterCrop(), RoundedCorners(radius))

            Glide.with(this.context)
                .load(drawableRes)
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(this)

        } else {

            val multiTransformation = MultiTransformation(RoundedCorners(radius))

            Glide.with(this.context)
                .load(drawableRes)
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .into(this)

        }

    }
}


fun AppCompatImageView.loadCircleDrawable(
    drawableRes: Int,
) {
    if (isValidContextForGlide(this.context)) {
        Glide.with(this.context)
            .load(drawableRes)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
    }
}


fun AppCompatImageView.loadCircle(
    strImageUrl: String,
    loaderColor: Int = R.color.colorPrimary,
    placeHolder: Int = R.mipmap.ic_launcher,
) {
    if (isValidContextForGlide(this.context)) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.setColorSchemeColors(
            resources.getColor(loaderColor, null)
        )
        circularProgressDrawable.start()

        Glide.with(this.context)
            .load(
                if (strImageUrl.isEmpty() || strImageUrl == "null")
                    R.mipmap.ic_launcher
                else
                    strImageUrl
            )
            .placeholder(circularProgressDrawable)
            .error(placeHolder)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
    }
}


fun AppCompatImageView.loadBitmap(bitmap: Bitmap) {

    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

    val multiTransformation = MultiTransformation(
        CenterCrop(),
        RoundedCorners(context.resources.getDimension(com.intuit.sdp.R.dimen._3sdp).toInt())
    )

    if (isValidContextForGlide(this.context)) {

        Glide.with(this.context)
            .asBitmap()
            .load(byteArrayOutputStream.toByteArray())
            .apply(RequestOptions.bitmapTransform(multiTransformation))
//                .animate(R.anim.load_image_animation)
            .into(this)

    }

}

fun AppCompatImageView.loadFile(url: File) {

    val multiTransformation = MultiTransformation(
        CenterCrop(),
        RoundedCorners(context.resources.getDimension(com.intuit.sdp.R.dimen._3sdp).toInt())
    )

    if (isValidContextForGlide(this.context)) {

        Glide.with(this.context)
            .asDrawable()
            .load(url)
            .apply(RequestOptions.bitmapTransform(multiTransformation))
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .into(this)

    }
}


fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun dpToPx(context: Context, valueInDp: Float): Float {
    val metrics: DisplayMetrics? = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}