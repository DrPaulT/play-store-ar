package uk.co.drdv.examples.playlistingar;

import uk.co.drdv.examples.playlistingar.opengl.ListingRenderer;
import uk.co.drdv.examples.playlistingar.opengl.VuforiaGlView;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class MainActivity extends VuforiaActivity implements OnClickListener {

	protected VuforiaGlView vuforiaGlView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		keepScreenOn();
		setContentView(R.layout.activity_main);
	}

	private void keepScreenOn() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onClick(View v) {
		buildImageTarget();
		findViewById(R.id.camera_button).setVisibility(View.GONE);
	}

	@Override
	protected void onPause() {
		vuforiaGlView.onPause();
		super.onPause();
	}

	@Override
	public void vuforiaInitialised() {
		vuforiaGlView = new VuforiaGlView(this);
		listingRenderer = new ListingRenderer(this);
		vuforiaGlView.setRenderer(listingRenderer);
		((RelativeLayout) findViewById(R.id.camera_overlay_layout)).addView(
				vuforiaGlView, 0);
		findViewById(R.id.camera_button).setOnClickListener(this);
	}

}
