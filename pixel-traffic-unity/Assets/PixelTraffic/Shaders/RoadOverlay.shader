Shader "PixelTraffic/RoadOverlay"
{
    Properties { _Ground("Wetness Lamp Snow Active seconds",Vector)=(0,0,0,0) }
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
            CBUFFER_END
            struct Attributes{float4 positionOS:POSITION;float2 uv:TEXCOORD0;float4 color:COLOR;};
            struct Varyings{float4 positionCS:SV_POSITION;float2 uv:TEXCOORD0;float3 world:TEXCOORD1;float4 color:COLOR;half fog:TEXCOORD2;};
            Varyings Vert(Attributes v)
            {
                Varyings o;o.positionCS=TransformObjectToHClip(v.positionOS.xyz);o.world=TransformObjectToWorld(v.positionOS.xyz);
                o.uv=v.uv;o.color=v.color;o.fog=ComputeFogFactor(o.positionCS.z);return o;
            }
            half4 Frag(Varyings i):SV_Target
            {
                float2 d=(i.uv-.5)*2;float alpha;float3 colour;
                if(i.color.a<.5)
                {
                    // Compact underbody and four darker tyre contacts, not a
                    // replacement for the scene's directional cast shadows.
                    alpha=pow(saturate(1-dot(d,d)),1.7)*i.color.r*(1-_Ground.z*.4);colour=0;
                }
                else if(i.color.a<1.5)
                {
                    float ripple=sin(i.world.z*11+_Ground.w*.55)+sin(i.world.z*29+i.world.x*8-_Ground.w*.34)*.4;
                    float edge=pow(saturate(1-d.x*d.x),2)*pow(saturate(1-d.y*d.y),1.3);
                    float broken=smoothstep(-.5,1.2,ripple);
                    float detail=1-smoothstep(.8,2.5,max(fwidth(i.world.z*29),fwidth(i.world.x*8)));
                    broken=lerp(.4,broken,detail);
                    alpha=edge*(.16+broken*.26)*_Ground.x*_Ground.y*(1-_Ground.z);colour=i.color.rgb;
                }
                else if(i.color.a<2.5)
                {
                    float edge=smoothstep(.08,.30,i.uv.x)*(1-smoothstep(.70,.92,i.uv.x));
                    float wear=.5+.5*sin(i.world.z*.21+sin(i.world.z*.61)*.4);
                    alpha=edge*(.025+wear*.045)*(1-_Ground.z);colour=float3(.045,.047,.05);
                }
                else
                {
                    // Forward tapered mesh, with a soft far cutoff and small wet streaks.
                    float edge=1-smoothstep(.62,1,abs(d.x));
                    float falloff=(1-smoothstep(.48,1,i.uv.y))*smoothstep(0,.035,i.uv.y);
                    float detail=1-smoothstep(.8,2.5,fwidth(i.world.z*24));
                    float ripple=lerp(.5,.5+.5*sin(i.world.z*24+_Ground.w*.6),detail);
                    alpha=edge*falloff*(.022+.008*ripple)*_Ground.x*_Ground.y*(1-_Ground.z);colour=i.color.rgb;
                }
                return half4(MixFog(colour,i.fog),alpha);
            }
            ENDHLSL
        }
    }
}
