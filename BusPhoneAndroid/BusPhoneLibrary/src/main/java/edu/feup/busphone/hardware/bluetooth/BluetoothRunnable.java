package edu.feup.busphone.hardware.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public abstract class BluetoothRunnable implements Runnable {
    protected Handler handler_;

    protected BluetoothSocket bluetooth_socket_;

    protected BluetoothRunnable(Handler handler) {
        handler_ = handler;
    }

    protected String receive() {
        try {
            InputStream input_stream = bluetooth_socket_.getInputStream();
            int available = 0;
            while ((available = input_stream.available()) == 0) {
                Thread.sleep(250);
            }
            byte[] buffer = new byte[available];
            input_stream.read(buffer);
            String response = new String(buffer);

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected boolean send(String message) {
        try {
            byte[] buffer = message.getBytes("UTF-8");
            OutputStream output_stream = bluetooth_socket_.getOutputStream();
            Thread.sleep(1000);
            output_stream.write(buffer);

            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
