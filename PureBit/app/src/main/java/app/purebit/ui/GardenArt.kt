package app.purebit.ui

import android.content.Context
import android.graphics.*
import android.os.SystemClock

object GardenClock {
    private val origin=SystemClock.uptimeMillis()
    fun seconds()=(SystemClock.uptimeMillis()-origin)/1000f
}

/** Bundled artwork is decoded once per process; views share immutable bitmaps. */
object GardenArt {
    private var garden:Bitmap?=null;private var moon:Bitmap?=null
    @Synchronized fun background(context:Context):Bitmap= garden ?: load(context,"garden.png").also {garden=it}
    @Synchronized fun moon(context:Context):Bitmap= moon ?: load(context,"moon.png").also {moon=it}
    private fun load(context:Context,name:String):Bitmap = context.applicationContext.assets.open("hwahyun/$name").use {
        requireNotNull(BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inPreferredConfig=Bitmap.Config.ARGB_8888}))
    }
}
