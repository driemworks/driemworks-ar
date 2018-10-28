# Driemworks
Driemworks is an open source augmented reality platform powered by OpenCV and OpenGL. 

### Motivation


Example (keypoint detection and tracking: see OpenARActivity.java)
![OpenARActivity screenshot](https://github.com/driemworks/driemworks/blob/master/screenshot.png?raw=true)

### Built with
- java
- gradle
- openCV
- openGL

### OpenCV Support
OpenCV support is supplied by the `OpenCVFragment`. To use the `OpenCVFragment` in your activity, the activity must implement `CameraBridgeViewBase.CvCameraViewListener2`.

#### Example
```
public class MyActivity extends FragmentActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ...
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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


### OpenGL Support
OpenGL support is supplied by the `OpenGLFragment`. To provide OpenGL and OpenCV support in an activity, the activity must implement `CameraBridgeViewBase.CvCameraViewListener2` and extend `AbstractARActivity`.

For a renderer with an orientation provider, the renderer should extend `AbstractOrientationRenderer`

#### Example
```
public class MyActivity extends AbstractARActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRenderer(new CubeRenderer());
        setContentView(R.layout.opengl_opencv_layout);
        setGlSurfaceViewId(R.id.opengl_surface_view);
        setOpenCVSurfaceViewId(R.id.opencv_surface_view);
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