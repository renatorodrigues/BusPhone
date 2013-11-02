package edu.feup.busphone.passenger.util;

import android.os.Handler;

public abstract class WebServiceCallRunnable implements Runnable {
    protected Handler handler_;

    public WebServiceCallRunnable(Handler handler) {
        handler_ = handler;
    }
}
