Shader "PixelTraffic/RoadOverlay"
{
    Properties { _Ground("Wetness Lamp Snow Active seconds",Vector)=(0,0,0,0) _Rain("Rain ripple strength",Range(0,1))=0 }
    SubShader
    {
        Tags {"RenderPipeline"="UniversalPipeline" "RenderType"="Transparent" "Queue"="Transparent"}
        Pass
        {
            Tags {"LightMode"="SRPDefaultUnlit"}
            Blend SrcAlpha OneMinusSrcAlpha
            ZWrite Off
            Cull Off
            HLSLPROGRAM
            #pragma vertex Vert
            #pragma fragment Frag
            #pragma multi_compile_fog
            #include "Packages/com.unity.render-pipelines.universal/ShaderLibrary/Core.hlsl"
            CBUFFER_START(UnityPerMaterial)
                float4 _Ground;
                float _Rain;
            CBUFFER_END
            struct Attributes{float4 positionOS:POSITION;float2 uv:TEXCOORD0;float4 color:COLOR;};
            struct Varyings{float4 positionCS:SV_POSITION;float2 uv:TEXCOORD0;float3 world:TEXCOORD1;float4 color:COLOR;half fog:TEXCOORD2;};
            Varyings Vert(Attributes v)
            {
                Varyings o;o.positionCS=TransformObjectToHClip(v.positionOS.xyz);o.world=TransformObjectToWorld(v.positionOS.xyz);
                o.uv=v.uv;o.color=v.color;o.fog=ComputeFogFactor(o.positionCS.z);return o;
            }
            float Hash(float2 p)
            {
                p=frac(p*float2(.1031,.11369));p+=dot(p,p.yx+33.33);
                return frac((p.x+p.y)*p.x);
            }
            float Noise(float2 p)
            {
                float2 cell=floor(p),f=frac(p);f=f*f*(3-2*f);
                return lerp(lerp(Hash(cell),Hash(cell+float2(1,0)),f.x),lerp(Hash(cell+float2(0,1)),Hash(cell+1),f.x),f.y);
            }
            half4 Frag(Varyings i):SV_Target
            {
                float2 d=(i.uv-.5)*2;float alpha;float3 colour;
                float wet=saturate(_Ground.x);float wetGain=wet*(.28+.72*wet);
                if(i.color.a<.5)
                {
                    // Compact underbody and four darker tyre contacts, not a
                    // replacement for the scene's directional cast shadows.
                    alpha=pow(saturate(1-dot(d,d)),1.7)*i.color.r*(1-_Ground.z*.4);colour=0;
                }
                else if(i.color.a<1.5)
                {
                    // World-anchored roughness breaks up light without regular horizontal bands.
                    float2 grainUV=i.world.xz*float2(2.6,4.6);
                    float detail=1-smoothstep(.6,1.6,max(length(ddx(grainUV)),length(ddy(grainUV))));
                    float grain=lerp(.5,Noise(grainUV),detail);
                    float patches=Noise(i.world.xz*float2(.62,.9));
                    float ripple=Noise(i.world.xz*float2(1.1,2.2)+float2(_Ground.w*.09,-_Ground.w*.12));
                    float edge=pow(saturate(1-d.x*d.x),2)*pow(saturate(1-d.y*d.y),1.3);
                    float shimmer=lerp(1,.88+.12*ripple,saturate(_Rain));
                    alpha=edge*(.095+.12*patches)*lerp(.72,1,grain)*shimmer*wetGain*_Ground.y*(1-_Ground.z);colour=i.color.rgb;
                }
                else if(i.color.a<2.5)
                {
                    float edge=smoothstep(.08,.30,i.uv.x)*(1-smoothstep(.70,.92,i.uv.x));
                    float wear=.5+.5*sin(i.world.z*.21+sin(i.world.z*.61)*.4);
                    alpha=edge*(.025+wear*.045)*(1-_Ground.z);colour=float3(.045,.047,.05);
                }
                else
                {
                    // Forward tapered mesh with soft cutoff and the same road roughness.
                    float edge=1-smoothstep(.62,1,abs(d.x));
                    float falloff=(1-smoothstep(.48,1,i.uv.y))*smoothstep(0,.035,i.uv.y);
                    float2 grainUV=i.world.xz*float2(2.6,4.6);
                    float detail=1-smoothstep(.6,1.6,max(length(ddx(grainUV)),length(ddy(grainUV))));
                    float grain=lerp(.5,Noise(grainUV),detail);
                    float patches=Noise(i.world.xz*float2(.62,.9));
                    float ripple=Noise(i.world.xz*float2(1.1,2.2)+float2(_Ground.w*.09,-_Ground.w*.12));
                    float shimmer=lerp(1,.88+.12*ripple,saturate(_Rain));
                    alpha=edge*falloff*(.014+.014*patches)*lerp(.72,1,grain)*shimmer*wetGain*_Ground.y*(1-_Ground.z);colour=i.color.rgb;
                }
                return half4(MixFog(colour,i.fog),alpha);
            }
            ENDHLSL
        }
    }
}
