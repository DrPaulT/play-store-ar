package uk.co.drdv.examples.playlistingar;

import uk.co.drdv.examples.playlistingar.opengl.ListingRenderer;
import android.app.Activity;
import android.os.Bundle;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;

public abstract class VuforiaActivity extends Activity implements
		UpdateCallbackInterface {

	private static final String USER_TARGET = "UserTarget";
	private static final float SCENE_WIDTH = 320.0f;

	public Object semaphore = new Object();
	public ListingRenderer listingRenderer;
	public DataSet dataSet = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new InitialiseVuforiaTask(this).execute();
	}

	@Override
	protected void onResume() {
		Vuforia.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		stopCamera();
		super.onPause();
		Vuforia.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			synchronized (semaphore) {
				destroyTrackerDataSet();
				deinitialiseTrackers();
				Vuforia.deinit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void QCAR_onUpdate(State s) {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) trackerManager
				.getTracker(ImageTracker.getClassType());
		if (listingRenderer.trackableSource != null) {
			imageTracker.deactivateDataSet(imageTracker.getActiveDataSet());
			dataSet.createTrackable(listingRenderer.trackableSource);
			listingRenderer.trackableSource = null;
			imageTracker.activateDataSet(dataSet);
		}
	}

	public void stopCamera() {
		stopTrackers();
		CameraDevice.getInstance().stop();
		CameraDevice.getInstance().deinit();
	}

	public void startTrackers() {
		Tracker imageTracker = TrackerManager.getInstance().getTracker(
				ImageTracker.getClassType());
		if (imageTracker != null)
			imageTracker.start();
	}

	public abstract void vuforiaInitialised();

	protected void buildImageTarget() {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) trackerManager
				.getTracker(ImageTracker.getClassType());
		ImageTargetBuilder targetBuilder = imageTracker.getImageTargetBuilder();
		targetBuilder.build(USER_TARGET, SCENE_WIDTH);
		listingRenderer.creatingTrackable = true;
	}

	private void stopTrackers() {
		Tracker imageTracker = TrackerManager.getInstance().getTracker(
				ImageTracker.getClassType());
		if (imageTracker != null)
			imageTracker.stop();
	}

	private void deinitialiseTrackers() {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) (trackerManager
				.getTracker(ImageTracker.getClassType()));
		ImageTargetBuilder targetBuilder = imageTracker.getImageTargetBuilder();
		if (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE) {
			targetBuilder.stopScan();
		}
		trackerManager.deinitTracker(ImageTracker.getClassType());
	}

	private void destroyTrackerDataSet() {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) trackerManager
				.getTracker(ImageTracker.getClassType());
		imageTracker.deactivateDataSet(dataSet);
		imageTracker.destroyDataSet(dataSet);
		dataSet = null;
	}

}
