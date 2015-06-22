package com.example.sherman.phonetocomputer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by sherman on 6/20/2015.
 */
public class ConnectionManager {
    static ConnectionManager connectionManager;
    String ip, messageKey, ipKey; Handler handler;
    int port;

    public static ConnectionManager newInstance(String ip, Handler handler, Activity a) {
        ip = ip.trim();
        if (connectionManager == null) {
            ConnectionManager cm = new ConnectionManager();
            connectionManager = cm;
            cm.ip = ip;
            cm.handler = handler;
            cm.messageKey = a.getString(R.string.message_key);
            cm.ipKey = a.getString(R.string.ip_key);
            cm.port = Integer.parseInt(a.getString(R.string.port));
            return cm;
        }
        connectionManager.ip = ip;
        connectionManager.handler = handler;

        return connectionManager;
    }

    public boolean validateIP () {
        String[] quad = ip.split("\\.");
        if (quad.length != 4)
            return false;
        for (String s : quad) {
            try {
                int num = Integer.parseInt(s);
                if ((num > 255) || (num < 0))
                    return false;
            } catch (NumberFormatException nfe) {
                System.out.println("nfe");
                return false;
            }
        }
        return true;
    }

    public void connect(final String data) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                boolean completed = true;
                try {
                    Socket s = new Socket(InetAddress.getByName(ip), port);
                    OutputStream os = s.getOutputStream();
                    String output = Integer.toString(data.length()) + ":" + data;
                    os.write(output.getBytes());
                    os.close();
                } catch (UnknownHostException uhe) {
                    completed = false;
                    System.out.println(uhe.getMessage());
                } catch (IOException e) {
                    completed = false;
                    System.out.println(e.getMessage());
                }
                if (completed) {
                    Bundle bundle = new Bundle();
                    bundle.putString(messageKey, "");
                    bundle.putString(ipKey, ip);
                    Message m = handler.obtainMessage();
                    m.setData(bundle);
                    handler.handleMessage(m);
                } else
                    handler.handleMessage(null);
            }
        }); t.start();
    }

    public void connect() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                boolean completed = true;
                String data = "";
                try {
                    Socket s = new Socket(InetAddress.getByName(ip), port);
                    InputStream is = s.getInputStream();

                    char x;

                    while ((x = (char) is.read()) != ':')
                        data += x;

                    int size = Integer.parseInt(data);
                    byte[] buffer = new byte[size];
                    int bytesRead = is.read(buffer, 0, size);

                    if (bytesRead == size)
                        data = new String(buffer);
                    else
                        completed = false;
                } catch (UnknownHostException uhe) {
                    completed = false;
                    System.out.println(uhe.getMessage());
                } catch (IOException e) {
                    completed = false;
                    System.out.println(e.getMessage());
                } catch (NumberFormatException nfe) {
                    completed = false;
                    System.out.println(nfe.getMessage());
                }
                if (completed) {
                    Bundle bundle = new Bundle();
                    bundle.putString(messageKey, data);
                    bundle.putString(ipKey, ip);
                    Message m = handler.obtainMessage();
                    m.setData(bundle);
                    handler.handleMessage(m);
                } else
                    handler.handleMessage(null);
            }
        }); t.start();
    }
}
