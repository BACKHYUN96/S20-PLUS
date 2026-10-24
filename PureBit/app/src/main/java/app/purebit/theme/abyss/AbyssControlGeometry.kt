package app.purebit.theme.abyss

import kotlin.math.min

/** Optical sizes are separate from native 48dp input targets. No playback settings. */
object AbyssControlGeometry {
    enum class Form { ORB, RING, CAPSULE, ICON, NAV, PANEL }
    private val transport=setOf("▶","Ⅱ","■")
    private val navigation=setOf("PLAYER","LIBRARY","QUEUE","SETTINGS")
    private val ring=setOf("−","＋")
    private val capsule=setOf("A–B","A–B 구간","LYRICS","가사")
    private val icon=setOf("◀◀","▶▶","⇄","↻","↻¹","⚙","⛶")
    fun form(label: String, primary: Boolean): Form=when {
        primary&&label in transport -> Form.ORB
        label in navigation -> Form.NAV
        label in ring -> Form.RING
        label in capsule -> Form.CAPSULE
        label in icon -> Form.ICON
        else -> Form.PANEL
    }
    data class Surface(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        val width get() = right-left
        val height get() = bottom-top
    }
    fun surface(width: Float, height: Float, density: Float, form: Form): Surface {
        val w=width.coerceAtLeast(0f);val h=height.coerceAtLeast(0f)
        val side=min(w,h)
        val sw=when(form) {
            Form.ORB -> side*.88f
            Form.RING -> min(side-4*density,28*density).coerceAtLeast(0f)
            Form.CAPSULE -> (w-12*density).coerceAtLeast(0f)
            else -> (w-4*density).coerceAtLeast(0f)
        }
        val sh=when(form) {
            Form.ORB,Form.RING -> sw
            Form.CAPSULE -> min((h-4*density).coerceAtLeast(0f),24*density)
            else -> (h-4*density).coerceAtLeast(0f)
        }
        return Surface((w-sw)/2,(h-sh)/2,(w+sw)/2,(h+sh)/2)
    }
    /** Keep narrow screens and large text usable, without giving up the centered reference group. */
    fun groupInset(widthDp: Float, fraction: Float, minimumDp: Float): Int =
        ((widthDp-maxOf(widthDp*fraction,minimumDp).coerceAtMost(widthDp))/2).toInt().coerceAtLeast(0)
}
