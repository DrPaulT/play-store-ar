package uk.co.drdv.examples.playlistingar.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uk.co.drdv.examples.playlistingar.VuforiaActivity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.TrackableSource;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

public class ListingRenderer implements Renderer {

	private static final float SCALE = 0.28125f;

	public boolean creatingTrackable = false;
	public TrackableSource trackableSource;
	public float touchOffset = 0;
	public float scrollOffset = 0;

	private VuforiaActivity vuforiaActivity;
	private Shaders shaders;
	private Texture texture;
	// Transparent clipping triangles on z = 0.
	// VC = vertex, colour interleaved: x, y, z, r, g, b, a.
	// I = index.
	private FloatBuffer clipVCBuffer;
	private ShortBuffer clipIBuffer;
	// Grey background rectangle.
	private FloatBuffer bgVCBuffer;
	// Feature graphic.
	// VT = vertex, texture interleaved: x, y, z, s, t.
	private FloatBuffer featureVTBuffer;
	private ShortBuffer featureIBuffer;
	// White background for detail pane.
	private FloatBuffer whiteVCBuffer;
	// App details.
	private FloatBuffer detailsVTBuffer;
	private ShortBuffer detailsIBuffer;
	// Status, action and footer bars.
	private FloatBuffer greenBgVTBuffer;
	private ShortBuffer greenBgIBuffer;
	private FloatBuffer statusVTBuffer;
	private ShortBuffer statusIBuffer;

	private Matrix44F projectionMatrix;
	private float[] modelViewProjection = new float[16];
	private float[] modelViewProjectionScrolling = new float[16];

	public ListingRenderer(VuforiaActivity vuforiaActivity) {
		this.vuforiaActivity = vuforiaActivity;
		trackableSource = null;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glEnable(GLES20.GL_DITHER);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		Vuforia.onSurfaceCreated();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		if (shaders == null) {
			shaders = new Shaders();
			texture = new Texture(vuforiaActivity.getApplicationContext());
			createClip();
			createBackground();
			createFeature();
			createWhite();
			createDetails();
			createStatus();
		}
		Vuforia.onSurfaceChanged(width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glDisable(GLES20.GL_BLEND);
		com.qualcomm.vuforia.Renderer renderer = com.qualcomm.vuforia.Renderer
				.getInstance();
		State state = renderer.begin();
		renderer.drawVideoBackground();
		maybeCreateTrackable();
		if (state.getNumTrackableResults() > 0) {
			generateMvpMatrices(state);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_BLEND);
			texture.bindTexture();
			drawClip();
			drawBackground();
			drawFeature();
			drawWhite();
			drawDetails();
			drawStatus();
		}
		renderer.end();
	}

	public void setProjectionMatrix() {
		CameraCalibration cameraCalibration = CameraDevice.getInstance()
				.getCameraCalibration();
		projectionMatrix = Tool
				.getProjectionGL(cameraCalibration, 50f, 3000.0f);
	}

	private void maybeCreateTrackable() {
		if (creatingTrackable) {
			TrackerManager trackerManager = TrackerManager.getInstance();
			ImageTracker imageTracker = (ImageTracker) (trackerManager
					.getTracker(ImageTracker.getClassType()));
			ImageTargetBuilder targetBuilder = imageTracker
					.getImageTargetBuilder();
			TrackableSource newTrackableSource = targetBuilder
					.getTrackableSource();
			if (newTrackableSource != null) {
				trackableSource = newTrackableSource;
				creatingTrackable = false;
				vuforiaActivity.startTrackers();
			}
		}
	}

