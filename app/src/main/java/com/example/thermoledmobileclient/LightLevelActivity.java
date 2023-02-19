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
import io.grpc.examples.iotservice.LightLevelJSON;
import io.grpc.examples.iotservice.LightLevelReply;
import io.grpc.examples.iotservice.LightLevelRequest;

public class LightLevelActivity extends AppCompatActivity {
    private String host, port, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_level);

        Intent intent = getIntent();
        String[] message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE).split(",");
        this.host = message[0];
        this.port = message[1];
        this.userId = message[2];

        new LightLevelActivity.GetLightLevel(this, this.host, this.port, this.userId).start();
    }

    public static class GetLightLevel extends Thread {
        private final Activity activity;
        private final String host, port, userId;

        private GetLightLevel(Activity activity, String host, String port, String userId) {
            this.activity = activity;
            this.host = host;
            this.port = port;
            this.userId = userId;
        }

        @Override
        public void run() {
            while (!activity.isDestroyed()) {
                new LightLevelActivity.GrpcTask(activity).execute(this.host, this.port,
                        this.userId, "lum1");
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
        private List<LightLevelJSON> listLightLevels;

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
                LightLevelRequest request = LightLevelRequest.newBuilder()
                        .setSensorId(sensorId).setUserId(userId).build();
                LightLevelReply reply = stub.sayLightLevel(request);
                listLightLevels = reply.getLightLevelJSONList();
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
            if (!listLightLevels.get(0).getDate().equals("NA")) {
                TextView meanText = activity.findViewById(R.id.mean);
                TextView lastText = activity.findViewById(R.id.lastLightLevel);
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
            int soma = 0;
            int qtd = 0;
            for(LightLevelJSON lightLevel: listLightLevels) {
                soma = soma + lightLevel.getLightlevel();
                qtd = qtd + 1;
            }
            return soma / qtd;
        }

        private String makeLastText() {
            int indice = 0;
            String text = "";
            for(LightLevelJSON lightLevel: listLightLevels) {
                indice = indice + 1;
                text = text + "Nº" + indice + "\n\t*Light Level: " +
                        lightLevel.getLightlevel() + "\n\t*Date: " +
                        lightLevel.getDate() + "\n";
            }
            return text;
        }
    }
}