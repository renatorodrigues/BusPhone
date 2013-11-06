package edu.feup.busphone.inspector.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import edu.feup.busphone.hardware.camera.CameraPreview;

public class CameraActivity extends Activity {
    static final String TAG = "CameraActivity";

    protected Camera camera_;
    protected CameraPreview camera_preview_;
    private Handler auto_focus_handler_;

    protected ImageScanner scanner_;

    protected boolean previewing_;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auto_focus_handler_ = new Handler();
        boolean camera_opened = safeCameraOpen();

        Log.d(TAG, Boolean.toString(camera_opened));

        previewing_ = true;

        scanner_ = new ImageScanner();
        scanner_.setConfig(0, Config.X_DENSITY, 3);
        scanner_.setConfig(0, Config.Y_DENSITY, 3);

        camera_preview_ = new CameraPreview(this, camera_, preview_callback_, auto_focus_callback_);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    // TODO: use camera id
    private boolean safeCameraOpen() {
        boolean opened = false;

        try {
            releaseCameraAndPreview();
            camera_ = Camera.open();
            opened = camera_ != null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Camera");
            e.printStackTrace();
        }

        return opened;
    }

    private void releaseCameraAndPreview() {
        previewing_ = false;
        if (camera_ != null) {
            camera_.setPreviewCallback(null);
            camera_.release();
        }
        camera_ = null;
    }

    protected void setPreviewEnabled(boolean enabled) {
        previewing_ = enabled;

        if (enabled) {
            camera_.setPreviewCallback(preview_callback_);
            camera_.startPreview();
            camera_.autoFocus(auto_focus_callback_);
        } else {
            camera_.setPreviewCallback(null);
            camera_.stopPreview();
        }
    }

    protected void releaseCamera() {
        if (camera_ != null) {
            previewing_ = false;
            camera_.setPreviewCallback(null);
            camera_.release();
            camera_ = null;
        }
    }

    private Runnable auto_focus_runnable_ = new Runnable() {
        @Override
        public void run() {
            if (previewing_) {
                camera_.autoFocus(auto_focus_callback_);
            }
        }
    };

    protected void onRead(String data) {

    }

    private Camera.PreviewCallback preview_callback_ = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner_.scanImage(barcode);

            if (result != 0) {
                setPreviewEnabled(false);

                SymbolSet symbols = scanner_.getResults();
                for (Symbol symbol : symbols) {
                    String scan_text = symbol.getData();

                    onRead(scan_text);
                }
            }
        }
    };

    private Camera.AutoFocusCallback auto_focus_callback_ = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            auto_focus_handler_.postDelayed(auto_focus_runnable_, 1000);
        }
    };
}
