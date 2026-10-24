package app.purebit.theme.abyss

import kotlin.math.min

/** Normalized geometry shared by drawing, touch target and the album transition. */
object AbyssGeometry {
    data class Circle(val x:Float,val y:Float,val radius:Float)
    fun portal(width:Float,height:Float,wide:Boolean=false):Circle {
        val r=min(width*(if(wide).23f else .45f),height*(if(wide).43f else .46f))
        return Circle(width*(if(wide).28f else .49f),height*(if(wide).46f else .48f),r)
    }
    fun album(portal:Circle)=Circle(portal.x+portal.radius*.80f,portal.y+portal.radius*.76f,portal.radius*.28f)
    fun landing(width:Float,height:Float,left:Float,top:Float,targetWidth:Float,targetHeight:Float,progress:Float):Circle {
        val target=portal(targetWidth,targetHeight)
        val q=smooth(progress)
        fun mix(a:Float,b:Float)=a+(b-a)*q
        return Circle(mix(width*.5f,left+target.x),mix(height*.40f,top+target.y),mix(min(width*.43f,height*.27f),target.radius))
    }
    fun stage(progress:Float,from:Float,to:Float)=smooth((progress-from)/(to-from))
    /** Separate arrival, uncropping and departure timings keep the glass readable. */
    fun albumTravel(progress:Float)=stage(progress,0f,.86f)
    fun albumUncrop(progress:Float)=stage(progress,.18f,.96f)
    fun bubbleEnvelope(progress:Float)=stage(progress,0f,.18f)*(1-stage(progress,.72f,1f))
    fun albumHit(x:Float,y:Float,portal:Circle):Boolean {
        val satellite=album(portal)
        fun inside(circle:Circle)=(x-circle.x)*(x-circle.x)+(y-circle.y)*(y-circle.y)<=circle.radius*circle.radius
        return inside(satellite)&&!inside(portal)
    }
    fun smooth(value:Float):Float {val t=value.coerceIn(0f,1f);return t*t*(3-2*t)}
    fun corner(side:Float,progress:Float)=side*(.5f+(.045f-.5f)*smooth(progress))
}
