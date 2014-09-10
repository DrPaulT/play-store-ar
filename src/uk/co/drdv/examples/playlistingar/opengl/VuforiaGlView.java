package uk.co.drdv.examples.playlistingar.opengl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

public class VuforiaGlView extends GLSurfaceView {

	private float clickX;
	private ListingRenderer renderer;

	public VuforiaGlView(Context context) {
		super(context);
		initialise();
	}

	@Override
	public void onPause() {
		setVisibility(View.INVISIBLE);
		super.onPause();
	}

	@Override
	public void onResume() {
		setVisibility(View.VISIBLE);
		super.onResume();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			clickX = event.getX();
			return true;
		case MotionEvent.ACTION_MOVE:
			renderer.touchOffset = event.getX() - clickX;
			if (renderer.touchOffset + renderer.scrollOffset < 0) {
				renderer.touchOffset = -renderer.scrollOffset;
			}
			return true;
		case MotionEvent.ACTION_UP:
			renderer.scrollOffset += renderer.touchOffset;
			renderer.touchOffset = 0;
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void setRenderer(Renderer renderer) {
		this.renderer = (ListingRenderer) renderer;
		super.setRenderer(renderer);
	}

	private void initialise() {
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setEGLContextClientVersion(2);
		setEGLConfigChooser(5, 6, 5, 0, 16, 0);
	}
}
