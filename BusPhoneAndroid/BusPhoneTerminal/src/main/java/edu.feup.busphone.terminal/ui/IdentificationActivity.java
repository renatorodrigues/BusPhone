package edu.feup.busphone.terminal.ui;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import edu.feup.busphone.terminal.R;
import edu.feup.busphone.terminal.client.Bus;
import edu.feup.busphone.terminal.util.qrcode.Contents;
import edu.feup.busphone.terminal.util.qrcode.QRCodeEncoder;

public class IdentificationActivity extends Activity {
    private static final String TAG = "IdentificationActivity";

    private ImageView qr_code_image_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.identification_activity);

        WindowManager window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = window_manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int display_width = size.x;
        int display_height = size.y;

        qr_code_image_ = (ImageView) findViewById(R.id.qr_code_image);

        String data = Bus.getInstance().getId();
        int dimension = display_width < display_height ? display_width : display_height; /* smaller dimension */
        String type = Contents.Type.TEXT;
        String format = BarcodeFormat.QR_CODE.toString();

        try {
            QRCodeEncoder qr_code_encoder = new QRCodeEncoder(data, null, type, format, dimension);
            Bitmap bitmap = qr_code_encoder.encodeAsBitmap();
            qr_code_image_.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, "Error encoding QR Code.");
            e.printStackTrace();
        }
    }
    
}
