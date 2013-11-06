package edu.feup.busphone.passenger.ui;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Activity;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import edu.feup.busphone.client.Ticket;
import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.client.TicketsWallet;
import edu.feup.busphone.passenger.util.qrcode.Contents;
import edu.feup.busphone.passenger.util.qrcode.QRCodeEncoder;

public class ShowValidatedTicketActivity extends Activity {
    private static final String TAG = "ShowValidatedTicketActivity";

    private ImageView qr_code_image_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.show_validated_ticket_activity);

        WindowManager window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = window_manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int display_width = size.x;
        int display_height = size.y;

        qr_code_image_ = (ImageView) findViewById(R.id.qr_code_image);

        Ticket validated_ticket = Passenger.getInstance().getTicketsWallet().getValidated();
        if (validated_ticket == null) {
            Toast.makeText(ShowValidatedTicketActivity.this, "No validated ticket to show", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String data = validated_ticket.getId();

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

}
