package app.purebit.theme.abyss

import kotlin.math.*

/** Time-based visual kinematics, inspired by propulsion/recovery and buoyant rise.
 * Not a fluid solver or a model of a particular jellyfish species. No audio inputs. */
object AbyssLife {
    private fun smooth(x:Float)=x.coerceIn(0f,1f).let {it*it*(3-2*it)}
    private fun fract(x:Float)=x-floor(x)
    fun contraction(time:Float):Float {
        val q=fract(time/3.8f)
        return if(q<.26f)smooth(q/.26f) else 1-smooth((q-.26f)/.74f)
    }
    /** Continuous ascent; most displacement occurs during the short power stroke. */
    fun swimDistance(time:Float):Float {
        val cycles=time/3.8f;val q=fract(cycles)
        val distance=if(q<.26f).7f*smooth(q/.26f) else .7f+.3f*smooth((q-.26f)/.74f)
        return floor(cycles)+distance
    }
    /** x/y/radius-in-viewport-width/opacity/angle/local-time, borrowed array. */
    fun jelly(index:Int,time:Float,out:FloatArray) {
        val local=time*(.70f+index*.075f)+index*2.37f
        val progress=fract(index*.219f+swimDistance(local)*.026f)
        val left=index%2==0
        out[0]=(if(left).125f else .87f)+sin(local*.095f+index)*.027f
        out[1]=1.12f-progress*1.32f
        out[2]=when(index){0->.040f;1->.027f;2->.033f;3->.057f;else->.044f}
        out[3]=smooth((out[1]+.18f)/.14f)*smooth((1.12f-out[1])/.14f)
        out[4]=sin(local*.13f+index)*7f
        out[5]=local
    }
    /** Depth-dependent streams. Larger bubbles rise faster and wobble/deform more.
     * x/y/radius(dp)/opacity/tilt/squash, borrowed array. */
    fun bubble(index:Int,time:Float,speed:Float,out:FloatArray) {
        val layer=index%3
        val seed=((index*73+19)%101)/101f
        val radius=when(layer){0->.55f+index%3*.23f;1->1.6f+index%4*.9f;else->5.5f+index%5*2.4f}
        val rise=when(layer){0->.009f;1->.014f+sqrt(radius)*.003f;else->.020f+sqrt(radius)*.006f}
        val clock=time*speed
        val phase=fract(seed+clock*rise)
        val y=1.14f-phase*1.30f
        val stream=index%7
        val base=when(stream){0->.055f;1->.16f;2->.83f;3->.945f;4->.09f;5->.90f;else->.51f}
        val nearBase=if(index%2==0).06f else .94f
        val wobble=(if(layer==2).012f else .004f)*sin(clock*(.55f+seed*.6f)+index)
        out[0]=(if(layer==2)nearBase else base)+wobble+sin(clock*.11f+index)*.004f
        out[1]=y
        // Slight expansion during ascent, expressed in dp regardless of pixel density.
        out[2]=radius*(.90f+phase*.14f)
        out[3]=(when(layer){0->28f;1->105f;else->190f})*smooth((y+.16f)/.12f)*smooth((1.14f-y)/.12f)
        out[4]=sin(clock*(.65f+seed)+index)*(if(layer==2)9f else 3f)
        out[5]=1f-(if(layer==2).035f else .009f)*(1+sin(clock*.9f+index))
    }
}
