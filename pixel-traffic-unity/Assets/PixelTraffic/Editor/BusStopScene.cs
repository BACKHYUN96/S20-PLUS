using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class BusStopScene
    {
        internal static void Create()
        {
            var root=new GameObject("Bus Stops").transform;
            var metal=new VehicleGeometry.Builder();var roof=new VehicleGeometry.Builder();var glass=new VehicleGeometry.Builder();var sign=new VehicleGeometry.Builder();
            for(int stop=0;stop<2;stop++)
            {
                float side=BusStops.Side(stop),z=BusStops.DoorZ(stop);
                void Box(VehicleGeometry.Builder b,float x,float y,float offset,float w,float h,float length)=>b.Box(new Vector3(side*x,y,z+offset),new Vector3(w,h,length));
                foreach(float x in new[]{8.95f,10.3f})foreach(float dz in new[]{-1.72f,1.72f})Box(metal,x,1.40f,dz,.07f,2.48f,.07f);
                Box(roof,9.625f,2.72f,0,1.63f,.12f,3.78f);
                Box(glass,10.29f,1.63f,0,.025f,2.36f,3.38f);
                foreach(float dz in new[]{-1.65f,1.65f})Box(glass,9.95f,1.63f,dz,.66f,2.36f,.025f);
                Box(metal,9.70f,.67f,0,.78f,.12f,2.5f);Box(metal,10.01f,1.02f,0,.08f,.62f,2.5f);
                foreach(float dz in new[]{-.9f,.9f})Box(metal,9.7f,.38f,dz,.08f,.40f,.08f);
                Box(metal,7.04f,1.28f,-2.15f,.065f,2.24f,.065f);
                Box(sign,7.04f,2.28f,-2.15f,.54f,.65f,.06f);
                // White pictogram built into the merged metal mesh, legible from either side.
                foreach(float face in new[]{-1f,1f})
                {
                    Box(metal,7.04f,2.30f,-2.15f+face*.036f,.33f,.31f,.012f);
                    foreach(float x in new[]{6.93f,7.15f})Box(metal,x,2.10f,-2.15f+face*.036f,.055f,.055f,.014f);
                }
            }
            void Part(string name,Mesh mesh,Material material)
            {var p=new GameObject(name);p.transform.SetParent(root,false);p.AddComponent<MeshFilter>().sharedMesh=mesh;p.AddComponent<MeshRenderer>().sharedMaterial=material;}
            Part("Shelter Posts Bench and Sign",metal.Save("BusStopMetal"),StarterScene.Surface("Vehicle Metal",new Color(.63f,.67f,.70f),.78f,.73f));
            Part("Shelter Roof",roof.Save("BusStopRoof"),StarterScene.Surface("Vehicle Trim",new Color(.025f,.031f,.038f),.05f,.2f));
            Part("Shelter Window",glass.Save("BusStopGlass"),StarterScene.Surface("Vehicle Glass",Color.white,.35f,.88f));
            Part("Bus Stop Blue Sign",sign.Save("BusStopSign"),StarterScene.Surface("Vehicle Blue",new Color(.07f,.24f,.60f),.55f,.68f));
        }
    }
}
