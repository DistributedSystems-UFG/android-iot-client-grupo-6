package com.example.thermoledmobileclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.iotservice.IoTServiceGrpc;
import io.grpc.examples.iotservice.TemperatureJSON;
import io.grpc.examples.iotservice.TemperatureReply;
import io.grpc.examples.iotservice.TemperatureRequest;

public class TemperatureActivity extends AppCompatActivity {
    private String host, port, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        Intent intent = getIntent();
        String[] message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE).split(",");
        this.host = message[0];
        this.port = message[1];
        this.userId = message[2];

        new GetTemperature(this, this.host, this.port, this.userId).start();
    }

    private static class GetTemperature extends Thread{
        private final Activity activity;
        private final String host, port, userId;

        private GetTemperature(Activity activity, String host, String port, String userId) {
            this.activity = activity;
            this.host = host;
            this.port = port;
            this.userId = userId;
        }

        @Override
        public void run() {
            while (!activity.isDestroyed()) {
                new TemperatureActivity.GrpcTask(activity).execute(this.host, this.port,
                        this.userId, "tem1");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static class GrpcTask extends AsyncTask<String, Void, String> {
        private final WeakReference<Activity> activityReference;
        private ManagedChannel channel;
        private List<TemperatureJSON> listTemperatures;

        private GrpcTask(Activity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            String host = params[0];
            String portStr = params[1];
            String userId = params[2];
            String sensorId = params[3];
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
                TemperatureRequest request = TemperatureRequest.newBuilder().setSensorId(sensorId)
                        .setUserId(userId).build();
                TemperatureReply reply = stub.sayTemperature(request);
                listTemperatures = reply.getTemperatureJSONList();
                return "OK";
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
                Thread.currentThread().interrupt();
            }
            if (!listTemperatures.get(0).getDate().equals("NA")) {
                TextView meanText = activity.findViewById(R.id.meanTemperature);
                TextView lastText = activity.findViewById(R.id.lastTemperature);
                float mean = calculateMean();
                String text = makeLastText();
                meanText.setText(String.format("%.2f", mean));
                lastText.setText(text);
            }
            else {
                activity.finish();
                Thread.currentThread().interrupt();
            }
        }

        private float calculateMean() {
            float soma = 0;
            int qtd = 0;
            for(TemperatureJSON temperature: listTemperatures) {
                soma = soma + temperature.getTemperature();
                qtd = qtd + 1;
            }
            return soma / qtd;
        }

        private String makeLastText() {
            int indice = 0;
            String text = "";
            for(TemperatureJSON temperature: listTemperatures) {
                indice = indice + 1;
                text = text + "Nº" + indice + "\n\t*Temperature: " +
                        temperature.getTemperature() + "\n\t*Date: " +
                        temperature.getDate() + "\n";
            }
            return text;
        }
    }
}
