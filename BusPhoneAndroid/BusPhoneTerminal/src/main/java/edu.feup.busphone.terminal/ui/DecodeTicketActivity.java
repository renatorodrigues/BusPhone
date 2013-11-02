package edu.feup.busphone.terminal.ui;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import edu.feup.busphone.terminal.R;
import edu.feup.busphone.terminal.util.CameraPreview;

public class DecodeTicketActivity extends Activity {
    private static final String TAG = "DecodeTicketActivity";

    private Camera camera_;
    private CameraPreview camera_preview_;
    private Handler auto_focus_handler_;

    private TextView scan_text_;
    private Button scan_button_;

    ImageScanner scanner_;

    private boolean barcode_scanned_ = false;
    private boolean previewing_ = true;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decode_ticket_activity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auto_focus_handler_ = new Handler();
        camera_ = getCameraInstance();

        scanner_ = new ImageScanner();
        scanner_.setConfig(0, Config.X_DENSITY, 3);
        scanner_.setConfig(0, Config.Y_DENSITY, 3);

        camera_preview_ = new CameraPreview(this, camera_, preview_callback, auto_focus_callback);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(camera_preview_);

        scan_text_ = (TextView) findViewById(R.id.scanText);

        scan_button_ = (Button) findViewById(R.id.ScanButton);
        scan_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcode_scanned_) {
                    barcode_scanned_ = false;
                    scan_text_.setText("Scanning...");
                    camera_.setPreviewCallback(preview_callback);
                    camera_.startPreview();
                    previewing_ = true;
                    camera_.autoFocus(auto_focus_callback);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    public static Camera getCameraInstance() {
        Camera camera = null;
        camera = Camera.open();
        return camera;
    }

    private void releaseCamera() {
        if (camera_ != null) {
            previewing_ = false;
            camera_.setPreviewCallback(null);
            camera_.release();
            camera_ = null;
        }
    }

    private Runnable auto_focus_runnable = new Runnable() {
        @Override
        public void run() {
            if (previewing_) {
                camera_.autoFocus(auto_focus_callback);
            }
        }
    };

    Camera.PreviewCallback preview_callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner_.scanImage(barcode);

            if (result != 0) {
                previewing_ = false;
                camera_.setPreviewCallback(null);
                camera_.stopPreview();

                SymbolSet symbols = scanner_.getResults();
                for (Symbol symbol : symbols) {
                    scan_text_.setText("barcode result " + symbol.getData());
                    barcode_scanned_ = true;
                }
            }
        }
    };

    Camera.AutoFocusCallback auto_focus_callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            auto_focus_handler_.postDelayed(auto_focus_runnable, 1000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.decode_ticket, menu);
        return true;
    }

}