	private void generateMvpMatrices(State state) {
		TrackableResult trackableResult = state.getTrackableResult(0);
		Matrix44F modelViewMatrixVuforia = Tool
				.convertPose2GLMatrix(trackableResult.getPose());
		float[] modelViewMatrix = modelViewMatrixVuforia.getData();
		Matrix.scaleM(modelViewMatrix, 0, SCALE, SCALE, SCALE);
		Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix.getData(),
				0, modelViewMatrix, 0);
		Matrix.translateM(modelViewMatrix, 0, 0, touchOffset + scrollOffset, 0);
		Matrix.multiplyMM(modelViewProjectionScrolling, 0,
				projectionMatrix.getData(), 0, modelViewMatrix, 0);
	}

	private void createClip() {
		float[] coords = {// x, y, z, r, g, b, a.
		-100000, -100000, 0, 0, 0, 0, 0,//
				100000, -100000, 0, 0, 0, 0, 0,//
				-100000, -320, 0, 0, 0, 0, 0,//
				-512, -320, 0, 0, 0, 0, 0,//
				512, -320, 0, 0, 0, 0, 0,//
				100000, -320, 0, 0, 0, 0, 0,//
				-100000, 320, 0, 0, 0, 0, 0,//
				-512, 320, 0, 0, 0, 0, 0,//
				512, 320, 0, 0, 0, 0, 0,//
				100000, 320, 0, 0, 0, 0, 0,//
				-100000, 100000, 0, 0, 0, 0, 0,//
				100000, 100000, 0, 0, 0, 0, 0 };
		clipVCBuffer = createBuffer(coords);

		short[] indices = { 0, 1, 2, 2, 1, 5, 6, 9, 10, 10, 9, 11, 2, 3, 6, 3,
				7, 6, 4, 5, 8, 5, 9, 8 };
		clipIBuffer = createBuffer(indices);
	}

	private void drawClip() {
		drawSimpleTriangles(modelViewProjection, clipVCBuffer, clipIBuffer);
	}

	private void createBackground() {
		float[] coords = {// x, y, z, r, g, b, a.
		-512, -320, 0, 0.9f, 0.9f, 0.9f, 1,//
				-512, 320, 0, 0.9f, 0.9f, 0.9f, 1,//
				512, -320, 0, 0.9f, 0.9f, 0.9f, 1,//
				-512, 320, 0, 0.9f, 0.9f, 0.9f, 1,//
				512, 320, 0, 0.9f, 0.9f, 0.9f, 1,//
				512, -320, 0, 0.9f, 0.9f, 0.9f, 1 };
		bgVCBuffer = createBuffer(coords);
	}

	private void drawBackground() {
		GLES20.glDepthMask(false);
		drawSimpleTriangles(modelViewProjection, bgVCBuffer);
		GLES20.glDepthMask(true);
	}

	private void createFeature() {
		float z0 = -250;
		float s0 = 1.7f;
		float z1 = -150;
		float s1 = 1.2f;
		float[] c = texture.coordinates[Texture.FEATURE];
		float[] d = texture.coordinates[Texture.PLAY_BUTTON];
		float[] coords = {// x, y, z, s, t.
		-512 * s0, 293 * s0, z0, c[0], c[1],// Feature graphic.
				-512 * s0, -207 * s0, z0, c[0], c[3],//
				512 * s0, -207 * s0, z0, c[2], c[3],//
				512 * s0, 293 * s0, z0, c[2], c[1],//
				-43 * s1, 186 * s1, z1, d[0], d[1],// Play movie button.
				-43 * s1, 100 * s1, z1, d[0], d[3],//
				43 * s1, 100 * s1, z1, d[2], d[3],//
				43 * s1, 186 * s1, z1, d[2], d[1] };
		featureVTBuffer = createBuffer(coords);

		short[] indices = { 0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4 };
		featureIBuffer = createBuffer(indices);
	}

	private void drawFeature() {
		drawTextureTriangles(modelViewProjectionScrolling, featureVTBuffer,
				featureIBuffer);
	}

	private void createWhite() {
		float z = -150;
		float s = 1.2f;
		float[] coords = {// x, y, z, r, g, b, a.
		-410 * s, -1063 * s, z, 1, 1, 1, 1,//
				-410 * s, 40 * s, z, 1, 1, 1, 1,//
				410 * s, -1063 * s, z, 1, 1, 1, 1,//
				-410 * s, 40 * s, z, 1, 1, 1, 1,//
				410 * s, 40 * s, z, 1, 1, 1, 1,//
				410 * s, -1063 * s, z, 1, 1, 1, 1 };
		whiteVCBuffer = createBuffer(coords);
	}

	private void drawWhite() {
		drawSimpleTriangles(modelViewProjectionScrolling, whiteVCBuffer);
	}

	private void createDetails() {
		float z = -100;
		float s = 1.1f;
		float[] c = texture.coordinates[Texture.DETAILS];
		float[] d = texture.coordinates[Texture.RATINGS];
		float[] e = texture.coordinates[Texture.SCREENS];
		float[] coords = {// x, y, z, s, t.
		-410 * s, 40 * s, z, c[0], c[1],//
				-410 * s, -439 * s, z, c[0], c[3],//
				410 * s, -439 * s, z, c[2], c[3],//
				410 * s, 40 * s, z, c[2], c[1],//
				-410 * s, -780 * s, z, d[0], d[1],//
				-410 * s, -1063 * s, z, d[0], d[3],//
				410 * s, -1063 * s, z, d[2], d[3],//
				410 * s, -780 * s, z, d[2], d[1],//
				-469, -499, -10, e[0], e[1],//
				-469, -840, -10, e[0], e[3],//
				469, -840, -10, e[2], e[3],//
				469, -499, -10, e[2], e[1] };
		detailsVTBuffer = createBuffer(coords);

		short[] indices = { 0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4, 8, 9, 10, 10,
				11, 8 };
		detailsIBuffer = createBuffer(indices);
	}

	private void drawDetails() {
		drawTextureTriangles(modelViewProjectionScrolling, detailsVTBuffer,
				detailsIBuffer);
	}

	private void createStatus() {
		float[] b = texture.coordinates[Texture.GREEN_BG];
		float[] coords0 = {// x, y, z, s, t.
		// Have to give the s texture coordinates a little nudge here to prevent
		// floating point errors.
				-512, 293, 0, b[0] + 0.001f, b[1],//
				-512, 233, 0, b[0] + 0.001f, b[3],//
				512, 233, 0, b[2], b[3],//
				512, 293, 0, b[2], b[1] };
		greenBgVTBuffer = createBuffer(coords0);

		short[] indices0 = { 0, 1, 2, 2, 3, 0 };
		greenBgIBuffer = createBuffer(indices0);

		float[] c = texture.coordinates[Texture.STATUS_BAR];
		float[] d = texture.coordinates[Texture.FOOTER];
		float[] e = texture.coordinates[Texture.ACTION_ICONS];
		float[] coords1 = {// x, y, z, s, t.
		-512, 320, 0, c[0], c[1],//
				-512, 293, 0, c[0], c[3],//
				512, 293, 0, c[2], c[3],//
				512, 320, 0, c[2], c[1],//
				-512, -269, 0, d[0], d[1],//
				-512, -320, 0, d[0], d[3],//
				512, -320, 0, d[2], d[3],//
				512, -269, 0, d[2], d[1],//
				-512, 293, 0, e[0], e[1],//
				-512, 233, 0, e[0], e[3],//
				512, 233, 0, e[2], e[3],//
				512, 293, 0, e[2], e[1] };
		statusVTBuffer = createBuffer(coords1);

		short[] indices1 = { 0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4, 8, 9, 10, 10,
				11, 8 };
		statusIBuffer = createBuffer(indices1);
	}

	private void drawStatus() {
		GLES20.glDepthMask(false);
		float alpha = Math.min((touchOffset + scrollOffset) / 280f, 1f);
		drawTextureAlphaTriangles(modelViewProjection, greenBgVTBuffer,
				greenBgIBuffer, alpha);
		GLES20.glDepthMask(true);
		drawTextureTriangles(modelViewProjection, statusVTBuffer, statusIBuffer);
	}

	private FloatBuffer createBuffer(float[] coords) {
		FloatBuffer buffer = ByteBuffer.allocateDirect(coords.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer.put(coords);
		return buffer;
	}

	private ShortBuffer createBuffer(short[] indices) {
		ShortBuffer buffer = ByteBuffer.allocateDirect(indices.length * 2)
				.order(ByteOrder.nativeOrder()).asShortBuffer();
		buffer.put(indices);
		buffer.rewind();
		return buffer;
	}

	private void drawSimpleTriangles(float[] mvpMatrix, FloatBuffer vcBuffer) {
		shaders.setSimpleParameters(mvpMatrix, vcBuffer);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vcBuffer.capacity() / 7);
		shaders.clearSimpleParameters();
	}

	private void drawSimpleTriangles(float[] mvpMatrix, FloatBuffer vcBuffer,
			ShortBuffer iBuffer) {
		shaders.setSimpleParameters(mvpMatrix, vcBuffer);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, iBuffer.capacity(),
				GLES20.GL_UNSIGNED_SHORT, iBuffer);
		shaders.clearSimpleParameters();
	}

	private void drawTextureTriangles(float[] mvpMatrix, FloatBuffer vtBuffer,
			ShortBuffer iBuffer) {
		shaders.setTextureParameters(mvpMatrix, vtBuffer);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, iBuffer.capacity(),
				GLES20.GL_UNSIGNED_SHORT, iBuffer);
		shaders.clearTextureParameters();
	}

	private void drawTextureAlphaTriangles(float[] mvpMatrix,
			FloatBuffer vtBuffer, ShortBuffer iBuffer, float alpha) {
		shaders.setTextureAlphaParameters(mvpMatrix, vtBuffer, alpha);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, iBuffer.capacity(),
				GLES20.GL_UNSIGNED_SHORT, iBuffer);
		shaders.clearTextureAlphaParameters();
	}

}
