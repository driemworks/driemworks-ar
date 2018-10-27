## Under Construction..
# OpenAR
OpenCV is an open source augmented reality platform powered by OpenCV and OpenGL. 

### OpenCV
OpenCV support is supplied by `OpenCVFragment`.
OpenGL support is supplied by the `OpenGLFragment`.
To add support for OpenCV to your activity, implement CameraBridgeViewBase.CvCameraViewListener2 and use the FragmentTransaction object to add the OpenCVFragment and commit the transaction.

```
public class MyActivity extends FragmentActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ...
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // add opengl fragment
        transaction.add(new OpenGLFragment(), "OpenGLGFragment");
        // add opencv fragment
        transaction.add(new OpenCVFragment(), "OpenCVFragment");
        transaction.commit();
        ...
    }

	@Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
		// your opencv logic here
	}
}
```

### Prerequisites
Follow the guide (https://opencv.org/platforms/android/) on getting started with OpenCV4Androicd
