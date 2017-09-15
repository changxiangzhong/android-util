package no.hyper.androidutil.view.scalableimageview;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.Arrays;

class TransformationManager extends GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = TransformationManager.class.getSimpleName();
    private final View view;
    private final Matrix drawMatrix;

    private float lastFocusX;
    private float lastFocusY;

    private RectF subject;
    private RectF visibleArea;

    /* Temp variables. Avoid new in onScroll and etcs.*/
    private final RectF transformed = new RectF();
    private final float rawMatrix[] = new float[9];

    public TransformationManager(View view) {
        drawMatrix = new Matrix();
        this.view = view;
    }

    public Matrix getDrawMatrix() {
        return drawMatrix;
    }

    /**
     * Adjust the Bitmap position and with height when necessary
     *  1. Avoid to scale down the image too much. 'CenterInside' is the limitation
     *  2. Vertically, make sure one of the image boundaries coincide with the visibleArea'.
     *  Horizontally, as well.
     */
    private void settlement() {
        drawMatrix.getValues(rawMatrix);

        Log.v(TAG, "transform_matrix = " + Arrays.toString(rawMatrix));

        // If the image is smaller than centerInside, use centerInside .
        if (rawMatrix[0] < minScaleFactor()) {
            rawMatrix[0] = rawMatrix[4] = minScaleFactor();
            drawMatrix.setValues(rawMatrix);
            drawMatrix.mapRect(transformed, subject);
            float halfX = (visibleArea.width() - transformed.width()) / 2;
            float halfY = (visibleArea.height() - transformed.height()) / 2;
            rawMatrix[2] = halfX;
            rawMatrix[5] = halfY;
            drawMatrix.setValues(rawMatrix);
        }

        drawMatrix.mapRect(transformed, subject);

        Log.v(TAG, "transformedRect = " + transformed + ", visibleArea = " + visibleArea);

        float deltaLeft = transformed.left - visibleArea.left;
        float deltaRight = transformed.right - visibleArea.right;
        float deltaBottom = transformed.bottom - visibleArea.bottom;
        float deltaTop = transformed.top - visibleArea.top;

        Log.v(TAG, "deltaLeft = " + deltaLeft + ", deltaRight = " + deltaRight + ", deltaBottom = " + deltaBottom + ", deltaTop = " + deltaTop);

        // Handle horizontal
        if (deltaRight > 0 && deltaLeft > 0) { // Right boundary
            rawMatrix[2] -= (Math.min(deltaLeft, deltaRight));
        } else  if (deltaRight < 0 && deltaLeft < 0) { // Left boundary
            rawMatrix[2] += (Math.min(-deltaLeft, -deltaRight));
        }

        // Handle vertical
        if (deltaTop > 0 && deltaBottom > 0) {
            rawMatrix[5] -= (Math.min(deltaTop, deltaBottom));
        } else if (deltaTop < 0 && deltaBottom < 0) {
            rawMatrix[5] += (Math.min(-deltaTop, -deltaBottom));
        }

        drawMatrix.setValues(rawMatrix);

    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        drawMatrix.getValues(rawMatrix);
        // force settlement() to use "centerInside"
        rawMatrix[0] = rawMatrix[4] = -1f;
        drawMatrix.setValues(rawMatrix);
        settlement();
        view.invalidate();
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean isUpScaled() {
        // setWidthHeight not called yet.
        if (subject == null || visibleArea == null) {
            return false;
        }

        drawMatrix.getValues(rawMatrix);
        boolean isUpScaled = rawMatrix[0] > minScaleFactor();
        Log.v(TAG, "isUpScaled = " + isUpScaled + ", rawMatrix[0] = " + rawMatrix[0] + ", minScaleFactor = " + minScaleFactor());
        return isUpScaled;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!isUpScaled()) {// Disable scroll while the image is not scaled up.
            return false;
        }

        drawMatrix.postTranslate(-distanceX, -distanceY);
        settlement();
        view.invalidate();
        return true;
    }


    /* items defined in OnScaleGestureListener*/
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        lastFocusX = detector.getFocusX();
        lastFocusY = detector.getFocusY();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Matrix transformationMatrix = new Matrix();
        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();

        //Zoom focus is where the fingers are centered,
        transformationMatrix.postTranslate(-focusX, -focusY);

        Log.v(TAG, "scaleFactor = " + detector.getScaleFactor());

        transformationMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor());

/* Adding focus shift to allow for scrolling with two pointers down. Remove it to skip this functionality. This could be done in fewer lines, but for clarity I do it this way here */
        //Edited after comment by chochim
        float focusShiftX = focusX - lastFocusX;
        float focusShiftY = focusY - lastFocusY;
        transformationMatrix.postTranslate(focusX + focusShiftX, focusY + focusShiftY);
        drawMatrix.postConcat(transformationMatrix);

        settlement();

        lastFocusX = focusX;
        lastFocusY = focusY;
        view.invalidate();
        return true;
    }

    public void setWidthHeight(int subjectWidth, int subjectHeight, int visibleWidth, int visibleHeight) {
        subject = new RectF(0, 0, subjectWidth, subjectHeight);
        visibleArea = new RectF(0, 0, visibleWidth, visibleHeight);
        Log.v(TAG, "subject = " + subject);
        Log.v(TAG, "visibleArea = " + visibleArea);

        drawMatrix.getValues(rawMatrix);
        // force settlement() to use "centerInside"
        rawMatrix[0] = rawMatrix[4] = -1f;
        drawMatrix.setValues(rawMatrix);
        settlement();
    }

    private final float minScaleFactor() {
        float minScaleFactorX = (float) visibleArea.right / subject.right;
        float minScaleFactorY = (float) visibleArea.bottom / subject.bottom;

        // Log.v(TAG, "minScaleFactorX = " + minScaleFactorX + ", minScaleFactorY = " + minScaleFactorY);

        return Math.min(minScaleFactorX, minScaleFactorY);
    }
}
