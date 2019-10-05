package graphics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.util.Log;

import com.driemworks.common.utils.TagUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class Cube2 {
	private Context context;
	private FloatBuffer vertexBuffer;
	private ShortBuffer indexBuffer;

	private int shaderProgram;
	/**
	 * The tag used for logging
	 */
	private final String TAG = TagUtils.getTag(this);

	public Cube2(Context c) {
		context = c;
		loadObj("cube.obj");
	}

	@SuppressLint("NewApi")
	private void loadObj(String filename) {
		ArrayList<Float> tempVertices = new ArrayList<>();
		ArrayList<Float> tempNormals = new ArrayList<>();
		ArrayList<Short> vertexIndices = new ArrayList<>();
		ArrayList<Short> normalIndices = new ArrayList<>();
		ArrayList<Float> uv = new ArrayList<>();
		try {
			AssetManager manager = context.getAssets();
			InputStreamReader in = new InputStreamReader(manager.open(filename));
			BufferedReader reader = new BufferedReader(in);

			long size = reader.lines().count();

//			float[] tempVertices = new float[(int) (size * 3)];
//			float[] vertexIndices = new float[(int) (size * 3)];

//			String line;
//			while ((line = reader.readLine()) != null) {
			reader.lines().forEach(line -> {
				if (line.startsWith("v")) {
					String[] tokens = line.split("[ ]+");
					tempVertices.add(Float.valueOf(tokens[1])); //vx
					tempVertices.add(Float.valueOf(tokens[2])); //vy
					tempVertices.add(Float.valueOf(tokens[3])); //vz
				} else if (line.startsWith("vn")) {
					String[] tokens = line.split("[ ]+");
					tempNormals.add(Float.valueOf(tokens[1])); //nx
					tempNormals.add(Float.valueOf(tokens[2])); //ny
					tempNormals.add(Float.valueOf(tokens[3])); //nz
				} else if (line.startsWith("vt")) {
					String[] tokens = line.split("[ ]+");
					uv.add(Float.valueOf(tokens[1]));
					uv.add(Float.valueOf(tokens[2]));
				} else if (line.startsWith("f")) {
                    /*
                    vertexIndices.add(Short.valueOf(tokens[1].split("/")[0])); //first point of a face
                    vertexIndices.add(Short.valueOf(tokens[2].split("/")[0])); //second point
                    vertexIndices.add(Short.valueOf(tokens[3].split("/")[0])); //third point
                    normalIndices.add(Short.valueOf(tokens[1].split("/")[2])); //first normal
                    normalIndices.add(Short.valueOf(tokens[2].split("/")[2])); //second normal
                    normalIndices.add(Short.valueOf(tokens[3].split("/")[2])); //third
                     */
					//                  for (int i = 1; i <= 3; i++) {
					//                      //String[] s = tokens[i].split("/");
					//                      vertexIndices.add(Short.valueOf());
					//                      //normalIndices.add(Short.valueOf(s[2]));
					//                  }
					String[] tokens = line.split("[ ]+");
					vertexIndices.add(Short.valueOf(tokens[1]));
					vertexIndices.add(Short.valueOf(tokens[2]));
					vertexIndices.add(Short.valueOf(tokens[3]));
				}
			});

			float[] vertices = new float[tempVertices.size()];
			for (int i = 0; i < tempVertices.size(); i++) {
				Float f = tempVertices.get(i);
				vertices[i] = (f != null ? f : Float.NaN);
			}

			short[] indices = new short[vertexIndices.size()];
			for (int i = 0; i < vertexIndices.size(); i++) {
				Short s = vertexIndices.get(i);
				indices[i] = (s != null ? s : 1);
			}

			vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
			vertexBuffer.put(vertices).position(0);

			indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
			indexBuffer.put(indices).position(0);

			int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
			GLES20.glShaderSource(vertexShader, vertexCode);
			GLES20.glCompileShader(vertexShader);
			int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
			GLES20.glShaderSource(fragmentShader, fragmentCode);
			GLES20.glCompileShader(fragmentShader);

			shaderProgram = GLES20.glCreateProgram();
			GLES20.glAttachShader(shaderProgram, vertexShader);
			GLES20.glAttachShader(shaderProgram, fragmentShader);
			GLES20.glLinkProgram(shaderProgram);

			int[] linked = new int[1];
			GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linked, 0);
			if (linked[0] == 0) {
				Log.e(TAG, "Shader code error.");
				Log.e(TAG, GLES20.glGetProgramInfoLog(shaderProgram));
				GLES20.glDeleteProgram(shaderProgram);
				return;
			}

			GLES20.glDeleteShader(vertexShader);
			GLES20.glDeleteShader(fragmentShader);
		} catch (Exception e) {
			Log.e(TAG, "Error.", e);
		}
	}

	private String vertexCode = "" +
			"attribute vec4 a_position;  \n" +
			"uniform mat4 mvpMatrix;     \n" +
			"void main() {               \n" +
			"   gl_Position = a_position * mvpMatrix;\n" +
			"}                           \n";

	private String fragmentCode = "" +
			"precision mediump float;                   \n" +
			"void main() {                              \n" +
			"   gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);\n" +
			"}                                          \n";

	private int attribute_Position;
	private int uniform_mvpMatrix;

	public void draw(float[] mvpMatrix) {
		GLES20.glUseProgram(shaderProgram);
		attribute_Position = GLES20.glGetAttribLocation(shaderProgram, "a_position");
		GLES20.glVertexAttribPointer(attribute_Position, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
		GLES20.glEnableVertexAttribArray(attribute_Position);
		uniform_mvpMatrix = GLES20.glGetUniformLocation(shaderProgram, "mvpMatrix");
		GLES20.glUniformMatrix4fv(uniform_mvpMatrix, 1, false, mvpMatrix, 0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		GLES20.glDisableVertexAttribArray(attribute_Position);
	}
}
