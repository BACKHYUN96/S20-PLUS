package app.purebit.theme.abyss

import kotlin.math.exp
import kotlin.math.max

/** Read-only source-energy visualization. Constant loudness cannot repeatedly trigger a wave. */
class AbyssRipples {
    val ages=FloatArray(4){10f}
    val strengths=FloatArray(4)
    var glow=0f;private set
    private var baseline=0f;private var previous=0f;private var cooldown=1f;private var armed=true;private var next=0
    fun reset(){ages.fill(10f);strengths.fill(0f);glow=0f;baseline=0f;previous=0f;cooldown=1f;armed=true;next=0}
    fun update(input:Float,seconds:Float,enabled:Boolean) {
        if(!enabled){reset();return}
        val dt=seconds.coerceIn(0f,.1f)
        val energy=if(input.isFinite())input.coerceIn(0f,1f)else 0f
        for(i in ages.indices)ages[i]+=dt
        cooldown+=dt;glow*=exp(-dt*5f)
        if(energy<baseline+.012f)armed=true
        val excess=energy-baseline
        if(armed&&cooldown>=.22f&&excess>.035f+baseline*.17f&&energy-previous>.002f){
            val strength=(excess*3f).coerceIn(.25f,1f)
            ages[next]=0f;strengths[next]=strength;next=(next+1)%ages.size
            glow=max(glow,strength);cooldown=0f;armed=false
        }
        baseline+=(energy-baseline)*(1-exp(-dt*2f));previous=energy
    }
    fun phase(index:Int)=(ages[index]/1.65f).coerceIn(0f,1f)
}
