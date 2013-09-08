package com.insideoutlier.glass.lib;

import com.insideoutlier.glass.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 
 * @author Joe Czubiak
 * @version 1.0
 */
public class Glass {
	private Matrix mMatrix;
	private static ImageView iv;
	private static FrameLayout fl;
	private static String tag = "Glass";
	private float CurrentAlpha = 1f;
	private double CurrentScale = 1.0;
	// default variables
	public static final double DEFAULT_SCALE = 2;
	public static final String DEFAULT_DIM_COLOR = "#88111111";
	public static final double DEFAULT_RESPONSIVENESS_MULTIPLIER = 5.0;

	// private and modifiable fields
	private static Boolean STRICT_BOUNDS = true;
	private static int TOTAL_TRANSLATION_X = 0;
	private static int TOTAL_TRANSLATION_Y = 0;

	// public CURRENT and modifiable fields
	public static double RESPONSIVENESS_MULTIPLIER_X = DEFAULT_RESPONSIVENESS_MULTIPLIER;
	public static double RESPONSIVENESS_MULTIPLIER_Y = DEFAULT_RESPONSIVENESS_MULTIPLIER;

	public static double SCALE = DEFAULT_SCALE;

	// GYRO
	private float timestamp;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private static Context context;;
	private Context Glass_Context;

	/**
	 * Constructs Glass object.
	 * 
	 * @param ctx
	 *            Context of activity Glass is being used in.
	 * @param frame_layout
	 *            FrameLayout. This should be the top level layout that you are
	 *            using.
	 * @param drawable
	 *            Drawable. The image you want to use as the background.
	 */
	public Glass(Context ctx, FrameLayout frame_layout, Drawable drawable) {
		this(ctx, frame_layout, drawable, Glass.SCALE);
	}

	/**
	 * Constructs Glass object.
	 * 
	 * @param ctx
	 *            Context of activity Glass is being used in.
	 * @param frame_layout
	 *            FrameLayout. This should be the top level layout that you are
	 *            using.
	 * @param imageview
	 *            . ImageView. Optionally you can supply your own ImageView.
	 *            Glass assumes that it is placed directly under your
	 *            FrameLayout.
	 */
	public Glass(Context ctx, FrameLayout frame_layout, ImageView imageview) {
		this(ctx, frame_layout, null, Glass.SCALE, imageview);
	}

	/**
	 * Constructs Glass object.
	 * 
	 * @param ctx
	 *            Context of activity Glass is being used in.
	 * @param frame_layout
	 *            FrameLayout. This should be the top level layout that you are
	 *            using.
	 * @param drawable
	 *            Drawable. The image you want to use as the background.
	 * @param Scale
	 *            Double. The image needs to be scaled larger than the screen
	 *            size. A bigger scale will allow for more movement.
	 */
	public Glass(Context ctx, FrameLayout frame_layout, Drawable drawable,
			Double Scale) {
		this(ctx, frame_layout, drawable, Glass.SCALE, null);

	}

	/**
	 * Constructs Glass object.
	 * 
	 * @param ctx
	 *            Context of activity Glass is being used in.
	 * @param frame_layout
	 *            FrameLayout. This should be the top level layout that you are
	 *            using.
	 * @param drawable
	 *            Drawable. The image you want to use as the background.
	 * @param Scale
	 *            Double. The image needs to be scaled larger than the screen
	 *            size. A bigger scale will allow for more movement.
	 * @param imageview
	 *            . ImageView. Optionally you can supply your own ImageView.
	 *            Glass assumes that it is placed directly under your
	 *            FrameLayout.
	 */
	public Glass(Context ctx, FrameLayout frame_layout, Drawable drawable,
			Double Scale, ImageView imageview) {
		context = ctx;
		fl = frame_layout;
		try {
			Glass_Context = Glass.context;
			if (imageview == null) {
				iv = new ImageView(ctx);
//				context.getResources().getIdentifier(null, null, null)
				iv.setId(R.id.glass_imageview);
				iv.setImageDrawable(drawable);
				addGlassImage(frame_layout);
			} else {
				iv = imageview;
			}
		} catch (Exception e) {
			Log.e(tag, "e " + e.toString());
		}
		mMatrix = new Matrix();
		iv.setImageMatrix(mMatrix);
		iv.setScaleType(ScaleType.MATRIX);
		// TODO: possibly calculate how much it needs to scale based on screen
		// size to image size ratio
		scale(Scale);

		mSensorManager = (SensorManager) ctx
				.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	}

