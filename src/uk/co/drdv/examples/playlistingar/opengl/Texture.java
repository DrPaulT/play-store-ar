package uk.co.drdv.examples.playlistingar.opengl;

import java.nio.IntBuffer;

import uk.co.drdv.examples.playlistingar.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

public class Texture {

	// Textures in the texture atlas.
	public static final int STATUS_BAR = 0;
	public static final int FEATURE = 1;
	public static final int FOOTER = 2;
	public static final int DETAILS = 3;
	public static final int RATINGS = 4;
	public static final int SCREENS = 5;
	public static final int ACTION_ICONS = 6;
	public static final int PLAY_BUTTON = 7;
	public static final int GREEN_BG = 8;

	// Id, minimum s, minimum t, maximum s, maximum t.
	public float[][] coordinates = new float[9][4];

	// Texture atlas properties (all the textures are embedded in one PNG file).
	private static final int TEXTURE_DRAWABLE = R.drawable.atlas;
	private static final int ATLAS_WIDTH = 1024;
	private static final int ATLAS_HEIGHT = 2048;

	// Left, top, width, height, all in pixels.
	private static final int[] TEXTURE_DATA = {//
	0, 0, 1024, 27,// Status bar.
			0, 28, 1024, 500,// Feature graphic.
			0, 529, 1024, 51,// Footer bar.
			0, 581, 820, 479,// Details pane.
			0, 1061, 820, 283,// Ratings pane.
			0, 1345, 938, 341,// Screen shots.
			0, 1687, 1024, 60,// Action bar icons.
			821, 581, 86, 86,// Play button.
			1023, 581, 1, 60 };// Alternate action bar green background.

	// OpenGL texture reference.
	private int[] texture = new int[1];

	public Texture(Context context) {
		int[] pixels = loadBitmapAsPixels(context);
		argbToAbgr(pixels);
		createGlTexture(pixels);
		generateTextureCoordinates();
	}

	public void bindTexture() {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
	}

	private int[] loadBitmapAsPixels(Context context) {
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				TEXTURE_DRAWABLE);
		int[] pixels = new int[ATLAS_WIDTH * ATLAS_HEIGHT];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
				bitmap.getHeight());
		bitmap.recycle();
		return pixels;
	}

	private void argbToAbgr(int[] pixels) {
		int length = pixels.length;
		for (int i = 0; i < length; i++) {
			int red = (pixels[i] >> 16) & 0xff;
			int green = (pixels[i] >> 8) & 0xff;
			int blue = pixels[i] & 0xff;
			int alpha = pixels[i] & 0xff000000;
			pixels[i] = alpha | (blue << 16) | (green << 8) | red;
		}
	}

	private void createGlTexture(int[] pixels) {
		GLES20.glGenTextures(1, texture, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
		IntBuffer intBuffer = IntBuffer.wrap(pixels);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
				ATLAS_WIDTH, ATLAS_HEIGHT, 0, GLES20.GL_RGBA,
				GLES20.GL_UNSIGNED_BYTE, intBuffer);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
	}

	private void generateTextureCoordinates() {
		for (int i = 0; i < coordinates.length; i++) {
			float minS = (float) TEXTURE_DATA[i * 4 + 0] / ATLAS_WIDTH;
			float minT = (float) TEXTURE_DATA[i * 4 + 1] / ATLAS_HEIGHT;
			float maxS = (float) (TEXTURE_DATA[i * 4 + 0] + TEXTURE_DATA[i * 4 + 2])
					/ ATLAS_WIDTH;
			float maxT = (float) (TEXTURE_DATA[i * 4 + 1] + TEXTURE_DATA[i * 4 + 3])
					/ ATLAS_HEIGHT;
			coordinates[i][0] = minS;
			coordinates[i][1] = minT;
			coordinates[i][2] = maxS;
			coordinates[i][3] = maxT;
		}
	}
}
