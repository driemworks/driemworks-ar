package graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_SMOOTH;
import static android.opengl.GLES10.glColorPointer;
import static android.opengl.GLES10.glShadeModel;
import static android.opengl.GLES10.glVertexPointer;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_CW;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glFrontFace;

/**
 * A simple colour-cube that is used for drawing the current rotation of the device
 * 
 */
public class Cube {
    /**
     * Buffer for the vertices
     */
    private FloatBuffer mVertexBuffer;
    /**
     * Buffer for the colours
     */
    private FloatBuffer mColorBuffer;
    /**
     * Buffer for indices
     */
    private ByteBuffer mIndexBuffer;

    /**
     * Initialises a new instance of the cube
     */
    public Cube() {
        final float[] vertices = { -1, -1, -1, 1, -1, -1, 1, 1, -1, -1, 1, -1, -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1, };

        final float[] colors = { 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0,
                1, 1, 1, };

        final byte[] indices = { 0, 4, 5, 0, 5, 1, 1, 5, 6, 1, 6, 2, 2, 6, 7, 2, 7, 3, 3, 7, 4, 3, 4, 0, 4, 7, 6, 4, 6,
                5, 3, 0, 1, 3, 1, 2 };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
    }

    /**
     * Draws this cube of the given GL-Surface
     * 
     * @param unused The GL-Surface this cube should be drawn upon.
     */
    public void draw(GL10 unused) {
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        glShadeModel(GL_SMOOTH);
        glVertexPointer(3, GL_FLOAT, 0, mVertexBuffer);
        glColorPointer(4, GL_FLOAT, 0, mColorBuffer);
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, mIndexBuffer);
    }
}
