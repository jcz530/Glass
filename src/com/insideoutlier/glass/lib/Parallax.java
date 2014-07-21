package com.insideoutlier.glass.lib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.insideoutlier.glass.R;

/**
 * 
 * @author Joe Czubiak
 * @version 1.0
 */
public class Parallax {
    private Matrix mMatrix;
    private static ImageView iv;
    private static FrameLayout fl;
    private static String tag = "Parallax";
    private float CurrentAlpha = 1f;
    private double CurrentScale = 1.0;
    // default variables
    public static final double DEFAULT_SCALE = 1.1;
    public static final String DEFAULT_DIM_COLOR = "#88111111";
    public static final double DEFAULT_RESPONSIVENESS_MULTIPLIER = 10.0;

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

    private int newImgWidth;
    private int newImgHeight;
    private int containerWidth;
    private int containerHeight;

    /**
     * Constructs Parallax object.
     * 
     * @param ctx
     *            Context of activity Parallax is being used in.
     * @param frameLayout
     *            FrameLayout. This should be the top level layout that you are
     *            using.
     * @param drawable
     *            Drawable. The image you want to use as the background.
     */
    public Parallax(Context ctx, FrameLayout frameLayout, Drawable drawable) {
        this(ctx, frameLayout, drawable, Parallax.SCALE);
    }

    /**
     * Constructs Parallax object.
     * 
     * @param ctx
     *            Context of activity Parallax is being used in.
     * @param frameLayout
     *            FrameLayout. This should be the top level layout that you are
     *            using.
     * @param imageview
     *            . ImageView. Optionally you can supply your own ImageView.
     *            Parallax assumes that it is placed directly under your
     *            FrameLayout.
     */
    public Parallax(Context ctx, FrameLayout frameLayout, ImageView imageview) {
        this(ctx, frameLayout, null, Parallax.SCALE, imageview);
    }

    /**
     * Constructs Parallax object.
     * 
     * @param ctx
     *            Context of activity Parallax is being used in.
     * @param frameLayout
     *            FrameLayout. This should be the top level layout that you are
     *            using.
     * @param drawable
     *            Drawable. The image you want to use as the background.
     * @param scale
     *            Double. The image needs to be scaled larger than the screen
     *            size. A bigger scale will allow for more movement.
     */
    public Parallax(Context ctx, FrameLayout frameLayout, Drawable drawable,
            Double scale) {
        this(ctx, frameLayout, drawable, scale, null);

    }

