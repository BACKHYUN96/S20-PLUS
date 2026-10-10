using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype
{
    public sealed class ClimateEffects : MonoBehaviour
    {
        [SerializeField] private MeshFilter rain, snow;
        [SerializeField] private Renderer paper;
        [SerializeField] private Renderer[] pools;
        private Mesh rainMesh, snowMesh;
        private Vector3[] rainPoints, snowPoints, rainVertices, snowVertices;
        private MaterialPropertyBlock block;
        private System.Random random;
        private float clock, paperWait = 4, paperAge = -1, paperZ;
        private bool initialized;
        public int RainCapacity => 160;
        public int SnowCapacity => 96;
        public int PaperLaunches { get; private set; }
        public float LastPaperInterval { get; private set; }
        public void Configure(MeshFilter rainFilter, MeshFilter snowFilter, Renderer newspaper, Renderer[] glow)
        { rain = rainFilter; snow = snowFilter; paper = newspaper; pools = glow; }
        public void Initialize()
        {
            if (initialized) return; initialized = true; block = new MaterialPropertyBlock(); random = new System.Random(4105);
            rainMesh = Instantiate(rain.sharedMesh); rain.sharedMesh = rainMesh; rainMesh.MarkDynamic();
            snowMesh = Instantiate(snow.sharedMesh); snow.sharedMesh = snowMesh; snowMesh.MarkDynamic();
            rainPoints = Points(RainCapacity); snowPoints = Points(SnowCapacity);
            rainVertices = new Vector3[RainCapacity * 4]; snowVertices = new Vector3[SnowCapacity * 4];
            rain.GetComponent<Renderer>().enabled = false; snow.GetComponent<Renderer>().enabled = false; paper.enabled = false;
        }
        private Vector3[] Points(int count)
        {
            var points = new Vector3[count];
            for (int i = 0; i < count; i++) points[i] = new Vector3((float)random.NextDouble() * 26 - 13, .2f + (float)random.NextDouble() * 16, -30 + (float)random.NextDouble() * 175);
            return points;
        }
        public void SetLighting(float gain)
        {
            Initialize(); block.Clear(); block.SetColor("_BaseColor", new Color(1, .66f, .24f, gain * .28f));
            block.SetColor("_EmissionColor", new Color(1, .66f, .24f) * .5f);
            foreach (var renderer in pools) { renderer.enabled = gain > .001f; renderer.SetPropertyBlock(block); }
        }
        public void Advance(float dt, float rainGain, float snowGain, float wind, bool stormTarget)
        {
            clock += dt;
            Particles(rain, rainMesh, rainPoints, rainVertices, rainGain, wind, dt, false);
            Particles(snow, snowMesh, snowPoints, snowVertices, snowGain, wind, dt, true);
            if (stormTarget)
            {
                paperWait -= dt;
                if (paperWait <= 0)
                { paperAge = 0; paperZ = 8 + (float)random.NextDouble() * 30; PaperLaunches++; LastPaperInterval = 3 + (float)random.NextDouble() * 2; paperWait += LastPaperInterval; }
            }
            if (paperAge >= 0)
            {
                paperAge += dt; if (paperAge >= 2.4f) paperAge = -1;
                else
                {
                    float t = paperAge / 2.4f;
                    paper.transform.position = new Vector3(14 - 28 * t, 3.3f + Mathf.Sin(t * Mathf.PI * 4) * .6f, paperZ - t * 5);
                    paper.transform.rotation = Quaternion.LookRotation(-Camera.main.transform.forward, Camera.main.transform.up) * Quaternion.Euler(0, 30 * Mathf.Sin(t * 12), t * 720);
                }
            }
            paper.enabled = paperAge >= 0 && wind > .001f;
            block.Clear(); block.SetColor("_BaseColor", new Color(.9f, .89f, .83f, wind)); paper.SetPropertyBlock(block);
        }
        private void Particles(MeshFilter filter, Mesh mesh, Vector3[] positions, Vector3[] vertices, float gain, float wind, float dt, bool flakes)
        {
            Renderer renderer = filter.GetComponent<Renderer>(); renderer.enabled = gain > .001f; if (!renderer.enabled) return;
            Vector3 right = Camera.main.transform.right, up = Camera.main.transform.up;
            for (int i = 0; i < positions.Length; i++)
            {
                Vector3 p = positions[i]; p.y -= (flakes ? 1.6f : 12 + gain * 5) * dt;
                p.x += ((flakes ? Mathf.Sin(clock + i) * .5f : -1) - wind * (flakes ? 2 : 7)) * dt;
                if (p.y < .2f) p.y += 16; if (p.x < -13) p.x += 26; if (p.x > 13) p.x -= 26; positions[i] = p;
                Vector3 a = right * (flakes ? .065f : .018f), b = flakes ? up * .065f : new Vector3(.05f + wind * .28f, .65f, 0);
                int k = i * 4; vertices[k] = p - a; vertices[k + 1] = p + a; vertices[k + 2] = p + a + b; vertices[k + 3] = p - a + b;
            }
            mesh.vertices = vertices;
            block.Clear(); block.SetColor("_BaseColor", new Color(.76f, .85f, .95f, gain * (flakes ? .8f : .42f)));
            block.SetColor("_EmissionColor", new Color(.12f, .16f, .22f)); renderer.SetPropertyBlock(block);
        }
        private void OnDestroy() { Release(rainMesh); Release(snowMesh); }
        private static void Release(Mesh mesh) { if (mesh == null) return; if (Application.isPlaying) Destroy(mesh); else DestroyImmediate(mesh); }
    }
}
