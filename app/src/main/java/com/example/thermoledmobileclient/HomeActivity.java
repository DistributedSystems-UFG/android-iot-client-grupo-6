package com.example.thermoledmobileclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.iotservice.GetDeviceReply;
import io.grpc.examples.iotservice.GetDeviceRequest;
import io.grpc.examples.iotservice.IoTServiceGrpc;
import io.grpc.examples.iotservice.LedReply;
import io.grpc.examples.iotservice.LedRequest;

public class HomeActivity extends AppCompatActivity {
    private Button temperatureButton, ledOnButtonRed, ledOffButtonRed,
            ledOnButtonGreen, ledOffButtonGreen;
    private TextView temperature, ledRed, ledGreen;
    private String host, port, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        String[] message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE).split(",");
        this.host = message[0];
        this.port = message[1];
        this.userId = message[2];

        temperature = findViewById(R.id.temperatureTextView);
        temperatureButton = findViewById(R.id.temperatureButton);
        ledRed = findViewById(R.id.ledRedTextView);
        ledOnButtonRed = findViewById(R.id.ledOnButtonRed);
        ledOffButtonRed = findViewById(R.id.ledOffButtonrRed);
        ledGreen = findViewById(R.id.ledGreenTextView);
        ledOnButtonGreen = findViewById(R.id.ledOnButtonrGreen);
        ledOffButtonGreen = findViewById(R.id.ledOffButtonrGreen);
        new UIThread(this, this.host, this.port, this.userId).start();
    }

    public void getTemperature(View view) {
        Intent intent = new Intent(this, TemperatureActivity.class);
        String message = host + "," + port + "," + userId;
        intent.putExtra(LoginActivity.EXTRA_MESSAGE, message);
        this.startActivity(intent);
    }

    public void ledOnRequestRed(View view) {
        ledOnButtonRed.setEnabled(false);
        new GrpcTask2(this).execute(this.host, this.port, "led1", this.userId, "1");
    }

    public void ledOffRequestRed(View view) {
        ledOffButtonRed.setEnabled(false);
        new GrpcTask2(this).execute(this.host, this.port, "led1", this.userId, "0");
    }

    public void ledOnRequestGreen(View view) {
        ledOnButtonRed.setEnabled(false);
        new GrpcTask2(this).execute(this.host, this.port, "led2", this.userId, "1");
    }

    public void ledOffRequestGreen(View view) {
        ledOffButtonRed.setEnabled(false);
        new GrpcTask2(this).execute(this.host, this.port, "led2", this.userId, "0");
    }

    private class UIThread extends Thread {
        private Activity activity;
        private ManagedChannel channel;
        private String host, portStr, userId;
        private List<String> userDevices;

        private UIThread(Activity activity, String host, String port, String userId) {
            this.activity = activity;
            this.host = host;
            this.portStr = port;
            this.userId = userId;
        }

        @Override
        public void run() {
            while (!activity.isDestroyed()) {
                getUserDevices();
                for (String device: userDevices) {
                    System.out.println(device);
                }
                changeTempVisibility();
                changeLedRedVisibility();
                changeLedGreenVisibility();
            }
        }

        private void changeTempVisibility() {
            int visibility;
            if (userDevices.contains("tem1")) {
                visibility = View.VISIBLE;
            }
            else {
                visibility = View.GONE;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    temperature.setVisibility(visibility);
                    temperatureButton.setVisibility(visibility);
                }
            });
        }

        private void changeLedRedVisibility() {
            int visibility;
            if (userDevices.contains("led1")) {
                visibility = View.VISIBLE;
            }
            else {
                visibility = View.GONE;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ledRed.setVisibility(visibility);
                    ledOnButtonRed.setVisibility(visibility);
                    ledOffButtonRed.setVisibility(visibility);
                }
            });
        }

        private void changeLedGreenVisibility() {
            int visibility;
            if (userDevices.contains("led2")) {
                visibility = View.VISIBLE;
            }
            else {
                visibility = View.GONE;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ledGreen.setVisibility(visibility);
                    ledOnButtonGreen.setVisibility(visibility);
                    ledOffButtonGreen.setVisibility(visibility);
                }
            });
        }

        private void getUserDevices() {
            try {
                int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
                channel = ManagedChannelBuilder.forAddress(this.host, port).usePlaintext().build();
                IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
                GetDeviceRequest request = GetDeviceRequest.newBuilder().setUserId(this.userId).build();
                GetDeviceReply reply = stub.getUserDevices(request);
                this.userDevices = reply.getDeviceIdList();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                this.userDevices = null;
            }
            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
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
            String ledId = params[2];
            String userId = params[3];
            int ledState = Integer.parseInt(params[4]);
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
                LedRequest request = LedRequest.newBuilder().setState(ledState)
                        .setUserId(userId).setLedId(ledId).build();
                LedReply reply = stub.blinkLed(request);
                return String.valueOf(reply.getLedstate());
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
                Button ledOnButton = (Button) activity.findViewById(R.id.ledOnButtonRed);
                ledOnButton.setEnabled(true);
                Button ledOnButtonGreen = (Button) activity.findViewById(R.id.ledOnButtonrGreen);
                ledOnButtonGreen.setEnabled(true);
            } else if (result.equals("0")){
                Button ledOffButton = (Button) activity.findViewById(R.id.ledOffButtonrRed);
                ledOffButton.setEnabled(true);
                Button ledOffButtonGreen = (Button) activity.findViewById(R.id.ledOffButtonrGreen);
                ledOffButtonGreen.setEnabled(true);
            }
            else {
                Toast myToast = Toast.makeText(activity, "You don't have access to this dispositive",
                        Toast.LENGTH_SHORT);
                myToast.show();
                Button ledOnButton = (Button) activity.findViewById(R.id.ledOnButtonRed);
                ledOnButton.setEnabled(true);
                Button ledOffButton = (Button) activity.findViewById(R.id.ledOffButtonrRed);
                ledOffButton.setEnabled(true);
                Button ledOnButtonGreen = (Button) activity.findViewById(R.id.ledOnButtonrGreen);
                ledOnButtonGreen.setEnabled(true);
                Button ledOffButtonGreen = (Button) activity.findViewById(R.id.ledOffButtonrGreen);
                ledOffButtonGreen.setEnabled(true);
            }

        }
    }
}
