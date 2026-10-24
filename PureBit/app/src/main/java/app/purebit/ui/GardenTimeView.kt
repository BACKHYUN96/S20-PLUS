package app.purebit.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import app.purebit.theme.ThemeSpec
import app.purebit.theme.BuiltInThemes

class GardenTimeView(context:Context,private val theme:ThemeSpec):TextView(context){
    constructor(context:Context):this(context,BuiltInThemes.hwahyun)
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(c:Canvas){
        val value=text.toString()
        if(!value.contains("───────────")){super.onDraw(c);return}
        val parts=value.split("───────────");p.color=theme.muted;p.typeface=Typeface.SERIF;p.textSize=textSize
        p.textAlign=Paint.Align.LEFT;c.drawText(parts[0].trim(),paddingLeft.toFloat(),height*.7f,p)
        p.textAlign=Paint.Align.RIGHT;c.drawText(parts[1].trim(),(width-paddingRight).toFloat(),height*.7f,p)
    }
}
