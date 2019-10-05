package com.driemworks.common.builders;

import android.app.Activity;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import static android.content.ContentValues.TAG;
import static android.content.Context.DEVICE_POLICY_SERVICE;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;

/**
 * @author Tony
 */
public class GLSurfaceViewBuilder {

	/**
	 * The GLSurfaceView
	 */
	private GLSurfaceView glSurfaceView;
	private int[] mGLContextAttrs;

	public GLSurfaceViewBuilder(Activity activity, int id) {
		glSurfaceView = activity.findViewById(id);
	}

	/**
	 * Build the GLSurfaceView
	 * @return glSurfaceView the GLSurfaceView
	 */
	public GLSurfaceView build() {
		return glSurfaceView;
	}

	/**
	 * Set the EGLConfigChooser
	 * @param redSize The red size
	 * @param greenSize The green size
	 * @param blueSize The blue size
	 * @param alphaSize The alpha size
	 * @param depthSize The depth size
	 * @param stencilSize The stencil size
	 * @return glSurfaceView the GLSurfaceView
	 */
	public GLSurfaceViewBuilder setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize,
													int depthSize, int stencilSize) {
		// use custom EGLConfigureChooser
//        customEGLConfigChooser chooser = new customEGLConfigChooser(this.mGLContextAttrs);
		customEGLConfigChooser chooser = new customEGLConfigChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize);
		glSurfaceView.setEGLConfigChooser(chooser);

//      glSurfaceView.setEGLConfigChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize);
		return this;
	}

	/**
	 * Set the onTouchListener
	 * @param listener The OnTouchListener
	 * @return glSurfaceView the GLSurfaceView
	 */
	public GLSurfaceViewBuilder setOnTouchListener(View.OnTouchListener listener) {
		glSurfaceView.setOnTouchListener(listener);
		return this;
	}

	/**
	 * Set the renderer
	 * @param renderer The renderer
	 * @return glSurfaceView the GLSurfaceView
	 */
	public GLSurfaceViewBuilder setRenderer(GLSurfaceView.Renderer renderer) {
		glSurfaceView.setRenderer(renderer);
		return this;
	}

	/**
	 * Set the render mode
	 * @param renderMode The render mode
	 * @return glSurfaceView the GLSurfaceView

	 */
	public GLSurfaceViewBuilder setRenderMode(int renderMode) {
		glSurfaceView.setRenderMode(renderMode);
		return this;
	}

	/**
	 * Set the format
	 * @param format The format
	 * @return glSurfaceView the GLSurfaceView
	 */
	public GLSurfaceViewBuilder setFormat(int format) {
		glSurfaceView.getHolder().setFormat(format);
		return this;
	}

	public GLSurfaceViewBuilder setEGLContextClientVersion(int i) {
		glSurfaceView.setEGLContextClientVersion(i);
		return this;
	}


	private class customEGLConfigChooser implements GLSurfaceView.EGLConfigChooser {
		private int[] mConfigAttributes;
		private final int EGL_OPENGL_ES2_BIT = 0x04;
		private final int EGL_OPENGL_ES3_BIT = 0x40;

		public customEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize, int multisamplingCount) {
			mConfigAttributes = new int[]{redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize, multisamplingCount};
		}

		public customEGLConfigChooser(int[] attributes) {
			mConfigAttributes = attributes;
		}

		public customEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
		mConfigAttributes = new int[]{redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize, 1};
		}

		@Override
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			int[][] EGLAttributes = {
					{
							// GL ES 2 with user set
							EGL10.EGL_RED_SIZE, mConfigAttributes[0],
							EGL10.EGL_GREEN_SIZE, mConfigAttributes[1],
							EGL10.EGL_BLUE_SIZE, mConfigAttributes[2],
							EGL10.EGL_ALPHA_SIZE, mConfigAttributes[3],
							EGL10.EGL_DEPTH_SIZE, mConfigAttributes[4],
							EGL10.EGL_STENCIL_SIZE, mConfigAttributes[5],
							EGL10.EGL_SAMPLE_BUFFERS, (mConfigAttributes[6] > 0) ? 1 : 0,
							EGL10.EGL_SAMPLES, mConfigAttributes[6],
							EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
							EGL10.EGL_NONE
					},
					{
							// GL ES 2 with user set 16 bit depth buffer
							EGL10.EGL_RED_SIZE, mConfigAttributes[0],
							EGL10.EGL_GREEN_SIZE, mConfigAttributes[1],
							EGL10.EGL_BLUE_SIZE, mConfigAttributes[2],
							EGL10.EGL_ALPHA_SIZE, mConfigAttributes[3],
							EGL10.EGL_DEPTH_SIZE, mConfigAttributes[4] >= 24 ? 16 : mConfigAttributes[4],
							EGL10.EGL_STENCIL_SIZE, mConfigAttributes[5],
							EGL10.EGL_SAMPLE_BUFFERS, (mConfigAttributes[6] > 0) ? 1 : 0,
							EGL10.EGL_SAMPLES, mConfigAttributes[6],
							EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
							EGL10.EGL_NONE
					},
					{
							// GL ES 2 with user set 16 bit depth buffer without multisampling
							EGL10.EGL_RED_SIZE, mConfigAttributes[0],
							EGL10.EGL_GREEN_SIZE, mConfigAttributes[1],
							EGL10.EGL_BLUE_SIZE, mConfigAttributes[2],
							EGL10.EGL_ALPHA_SIZE, mConfigAttributes[3],
							EGL10.EGL_DEPTH_SIZE, mConfigAttributes[4] >= 24 ? 16 : mConfigAttributes[4],
							EGL10.EGL_STENCIL_SIZE, mConfigAttributes[5],
							EGL10.EGL_SAMPLE_BUFFERS, 0,
							EGL10.EGL_SAMPLES, 0,
							EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
							EGL10.EGL_NONE
					},
					{
							// GL ES 2 by default
							EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
							EGL10.EGL_NONE
					}
			};

			EGLConfig result = null;
			for (int[] eglAtribute : EGLAttributes) {
				result = this.doChooseConfig(egl, display, eglAtribute);
				EGLContext GLCtx = createContext(egl, display, result);

				if (GLCtx != null) return result;
//				if (result != null) return result;
			}

			Log.e(DEVICE_POLICY_SERVICE, "Can not select an EGLConfig for rendering.");
			return null;
		}

		private EGLConfig doChooseConfig(EGL10 egl, EGLDisplay display, int[] attributes) {
			EGLConfig[] configs = new EGLConfig[1];
			int[] matchedConfigNum = new int[1];
			boolean result = egl.eglChooseConfig(display, attributes, configs, 1, matchedConfigNum);
			if (result && matchedConfigNum[0] > 0) {
				return configs[0];
			}
			return null;
		}

		public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {

			int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
			double glVersion = 3.0;

			Log.w(TAG, "creating OpenGL ES " + glVersion + " context");
			int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, (int) glVersion,
					EGL10.EGL_NONE };
			// attempt to create a OpenGL ES 3.0 context
			EGLContext context = egl.eglCreateContext(
					display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
			Log.w(TAG, "OpenGL ES " + glVersion + " config " + context);

			return context; // returns null if 3.0 is not supported;
		}
	}

}
