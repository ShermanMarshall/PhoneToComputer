package com.example.sherman.phonetocomputer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    EditText data;
    boolean send;
    String ip;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = (EditText) findViewById(R.id.data);
        send = true;
        ip = null;
        handler = new Handler() {
            public void handleMessage(Message m) {
                ConfirmResult cr;
                if (m == null)
                    cr = ConfirmResult.newInstance(false);
                else {
                    String received = m.getData().getString(getString(R.string.message_key));
                    ip = m.getData().getString(getString(R.string.ip_key));
                    handleData(received);
                    if (received.equals(""))
                        cr = ConfirmResult.newInstance(true);
                    else
                        cr = ConfirmResult.newInstance(received);

                    cr = ConfirmResult.newInstance(true);
                }
                cr.show(getSupportFragmentManager(), getString(R.string.connection_status));
            }
        };
    }

    public void handleData(String received) {
        data.setText(received);
    }

    public void onRadioButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.send:
                send = true;
                break;
            case R.id.receive:
                send = false;
                data.setText("");
                break;
            default:
                data.setText(getString(R.string.radio_error));
                break;
        }
    }

    public void connect(View v) {
        PrepConnection pc;
        if (send)
             pc = PrepConnection.newInstance(data.getText().toString(), handler, ip);
        else
            pc = PrepConnection.newInstance(null, handler, ip);

        pc.show(getSupportFragmentManager(), getString(R.string.ip_dialog));
    }

    public static class ConfirmResult extends DialogFragment {
        boolean completed; String data = null;

        public static ConfirmResult newInstance (boolean completed) {
            ConfirmResult cr = new ConfirmResult();
            cr.completed = completed;
            return cr;
        }

        public static ConfirmResult newInstance (String data) {
            ConfirmResult cr = new ConfirmResult();
            cr.completed = true;
            cr.data = data;
            return cr;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.result_layout, null);
            TextView tv = (TextView) v.findViewById(R.id.resultStatus);
            TextView dataReceived = (TextView) v.findViewById(R.id.dataReceived);
            TextView receivedPrompt = (TextView) v.findViewById(R.id.receivedPrompt);

            if (completed)
                tv.setText(getString(R.string.complete));
            else
                tv.setText(getString(R.string.error));

            if (data == null)
                receivedPrompt.setVisibility(View.INVISIBLE);
            else {
                receivedPrompt.setVisibility(View.VISIBLE);
                dataReceived.setText(data);
            }

            AlertDialog d = new AlertDialog.Builder(getActivity())
                                .setView(v)
                                .setPositiveButton(R.string.acknowledge, null)
                                .create();
            return d;
        }
    }

    public static class PrepConnection extends DialogFragment {
        String data, ip; Handler handler;

        public static PrepConnection newInstance(String data, Handler handler, String ip) {
            PrepConnection pc = new PrepConnection();
            pc.data = data;
            pc.handler = handler;
            pc.ip = ip;
            return pc;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.input_ip, null);
            final TextView errorPrompt = (TextView) v.findViewById(R.id.errorPrompt);
            final EditText dataToSend = (EditText) v.findViewById(R.id.confirmData);
            final EditText ipInput = (EditText) v.findViewById(R.id.confirmIp);
                if (ip != null)
                    ipInput.setText(ip);
                if (data != null)
                    dataToSend.setText(data);

            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(R.string.connect, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new Button.OnClickListener(){
                public void onClick(View view) {
                    ConnectionManager cm = ConnectionManager.newInstance(ipInput.getText().toString(), handler, getActivity());
                    if (cm.validateIP()) {
                        if (data != null)
                            cm.connect(data);
                        else
                            cm.connect();
                        dialog.dismiss();
                    } else
                        errorPrompt.setText(getString(R.string.ip_invalid));
                }
            });
            return dialog;
        }
    }
}
