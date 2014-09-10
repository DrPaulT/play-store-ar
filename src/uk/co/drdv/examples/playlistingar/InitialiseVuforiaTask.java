package uk.co.drdv.examples.playlistingar;

import android.os.AsyncTask;
import android.util.DisplayMetrics;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;

public class InitialiseVuforiaTask extends AsyncTask<Void, Integer, Void> {

	private int progress = -1;
	private VuforiaActivity vuforiaActivity;

	public InitialiseVuforiaTask(VuforiaActivity vuforiaActivity) {
		this.vuforiaActivity = vuforiaActivity;
	}

	protected Void doInBackground(Void... params) {
		synchronized (vuforiaActivity.semaphore) {
			Vuforia.setInitParameters(vuforiaActivity, Vuforia.GL_20);
			do {
				progress = Vuforia.init();
			} while (!isCancelled() && progress >= 0 && progress < 100);
			if (progress < 0) {
				vuforiaActivity.finish();
			} else {
				try {
					createTrackerDataSet();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	protected void onPostExecute(Void result) {
		Vuforia.registerCallback(vuforiaActivity);
		vuforiaActivity.vuforiaInitialised();
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) (trackerManager
				.getTracker(ImageTracker.getClassType()));
		ImageTargetBuilder targetBuilder = imageTracker.getImageTargetBuilder();
		targetBuilder.startScan();
		startCamera();
	}

	private void createTrackerDataSet() {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) trackerManager
				.initTracker(ImageTracker.getClassType());
		vuforiaActivity.dataSet = imageTracker.createDataSet();
		imageTracker.activateDataSet(vuforiaActivity.dataSet);
	}

	private void startCamera() {
		CameraDevice.getInstance().init(CameraDevice.CAMERA.CAMERA_DEFAULT);
		configureVideoBackground();
		CameraDevice.getInstance().selectVideoMode(
				CameraDevice.MODE.MODE_DEFAULT);
		CameraDevice.getInstance().start();
		Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
		vuforiaActivity.listingRenderer.setProjectionMatrix();
		CameraDevice.getInstance().setFocusMode(
				CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
	}

	private void configureVideoBackground() {
		DisplayMetrics metrics = new DisplayMetrics();
		vuforiaActivity.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		CameraDevice cameraDevice = CameraDevice.getInstance();
		VideoMode vm = cameraDevice
				.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

		VideoBackgroundConfig config = new VideoBackgroundConfig();
		config.setEnabled(true);
		config.setSynchronous(true);
		config.setPosition(new Vec2I(0, 0));

		int xSize = 0, ySize = 0;
		xSize = (int) (vm.getHeight() * (height / (float) vm.getWidth()));
		ySize = height;

		if (xSize < width) {
			xSize = width;
			ySize = (int) (width * (vm.getWidth() / (float) vm.getHeight()));
		}
		config.setSize(new Vec2I(xSize, ySize));
		Renderer.getInstance().setVideoBackgroundConfig(config);
	}
}