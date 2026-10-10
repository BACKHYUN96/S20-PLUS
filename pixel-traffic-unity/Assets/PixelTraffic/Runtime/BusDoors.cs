using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    public sealed class BusDoors : MonoBehaviour
    {
        [SerializeField] Transform front,rear;
        public Transform Front=>front;public Transform Rear=>rear;
        public void Configure(Transform a,Transform b){front=a;rear=b;Apply(0);}
        public void Apply(float openness)
        {
            float slide=Mathf.SmoothStep(0,1,openness)*.58f;
            front.localPosition=new Vector3(1.314f,1.62f,4.38f+slide);
            rear.localPosition=new Vector3(1.314f,1.62f,3.77f-slide);
        }
    }
}
