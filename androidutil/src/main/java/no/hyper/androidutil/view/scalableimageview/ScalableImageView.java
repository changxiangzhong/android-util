package no.hyper.androidutil.view.scalableimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ScalableImageView extends View implements Target {
    private static final String TAG = ScalableImageView.class.getSimpleName();

    private Paint paint;
    private Bitmap bitmap;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector scrollDetector;
    private TransformationManager transformationManager;

    public ScalableImageView(Context context) {
        this(context, null);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            transformationManager = new TransformationManager(this);
            scaleDetector = new ScaleGestureDetector(context, transformationManager);
            scrollDetector = new GestureDetector(context, transformationManager);
            paint = new Paint();
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, transformationManager.getDrawMatrix(), paint);
        } else {
            Log.v(TAG, "Bitmap is null, return");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = false;
        if (bitmap != null) {
            ret |= scaleDetector.onTouchEvent(event);
            ret |= scrollDetector.onTouchEvent(event);
        }
        ret |=  super.onTouchEvent(event);
        return ret;
    }

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
        Log.v(TAG, "Image loaded! width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
        this.bitmap = bitmap;
        post(new Runnable() {
            @Override
            public void run() {
                transformationManager.setWidthHeight(bitmap.getWidth(), bitmap.getHeight(), getMeasuredWidth(), getMeasuredHeight());
                requestLayout();
                invalidate();
            }
        });
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.v(TAG, "Image loading failure");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        Log.v(TAG, "Image loading preparing");
    }

    public boolean isUpScaled() {
        return transformationManager.isUpScaled();
    }
}
