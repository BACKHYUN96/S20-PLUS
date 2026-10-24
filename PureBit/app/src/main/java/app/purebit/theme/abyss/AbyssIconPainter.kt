package app.purebit.theme.abyss

import android.graphics.*
import kotlin.math.*

/** One set of optical proportions for the reference transport, header and navigation. */
class AbyssIconPainter {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    private val rect=RectF()
    fun draw(c:Canvas,key:String,x:Float,y:Float,r:Float,color:Int,density:Float) {
        c.save();c.translate(x,y)
        p.shader=null;p.color=color;p.style=Paint.Style.STROKE
        p.strokeWidth=.95f*density;p.strokeCap=Paint.Cap.ROUND;p.strokeJoin=Paint.Join.ROUND
        fun line(x1:Float,y1:Float,x2:Float,y2:Float)=c.drawLine(x1*r,y1*r,x2*r,y2*r,p)
        when(key) {
            "▶","Ⅱ","■","◀◀","▶▶" -> {
                p.style=Paint.Style.FILL
                when(key) {
                    "▶" -> {path.reset();path.moveTo(-r*.55f,-r);path.lineTo(r*.98f,0f);path.lineTo(-r*.55f,r);path.close();c.drawPath(path,p)}
                    "Ⅱ" -> {rect.set(-r*.66f,-r,-r*.2f,r);c.drawRoundRect(rect,r*.1f,r*.1f,p);rect.set(r*.2f,-r,r*.66f,r);c.drawRoundRect(rect,r*.1f,r*.1f,p)}
                    "■" -> c.drawRect(-r*.7f,-r*.7f,r*.7f,r*.7f,p)
                    else -> {
                        if(key=="◀◀")c.scale(-1f,1f)
                        path.reset();path.moveTo(-r*.88f,-r);path.lineTo(r*.65f,0f);path.lineTo(-r*.88f,r);path.close();c.drawPath(path,p)
                        c.drawRect(r*.76f,-r,r*.95f,r,p)
                    }
                }
            }
            "PLAYER" -> {
                rect.set(-r*.78f,-r*.94f,r*.78f,r*.83f);c.drawArc(rect,180f,180f,false,p)
                line(-.78f,-.08f,-.78f,.8f);line(.78f,-.08f,.78f,.8f)
                rect.set(-r*.95f,-r*.05f,-r*.53f,r*.85f);c.drawRoundRect(rect,r*.18f,r*.18f,p)
                rect.set(r*.53f,-r*.05f,r*.95f,r*.85f);c.drawRoundRect(rect,r*.18f,r*.18f,p)
            }
            "LIBRARY" -> {for(i in 0..4){val xx=-.85f+i*.425f;line(xx,.75f,xx,when(i){1->-.95f;2->-.4f;3->-.65f;else->-.1f})};line(-1f,.98f,1f,.98f)}
            "QUEUE" -> {for(i in 0..2){val yy=-.8f+i*.57f;line(-1f,yy,.16f,yy)};path.reset();path.moveTo(r*.43f,-r*.2f);path.lineTo(r*.99f,r*.25f);path.lineTo(r*.43f,r*.7f);path.close();c.drawPath(path,p)}
            "SETTINGS","⚙" -> {
                path.reset()
                for(i in 0 until 32){val a=-PI/2+i*PI/16;val rr=if(i%4==1||i%4==2)r else r*.79f;val xx=cos(a).toFloat()*rr;val yy=sin(a).toFloat()*rr;if(i==0)path.moveTo(xx,yy)else path.lineTo(xx,yy)}
                path.close();c.drawPath(path,p);c.drawCircle(0f,0f,r*.37f,p)
            }
            "−","＋" -> {line(-.55f,0f,.55f,0f);if(key=="＋")line(0f,-.55f,0f,.55f)}
            "⇄" -> {
                path.reset();path.moveTo(-r*.98f,-r*.55f);path.lineTo(-r*.6f,-r*.55f);path.cubicTo(-r*.05f,-r*.55f,r*.06f,r*.55f,r*.65f,r*.55f);path.lineTo(r*.98f,r*.55f);c.drawPath(path,p)
                // The crossing has a tiny gap, so the two paths remain distinct at native size.
                path.reset();path.moveTo(-r*.98f,r*.55f);path.lineTo(-r*.6f,r*.55f);path.cubicTo(-r*.4f,r*.55f,-r*.23f,r*.35f,-r*.14f,r*.18f);c.drawPath(path,p)
                path.reset();path.moveTo(r*.14f,-r*.18f);path.cubicTo(r*.3f,-r*.43f,r*.4f,-r*.55f,r*.65f,-r*.55f);path.lineTo(r*.98f,-r*.55f);c.drawPath(path,p)
                line(.72f,-.82f,1f,-.55f);line(1f,-.55f,.72f,-.28f);line(.72f,.28f,1f,.55f);line(1f,.55f,.72f,.82f)
            }
            "↻","↻¹" -> {
                path.reset();path.moveTo(-r*.94f,-r*.05f);path.cubicTo(-r*.94f,-r*.5f,-r*.62f,-r*.58f,-r*.25f,-r*.58f);path.lineTo(r*.85f,-r*.58f);c.drawPath(path,p)
                path.reset();path.moveTo(r*.94f,r*.05f);path.cubicTo(r*.94f,r*.5f,r*.62f,r*.58f,r*.25f,r*.58f);path.lineTo(-r*.85f,r*.58f);c.drawPath(path,p)
                line(.62f,-.85f,.92f,-.58f);line(.92f,-.58f,.62f,-.31f);line(-.62f,.31f,-.92f,.58f);line(-.92f,.58f,-.62f,.85f)
                if(key=="↻¹"){p.style=Paint.Style.FILL;p.textSize=r*.85f;p.textAlign=Paint.Align.CENTER;c.drawText("1",0f,r*.28f,p)}
            }
            "⛶" -> {for(i in 0..3){c.save();c.rotate(i*90f);line(.4f,-.85f,.85f,-.85f);line(.85f,-.85f,.85f,-.4f);c.restore()}}
        }
        c.restore()
    }
}
