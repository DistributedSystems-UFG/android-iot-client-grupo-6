package com.example.thermoledmobileclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.iotservice.DeviceReply;
import io.grpc.examples.iotservice.DeviceRequest;
import io.grpc.examples.iotservice.GetDeviceReply;
import io.grpc.examples.iotservice.GetDeviceRequest;
import io.grpc.examples.iotservice.IoTServiceGrpc;
import io.grpc.examples.iotservice.LoginReply;
import io.grpc.examples.iotservice.LoginRequest;

public class AdminActivity extends AppCompatActivity {
    private Activity activity;
    private String host, port, adminId;
    private SwitchCompat temp1, light1, ledGreen1, ledRed1,
            temp2, light2, ledGreen2, ledRed2,
            temp3, light3, ledGreen3, ledRed3;
    private List<String> usersId = Arrays.asList("usr1", "usr2", "usr3");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        activity = this;

        Intent intent = getIntent();
        String[] message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE).split(",");
        this.host = message[0];
        this.port = message[1];
        this.adminId = message[2];

        temp1 = findViewById(R.id.switchtTemp1);
        light1 = findViewById(R.id.switchLight1);
        ledGreen1 = findViewById(R.id.switchLedGreen1);
        ledRed1 = findViewById(R.id.switchLedRed1);
        temp2 = findViewById(R.id.switchtTemp2);
        light2 = findViewById(R.id.switchLight2);
        ledGreen2 = findViewById(R.id.switchLedGreen2);
        ledRed2 = findViewById(R.id.switchLedRed2);
        temp3 = findViewById(R.id.switchtTemp3);
        light3 = findViewById(R.id.switchLight3);
        ledGreen3 = findViewById(R.id.switchLedGreen3);
        ledRed3 = findViewById(R.id.switchLedRed3);

        initalize();
        temp1.setOnCheckedChangeListener(new Handler());
        light1.setOnCheckedChangeListener(new Handler());
        ledGreen1.setOnCheckedChangeListener(new Handler());
        ledRed1.setOnCheckedChangeListener(new Handler());
        temp2.setOnCheckedChangeListener(new Handler());
        light2.setOnCheckedChangeListener(new Handler());
        ledGreen2.setOnCheckedChangeListener(new Handler());
        ledRed2.setOnCheckedChangeListener(new Handler());
        temp3.setOnCheckedChangeListener(new Handler());
        light3.setOnCheckedChangeListener(new Handler());
        ledGreen3.setOnCheckedChangeListener(new Handler());
        ledRed3.setOnCheckedChangeListener(new Handler());
    }

    // Funcao que pega todas as permissoes de dispositivos dos usuarios
    // para mostrar corretamente nos switch da activity
    private void initalize() {
        List<String> userDevices;
        // Um loop para configurar os switchs de cada usuario
        for (String userId : usersId) {
            userDevices = getUserDevice(userId);
            if (userDevices.contains("tem1")) {
                changeSwitchTemp(userId, true);
            }
            if (!userDevices.contains("tem1")) {
                changeSwitchTemp(userId, false);
            }
            if (userDevices.contains("lum1")) {
                changeSwitchLight(userId, true);
            }
            if (!userDevices.contains("lum1")) {
                changeSwitchLight(userId, false);
            }
            if (userDevices.contains("led1")) {
                changeSwitchLedRed(userId, true);
            }
            if (!userDevices.contains("led1")) {
                changeSwitchLedRed(userId, false);
            }
            if (userDevices.contains("led2")) {
                changeSwitchLedGreen(userId, true);
            }
            if (!userDevices.contains("led2")) {
                changeSwitchLedGreen(userId, false);
            }
        }
    }

    // Funcao que faz a requisicao GetUserDevice ao servidor gRPC
    private List<String> getUserDevice(String userId) {
        ManagedChannel channel;
        // Conexao com o servidor e realizacao da requisicao
        try {
            int portAux = TextUtils.isEmpty(port) ? 0 : Integer.parseInt(port);
            channel = ManagedChannelBuilder.forAddress(host, portAux).usePlaintext().build();
            IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
            GetDeviceRequest request = GetDeviceRequest.newBuilder().setUserId(userId).build();
            GetDeviceReply reply = stub.getUserDevices(request);
            // Encerrar a conexao com o servidor
            channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            // Retornando a lista de dispositivos do usuario
            return reply.getDeviceIdList();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            return null;
        }
    }

    // Funcao para trocar o switch da temperatura
    private void changeSwitchTemp(String userId, boolean checked) {
        switch (userId) {
            case "usr1":
                temp1.setChecked(checked);
                break;
            case "usr2":
                temp2.setChecked(checked);
                break;
            case "usr3":
                temp3.setChecked(checked);
                break;
        }
    }

    // Funcao para trocar o switch do nivel de luz
    private void changeSwitchLight(String userId, boolean checked) {
        switch (userId) {
            case "usr1":
                light1.setChecked(checked);
                break;
            case "usr2":
                light2.setChecked(checked);
                break;
            case "usr3":
                light3.setChecked(checked);
                break;
        }
    }

    // Funcao para trocar o switch do led vermelho
    private void changeSwitchLedRed(String userId, boolean checked) {
        switch (userId) {
            case "usr1":
                ledRed1.setChecked(checked);
                break;
            case "usr2":
                ledRed2.setChecked(checked);
                break;
            case "usr3":
                ledRed3.setChecked(checked);
                break;
        }
    }

    // Funcao para trocar o switch do led verde
    private void changeSwitchLedGreen(String userId, boolean checked) {
        switch (userId) {
            case "usr1":
                ledGreen1.setChecked(checked);
                break;
            case "usr2":
                ledGreen2.setChecked(checked);
                break;
            case "usr3":
                ledGreen3.setChecked(checked);
                break;
        }
    }

    // Classe handler para os switchs
    private class Handler implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            checkUser1();
            checkUser2();
            checkUser3();
        }

        // Funcao para ficar verificando os switchs do usuario 1
        private void checkUser1() {
            if (temp1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "tem1", "ADD");
            }
            if (!temp1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "tem1", "REM");
            }
            if (light1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "lum1", "ADD");
            }
            if (!light1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "lum1", "REM");
            }
            if (ledRed1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "led1", "ADD");
            }
            if (!ledRed1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "led1", "REM");
            }
            if (ledGreen1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "led2", "ADD");
            }
            if (!ledGreen1.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr1", "led2", "REM");
            }
        }

        // Funcao para ficar verificando os switchs do usuario 2
        private void checkUser2() {
            if (temp2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "tem1", "ADD");
            }
            if (!temp2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "tem1", "REM");
            }
            if (light2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "lum1", "ADD");
            }
            if (!light2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "lum1", "REM");
            }
            if (ledRed2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "led1", "ADD");
            }
            if (!ledRed2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "led1", "REM");
            }
            if (ledGreen2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "led2", "ADD");
            }
            if (!ledGreen2.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr2", "led2", "REM");
            }
        }

        // Funcao para ficar verificando os switchs do usuario 3
        private void checkUser3() {
            if (temp3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "tem1", "ADD");
            }
            if (!temp3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "tem1", "REM");
            }
            if (light3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "lum1", "ADD");
            }
            if (!light3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "lum1", "REM");
            }
            if (ledRed3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "led1", "ADD");
            }
            if (!ledRed3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "led1", "REM");
            }
            if (ledGreen3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "led2", "ADD");
            }
            if (!ledGreen3.isChecked()) {
                new GrpcTask(activity).execute(host, port, "usr3", "led2", "REM");
            }
        }
    }

    // Classe que faz a solicitacao AddUserDevice ou RemoveUserDevice
    // do servidor gRPC
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
            String userId = params[2];
            String deviceId = params[3];
            String operation = params[4];
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
            // Conexao com o servidor e realizacao da requisicao
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
                DeviceRequest request = DeviceRequest.newBuilder().setUserId(userId)
                        .setDeviceId(deviceId).build();
                DeviceReply reply;
                // Verificar se vai fazer uma requisicao de adicao ou remocao
                if (operation.equals("ADD")) {
                    reply = stub.addUserDevice(request);
                }
                else {
                    reply = stub.removeUserDevice(request);
                }
                return reply.getConfirmation();
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
            // Encerrar a conexao com o servidor
            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Verificar se a activity ainda esta ativa e retornar se nao estiver
            Activity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            // Verificar se ocorreu algum erro
            if (!result.equals("OK")) {
                Toast myToast = Toast.makeText(activity, result,
                        Toast.LENGTH_SHORT);
                myToast.show();
            }
        }
    }
}