	/**
	 * This makes Glass recognize and use the phone's rotations. This should be
	 * called after creating the Glass object and in onResume()
	 */
	public void start() {
		mSensorManager.registerListener(mySensorEventListener, mSensor,
				SensorManager.SENSOR_DELAY_UI);
	}

	/**
	 * This tells Glass to stop listening to the phone's rotations. This should
	 * be called in onPause() or any other time the app or activity is no longer
	 * being used. Failure to use stop() will result in the app continually
	 * listening for the phone's movements even when the app is closed. This
	 * will hurt the user's battery life significantly.
	 * 
	 */
	public void stop() {
		mSensorManager.unregisterListener(mySensorEventListener);
	}

	/**
	 * Set the size of your background image. To achieve the glass effect your
	 * background image needs to be bigger than the screen's size. This will
	 * scale your image to your desired scale.
	 * 
	 * @param scale
	 *            Double. 1 = imagesize. 2 = double imagesize. So on.
	 */
	public void scale(double scale) {
		Display display = getDisplay();
		if (CurrentScale != 1.0) {
			// reset the scale first then scale so that it is not so confusing
			mMatrix.preScale((float) (1.0 / CurrentScale),
					(float) (1.0 / CurrentScale),
					(float) (display.getWidth() / 5),
					(float) (display.getHeight() / 5));
			CurrentScale = 1.0;
			scale(scale);
		}
		Glass.SCALE = scale;
		mMatrix.setScale((float) scale, (float) scale);
		// Rect r = iv.getDrawable().getBounds();
		// Log.d(tag, "L: " + r.left + " R: " + r.right + " T: " + r.top +
		// " B: "+ r.bottom);

		double img_height = iv.getDrawable().getIntrinsicHeight() * Glass.SCALE;
		double img_width = iv.getDrawable().getIntrinsicWidth() * Glass.SCALE;

		int screen_width = getDisplay().getWidth();
		int screen_height = getDisplay().getHeight();

		mMatrix.postTranslate((float) -((img_width - screen_width) / 2),
				(float) -((img_height - screen_height) / 2));
		Glass.TOTAL_TRANSLATION_X = 0;
		Glass.TOTAL_TRANSLATION_Y = 0;

		FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		iv.setLayoutParams(frameParams);
		iv.setImageMatrix(mMatrix);
		iv.invalidate();
	}

	/**
	 * This allows you to set the opacity of the image, from 0f (transparent) to
	 * 1f (visible). This could slow down performance and should be used
	 * sparingly.
	 * 
	 * @param alpha
	 *            Float. Between 0f and 1f
	 */
	public void setAlpha(float alpha) {
		CurrentAlpha = alpha;
		iv.setAlpha(alpha);
		iv.setImageMatrix(mMatrix);
		iv.invalidate();
	}

	/**
	 * This darkens the background image with our default color value.
	 */
	public void dimLights() {
		if (fl != null)
			dimLights(Glass.DEFAULT_DIM_COLOR);
		else
			Log.e(tag,
					"NullPointerException. Your FrameLayout may not have been set. Make sure you call the constructor before dimming lights.");
	}

	/**
	 * 
	 * @param color
	 *            String. You should use an alpha hex String. Ex "#88111111".
	 */
	public void dimLights(String color) {
		ImageView iv_dim = new ImageView(context);
		FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		iv_dim.setBackgroundColor(Color.parseColor(color));
		iv_dim.setLayoutParams(frameParams);
		fl.addView(iv_dim, 1);
	}

	/**
	 * This sets both X and Y responsiveness to same value.
	 * 
	 * Default is 5. 0 = no movement. Negative numbers invert movements. Larger
	 * numbers = larger movements.
	 * 
	 * @param responsiveness
	 *            Double.
	 */
	public void setResponsiveness(double responsiveness) {
		Glass.RESPONSIVENESS_MULTIPLIER_X = responsiveness;
		Glass.RESPONSIVENESS_MULTIPLIER_Y = responsiveness;
	}

	/**
	 * This sets the X axis responsiveness.
	 * 
	 * Default is 5. 0 = no movement. Negative numbers invert movements. Larger
	 * numbers = larger movements.
	 * 
	 * @param responsiveness
	 *            Double.
	 */
	public void setResponsivenessX(double responsiveness) {
		Glass.RESPONSIVENESS_MULTIPLIER_X = responsiveness;
	}

