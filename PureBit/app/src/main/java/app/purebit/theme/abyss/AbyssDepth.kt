package app.purebit.theme.abyss

import kotlin.math.sin

/** Three deterministic depth planes; fills a reused x/y/radius/alpha array. */
object AbyssDepth {
    fun layer(index:Int)=index%3
    fun sample(index:Int,time:Float,speed:Float,out:FloatArray) {
        val layer=layer(index)
        val seed=((index*73+19)%101)/101f
        val velocity=when(layer){0->.008f;1->.016f;else->.029f}
        val phase=((time*velocity*speed+seed)%1.3f+1.3f)%1.3f
        out[0]=((index*47+11)%103)/103f+sin(time*(.13f+layer*.08f)+index)*(.008f+layer*.012f)
        if(layer==2)out[0]=if(index%2==0).04f+out[0]*.085f else .875f+out[0]*.085f
        out[1]=1.15f-phase
        out[2]=when(layer){0->.5f+index%3*.25f;1->1.3f+index%4*.6f;else->4f+index%5*1.6f}
        out[3]=when(layer){0->28f;1->55f;else->38f}
    }
}
