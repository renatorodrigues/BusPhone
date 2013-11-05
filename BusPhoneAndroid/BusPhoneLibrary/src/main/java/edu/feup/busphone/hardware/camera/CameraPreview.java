package edu.feup.busphone.hardware.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder holder_;
    private Camera camera_;
    private Camera.PreviewCallback preview_callback_;
    private Camera.AutoFocusCallback auto_focus_callback_;

    public CameraPreview(Context context, Camera camera, Camera.PreviewCallback preview_callback, Camera.AutoFocusCallback autofocus_callback) {
        super(context);
        camera_ = camera;
        preview_callback_ = preview_callback;
        auto_focus_callback_ = autofocus_callback;

        /*Camera.Parameters parameters = camera_.getParameters();
        for (String f : parameters.getSupportedFocusModes()) {
            if (f.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                Log.d(TAG, "Camera supports continuous focus");
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
                auto_focus_callback_ = null;
                break;
            }
        }*/

        holder_ = getHolder();
        holder_.addCallback(this);

        holder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera_.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder_.getSurface() == null) {
            return;
        }

        camera_.stopPreview();

        camera_.setDisplayOrientation(90);
        try {
            camera_.setPreviewDisplay(holder_);
        } catch (IOException e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
        camera_.setPreviewCallback(preview_callback_);
        camera_.startPreview();
        camera_.autoFocus(auto_focus_callback_);
    }
}