	/**
	 * This sets the Y axis responsiveness.
	 * 
	 * Default is 5. 0 = no movement. Negative numbers invert movements. Larger
	 * numbers = larger movements.
	 * 
	 * @param responsiveness
	 *            Double.
	 */
	public void setResponsivenessY(double responsiveness) {
		Glass.RESPONSIVENESS_MULTIPLIER_Y = responsiveness;

	}

	/**
	 * This sets the X and Y axis responsiveness.
	 * 
	 * Default is 5. 0 = no movement. Negative numbers invert movements. Larger
	 * numbers = larger movements.
	 * 
	 * @param x Double. X axis responsiveness
	 * @param y Double. Y axis responsiveness
	 */
	public void setResponsiveness(double x, double y) {
		Glass.RESPONSIVENESS_MULTIPLIER_X = x;
		Glass.RESPONSIVENESS_MULTIPLIER_Y = y;

	}
	/**
	 * 
	 * @return Double. Responsiveness of X axis
	 */
	public double getResponsivenessX() {
		// TODO:return Array
		return Glass.RESPONSIVENESS_MULTIPLIER_X;
	}
	/**
	 * 
	 * @return Double. Responsiveness of Y axis
	 */
	public double getResponsivenessY() {
		// TODO:return Array
		return Glass.RESPONSIVENESS_MULTIPLIER_Y;
	}
	
	/**
	 * 
	 * @return Double. The current scale of the background image.
	 */
	public double getScale() {
		return Glass.SCALE;
	}

	/**
	 * When strictBounds is true, (assuming the image is bigger than the screen)
	 * the edges of the image cannot come into view on the screen. It will stop
	 * moving the image when the edge of the image meets the edge of the screen.
	 * 
	 * @param strict
	 *            Default: True
	 */
	public void setStrictBounds(Boolean strict) {
		Glass.STRICT_BOUNDS = strict;
	}

	/**
	 * 
	 * @return Boolean. True is StrictBounds is set. Default: true.
	 */
	public boolean hasStrictBounds() {
		return Glass.STRICT_BOUNDS;
	}

	
	/********************* PRIVATE METHODS **********************/

	/**
	 * 
	 * @param fl
	 */
	private static void addGlassImage(FrameLayout fl) {
		try {
			// Log.d(tag, "frame");
			fl.addView(iv, 0);
		} catch (Exception e) {
			Log.e(tag, "e " + e.toString());
		}
	}

	private void translate(int x, int y) {
		// ImageView iv = (ImageView) findViewById(R.id.image);
		// TODO: need to limit how far it can translate determined by image size
		// of scaled image.

		if (Glass.STRICT_BOUNDS) {
			double img_height = iv.getDrawable().getIntrinsicHeight()
					* Glass.SCALE;
			double img_width = iv.getDrawable().getIntrinsicWidth()
					* Glass.SCALE;
			int screen_width = getDisplay().getWidth();
			int screen_height = getDisplay().getHeight();

			if (Math.abs(Glass.TOTAL_TRANSLATION_X + x) > Math
					.abs((img_width - screen_width) / 2))
				x = 0;
			if (Math.abs(Glass.TOTAL_TRANSLATION_Y + y) > Math
					.abs((img_height - screen_height) / 2))
				y = 0;
		}
		Glass.TOTAL_TRANSLATION_X += x;
		Glass.TOTAL_TRANSLATION_Y += y;
		mMatrix.postTranslate(x, y);
		iv.setImageMatrix(mMatrix);
		iv.invalidate();

	}

	
	private Display getDisplay() {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay();
	}

	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		// TODO: possibly make it more sensitive to slight movements and less sensitive
		// to faster movements
		@Override
		public void onSensorChanged(SensorEvent event) {
			Log.d(tag, "onSensorChanged called");
			if (timestamp != 0) {
				float axisX = event.values[0];
				float axisY = event.values[1];
				float axisZ = event.values[2];
				Log.d(tag, "X = " + axisX + " Y = " + axisY + " Z = " + axisZ);
				translate((int) (axisY * Glass.RESPONSIVENESS_MULTIPLIER_Y),
						(int) (axisX * Glass.RESPONSIVENESS_MULTIPLIER_X));
				translate((int) (axisZ * Glass.RESPONSIVENESS_MULTIPLIER_Y),
						(int) (axisZ * Glass.RESPONSIVENESS_MULTIPLIER_X));

				// }
			}
			timestamp = event.timestamp;
		}

	};

}
