package com.example.q.cs496_week3;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatApplication extends Application {

    public String IPAddress = "http://52.79.200.191:4000/";
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(IPAddress);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
