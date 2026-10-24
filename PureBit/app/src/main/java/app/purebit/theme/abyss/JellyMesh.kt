package app.purebit.theme.abyss

import kotlin.math.*

/** Bell contraction and delayed tentacle currents deform independent rows of a borrowed sprite. */
object JellyMesh {
    const val COLUMNS=8
    const val ROWS=16
    const val FLOATS=(COLUMNS+1)*(ROWS+1)*2
    private val tails=FloatArray(ROWS+1){((it.toFloat()/ROWS-.34f)/.66f).coerceIn(0f,1f)}
    private val bells=FloatArray(ROWS+1){exp(-((it.toFloat()/ROWS-.22f)/.24f).pow(2))}
    private val columnSin=FloatArray(COLUMNS+1){sin((it.toFloat()/COLUMNS-.5f)*1.6f)}
    private val columnCos=FloatArray(COLUMNS+1){cos((it.toFloat()/COLUMNS-.5f)*1.6f)}
    fun fill(left:Float,top:Float,width:Float,height:Float,time:Float,out:FloatArray,variant:Int=0) {
        require(out.size>=FLOATS)
        var index=0
        val contraction=AbyssLife.contraction(time)
        for(row in 0..ROWS){
            val v=row.toFloat()/ROWS
            val tail=tails[row]
            val bell=bells[row]
            val lagged=AbyssLife.contraction(time-tail*.85f)
            val flow=time*.62f-tail*3.8f+variant*.73f
            val sinFlow=sin(flow);val cosFlow=cos(flow)
            for(column in 0..COLUMNS){
                val u=column.toFloat()/COLUMNS
                // Each strand column trails the current differently; the bell keeps its propulsion phase.
                val sway=(sinFlow*columnCos[column]+cosFlow*columnSin[column])*width*.055f*tail
                // Narrowing bell pushes water out; delayed strands trail the power stroke.
                out[index++]=left+width*(.5f+(u-.5f)*(1-contraction*.20f*bell))+sway
                out[index++]=top+height*(v+(lagged-.5f)*.028f*tail+contraction*.035f*bell)
            }
        }
    }
}
