using System;
using System.Collections.Generic;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class WiperChecks
    {
        internal static Report Validate()
        {
            var cars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);
            var models=new HashSet<string>();int greyBlades=0;var tint=new MaterialPropertyBlock();int vertices=0,lightSweeps=0,heavySweeps=0,finalWipes=0;
            int[] rates={15,30,60,120};var positions=new float[cars.Length];
            for(int i=0;i<cars.Length;i++)positions[i]=cars[i].transform.position.z;
            var street=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();
            try
            {
                Need(cars.Length==24,"Wiper fleet incomplete.");
                foreach(var car in cars)
                {
                    var pair=car.GetComponent<VehicleWipers>();Need(pair!=null&&pair.Pivots.Length==2,"Vehicle lacks paired wipers.");models.Add(car.Model);
                    foreach(var pivot in pair.Pivots)
                    {
                        var blade=pivot.GetComponent<Renderer>();blade.GetPropertyBlock(tint);Color grey=tint.GetColor("_BaseColor");
                        Need(Mathf.Abs(grey.r-.52f)<.001f&&Mathf.Abs(grey.g-.55f)<.001f&&Mathf.Abs(grey.b-.58f)<.001f,"Wiper tint is not the requested contrasting grey.");
                        Need(blade.sharedMaterial==car.transform.Find("Front Lamps").GetComponent<Renderer>().sharedMaterial,"Wiper creates a separate material instance.");
                        Color floor=tint.GetColor("_EmissionColor");
                        Need(blade.sharedMaterial.IsKeywordEnabled("_EMISSION"),"Existing lamp emission variant missing on wiper.");
                        Need(Mathf.Abs(floor.r-.075f)+Mathf.Abs(floor.g-.08f)+Mathf.Abs(floor.b-.085f)<.001f,"Wiper night brightness floor missing.");
                        var glassMaterial=car.transform.Find("Sloped Windows").GetComponent<Renderer>().sharedMaterial;
                        var glassTexture=glassMaterial.GetTexture("_BaseMap") as Texture2D;
                        Need(glassTexture!=null&&(glassTexture.GetPixel(32,32)*glassMaterial.GetColor("_BaseColor")).grayscale<grey.grayscale-.15f,"Wiper grey blends with windshield texture.");
                        // Recreate a scene/player load where property blocks start empty.
                        blade.SetPropertyBlock(null);pair.RestoreBladeTint();blade.GetPropertyBlock(tint);
                        Color restored=tint.GetColor("_BaseColor");
                        Need(Mathf.Abs(restored.r-grey.r)+Mathf.Abs(restored.g-grey.g)+Mathf.Abs(restored.b-grey.b)<.003f,"Player load loses the nonserialized wiper tint.");Color restoredFloor=tint.GetColor("_EmissionColor");
                        Need(Mathf.Abs(restoredFloor.r-floor.r)+Mathf.Abs(restoredFloor.g-floor.g)+Mathf.Abs(restoredFloor.b-floor.b)<.003f,"Player load loses the wiper night brightness floor.");greyBlades++;
                    }
                    var s=pair.Screen;Need(s.Length==4,"Wiper windshield profile missing.");
                    var glass=car.transform.Find("Sloped Windows").GetComponent<MeshFilter>().sharedMesh;
                    foreach(var corner in s)
                    {bool found=false;foreach(var v in glass.vertices)if((v-corner).sqrMagnitude<1e-8f)found=true;Need(found,"Wiper profile does not match actual glass: "+car.Model);}
                    Vector3 bottom=(s[0]+s[1])*.5f,up=((s[2]+s[3])*.5f-bottom).normalized;
                    float height=Vector3.Distance(bottom,(s[2]+s[3])*.5f);Vector3 normal=Vector3.Cross(Vector3.right,up).normalized;
                    for(int sample=0;sample<=16;sample++)foreach(var pivot in pair.Pivots)
                    {
                        pivot.localRotation=Quaternion.AngleAxis(Mathf.Lerp(VehicleWipers.ParkAngle,VehicleWipers.TopAngle,sample/16f),Vector3.forward);
                        foreach(var v in pivot.GetComponent<MeshFilter>().sharedMesh.vertices)
                        {
                            Vector3 point=car.transform.InverseTransformPoint(pivot.TransformPoint(v));float fraction=Vector3.Dot(point-bottom,up)/height;
                            float left=Mathf.Lerp(s[0].x,s[2].x,fraction),right=Mathf.Lerp(s[1].x,s[3].x,fraction),depth=Vector3.Dot(point-bottom,normal);
                            Need(fraction>.005f&&fraction<.98f&&point.x>left+.003f&&point.x<right-.003f,"Blade leaves glass or strikes frame: "+car.Model+" "+point);
                            Need(depth>.014f&&depth<.027f,"Blade clips into glass or floats off it.");vertices++;
                        }
                    }
                    foreach(float rain in new[]{.38f,1f})
                    {
                        int expected=-1;float angle=0;
                        foreach(int fps in rates)
                        {
                            pair.ResetState();for(int n=0;n<fps*30;n++)pair.Advance(1d/fps,rain);
                            if(expected<0){expected=pair.CompletedSweeps;angle=pair.Angle;}
                            Need(pair.CompletedSweeps==expected&&Mathf.Abs(pair.Angle-angle)<.003f,"Wiper clock depends on frame rate.");
                        }
                        if(rain<.5f)lightSweeps+=expected;else heavySweeps+=expected;
                    }
                    pair.ResetState();pair.Advance(12,0);Need(!pair.Moving&&pair.CompletedSweeps==0,"Dry weather starts wipers.");
                    pair.Advance(pair.StartDelay+.2,1);float before=pair.Angle;int completed=pair.CompletedSweeps;
                    pair.Advance(1d/120,0);Need(Mathf.Abs(pair.Angle-before)<4,"Rain cessation snaps blade to park.");
                    pair.Advance(3,0);Need(!pair.Moving&&Mathf.Abs(pair.Angle-VehicleWipers.ParkAngle)<.001f&&pair.CompletedSweeps==completed+1,"Last wet stroke fails to finish and park.");
                    pair.Advance(12,0);Need(pair.CompletedSweeps==completed+1,"Dry wipers keep cycling.");
                    pair.ResetState();pair.Advance(2,.38f);Need(!pair.Moving&&pair.CompletedSweeps==1,"Drizzle has no intermittent rest.");
                    pair.Advance(2,0);Need(!pair.Moving&&pair.CompletedSweeps==2,"Stopping during intermittent rest misses final wipe.");finalWipes+=2;
                    pair.ResetState();pair.Advance(pair.StartDelay+.2,1);before=pair.Angle;pair.Advance(0,1);Need(pair.Angle==before,"Zero elapsed changes wiper pose.");
                    pair.Advance(.02,.38f);Need(Mathf.Abs(pair.Angle-before)<8,"Rain intensity change snaps active blade.");
                }
                Need(models.Count==6&&heavySweeps>lightSweeps*4,"Heavy rain does not sweep faster than drizzle.");
                climate.Preview(0,2);street.ResetModel();foreach(var car in street.Model.Cars){car.cruise=car.speed=0;}
                street.Advance(1);int stoppedWipers=0;
                foreach(var car in cars)if(car.gameObject.activeSelf)
                {var pair=car.GetComponent<VehicleWipers>();Need(pair.Moving||pair.CompletedSweeps>0,"Stopped active car does not wipe in rain.");stoppedWipers++;}
                Need(stoppedWipers>=8,"Stationary integration probe has too few active cars.");
                VehicleWipers probe=null;foreach(var car in cars)if(car.gameObject.activeSelf&&car.GetComponent<VehicleWipers>().Moving){probe=car.GetComponent<VehicleWipers>();break;}
                Need(probe!=null,"Pause probe lacks a moving active wiper.");float frozen=probe.Angle;int frozenCount=probe.CompletedSweeps;
                street.SetPaused(true);street.Advance(10);street.ApplyViews();Need(probe.Angle==frozen&&probe.CompletedSweeps==frozenCount,"Paused street advances wipers.");
                return new Report{result="PASS: contrasting grey restored on player load, actual 24-car/48-blade glass fit, drizzle/heavy cadence, final wipe from moving/rest, stopped cars, frame clock and pause; phone test pending",models=models.Count,greyBlades=greyBlades,vehicles=cars.Length,blades=cars.Length*2,checkedVertices=vertices,drizzleSweeps=lightSweeps,heavySweeps=heavySweeps,finalWipes=finalWipes,stoppedWipers=stoppedWipers,frameRates=rates};
            }
            finally
            {street.SetPaused(false);for(int i=0;i<cars.Length;i++)cars[i].ResetPosition(positions[i]);climate.Preview(0,0);street.ResetModel();street.ApplyViews();}
        }
        static void Need(bool condition,string message){if(!condition)throw new InvalidOperationException(message);}
        [Serializable] internal sealed class Report
        {public string result;public int models,vehicles,blades,greyBlades,checkedVertices,drizzleSweeps,heavySweeps,finalWipes,stoppedWipers;public int[] frameRates;}
    }
}
