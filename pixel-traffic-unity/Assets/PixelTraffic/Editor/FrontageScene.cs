using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    // Baked facade meshes and shared atlases: no individual window controllers or lights.
    internal static class FrontageScene
    {
        internal static Windows BeginWindows(Transform root,int index,float x,Material frame) => new Windows(root,index,x,frame);
        internal sealed class Windows
        {
            readonly Transform root; readonly int index; readonly float x,outward; readonly Material frame;
            readonly Geometry frames=new Geometry(),glass=new Geometry();
            internal Windows(Transform root,int index,float x,Material frame)
            {this.root=root;this.index=index;this.x=x;this.frame=frame;outward=Mathf.Sign(x);}
            internal void Add(Vector3 p,int floor,int column)
            {
                float front=x+outward*.07f,back=x+outward*.025f;
                // Four front strips and four recessed reveals, with a real glass plane behind them.
                for(int side=0;side<4;side++)
                {
                    Vector3 a=Corner(front,p,side,.9f),b=Corner(front,p,(side+1)%4,.9f);
                    Vector3 c=Corner(front,p,(side+1)%4,.82f),d=Corner(front,p,side,.82f);
                    frames.Quad(a,b,c,d,Rect.MinMaxRect(0,0,1,1),Vector3.right*outward);
                    Vector3 inward=side==0?Vector3.up:side==1?Vector3.back:side==2?Vector3.down:Vector3.forward;
                    frames.Quad(d,c,Corner(back,p,(side+1)%4,.8f),Corner(back,p,side,.8f),Rect.MinMaxRect(0,0,1,1),inward);
                }
                int variant=(index*17+floor*13+column*7+(outward<0?3:0))%8;
                glass.Plane(back,p.y-.8f,p.y+.8f,p.z-.8f,p.z+.8f,Tile(variant%4,variant/4,4,2,256,128),outward);
            }
            static Vector3 Corner(float x,Vector3 p,int side,float radius)
            {return new Vector3(x,p.y+(side==0||side==1?-radius:radius),p.z+(side==0||side==3?-radius:radius));}
            internal void Save()
            {
                string key=(outward>0?"Left":"Right")+index.ToString("00");
                frames.Save(root,"Window Frame",key+"WindowFrames",frame,true);
                glass.Save(root,"Window Glass",key+"Windows",AtlasMaterial(false),false);
            }
        }

        internal static void Shop(Transform root,int index,float x,Material frame)
        {
            float outward=Mathf.Sign(x),door=(index%3-1)*3.6f;
            var frames=new Geometry();var glass=new Geometry();
            void Box(float y,float z,float depth,float height,float width)
            {frames.Box(new Vector3(x+outward*.09f,y,z),new Vector3(depth,height,width));}
            foreach(float z in new[]{-5.9f,door-.86f,door+.86f,5.9f})Box(1.4f,z,.14f,2.6f,.11f);
            Box(2.6f,0,.14f,.12f,11.8f);Box(2.55f,door,.14f,.12f,1.65f);
            float left=(-5.78f+door-.8f)*.5f,right=(door+.8f+5.78f)*.5f;
            float leftWidth=door-.8f+5.78f,rightWidth=5.78f-door-.8f;
            Box(.24f,left,.14f,.12f,leftWidth);Box(.24f,right,.14f,.12f,rightWidth);
            Box(1.4f,left,.07f,2.2f,.04f);Box(1.4f,right,.07f,2.2f,.04f);
            Box(.075f,door,.16f,.07f,1.7f);
            int style=index%3;
            Rect tile(int row)=>Tile(style,row,4,4,512,512);
            float plane=x+outward*.075f;
            glass.Plane(plane,.35f,2.52f,-5.78f,door-.8f,tile(0),outward);
            glass.Plane(plane,.35f,2.52f,door+.8f,5.78f,tile(0),outward);
            glass.Plane(plane,.08f,2.52f,door-.8f,door+.8f,tile(1),outward);
            glass.Plane(x+outward*1.18f,2.475f,3.125f,-5.7f,5.7f,tile(2),outward);
            float poster=leftWidth>rightWidth?left:right;
            glass.Plane(x+outward*.115f,1.05f,1.95f,poster-.6f,poster+.6f,tile(3),outward);
            string key=(outward>0?"Left":"Right")+index.ToString("00");
            frames.Save(root,"Shop Frames",key+"ShopFrames",frame,true);
            glass.Save(root,"Shop Interior",key+"Shop",AtlasMaterial(true),false);
        }

        static Rect Tile(int column,int row,int columns,int rows,int width,int height)
        {return Rect.MinMaxRect((float)column/columns+2f/width,(float)row/rows+2f/height,(float)(column+1)/columns-2f/width,(float)(row+1)/rows-2f/height);}
        static Material AtlasMaterial(bool shop)
        {
            string name=shop?"Shop Interior":"Window Glass";
            Material material=StarterScene.Surface(name,Color.white,shop?.08f:.25f,shop?.45f:.52f);
            if(material.GetTexture("_EmissionMap")!=null)return material;
            int width=shop?512:256,height=shop?512:128;
            var basePixels=new Color[width*height];var emissionPixels=new Color[width*height];
            int cell=shop?128:64;
            for(int y=0;y<height;y++)for(int x=0;x<width;x++)
            {
                int column=x/cell,row=y/cell;float u=(x%cell)/(float)(cell-1),v=(y%cell)/(float)(cell-1);
                Color b,e;if(shop)ShopPixel(column,row,u,v,out b,out e);else WindowPixel(row*4+column,u,v,out b,out e);
                basePixels[y*width+x]=b;emissionPixels[y*width+x]=e;
            }
            if(shop)for(int style=0;style<3;style++)
            {
                string label=new[]{"COFFEE","MARKET","STORE"}[style];
                Text(basePixels,emissionPixels,width,style*128,256,label,1,14,new Color(.94f,.87f,.67f));
                Text(basePixels,emissionPixels,width,style*128,384,"OPEN",3,3,new Color(.90f,.78f,.48f));
            }
            material.SetTexture("_BaseMap",Texture(name+"Colour",width,height,basePixels));
            material.SetTexture("_EmissionMap",Texture(name+"Emission",width,height,emissionPixels));
            material.SetColor("_EmissionColor",Color.white*.001f);material.EnableKeyword("_EMISSION");
            EditorUtility.SetDirty(material);return material;
        }
        static Texture2D Texture(string name,int width,int height,Color[] pixels)
        {
            string path=StarterScene.Generated+"/"+name.Replace(" ","")+".asset";
            var old=AssetDatabase.LoadAssetAtPath<Texture2D>(path);if(old!=null)return old;
            var texture=new Texture2D(width,height,TextureFormat.RGB24,true){name=name,filterMode=FilterMode.Trilinear,wrapMode=TextureWrapMode.Clamp,anisoLevel=4};
            texture.SetPixels(pixels);texture.Apply(true,false);AssetDatabase.CreateAsset(texture,path);return texture;
        }
        static void WindowPixel(int variant,float u,float v,out Color b,out Color e)
        {
            bool lit=variant>=3;
            Color tone=variant==3||variant==4||variant==7?new Color(1,.62f,.26f):variant==5?new Color(.35f,.60f,1):new Color(.78f,.83f,.72f);
            float shade=.37f+.22f*v;
            // Curtains, blinds, a sill and different silhouettes are baked into room variants.
            if(u<.10f||u>.90f)shade*=.23f;
            if(variant%3==0&&v>.18f&&v<.90f&&((int)(v*24)%3==0))shade*=.65f;
            if(variant%3==1&&u>.60f&&u<.77f&&v<.52f)shade*=.24f;
            if(v<.10f||v>.94f)shade*=.18f;
            b=Color.Lerp(new Color(.055f,.095f,.13f),new Color(.28f,.33f,.35f),v*.35f);
            if(u>.18f&&u<.23f)b+=new Color(.035f,.045f,.055f);
            e=lit?tone*shade:Color.black;
        }
        static void ShopPixel(int style,int row,float u,float v,out Color b,out Color e)
        {
            Color tone=style==0?new Color(1,.61f,.25f):style==1?new Color(.77f,.86f,.74f):new Color(.36f,.65f,.93f);
            b=Color.Lerp(new Color(.08f,.12f,.15f),new Color(.23f,.25f,.24f),v);e=tone*(.22f+.12f*v);
            if(style>2){b=Color.black;e=Color.black;return;}
            bool border=u<.045f||u>.955f||v<.035f||v>.965f;
            if(row==0)
            {
                // Repeated small products/mugs and shelves read through the display glass.
                bool shelf=(v>.24f&&v<.28f)||(style!=0&&v>.57f&&v<.61f);
                bool product=(u*9%1)>.20f&&(u*9%1)<.70f&&((v>.28f&&v<.43f)||(style!=0&&v>.61f&&v<.76f));
                if(shelf){b=new Color(.25f,.19f,.12f);e=tone*.07f;}
                if(product){b=Color.Lerp(new Color(.54f,.30f,.16f),new Color(.31f,.46f,.47f),(int)(u*9)%3*.5f);e=b*.32f;}
                if(style==0&&v<.25f){b=new Color(.25f,.16f,.09f);e=tone*.06f;}
                if(u>.20f&&u<.24f){b+=new Color(.07f,.08f,.09f);e*=.8f;}
            }
            else if(row==1)
            {
                b*=.7f;e*=.60f;
                if(v<.20f){b=new Color(.22f,.24f,.23f);e=Color.black;}
                if(u>.78f&&u<.84f&&v>.42f&&v<.66f){b=new Color(.74f,.76f,.72f);e=Color.black;}
            }
            else if(row==2)
            {
                b=style==0?new Color(.22f,.115f,.055f):style==1?new Color(.07f,.26f,.14f):new Color(.10f,.19f,.29f);e=b*.18f;
                if(v>.08f&&v<.12f||v>.88f&&v<.92f){b=new Color(.61f,.48f,.25f);e=b*.12f;}
            }
            else
            {
                b=new Color(.12f,.17f,.20f);e=tone*.025f;
                if(v>.15f&&v<.32f&&u>.2f&&u<.8f){b=new Color(.54f,.52f,.42f);e=b*.08f;}
            }
            if(border){b=new Color(.09f,.12f,.13f);e=Color.black;}
        }
        static readonly Dictionary<char,string> Font=new Dictionary<char,string>{
            {'A',"01110/10001/10001/11111/10001/10001/10001"},{'C',"01111/10000/10000/10000/10000/10000/01111"},
            {'E',"11111/10000/10000/11110/10000/10000/11111"},{'F',"11111/10000/10000/11110/10000/10000/10000"},
            {'K',"10001/10010/10100/11000/10100/10010/10001"},{'M',"10001/11011/10101/10101/10001/10001/10001"},
            {'N',"10001/11001/11001/10101/10011/10011/10001"},{'O',"01110/10001/10001/10001/10001/10001/01110"},
            {'P',"11110/10001/10001/11110/10000/10000/10000"},{'R',"11110/10001/10001/11110/10100/10010/10001"},
            {'S',"01111/10000/10000/01110/00001/00001/11110"},{'T',"11111/00100/00100/00100/00100/00100/00100"}};
        static void Text(Color[] b,Color[] e,int width,int ox,int oy,string word,int sx,int sy,Color ink)
        {
            int startX=ox+(128-(word.Length*6-1)*sx)/2,startY=oy+(128-7*sy)/2;
            for(int c=0;c<word.Length;c++)
            {
                string[] glyph=Font[word[c]].Split('/');
                for(int y=0;y<7;y++)for(int x=0;x<5;x++)if(glyph[y][x]=='1')
                    for(int dy=0;dy<sy;dy++)for(int dx=0;dx<sx;dx++)
                    {int i=(startY+(6-y)*sy+dy)*width+startX+(c*6+x)*sx+dx;b[i]=ink;e[i]=ink*.65f;}
            }
        }
        sealed class Geometry
        {
            readonly List<Vector3> positions=new List<Vector3>();readonly List<Vector2> uv=new List<Vector2>();readonly List<int> indices=new List<int>();
            internal void Quad(Vector3 a,Vector3 b,Vector3 c,Vector3 d,Rect rect,Vector3 normal,bool reverseU=false)
            {
                int n=positions.Count;positions.AddRange(new[]{a,b,c,d});
                float lo=reverseU?rect.xMax:rect.xMin,hi=reverseU?rect.xMin:rect.xMax;
                uv.AddRange(new[]{new Vector2(lo,rect.yMin),new Vector2(hi,rect.yMin),new Vector2(hi,rect.yMax),new Vector2(lo,rect.yMax)});
                bool flip=Vector3.Dot(Vector3.Cross(b-a,c-a),normal)<0;
                indices.AddRange(flip?new[]{n,n+2,n+1,n,n+3,n+2}:new[]{n,n+1,n+2,n,n+2,n+3});
            }
            internal void Plane(float x,float bottom,float top,float start,float end,Rect rect,float outward)
            {Quad(new Vector3(x,bottom,start),new Vector3(x,bottom,end),new Vector3(x,top,end),new Vector3(x,top,start),rect,Vector3.right*outward,outward>0);}
            internal void Box(Vector3 centre,Vector3 size)
            {
                Vector3 h=size*.5f;Vector3[] p={new Vector3(-h.x,-h.y,-h.z),new Vector3(h.x,-h.y,-h.z),new Vector3(h.x,h.y,-h.z),new Vector3(-h.x,h.y,-h.z),new Vector3(-h.x,-h.y,h.z),new Vector3(h.x,-h.y,h.z),new Vector3(h.x,h.y,h.z),new Vector3(-h.x,h.y,h.z)};
                int[] faces={0,3,2,1,4,5,6,7,0,4,7,3,1,2,6,5,0,1,5,4,3,7,6,2};
                for(int f=0;f<6;f++)Quad(centre+p[faces[f*4]],centre+p[faces[f*4+1]],centre+p[faces[f*4+2]],centre+p[faces[f*4+3]],Rect.MinMaxRect(0,0,1,1),Vector3.zero);
            }
            internal void Save(Transform parent,string name,string asset,Material material,bool shadows)
            {
                var mesh=new Mesh{name=asset};mesh.SetVertices(positions);mesh.SetUVs(0,uv);mesh.SetTriangles(indices,0);mesh.RecalculateNormals();mesh.RecalculateBounds();
                string path=StarterScene.Generated+"/"+asset+".asset";var old=AssetDatabase.LoadAssetAtPath<Mesh>(path);
                if(old!=null){UnityEngine.Object.DestroyImmediate(mesh);mesh=old;}else AssetDatabase.CreateAsset(mesh,path);
                var go=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));go.transform.SetParent(parent,false);go.GetComponent<MeshFilter>().sharedMesh=mesh;
                var renderer=go.GetComponent<MeshRenderer>();renderer.sharedMaterial=material;renderer.shadowCastingMode=shadows?ShadowCastingMode.On:ShadowCastingMode.Off;renderer.receiveShadows=true;renderer.lightProbeUsage=LightProbeUsage.Off;renderer.reflectionProbeUsage=ReflectionProbeUsage.Off;
            }
        }
    }
}