    /**
     * Constructs Parallax object.
     * 
     * @param ctx
     *            Context of activity Parallax is being used in.
     * @param container
     *            FrameLayout. This should be the top level layout that you are
     *            using.
     * @param drawable
     *            Drawable. The image you want to use as the background.
     * @param scale
     *            Double. The image needs to be scaled larger than the screen
     *            size. A bigger scale will allow for more movement.
     * @param imageview
     *            . ImageView. Optionally you can supply your own ImageView.
     *            Parallax assumes that it is placed directly under your
     *            FrameLayout.
     */
    public Parallax(Context ctx, final FrameLayout container, Drawable drawable,
            final Double scale, ImageView imageview) {
        context = ctx;
        fl = container;
        try {
            Glass_Context = Parallax.context;
            if (imageview == null) {
                iv = new ImageView(ctx);
                // context.getResources().getIdentifier(null, null, null)
                iv.setId(R.id.glass_imageview);
                iv.setImageDrawable(drawable);
                addGlassImage(container);
            } else {
                iv = imageview;
            }
        } catch (Exception e) {
            Log.e(tag, "e " + e.toString());
        }

        mMatrix = new Matrix();
        mMatrix.reset();
        iv.setImageMatrix(mMatrix);
        iv.setScaleType(ScaleType.MATRIX);

        // size to image size ratio
        // scale(Scale);

        container.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // size to image size ratio
                        scale(container, scale);
                        container.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });

        mSensorManager = (SensorManager) ctx
                .getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    /**
     * This makes Parallax recognize and use the phone's rotations. This should
     * be called after creating the Parallax object and in onResume()
     */
    public void start() {
        mSensorManager.registerListener(mySensorEventListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * This tells Parallax to stop listening to the phone's rotations. This
     * should be called in onPause() or any other time the app or activity is no
     * longer being used. Failure to use stop() will result in the app
     * continually listening for the phone's movements even when the app is
     * closed. This will hurt the user's battery life significantly.
     * 
     */
    public void stop() {
        mSensorManager.unregisterListener(mySensorEventListener);
    }

    /**
     * Set the size of your background image. To achieve the glass effect your
     * background image needs to be bigger than the screen's size. This will
     * scale your image to your desired scale based on the screen size.
     * 
     * @param container
     * @param scale
     */
    public void scale(FrameLayout container, double scale) {

        // These are the real unmodified image size
        double imgHeight = iv.getDrawable().getIntrinsicHeight();
        double imgWidth = iv.getDrawable().getIntrinsicWidth();
        Log.d(tag, "image size (width x height): " + imgWidth + "x" + imgHeight);

        containerWidth = container.getWidth();
        containerHeight = container.getHeight();
        double perfectWidth = containerWidth * scale;
        double perfectHeight = containerHeight * scale;
        Log.d(tag, "container size (width x height): " + containerWidth + "x"
                + containerHeight);
        Log.d(tag, "perfect img size (width x height): " + perfectWidth + "x"
                + perfectHeight);

        double imageProportion = imgWidth / imgHeight;
        Log.d(tag, "image proportion : " + imageProportion);

        double newImgWidthByWidth = perfectWidth;
        double newImgHeightByWidth = perfectWidth * imgHeight / imgWidth;
        Log.d(tag, "scale by width (width x height): " + newImgWidthByWidth
                + "x" + newImgHeightByWidth);

        double newImgHeightByHeight = perfectHeight;
        double newImgWidthByHeight = perfectHeight * imgWidth / imgHeight;
        Log.d(tag, "scale by height (width x height): " + newImgWidthByHeight
                + "x" + newImgHeightByHeight);

        // Bigger one will be used since is a less distortion img
        newImgWidth = (int) (newImgWidthByWidth > newImgWidthByHeight ? newImgWidthByWidth
                : newImgWidthByHeight);
        newImgHeight = (int) (newImgWidthByWidth > newImgWidthByHeight ? newImgHeightByWidth
                : newImgHeightByHeight);

        Log.d(tag, "new image size(width x height): " + newImgWidth + "x"
                + newImgHeight);

        Parallax.SCALE = newImgWidthByWidth < newImgWidthByHeight ? newImgWidth
                / imgWidth : newImgHeight / imgHeight;
        Log.d(tag, "image scale: " + Parallax.SCALE);
        mMatrix.setScale((float) Parallax.SCALE, (float) Parallax.SCALE);

        mMatrix.postTranslate((float) -((newImgWidth - containerWidth) / 2),
                (float) -((newImgHeight - containerHeight) / 2));
        Parallax.TOTAL_TRANSLATION_X = 0;
        Parallax.TOTAL_TRANSLATION_Y = 0;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            iv.setAlpha(alpha);
        iv.setImageMatrix(mMatrix);
        iv.invalidate();
    }

    /**
     * This darkens the background image with our default color value.
     */
    public void dimLights() {
        if (fl != null)
            dimLights(Parallax.DEFAULT_DIM_COLOR);
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
        Parallax.RESPONSIVENESS_MULTIPLIER_X = responsiveness;
        Parallax.RESPONSIVENESS_MULTIPLIER_Y = responsiveness;
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
        Parallax.RESPONSIVENESS_MULTIPLIER_X = responsiveness;
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
        Parallax.RESPONSIVENESS_MULTIPLIER_Y = responsiveness;

    }

    /**
     * This sets the X and Y axis responsiveness.
     * 
     * Default is 5. 0 = no movement. Negative numbers invert movements. Larger
     * numbers = larger movements.
     * 
     * @param x
     *            Double. X axis responsiveness
     * @param y
     *            Double. Y axis responsiveness
     */
    public void setResponsiveness(double x, double y) {
        Parallax.RESPONSIVENESS_MULTIPLIER_X = x;
        Parallax.RESPONSIVENESS_MULTIPLIER_Y = y;

    }

    /**
     * 
     * @return Double. Responsiveness of X axis
     */
    public double getResponsivenessX() {
        return Parallax.RESPONSIVENESS_MULTIPLIER_X;
    }

    /**
     * 
     * @return Double. Responsiveness of Y axis
     */
    public double getResponsivenessY() {
        return Parallax.RESPONSIVENESS_MULTIPLIER_Y;
    }

    /**
     * 
     * @return Double. The current scale of the background image.
     */
    public double getScale() {
        return Parallax.SCALE;
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
        Parallax.STRICT_BOUNDS = strict;
    }

    /**
     * 
     * @return Boolean. True is StrictBounds is set. Default: true.
     */
    public boolean hasStrictBounds() {
        return Parallax.STRICT_BOUNDS;
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

        if (Parallax.STRICT_BOUNDS) {
            if (Math.abs(Parallax.TOTAL_TRANSLATION_X + x) > Math
                    .abs((newImgWidth - containerWidth) / 2))
                x = 0;
            if (Math.abs(Parallax.TOTAL_TRANSLATION_Y + y) > Math
                    .abs((newImgHeight - containerHeight) / 2))
                y = 0;
        }

        Parallax.TOTAL_TRANSLATION_X += x;
        Parallax.TOTAL_TRANSLATION_Y += y;
        mMatrix.postTranslate((float) (x / RESPONSIVENESS_MULTIPLIER_X),
                (float) (y / RESPONSIVENESS_MULTIPLIER_Y));
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

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Log.d(tag, "onSensorChanged called");
            if (timestamp != 0) {
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                Log.d(tag, "X = " + axisX + " Y = " + axisY + " Z = " + axisZ);

                translate(
                        (int) Math.round(axisY
                                * Parallax.RESPONSIVENESS_MULTIPLIER_Y),
                        (int) Math.round(axisX
                                * Parallax.RESPONSIVENESS_MULTIPLIER_X));
                translate(
                        (int) Math.round(axisZ
                                * Parallax.RESPONSIVENESS_MULTIPLIER_Y),
                        (int) Math.round(axisZ
                                * Parallax.RESPONSIVENESS_MULTIPLIER_X));

            }
            timestamp = event.timestamp;
        }

    };

}
