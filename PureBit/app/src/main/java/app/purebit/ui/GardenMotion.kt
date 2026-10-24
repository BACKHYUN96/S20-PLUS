package app.purebit.ui

import kotlin.math.*

/** Normalized ornamental motion only. No dependency on audio transport or a device clock. */
object GardenMotion {
    fun heroHeight(screenDp:Float)=(screenDp*.34f).coerceIn(230f,340f)
    fun count(base:Int,scale:Float)=(base*scale.coerceIn(0f,2f)).toInt().coerceIn(0,96)
    /** out = normalized x/y, size dp, rotation, alpha. Shared wind with three depth layers. */
    fun petal(index:Int,seconds:Float,speed:Float,out:FloatArray){
        val phase=seconds.coerceAtLeast(0f)*speed.coerceIn(.2f,2f)
        val near=index%9==0;val far=index%3==0&&!near
        val z=if(near)1.6f else if(far).45f else 1f
        val seed=((index*0.618034f)%1f+1f)%1f
        val fall=(seed+phase*(.019f+index%5*.002f)*z)%1f
        var x=((index*.381966f-phase*.012f*z)%1f+1f)%1f
        x+=sin(phase*.27f+index*1.7f)*.07f
        if(near)x=if(index%2==0)x*.20f else .8f+x*.20f
        out[0]=x;out[1]=fall*1.18f-.09f
        out[2]=if(near)24f+index%4*5f else if(far)3f+index%3 else 6f+index%5
        out[3]=index*37f+phase*(9f+index%11)
        val fade=(min(fall,1-fall)*12f).coerceIn(0f,1f)
        out[4]=(if(near).36f else if(far).25f else .58f)*fade
    }
}
