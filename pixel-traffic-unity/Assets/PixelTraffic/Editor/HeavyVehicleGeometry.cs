using System;
using System.Collections.Generic;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    // Metre-scale, symmetric geometry. +Z is the front; the camera supplies perspective.
    internal static class HeavyVehicleGeometry
    {
        internal static VehicleGeometry.Shape Build(VehicleGeometry.Kind kind)
        {
            bool bus=kind==VehicleGeometry.Kind.CityBus;
            if(!bus&&kind!=VehicleGeometry.Kind.BoxTruck)throw new ArgumentOutOfRangeException(nameof(kind));
            var paint=new VehicleGeometry.Builder();var glass=new VehicleGeometry.Builder();
            var trim=new VehicleGeometry.Builder();var metal=new VehicleGeometry.Builder();
            var head=new VehicleGeometry.Builder();var tail=new VehicleGeometry.Builder();
            float front=bus?5.30f:3.70f,rear=-front,half=bus?1.275f:1.25f;
            float radius=.46f,frontAxle=bus?3.64f:2.35f,rearAxle=bus?-3.10f:-2.22f;
            float frontY=bus?.88f:.83f,rearY=bus?.82f:.75f,lampX=bus?1.04f:1.02f;
            if(bus)
            {
                BusBody(paint,half,.38f,3.13f,rear,front,.13f);
                glass.Box(new Vector3(0,2.12f,front+.009f),new Vector3(2.18f,1.27f,.016f));
                glass.Box(new Vector3(0,2.30f,rear-.009f),new Vector3(2.10f,.90f,.016f));
                trim.Box(new Vector3(0,2.97f,front+.018f),new Vector3(1.8f,.19f,.027f));
                head.Box(new Vector3(0,2.985f,front+.034f),new Vector3(.85f,.055f,.012f));
                foreach(float side in new[]{-1f,1f})
                {
                    for(int n=0;n<7;n++)
                        if(side<0||n<6)glass.Box(new Vector3(side*(half+.009f),2.22f,-4.30f+n*1.27f),new Vector3(.016f,1.06f,1.08f));
                        else glass.Box(new Vector3(side*(half+.009f),2.22f,2.96f),new Vector3(.016f,1.06f,.70f));
                    trim.Box(new Vector3(side*(half+.018f),1.53f,side>0?-.765f:-.12f),new Vector3(.025f,.10f,side>0?8.49f:9.78f));
                    metal.Box(new Vector3(side*(half+.019f),1.04f,side>0?-.765f:-.12f),new Vector3(.026f,.055f,side>0?8.49f:9.78f));
                    trim.Box(new Vector3(side*1.345f,2.37f,4.67f),new Vector3(.15f,.38f,.25f));
                    metal.Beam(new Vector3(side*1.26f,2.55f,4.64f),new Vector3(side*1.34f,2.55f,4.64f),.045f,.045f);
                }
                // Tall paired panes identify the bus's curbside entry door.
                trim.Box(new Vector3(half, .59f,4.075f),new Vector3(.10f,.08f,1.18f));
                trim.Box(new Vector3(1.01f,.59f,4.075f),new Vector3(.50f,.10f,1.18f));
                trim.Box(new Vector3(0,.55f,front+.024f),new Vector3(2.32f,.13f,.030f));
                trim.Box(new Vector3(0,.55f,rear-.024f),new Vector3(2.32f,.13f,.030f));
                for(int n=0;n<5;n++)trim.Box(new Vector3(0,1.20f+n*.105f,rear-.014f),new Vector3(1.55f,.038f,.021f));
                metal.Box(new Vector3(0,3.255f,-.15f),new Vector3(1.43f,.25f,1.85f));
                foreach(float z in new[]{-.63f,.33f})
                    trim.Box(new Vector3(0,3.389f,z),new Vector3(1.02f,.022f,.55f));
                trim.Box(new Vector3(0,3.145f,3.15f),new Vector3(.96f,.025f,.67f));
            }
            else
            {
                // Independent cab and cargo box with an actual paired rear-door seam.
                Chamfered(paint,1.14f,.39f,2.44f,1.15f,front,.12f);
                paint.Box(new Vector3(0,2.135f,-1.10f),new Vector3(2.50f,2.43f,5.20f));
                trim.Box(new Vector3(0,.61f,-.30f),new Vector3(1.85f,.22f,6.45f));
                glass.Box(new Vector3(0,1.91f,front+.009f),new Vector3(1.94f,.73f,.016f));
                foreach(float side in new[]{-1f,1f})
                {
                    glass.Box(new Vector3(side*1.148f,1.90f,2.54f),new Vector3(.016f,.76f,1.20f));
                    trim.Box(new Vector3(side*1.146f,1.58f,1.65f),new Vector3(.022f,1.47f,.035f));
                    metal.Box(new Vector3(side*1.154f,1.48f,1.83f),new Vector3(.032f,.06f,.24f));
                    trim.Box(new Vector3(side*1.325f,1.93f,3.17f),new Vector3(.15f,.38f,.23f));
                    metal.Beam(new Vector3(side*1.12f,2.10f,3.16f),new Vector3(side*1.32f,2.10f,3.16f),.05f,.05f);
                    metal.Box(new Vector3(side*1.259f,.945f,-1.10f),new Vector3(.025f,.055f,5.20f));
                    metal.Box(new Vector3(side*1.259f,3.33f,-1.10f),new Vector3(.025f,.055f,5.20f));
                    // Door perimeter, central locking bars, hinges and lower catches.
                    metal.Box(new Vector3(side*1.20f,2.12f,rear-.015f),new Vector3(.035f,2.36f,.026f));
                    metal.Box(new Vector3(side*.28f,2.10f,rear-.030f),new Vector3(.031f,2.24f,.035f));
                    metal.Box(new Vector3(side*.35f,1.20f,rear-.055f),new Vector3(.22f,.055f,.043f));
                    for(int n=0;n<3;n++)metal.Box(new Vector3(side*1.12f,1.36f+n*.70f,rear-.030f),new Vector3(.20f,.055f,.035f));
                }
                trim.Box(new Vector3(0,2.12f,rear-.012f),new Vector3(.025f,2.38f,.020f));
                foreach(float y in new[]{.955f,3.32f})metal.Box(new Vector3(0,y,rear-.015f),new Vector3(2.43f,.035f,.026f));
                trim.Box(new Vector3(0,.64f,front+.018f),new Vector3(2.15f,.19f,.035f));
                trim.Box(new Vector3(0,1.08f,front+.018f),new Vector3(1.20f,.28f,.035f));
                for(int n=0;n<4;n++)metal.Box(new Vector3(0,.995f+n*.055f,front+.040f),new Vector3(1.08f,.018f,.016f));
                trim.Box(new Vector3(0,.49f,rear-.018f),new Vector3(2.28f,.14f,.035f));
            }
            foreach(float side in new[]{-1f,1f})
            {
                head.Box(new Vector3(side*lampX,frontY,front+.030f),new Vector3(.27f,.15f,.040f));
                tail.Box(new Vector3(side*lampX,rearY,rear-.030f),new Vector3(.22f,.18f,.040f));
            }
            metal.Box(new Vector3(0,bus?.58f:.54f,front+.055f),new Vector3(.43f,.11f,.020f));
            metal.Box(new Vector3(0,bus?.58f:.54f,rear-.055f),new Vector3(.43f,.11f,.020f));
            var tyre=new VehicleGeometry.Builder();var rim=new VehicleGeometry.Builder();
            tyre.RoundedTyre(radius,.24f,12);rim.Cylinder(radius*.73f,.25f,12);rim.Cylinder(radius*.25f,.275f,10);
            foreach(float side in new[]{-1f,1f})for(int n=0;n<6;n++)
            {
                float a=n*Mathf.PI/3;
                rim.Box(new Vector3(side*.144f,Mathf.Cos(a)*radius*.50f,Mathf.Sin(a)*radius*.50f),new Vector3(.008f,.027f,.027f));
            }
            // Wheel fasteners belong to the rim mesh, never the body-space trim mesh.
            // Cheap steel rims preserve the existing shared wheel-pivot/renderer contract.
            string prefix=kind.ToString();
            return new VehicleGeometry.Shape{paint=paint.Save(prefix+"Paint",55),glass=glass.Save(prefix+"Glass"),
                trim=trim.Save(prefix+"Trim"),metal=metal.Save(prefix+"Metal"),head=head.Save(prefix+"Head"),tail=tail.Save(prefix+"Tail"),
                tyre=tyre.Save(prefix+"Tyre",65),rim=rim.Save(prefix+"Rim",55),radius=radius,axle=frontAxle,
                wheelX=bus?1.23f:1.20f,frontAxle=frontAxle,rearAxle=rearAxle,lampX=lampX,frontLampY=frontY,rearLampY=rearY};
        }

        private static void BusBody(VehicleGeometry.Builder b,float half,float bottom,float top,float rear,float front,float bevel)
        {
            Vector3[] ring={new Vector3(-half+bevel,bottom,0),new Vector3(half-bevel,bottom,0),new Vector3(half,bottom+bevel,0),new Vector3(half,top-bevel,0),new Vector3(half-bevel,top,0),new Vector3(-half+bevel,top,0),new Vector3(-half,top-bevel,0),new Vector3(-half,bottom+bevel,0)};
            for(int k=0;k<8;k++)if(k!=2)
            {int n=(k+1)%8;var a=ring[k]+Vector3.forward*rear;var c=ring[n]+Vector3.forward*front;b.Quad(a,ring[n]+Vector3.forward*rear,c,ring[k]+Vector3.forward*front,(ring[k]+ring[n])/2-new Vector3(0,(bottom+top)/2,0));}
            foreach(float z in new[]{rear,front})for(int k=0;k<8;k++)b.Triangle(new Vector3(0,(bottom+top)/2,z),ring[k]+Vector3.forward*z,ring[(k+1)%8]+Vector3.forward*z,z<0?Vector3.back:Vector3.forward);
            void Skin(float y0,float y1,float z0,float z1)=>b.Quad(new Vector3(half,y0,z0),new Vector3(half,y0,z1),new Vector3(half,y1,z1),new Vector3(half,y1,z0),Vector3.right);
            Skin(bottom+bevel,top-bevel,rear,3.48f);Skin(bottom+bevel,top-bevel,4.67f,front);Skin(bottom+bevel,.59f,3.48f,4.67f);Skin(2.69f,top-bevel,3.48f,4.67f);
        }

        private static void Chamfered(VehicleGeometry.Builder b,float half,float bottom,float top,float rear,float front,float bevel)
        {
            var rings=new List<Vector3[]>();
            foreach(float z in new[]{rear,front})rings.Add(new[]{
                new Vector3(-half+bevel,bottom,z),new Vector3(half-bevel,bottom,z),
                new Vector3(half,bottom+bevel,z),new Vector3(half,top-bevel,z),
                new Vector3(half-bevel,top,z),new Vector3(-half+bevel,top,z),
                new Vector3(-half,top-bevel,z),new Vector3(-half,bottom+bevel,z)});
            b.Loft(rings);
        }
    }
}
