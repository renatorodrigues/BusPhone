package edu.feup.busphone.passenger.ui;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.util.qrcode.Contents;
import edu.feup.busphone.passenger.util.qrcode.QRCodeEncoder;

public class ShowTicketActivity extends Activity {
    private static final String TAG = "ShowTicketActivity";

    public static final String EXTRA_TICKET_TYPE = "ticket_type";

    ImageView qr_code_image_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_ticket_activity);

        WindowManager window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = window_manager.getDefaultDisplay();
        int display_width = display.getWidth();
        int display_height = display.getHeight();

        qr_code_image_  = (ImageView) findViewById(R.id.qr_code_image);

        int ticket_type = getIntent().getIntExtra(EXTRA_TICKET_TYPE, -1);
        if (ticket_type == -1) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String data = BluetoothAdapter.getDefaultAdapter().getAddress();
        Log.d(TAG, "Bluetooth MAC Address: " + data);

        int dimension = display_width < display_height ? display_width : display_height; /* smaller dimension */
        String type = Contents.Type.TEXT;
        String format = BarcodeFormat.QR_CODE.toString();

        try {
            QRCodeEncoder qr_code_encoder = new QRCodeEncoder(data, null, type, format, dimension);

            Bitmap bitmap = qr_code_encoder.encodeAsBitmap();
            qr_code_image_.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_ticket, menu);
        return true;
    }

}
