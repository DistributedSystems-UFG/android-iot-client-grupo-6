package com.example.thermoledmobileclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.iotservice.IoTServiceGrpc;
import io.grpc.examples.iotservice.LedMessage;
import io.grpc.examples.iotservice.TemperatureReply;
import io.grpc.examples.iotservice.TemperatureRequest;

public class MainActivity extends AppCompatActivity {
    private TextView temperatureResultText;
    private EditText hostEdit;
    private EditText portEdit;
    private Button getTemperatureButton;
    private Button ledOnButton;
    private Button ledOffButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatureResultText = (TextView) findViewById(R.id.textViewTemperature);
        hostEdit = (EditText) findViewById(R.id.host);
        portEdit = (EditText) findViewById(R.id.port);
        getTemperatureButton = (Button) findViewById(R.id.getTempButton);
        ledOnButton = (Button) findViewById(R.id.ledOnButton);
        ledOffButton = (Button) findViewById(R.id.ledOffButton);

    }

    public void sendTemperatureRequest(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
        getTemperatureButton.setEnabled(false);
        temperatureResultText.setText("");
        new GrpcTask(this).execute(hostEdit.getText().toString(), portEdit.getText().toString(), "sensor01");
    }

    private static class GrpcTask extends AsyncTask<String, Void, String> {
        private final WeakReference<Activity> activityReference;
        private ManagedChannel channel;


        private GrpcTask(Activity activity) {
            this.activityReference = new WeakReference<Activity>(activity);
        }

        @Override
        protected String doInBackground(String... params) {

            String host = params[0];
            String portStr = params[1];
            String sensorName = params[2];
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
                TemperatureRequest request = TemperatureRequest.newBuilder().setSensorName(sensorName).build();
                TemperatureReply reply = stub.sayTemperature(request);
                return reply.getTemperature();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return String.format("Failed... : %n%s", sw);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Activity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            TextView temperatureResultText = (TextView) activity.findViewById(R.id.textViewTemperature);
            Button getTemperatureButton = (Button) activity.findViewById(R.id.getTempButton);
            temperatureResultText.setText(result);
            getTemperatureButton.setEnabled(true);
        }
    }

    public void ledOnRequest(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
        ledOnButton.setEnabled(false);
        new GrpcTask2(this).execute(hostEdit.getText().toString(), portEdit.getText().toString(), "1");
    }

    public void ledOffRequest(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
        ledOffButton.setEnabled(false);
        new GrpcTask2(this).execute(hostEdit.getText().toString(), portEdit.getText().toString(), "0");
    }

    private static class GrpcTask2 extends AsyncTask<String, Void, String> {
        private final WeakReference<Activity> activityReference;
        private ManagedChannel channel;


        private GrpcTask2(Activity activity) {
            this.activityReference = new WeakReference<Activity>(activity);
        }

        @Override
        protected String doInBackground(String... params) {

            String host = params[0];
            String portStr = params[1];
            String ledState = params[2];
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
                LedMessage request = LedMessage.newBuilder().setState(Integer.parseInt(ledState)).build();
                LedMessage reply = stub.blinkLed(request);
                return Integer.toString(reply.getState());
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return String.format("Failed... : %n%s", sw);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Activity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            if (result.equals("1")) {
                Button ledOnButton = (Button) activity.findViewById(R.id.ledOnButton);
                ledOnButton.setEnabled(true);
            } else {
                Button ledOffButton = (Button) activity.findViewById(R.id.ledOffButton);
                ledOffButton.setEnabled(true);
            }

        }
    }
}