package uk.co.drdv.examples.playlistingar.opengl;

import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.util.Log;

public class Shaders {

	private static final String SIMPLE_VERTEX = //
	""//
			+ "uniform mat4 u_mvpMatrix;                  \n"//
			+ "attribute vec4 a_position;                 \n"//
			+ "attribute vec4 a_colour;                   \n"//
			+ "varying vec4 v_colour;                     \n"//

			+ "void main() {                              \n"//
			+ "  gl_Position = u_mvpMatrix * a_position;  \n"//
			+ "  v_colour = a_colour;                     \n"//
			+ "}                                          \n";

	private static final String SIMPLE_FRAGMENT = //
	""//
			+ "precision mediump float;    \n"//
			+ "varying vec4 v_colour;      \n"//

			+ "void main() {               \n"//
			+ "  gl_FragColor = v_colour;  \n"//
			+ "}                           \n";

	private static final String TEXTURE_VERTEX = //
	""//
			+ "uniform mat4 u_mvpMatrix;                  \n" //
			+ "attribute vec4 a_position;                 \n" //
			+ "attribute vec2 a_texCoord;                 \n" //
			+ "varying vec2 v_texCoord;                   \n" //

			+ "void main() {                              \n" //
			+ "  gl_Position = u_mvpMatrix * a_position;  \n" //
			+ "  v_texCoord = a_texCoord;                 \n" //
			+ "}                                          \n";

	private static final String TEXTURE_FRAGMENT = //
	""//
			+ "precision mediump float;                            \n" //
			+ "uniform sampler2D s_texture;                        \n" //
			+ "varying vec2 v_texCoord;                            \n" //

			+ "void main(){                                        \n" //
			+ "  gl_FragColor = texture2D(s_texture, v_texCoord);  \n" //
			+ "}                                                   \n";

	private static final String TEXTURE_ALPHA_FRAGMENT = //
	""//
			+ "precision mediump float;                                      \n" //
			+ "uniform sampler2D s_texture;                                  \n" //
			+ "uniform float u_alpha;                                        \n" //
			+ "varying vec2 v_texCoord;                                      \n" //

			+ "void main(){                                                  \n" //
			+ "  gl_FragColor = texture2D(s_texture, v_texCoord) * u_alpha;  \n" //
			+ "}                                                             \n";

	private int simple;
	private int positionS;
	private int colourS;
	private int uMvpMatrixS;

	private int texture;
	private int positionT;
	private int textureArrayT;
	private int textureT;
	private int uMvpMatrixT;

	private int textureAlpha;
	private int positionTA;
	private int textureArrayTA;
	private int alphaTA;
	private int textureTA;
	private int uMvpMatrixTA;

	public Shaders() {
		createSimpleProgram();
		createTextureProgram();
		createTextureAlphaProgram();
	}

	public void setSimpleParameters(float[] mvpMatrix, FloatBuffer vcBuffer) {
		GLES20.glUseProgram(simple);
		GLES20.glEnableVertexAttribArray(positionS);
		GLES20.glEnableVertexAttribArray(colourS);
		GLES20.glUniformMatrix4fv(uMvpMatrixS, 1, false, mvpMatrix, 0);
		vcBuffer.position(0);
		GLES20.glVertexAttribPointer(positionS, 3, GLES20.GL_FLOAT, false, 28,
				vcBuffer);
		vcBuffer.position(3);
		GLES20.glVertexAttribPointer(colourS, 4, GLES20.GL_FLOAT, false, 28,
				vcBuffer);
	}

	public void clearSimpleParameters() {
		GLES20.glDisableVertexAttribArray(positionS);
		GLES20.glDisableVertexAttribArray(colourS);
	}

