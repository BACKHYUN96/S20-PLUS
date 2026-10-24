package app.purebit.theme.abyss

import android.content.Context
import android.graphics.*
import app.purebit.theme.ThemeSpec
import kotlin.math.*

/** Visuals only. No audio, device, microphone or playback control dependencies. */
class AbyssScene(context:Context) {
    private var effects=AbyssEffects()
    fun configure(value:AbyssEffects){effects=value}
    companion object {
        private var cached:Bitmap?=null
        private var portalTexture:Bitmap?=null
        @Synchronized private fun portalArtwork(context:Context):Bitmap? {
            if(portalTexture==null)portalTexture=runCatching {context.applicationContext.assets.open("abyss/portal.png").use {BitmapFactory.decodeStream(it)}}.getOrNull()
            return portalTexture
        }
        private var jellyTexture:Bitmap?=null
        private var foregroundTexture:Bitmap?=null
        @Synchronized private fun foregroundArtwork(context:Context):Bitmap? {
            if(foregroundTexture==null)foregroundTexture=runCatching {context.applicationContext.assets.open("abyss/foreground.png").use {BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=2})}}.getOrNull()
            return foregroundTexture
        }
        @Synchronized private fun jellyArtwork(context:Context):Bitmap? {
            if(jellyTexture==null)jellyTexture=runCatching {context.applicationContext.assets.open("abyss/jellyfish.png").use {BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=2})}}.getOrNull()
            return jellyTexture
        }
        @Synchronized fun artwork(context:Context):Bitmap? {
            if(cached==null)cached=runCatching {context.applicationContext.assets.open("abyss/ocean.png").use {BitmapFactory.decodeStream(it)}}.getOrNull()
            return cached
        }
    }
    private val ocean=artwork(context)
    private val portalImage=portalArtwork(context)?:ocean
    private val jellyImage=jellyArtwork(context)
    private val foregroundImage=foregroundArtwork(context)
    private val water by lazy {ocean?.let {AbyssWater(it)}}
    private val portalWater by lazy {portalImage?.let {AbyssWater(it)}}
    private val jellyTint=ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.62f,0f,0f,0f,0f,0f,.88f,0f,0f,0f,0f,0f,1f,0f,0f,0f,0f,0f,1f,0f)))
    private val distantJellyTint=ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.36f,0f,0f,0f,0f,0f,.68f,0f,0f,0f,0f,0f,.83f,0f,0f,0f,0f,0f,1f,0f)))
    private val mesh=FloatArray(JellyMesh.FLOATS)
    private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val dst=RectF();private val src=Rect();private val clip=Path();private val ray=Path()
    private val d=context.resources.displayMetrics.density
    private val point=FloatArray(6)
    private val life=FloatArray(6)
    private val soft=RadialGradient(0f,0f,1f,intArrayOf(0x008beaff,0x008beaff,0x408beaff,0x008beaff),floatArrayOf(0f,.45f,.7f,1f),Shader.TileMode.CLAMP)
    private val softMatrix=Matrix()
    private val lens=RadialGradient(0f,0f,1f,intArrayOf(0x00000000,0x00000000,0x48000612,0x503894a5,0xd0d1ffff.toInt(),0xb02295b9.toInt(),0x002295b9),floatArrayOf(0f,.90f,.945f,.965f,.978f,.989f,1f),Shader.TileMode.CLAMP)
    private val lensMatrix=Matrix()
    private val bubbleLight=RadialGradient(-.32f,-.38f,1.5f,intArrayOf(0x708fefff,0x00174558,0x186bb8ce),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP)
    private val bubbleMatrix=Matrix()
    private val covers=CoverFade<Bitmap>()
    private var coverAt=-10f
    private val light=LinearGradient(0f,0f,0f,1f,intArrayOf(0x2473ddeb,0x0873ddeb,0x0073ddeb),floatArrayOf(0f,.35f,.72f),Shader.TileMode.CLAMP)
    private val lightMatrix=Matrix()
    private val beam=LinearGradient(-1f,0f,1f,0f,intArrayOf(0x0088dfee,0x6088dfee,0xff88dfee.toInt(),0x6088dfee,0x0088dfee),floatArrayOf(0f,.3f,.5f,.7f,1f),Shader.TileMode.CLAMP)
    private val beamMatrix=Matrix()
    private val shafts=ComposeShader(beam,light,PorterDuff.Mode.DST_IN)
    private val controlShade=RadialGradient(0f,0f,1f,intArrayOf(0xe5021221.toInt(),0x99021221.toInt(),0x00021221),floatArrayOf(0f,.55f,1f),Shader.TileMode.CLAMP)
    private val depthFog=RadialGradient(0f,0f,1f,intArrayOf(0x0a438ba4,0x00132f40,0x65000913),floatArrayOf(0f,.55f,1f),Shader.TileMode.CLAMP)
    private val fogMatrix=Matrix()
    private val backgroundShade=LinearGradient(0f,0f,0f,1f,intArrayOf(0x10020810,0x30020810,0xe8020810.toInt()),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP)
    private val backgroundShadeMatrix=Matrix()
    private val albumShine=LinearGradient(-1f,-1f,1f,1f,intArrayOf(0x70ffffff,0x00ffffff,0x10ffffff),null,Shader.TileMode.CLAMP)
    private val albumShineMatrix=Matrix()
    private val refraction=ocean?.let {BitmapShader(it,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP)}
    private val refractionMatrix=Matrix()
    fun setArtwork(bitmap:Bitmap?,time:Float,animate:Boolean){if(bitmap!==covers.current&&animate)coverAt=time;covers.submit(bitmap,time,animate)}
    fun settleArtwork(){covers.settle();coverAt=-10f}
    fun background(c:Canvas,w:Float,h:Float,t:Float,quiet:Boolean=false,descent:Float=1f) {
        c.drawColor(0xff020e19.toInt())
        ocean?.let {bitmap ->
            val zoom=1.055f+(1-descent)*.14f
            val scale=max(w/bitmap.width,h/bitmap.height)*zoom
            val bw=bitmap.width*scale;val bh=bitmap.height*scale
            val dx=sin(t*.045f)*w*.007f;val dy=sin(t*.037f)*h*.004f
            dst.set((w-bw)/2+dx,(h-bh)/2+(1-descent)*h*.045f+dy,(w+bw)/2+dx,(h+bh)/2+(1-descent)*h*.045f+dy)
            water?.draw(c,null,dst,t,if(quiet)110 else 225,if(quiet)effects.light*.4f else effects.light)
        }
        foregroundImage?.let {bitmap ->
            val dw=w*1.035f;val dh=h*1.025f;val dx=sin(t*.071f)*w*.006f;val dy=sin(t*.052f)*h*.005f
            dst.set((w-dw)/2+dx,(h-dh)/2+dy,(w+dw)/2+dx,(h+dh)/2+dy)
            fill(Color.WHITE);p.alpha=if(quiet)25 else 55;c.drawBitmap(bitmap,null,dst,p)
        }
        fogMatrix.setScale(w*.78f,h*.67f);fogMatrix.postTranslate(w*.5f,h*.32f);depthFog.setLocalMatrix(fogMatrix)
        fill(Color.WHITE);p.shader=depthFog;p.alpha=if(quiet)100 else 200;c.drawRect(0f,0f,w,h,p);p.shader=null
        backgroundShadeMatrix.setScale(w,h);backgroundShade.setLocalMatrix(backgroundShadeMatrix)
        fill(Color.WHITE);p.shader=backgroundShade;c.drawRect(0f,0f,w,h,p);p.shader=null
        // Dark central water behind metadata and controls; side rocks keep their detail.
        fogMatrix.setScale(w*.62f,h*.38f);fogMatrix.postTranslate(w*.5f,h*.77f);controlShade.setLocalMatrix(fogMatrix)
        fill(Color.WHITE);p.shader=controlShade;c.drawRect(0f,h*.48f,w,h,p);p.shader=null
        for(i in 0..3) {
            val x=w*(.29f+i*.13f)+sin(t*.055f+i)*w*.018f
            // A soft horizontal falloff prevents visible triangle edges.
            c.save();c.rotate(-5f+i*3f,x,0f)
            beamMatrix.setScale(w*(.045f+i*.009f),1f);beamMatrix.postTranslate(x,0f);beam.setLocalMatrix(beamMatrix)
            lightMatrix.setScale(w,h);light.setLocalMatrix(lightMatrix)
            fill(Color.WHITE);p.shader=shafts;p.alpha=((if(quiet)40f else 150+20*sin(t*.13f+i))*effects.light).toInt().coerceIn(0,255);c.drawRect(x-w*.10f,-h*.04f,x+w*.10f,h*.76f,p);p.shader=null;c.restore()
        }
    }
    fun particles(c:Canvas,w:Float,h:Float,t:Float,theme:ThemeSpec,front:Boolean,scale:Float=1f,readability:AbyssReadability?=null) {
        val n=(theme.petals*scale*effects.bubbles).toInt().coerceIn(0,96)
        for(i in 0 until n) {
            val layer=AbyssDepth.layer(i)
            if((layer==2)!=front)continue
            AbyssLife.bubble(i,t,theme.petalSpeed,point)
            val x=point[0]*w;val y=point[1]*h;val radius=point[2]*d*(if(layer==2).70f else 1f)
            val visibility=readability?.visibility(x-radius,y-radius,x+radius,y+radius,18*d)?:1f
            if(layer==2){
                softMatrix.setScale(radius*2,radius*2);softMatrix.postTranslate(x,y);soft.setLocalMatrix(softMatrix)
                fill(Color.WHITE);p.shader=soft;p.alpha=(65*visibility).toInt();c.drawCircle(x,y,radius*2,p);p.shader=null
                if(refraction!=null&&ocean!=null){
                    val zoom=max(w/ocean.width,h/ocean.height)*1.10f
                    refractionMatrix.setScale(zoom,zoom);refractionMatrix.postTranslate((w-ocean.width*zoom)/2,(h-ocean.height*zoom)/2);refraction.setLocalMatrix(refractionMatrix)
                    fill(Color.WHITE);p.shader=refraction;p.alpha=(18*visibility).toInt();c.drawCircle(x,y,radius,p);p.shader=null
                }
                c.save();c.rotate(point[4],x,y);c.scale(1f/point[5],point[5],x,y)
                bubble(c,x,y,radius,theme.accent,(point[3]*.75f*visibility).toInt());c.restore()
            }else if(layer==1)bubble(c,x,y,radius,theme.accent,point[3].toInt())
            else {fill(theme.accent);p.alpha=point[3].toInt();c.drawCircle(x,y,radius,p)}
        }
        if(effects.jellyfish>0f)for(i in 0..4){
            if((i>=3)!=front)continue
            AbyssLife.jelly(i,t,life)
            val x=w*life[0];val y=h*life[1];val r=min(w*life[2],18*d)*(if(front)1f else .72f)
            val visibility=readability?.visibility(x-r*1.5f,y-r,x+r*1.5f,y+r*3.9f,24*d)?:1f
            jelly(c,x,y,r,life[5],life[3]*(if(front).78f else .46f)*visibility,life[4],i,!front)
        }
    }
    private fun jelly(c:Canvas,x:Float,y:Float,r:Float,t:Float,opacity:Float,angle:Float,variant:Int,distant:Boolean) {
        val contraction=AbyssLife.contraction(t)
        val pulse=1.08f-contraction*.2f
        jellyImage?.let {bitmap->
            c.save();c.rotate(angle,x,y)
            // Slow halo suggests scattering; the sprite retains visible translucent tissue.
            softMatrix.setScale(r*1.7f,r*1.2f);softMatrix.postTranslate(x,y);soft.setLocalMatrix(softMatrix)
            fill(Color.WHITE);p.shader=soft;p.alpha=(opacity*65*effects.jellyfish).toInt().coerceIn(0,120);c.drawOval(x-r*1.7f,y-r*1.2f,x+r*1.7f,y+r*1.2f,p);p.shader=null
            JellyMesh.fill(x-r*1.35f,y-r*.8f,r*2.7f,r*4.7f,t,mesh,variant)
            fill(Color.WHITE);p.colorFilter=if(distant)distantJellyTint else jellyTint;p.alpha=(opacity*200*effects.jellyfish).toInt().coerceIn(0,225);c.drawBitmapMesh(bitmap,JellyMesh.COLUMNS,JellyMesh.ROWS,mesh,0,null,0,p);p.colorFilter=null
            c.restore();return
        }
        fill(0xffa1edfa.toInt());p.alpha=(25*opacity*effects.jellyfish).toInt().coerceIn(0,255)
        dst.set(x-r*pulse,y-r*(.58f+contraction*.2f),x+r*pulse,y+r*.35f);c.drawArc(dst,180f,180f,true,p)
        stroke(0xffa1edfa.toInt(),(40*opacity*effects.jellyfish).toInt(),.6f*d);c.drawArc(dst,180f,180f,false,p)
        for(i in 0..7){val xx=x+(-.77f+i*.22f)*r*pulse;val lag=t*1.25f-i*.15f-.65f
            ray.reset();ray.moveTo(xx,y);ray.cubicTo(xx+sin(lag)*r*.17f,y+r*.6f,xx+sin(lag-.7f)*r*.35f,y+r*1.3f,xx+sin(lag-1.4f)*r*.4f,y+r*(1.7f+i%3*.16f));c.drawPath(ray,p)}
    }
    fun hero(c:Canvas,w:Float,h:Float,t:Float,theme:ThemeSpec,art:Bitmap?,bass:Float,embedded:Boolean,wide:Boolean=false,hideAlbum:Boolean=false,portalOverride:AbyssGeometry.Circle?=null,ripples:AbyssRipples?=null) {
        if(!embedded){background(c,w,h,t);particles(c,w,h,t,theme,false)}
        val portal=portalOverride?:AbyssGeometry.portal(w,h,wide);val album=AbyssGeometry.album(portal)
        if(!hideAlbum)albumDisc(c,album,art,theme,t)
        val change=t-coverAt
        if(!hideAlbum&&change in 0f..1.1f)for(i in 0..13){val angle=i*2*PI/14;val envelope=sin(change/1.1f*PI).toFloat()
            bubble(c,album.x+cos(angle).toFloat()*album.radius*(1+change*.15f),album.y+sin(angle).toFloat()*album.radius-change*(22+i%5*7)*d,(1.5f+i%4)*d,theme.accent,(envelope*140).toInt())}
        val x=portal.x;val y=portal.y;val r=portal.radius
        // Opaque scenic window is drawn AFTER the satellite: its edge occludes the album.
        c.save();clip.reset();clip.addCircle(x,y,r,Path.Direction.CW);c.clipPath(clip)
        fill(0xff041b2a.toInt());c.drawCircle(x,y,r,p)
        portalImage?.let {bitmap ->
            val side=min(bitmap.width,bitmap.height);val top=(bitmap.height-side)/2
            src.set((bitmap.width-side)/2,top,(bitmap.width+side)/2,top+side)
            dst.set(x-r,y-r,x+r,y+r);portalWater?.draw(c,src,dst,t,250,effects.light)
        }
        particles(c,w,h,t,theme,false,.4f)
        fogMatrix.setScale(r,r);fogMatrix.postTranslate(x,y-r*.08f);depthFog.setLocalMatrix(fogMatrix)
        fill(Color.WHITE);p.shader=depthFog;p.alpha=210;c.drawCircle(x,y,r,p);p.shader=null
        c.restore()
        val energy=((ripples?.glow?:0f)*effects.waves).coerceAtMost(1.5f)
        lensMatrix.setScale(r*1.035f,r*1.035f);lensMatrix.postTranslate(x,y);lens.setLocalMatrix(lensMatrix)
        fill(Color.WHITE);p.shader=lens;p.alpha=210;c.drawCircle(x,y,r*1.035f,p);p.shader=null
        for(i in 4 downTo 1){stroke(theme.accent,((7+energy*10)/i).toInt(),i*4*d);c.drawCircle(x,y,r+2*d,p)}
        stroke(theme.accent,80,5f*d);c.drawCircle(x,y,r,p)
        stroke(theme.accent,220,1.7f*d);c.drawCircle(x,y,r,p)
        stroke(Color.WHITE,175,.65f*d);c.drawCircle(x,y,r-3*d,p)
        dst.set(x-r,y-r,x+r,y+r);stroke(Color.WHITE,100,1.2f*d);c.drawArc(dst,205f,50f,false,p)
        dst.inset(2*d,2*d);stroke(theme.accent,75,.7f*d);c.drawArc(dst,28f,68f,false,p)
        for(i in 0..1){val q=(t*.07f+i/2f)%1f;stroke(theme.accent,((1-q)*9).toInt(),.6f*d);c.drawCircle(x,y,r*(1.02f+q*.15f),p)}
        ripples?.let {waves->for(i in waves.ages.indices){val q=waves.phase(i);if(q<1f){stroke(theme.accent,((1-q)*(1-q)*waves.strengths[i]*125*effects.waves).toInt(),(1.3f-q*.6f)*d);c.drawCircle(x,y,r*(1.005f+q*.29f),p)}}}
        if(!embedded)particles(c,w,h,t,theme,true)
    }
    private fun albumDisc(c:Canvas,circle:AbyssGeometry.Circle,art:Bitmap?,theme:ThemeSpec,t:Float) {
        val (x,y,r)=circle
        for(i in 3 downTo 1){stroke(theme.accent,9,i*3*d);c.drawCircle(x,y,r,p)}
        c.save();clip.reset();clip.addCircle(x,y,r,Path.Direction.CW);c.clipPath(clip)
        fill(theme.surface);c.drawCircle(x,y,r,p)
        val mix=if(art===covers.current)covers.progress(t)else 1f
        val old=covers.previous
        fun image(bitmap:Bitmap?,alpha:Float){if(bitmap!=null&&!bitmap.isRecycled){val side=min(bitmap.width,bitmap.height);src.set((bitmap.width-side)/2,(bitmap.height-side)/2,(bitmap.width+side)/2,(bitmap.height+side)/2);dst.set(x-r,y-r,x+r,y+r);fill(Color.WHITE);p.alpha=(255*alpha).toInt();c.drawBitmap(bitmap,src,dst,p)}}
        stroke(theme.accent,70,d);c.drawCircle(x,y,r*.5f,p)
        if(mix<1f)image(old,1-mix)
        image(art,mix)
        albumShineMatrix.setScale(r,r);albumShineMatrix.postTranslate(x,y);albumShine.setLocalMatrix(albumShineMatrix)
        fill(Color.WHITE);p.shader=albumShine;c.drawCircle(x,y,r,p);p.shader=null;c.restore()
        stroke(theme.accent,190,1.2f*d);c.drawCircle(x,y,r,p)
    }
    fun bubble(c:Canvas,x:Float,y:Float,r:Float,color:Int,a:Int) {
        val opacity=(a*effects.bubbles).toInt()
        if(opacity<=0||r<=0f)return
        bubbleMatrix.setScale(r,r);bubbleMatrix.postTranslate(x,y);bubbleLight.setLocalMatrix(bubbleMatrix)
        fill(Color.WHITE);p.shader=bubbleLight;p.alpha=opacity.coerceIn(0,255);c.drawCircle(x,y,r,p);p.shader=null
        stroke(color,(opacity*.18f).toInt(),.45f*d);c.drawCircle(x,y,r,p)
        stroke(Color.WHITE,(opacity*.85f).toInt(),.65f*d);dst.set(x-r*.65f,y-r*.65f,x+r*.65f,y+r*.65f);c.drawArc(dst,205f,70f,false,p)
        fill(Color.WHITE);p.alpha=(opacity*.65f).toInt().coerceIn(0,255);c.drawCircle(x-r*.35f,y-r*.38f,(r*.12f).coerceAtLeast(.35f*d),p)
    }
    private fun fill(color:Int){p.shader=null;p.style=Paint.Style.FILL;p.color=color;p.alpha=255}
    private fun stroke(color:Int,a:Int,width:Float){fill(color);p.alpha=a.coerceIn(0,255);p.style=Paint.Style.STROKE;p.strokeWidth=width}
}
