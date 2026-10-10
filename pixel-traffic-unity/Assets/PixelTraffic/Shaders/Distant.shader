Shader "PixelTraffic/Distant"
{
    Properties
    {
        _Theme("Day Dusk Night",Vector)=(1,0,0,0)
        _Weather("Cloud Fog Snow Rain",Vector)=(0,0,0,0)
        _Motion("Active seconds and cloud offset",Vector)=(0,0,0,0)
        _HazeColor("Distant haze",Color)=(.72,.78,.81,1)
    }
    SubShader
    {
        Tags {"RenderPipeline"="UniversalPipeline" "RenderType"="Opaque" "Queue"="Geometry"}
        Pass
        {
            Tags {"LightMode"="SRPDefaultUnlit"}
            Cull Off
            ZWrite On
            HLSLPROGRAM
            #pragma vertex Vert
            #pragma fragment Frag
            #include "Packages/com.unity.render-pipelines.universal/ShaderLibrary/Core.hlsl"
            CBUFFER_START(UnityPerMaterial)
                float4 _Theme, _Weather, _Motion;
                half4 _HazeColor;
            CBUFFER_END
            struct Attributes {float4 positionOS:POSITION;float3 normalOS:NORMAL;float2 uv:TEXCOORD0;float4 color:COLOR;};
            struct Varyings {float4 positionCS:SV_POSITION;float3 normalWS:TEXCOORD0;float2 uv:TEXCOORD1;float4 color:TEXCOORD2;float3 positionWS:TEXCOORD3;};
            Varyings Vert(Attributes v)
            {
                Varyings o;o.positionCS=TransformObjectToHClip(v.positionOS.xyz);o.normalWS=TransformObjectToWorldNormal(v.normalOS);o.uv=v.uv;o.color=v.color;o.positionWS=TransformObjectToWorld(v.positionOS.xyz);return o;
            }
            float Hash(float2 p){return frac(sin(dot(p,float2(127.1,311.7)))*43758.5453);}
            float Noise(float2 p)
            {
                float2 a=floor(p),f=frac(p);f=f*f*(3-2*f);
                return lerp(lerp(Hash(a),Hash(a+float2(1,0)),f.x),lerp(Hash(a+float2(0,1)),Hash(a+1),f.x),f.y);
            }
            half4 Frag(Varyings i):SV_Target
            {
                float day=_Theme.x,dusk=_Theme.y,night=_Theme.z;
                float cloud=_Weather.x,fog=_Weather.y,snow=_Weather.z,rain=_Weather.w;
                float2 uv=i.uv;float kind=i.color.x;float3 color;
                if(kind<.5)
                {
                    float elevation=saturate(uv.y*1.65);
                    float3 low=float3(.64,.82,.95)*day+float3(1,.55,.30)*dusk+float3(.09,.13,.24)*night;
                    float3 high=float3(.06,.35,.81)*day+float3(.29,.24,.52)*dusk+float3(.012,.022,.065)*night;
                    color=lerp(low,high,pow(elevation,.65));
                    float2 p=uv*float2(10,15)-float2(_Motion.y,0);
                    float n=Noise(p)*.58+Noise(p*2.1)*.28+Noise(p*4.2)*.14;
                    float mass=smoothstep(.54-cloud*.16,.63-cloud*.16,n);
                    float3 clouds=float3(.97,.98,1)*day+float3(.91,.66,.56)*dusk+float3(.12,.16,.27)*night;
                    clouds*=1-cloud*.28;color=lerp(color,clouds,mass*.94);
                    float2 starUV=uv*float2(140,180);
                    float stars=step(.997,Hash(floor(starUV)))*(1-smoothstep(.03,.17,length(frac(starUV)-.5)));
                    color+=stars*night*(1-mass)*(1-cloud)*float3(.7,.8,1);
                    color=lerp(color,_HazeColor.rgb,saturate(fog*.83+rain*.24+snow*.23));
                }
                else if(kind<1.5)
                {
                    float far=i.color.y;
                    float3 green=lerp(float3(.10,.29,.21),float3(.20,.43,.67),far);
                    // Shared world coordinates keep terrain noise continuous between ridge strips.
                    float2 terrain=i.positionWS.xy*float2(.065,.11);
                    float shade=.79+Noise(terrain+i.color.z)*.15+Noise(terrain*2.3)*.06;
                    color=(green*day+lerp(float3(.34,.34,.30),float3(.49,.43,.53),far)*dusk+float3(.035,.07,.12)*night)*shade;
                    color=lerp(color,float3(.84,.87,.9)*(day+dusk*.65+night*.19),snow*smoothstep(48,80,i.positionWS.y)*.62);
                    color=lerp(color,_HazeColor.rgb,saturate(.06+far*.11+fog*.84+rain*.26));
                }
                else if(kind<2.5)
                {
                    float seed=i.color.z;
                    float light=.52+.48*saturate(dot(normalize(i.normalWS),normalize(float3(-.5,.7,-.5))));
                    color=lerp(float3(.32,.48,.61),float3(.72,.78,.80),Hash(float2(seed*83,3)))*light*(day+dusk*.68+night*.17);
                    float2 grid=uv*float2(5,max(3,i.color.y));float2 cell=frac(grid);
                    // Fade subpixel windows to their area average, including lighting.
                    // Hard step/hash cells alias on narrow distant building side faces.
                    float2 footprint=max(fwidth(grid),float2(.005,.005));
                    float detail=1-smoothstep(.22,.85,max(footprint.x,footprint.y));
                    float2 edge=min(footprint*.5,float2(.2,.2));
                    float window=smoothstep(.18-edge.x,.18+edge.x,cell.x)*(1-smoothstep(.73-edge.x,.73+edge.x,cell.x))
                        *smoothstep(.22-edge.y,.22+edge.y,cell.y)*(1-smoothstep(.74-edge.y,.74+edge.y,cell.y));
                    window=lerp(.286,window,detail);
                    color=lerp(color,float3(.22,.40,.51)*(day+dusk*.5+night*.1),window*.72);
                    float lit=lerp(.66,step(.34,Hash(floor(grid)+seed*43)),detail);
                    float3 lamp=lerp(float3(1,.69,.27),float3(.46,.79,1),step(.66,Hash(floor(grid)+seed*71)));
                    lamp=lerp(float3(.82,.72,.51),lamp,detail);
                    color+=window*lit*lamp*(night*.85+dusk*.28)*(1-fog*.8);
                    color=lerp(color,_HazeColor.rgb,saturate(.12+fog*.82+rain*.23+snow*.15));
                }
                else if(kind<3.5)
                {
                    color=float3(.025,.29,.60)*day+float3(.39,.36,.46)*dusk+float3(.018,.075,.15)*night;
                    float distortion=Noise(uv*float2(130,37))*4;
                    float ripples=sin(uv.y*250+uv.x*39+distortion+_Motion.x*.65)+sin(uv.y*590-uv.x*67-distortion-_Motion.x*.43)*.38;
                    float glints=smoothstep(.88,1.33,ripples)*smoothstep(.32,.72,Noise(uv*float2(170,210)));
                    float ribbon=exp(-abs(uv.x-.65)*(5+uv.y*8));
                    color+=glints*(.14+ribbon*.55)*(float3(.64,.82,.95)*day+float3(1,.56,.25)*dusk)*(1-cloud*.72);
                    // World-space reflections sit below the authored ten-metre tower
                    // spacing and bridge lamps. One local cell, no lights/reflection camera.
                    float reflectedX=i.positionWS.x+sin(uv.y*81+_Motion.x*.38)*.5;
                    float towerCell=floor((reflectedX+170)/10);
                    float towerX=-165+towerCell*10;
                    float seed=towerCell/34;
                    float height=towerCell==20?66:12+fmod(towerCell*19,31);
                    float cityRibbon=exp(-abs(reflectedX-towerX)*1.2)*step(0,towerCell)*step(towerCell,33);
                    float lengthFade=smoothstep(.08,.38+height*.002,uv.y);
                    float3 lamp=lerp(float3(1,.69,.27),float3(.46,.79,1),step(.66,Hash(float2(seed*71,seed*71))));
                    color+=cityRibbon*lengthFade*smoothstep(-.35,1.2,ripples)*lamp*(night*.52+dusk*.14)*(1-cloud*.65);
                    float bridgeX=-115+round((reflectedX+115)/23)*23;
                    float bridgeRibbon=exp(-abs(reflectedX-bridgeX)*1.6)*step(abs(bridgeX),116);
                    color+=bridgeRibbon*smoothstep(-.45,1.1,ripples)*(1-smoothstep(.40,.58,uv.y))*float3(1,.64,.26)*(night*.33+dusk*.08);
                    color=lerp(color,_HazeColor.rgb,saturate(.08+fog*.86+rain*.22+snow*.22));
                }
                else if(kind<4.5)
                {
                    color=float3(.58,.63,.64)*(day+dusk*.67+night*.2);
                    color=lerp(color,_HazeColor.rgb,saturate(.12+fog*.84+rain*.23));
                }
                else if(kind<5.5)
                {
                    color=lerp(float3(.28,.37,.34),float3(.10,.31,.17),i.color.y)*(day+dusk*.65+night*.16);
                    color=lerp(color,float3(.84,.87,.9)*(day+dusk*.65+night*.19),snow*.65);
                    color=lerp(color,_HazeColor.rgb,saturate(.12+fog*.86+rain*.26));
                }
                else
                {
                    color=float3(.35,.37,.36)*day+float3(1,.64,.26)*(dusk*.6+night*.95);
                    color=lerp(color,_HazeColor.rgb,saturate(.09+fog*.83+rain*.25));
                }
                return half4(color,1);
            }
            ENDHLSL
        }
    }
}
