# Driemworks

Driemworks is an open source markerless augmented reality platform for Android powered by OpenCV and OpenGL. 
* Monocular Visual Odometry
* Color based blob detection
* Feature based object tracking

## Getting Started

To create your own activity (MyCameraActivity) that has support for capturing camera frames via the onCameraFrameMethod, simply extend the AbstractOpenCVActivity.
Override the onCameraFrameMethod to add your own logic for what occurs when a new frame is available.


```
public class MyActivity extends AbstractOpenCVActivity {
	@Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
		// your logic here
	}
}
```

To create your own activity (MyActivity) that has support for rendering graphics (via Open GL) on the current camera frame, extend the AbstractARActivity
and pass the id of your layout, the id of the opencv surface view, the id of the gl surface view, the renderer, the screen resolution, and a boolean flag
to implement the onTouchListener or not.


```
public class MyActivity extends AbstractARActivity implements View.OnTouchListener {
	public MyActivity() {
        super(R.layout.my_layout, R.id.opencv_surface_view, R.id.gl_surface_view, renderer, Resolution.RES_STANDARD, true);
	}
}

```

### Prerequisites

Follow the guide (https://opencv.org/platforms/android/) on getting started with OpenCV4Androicd

To run on a device, install the appropriate OpenCV Manager.
* For a physical device install via the Google Play Store. 
* For an emulator, install the appropriate apk for your system architecture here:       https://sourceforge.net/projects/opencvlibrary/files/opencv-win/

## Built With

* [OpenCV4Android](https://opencv.org/platforms/android/) - Open Source Computer Vision Library
* [OpenGL](https://rometools.github.io/rome/) - Open source graphics library
* [Gradle](https://gradle.org/) - Dependency Management