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
            struct Varyings {float4 positionCS:SV_POSITION;float3 normalWS:TEXCOORD0;float2 uv:TEXCOORD1;float4 color:TEXCOORD2;};
            Varyings Vert(Attributes v)
            {
                Varyings o;o.positionCS=TransformObjectToHClip(v.positionOS.xyz);o.normalWS=TransformObjectToWorldNormal(v.normalOS);o.uv=v.uv;o.color=v.color;return o;
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
                    float mass=smoothstep(.53-cloud*.16,.67-cloud*.16,n);
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
                    float3 green=lerp(float3(.16,.36,.23),float3(.39,.58,.72),far);
                    float shade=.84+Noise(uv*float2(28,9)+i.color.z)*.18;
                    color=(green*day+lerp(float3(.34,.34,.30),float3(.49,.43,.53),far)*dusk+float3(.035,.07,.12)*night)*shade;
                    color=lerp(color,float3(.84,.87,.9)*(day+dusk*.65+night*.19),snow*smoothstep(.65,.98,uv.y)*.62);
                    color=lerp(color,_HazeColor.rgb,saturate(.09+far*.13+fog*.84+rain*.26));
                }
                else if(kind<2.5)
                {
                    float seed=i.color.z;
                    float light=.58+.42*saturate(dot(normalize(i.normalWS),normalize(float3(-.5,.7,-.5))));
                    color=lerp(float3(.39,.53,.63),float3(.65,.73,.76),seed)*light*(day+dusk*.68+night*.17);
                    float2 grid=uv*float2(5,max(3,i.color.y));float2 cell=frac(grid);
                    float window=step(.18,cell.x)*step(cell.x,.73)*step(.22,cell.y)*step(cell.y,.74);
                    color=lerp(color,float3(.22,.40,.51)*(day+dusk*.5+night*.1),window*.72);
                    float lit=step(.34,Hash(floor(grid)+seed*43));
                    float3 lamp=lerp(float3(1,.69,.27),float3(.46,.79,1),step(.66,Hash(floor(grid)+seed*71)));
                    color+=window*lit*lamp*(night*.85+dusk*.28)*(1-fog*.8);
                    color=lerp(color,_HazeColor.rgb,saturate(.12+fog*.82+rain*.23+snow*.15));
                }
                else if(kind<3.5)
                {
                    color=float3(.14,.48,.72)*day+float3(.39,.36,.46)*dusk+float3(.018,.075,.15)*night;
                    float ripples=sin(uv.y*250+uv.x*11+_Motion.x*.65)+sin(uv.y*590-uv.x*21-_Motion.x*.43)*.38;
                    float glints=smoothstep(.88,1.33,ripples)*(Noise(uv*float2(90,210))*.6+.4);
                    float ribbon=exp(-abs(uv.x-.65)*(5+uv.y*8));
                    color+=glints*(.14+ribbon*.55)*(float3(.64,.82,.95)*day+float3(1,.56,.25)*dusk);
                    float cityReflection=pow(saturate(sin(uv.x*150)*.5+.5),16)*(.4+.6*uv.y)*smoothstep(.1,1.1,ripples);
                    color+=cityReflection*float3(.85,.61,.29)*night*.6;
                    color=lerp(color,_HazeColor.rgb,saturate(.08+fog*.86+rain*.22+snow*.22));
                }
                else
                {
                    color=float3(.58,.63,.64)*(day+dusk*.67+night*.2);
                    color+=float3(1,.69,.31)*step(.96,frac(uv.x*30))*(night*.6+dusk*.2);
                    color=lerp(color,_HazeColor.rgb,saturate(.12+fog*.84+rain*.23));
                }
                return half4(color,1);
            }
            ENDHLSL
        }
    }
}
