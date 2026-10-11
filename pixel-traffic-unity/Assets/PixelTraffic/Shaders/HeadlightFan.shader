Shader "PixelTraffic/HeadlightFan"
{
    Properties
    {
        _BaseColor("Color",Color)=(1,1,1,0)
        _EmissionColor("Emission",Color)=(0,0,0,1)
    }
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
                half4 _BaseColor;
                half4 _EmissionColor;
            CBUFFER_END
            struct Attributes {float4 positionOS:POSITION;float2 uv:TEXCOORD0;};
            struct Varyings {float4 positionCS:SV_POSITION;float2 uv:TEXCOORD0;half fog:TEXCOORD1;};
            Varyings Vert(Attributes v)
            {
                Varyings o;o.positionCS=TransformObjectToHClip(v.positionOS.xyz);o.uv=v.uv;
                o.fog=ComputeFogFactor(o.positionCS.z);return o;
            }
            half4 Frag(Varyings i):SV_Target
            {
                // UVs run from a narrow lamp origin to a broad road footprint.
                float across=abs(i.uv.x*2-1);
                float side=1-smoothstep(.60,1,across);
                float core=.6+.4*(1-across*across);
                float distanceFade=1-smoothstep(.45,1,i.uv.y);
                float originFade=smoothstep(0,.018,i.uv.y);
                float alpha=_BaseColor.a*side*core*distanceFade*originFade;
                return half4(MixFog(_BaseColor.rgb+_EmissionColor.rgb,i.fog),alpha);
            }
            ENDHLSL
        }
    }
}
