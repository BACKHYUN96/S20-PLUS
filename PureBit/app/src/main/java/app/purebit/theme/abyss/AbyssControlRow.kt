package app.purebit.theme.abyss

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout

/** Center the reference's narrow auxiliary groups without fixed padding on small screens. */
class AbyssControlRow @JvmOverloads constructor(context: Context, private val fraction: Float=.64f, private val minimumDp: Float=232f): LinearLayout(context) {
    init {orientation=HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
    override fun onSizeChanged(w: Int,h: Int,oldw: Int,oldh: Int) {
        super.onSizeChanged(w,h,oldw,oldh)
        val d=resources.displayMetrics.density
        val accessibleMinimum=if(resources.configuration.fontScale>1.15f)minimumDp*1.35f else minimumDp
        val inset=(AbyssControlGeometry.groupInset(w/d,fraction,accessibleMinimum)*d).toInt()
        if(paddingLeft!=inset)setPadding(inset,paddingTop,inset,paddingBottom)
    }
}