	public void setTextureParameters(float[] mvpMatrix, FloatBuffer vtBuffer) {
		GLES20.glUseProgram(texture);
		GLES20.glEnableVertexAttribArray(positionT);
		GLES20.glEnableVertexAttribArray(textureArrayT);
		GLES20.glUniformMatrix4fv(uMvpMatrixT, 1, false, mvpMatrix, 0);
		vtBuffer.position(0);
		GLES20.glVertexAttribPointer(positionT, 3, GLES20.GL_FLOAT, false, 20,
				vtBuffer);
		GLES20.glUniform1i(textureT, 0);
		vtBuffer.position(3);
		GLES20.glVertexAttribPointer(textureArrayT, 2, GLES20.GL_FLOAT, false,
				20, vtBuffer);
	}

	public void clearTextureParameters() {
		GLES20.glDisableVertexAttribArray(positionT);
		GLES20.glDisableVertexAttribArray(textureArrayT);
	}

	public void setTextureAlphaParameters(float[] mvpMatrix,
			FloatBuffer vtBuffer, float alpha) {
		GLES20.glUseProgram(textureAlpha);
		GLES20.glEnableVertexAttribArray(positionTA);
		GLES20.glEnableVertexAttribArray(textureArrayTA);
		GLES20.glUniformMatrix4fv(uMvpMatrixTA, 1, false, mvpMatrix, 0);
		vtBuffer.position(0);
		GLES20.glVertexAttribPointer(positionTA, 3, GLES20.GL_FLOAT, false, 20,
				vtBuffer);
		GLES20.glUniform1i(textureTA, 0);
		vtBuffer.position(3);
		GLES20.glVertexAttribPointer(textureArrayTA, 2, GLES20.GL_FLOAT, false,
				20, vtBuffer);
		GLES20.glUniform1f(alphaTA, alpha);
	}

	public void clearTextureAlphaParameters() {
		GLES20.glDisableVertexAttribArray(positionTA);
		GLES20.glDisableVertexAttribArray(textureArrayTA);
	}

	private void createSimpleProgram() {
		simple = createProgram(SIMPLE_VERTEX, SIMPLE_FRAGMENT);
		positionS = GLES20.glGetAttribLocation(simple, "a_position");
		colourS = GLES20.glGetAttribLocation(simple, "a_colour");
		uMvpMatrixS = GLES20.glGetUniformLocation(simple, "u_mvpMatrix");
		checkProgram(simple);
	}

	private void createTextureProgram() {
		texture = createProgram(TEXTURE_VERTEX, TEXTURE_FRAGMENT);
		positionT = GLES20.glGetAttribLocation(texture, "a_position");
		textureArrayT = GLES20.glGetAttribLocation(texture, "a_texCoord");
		textureT = GLES20.glGetUniformLocation(texture, "s_texture");
		uMvpMatrixT = GLES20.glGetUniformLocation(texture, "u_mvpMatrix");
		checkProgram(texture);
	}

	private void createTextureAlphaProgram() {
		textureAlpha = createProgram(TEXTURE_VERTEX, TEXTURE_ALPHA_FRAGMENT);
		positionTA = GLES20.glGetAttribLocation(textureAlpha, "a_position");
		textureArrayTA = GLES20.glGetAttribLocation(textureAlpha, "a_texCoord");
		alphaTA = GLES20.glGetUniformLocation(textureAlpha, "u_alpha");
		textureTA = GLES20.glGetUniformLocation(textureAlpha, "s_texture");
		uMvpMatrixTA = GLES20.glGetUniformLocation(textureAlpha, "u_mvpMatrix");
		checkProgram(textureAlpha);
	}

	private static int createProgram(String vertex, String fragment) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
		return program;
	}

	private static int loadShader(int type, String shaderSourceCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderSourceCode);
		GLES20.glCompileShader(shader);
		checkShader(shader);
		return shader;
	}

	private static void checkShader(int shader) {
		Log.i("GL",
				"Shader info:" + shader + " "
						+ GLES20.glGetShaderInfoLog(shader));
	}

	private static void checkProgram(int program) {
		Log.i("GL",
				"Program info:" + program + " "
						+ GLES20.glGetProgramInfoLog(program));
	}
}
