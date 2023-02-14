package com.example.thermoledmobileclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.iotservice.IoTServiceGrpc;
import io.grpc.examples.iotservice.LoginReply;
import io.grpc.examples.iotservice.LoginRequest;

public class LoginActivity extends AppCompatActivity {
    private EditText hostEdit;
    private EditText portEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private Button loginButton;
    public static final String EXTRA_MESSAGE = "com.example.thermoledmobileclient.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        hostEdit = findViewById(R.id.host);
        portEdit = findViewById(R.id.port);
        usernameEdit = findViewById(R.id.username);
        passwordEdit = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
    }

    public void sendLoginRequest(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
        loginButton.setEnabled(false);
        new GrpcTask(this).execute(hostEdit.getText().toString(), portEdit.getText().toString(),
                usernameEdit.getText().toString(), passwordEdit.getText().toString());
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
            String username = params[2];
            String password = params[3];
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                IoTServiceGrpc.IoTServiceBlockingStub stub = IoTServiceGrpc.newBlockingStub(channel);
                LoginRequest request = LoginRequest.newBuilder().setUsername(username)
                        .setPassword(password).build();
                LoginReply reply = stub.login(request);
                return reply.getUserId();
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
            Button loginButton = activity.findViewById(R.id.loginButton);
            loginButton.setEnabled(true);
            if (!result.equals("") && !result.startsWith("Failed")) {
                Intent intent;
                if (result.startsWith("usr")) {
                    intent = new Intent(activity, HomeActivity.class);
                }
                else if (result.startsWith("adm")) {
                    intent = new Intent(activity, AdminActivity.class);
                }
                else {
                    return;
                }
                EditText hostEdit = activity.findViewById(R.id.host);
                EditText portEdit = activity.findViewById(R.id.port);

                String host = hostEdit.getText().toString();
                String port = portEdit.getText().toString();
                String message = host + "," + port + "," + result;
                intent.putExtra(EXTRA_MESSAGE, message);
                activity.startActivity(intent);
            }
            else if (result.equals("")) {
                Toast myToast = Toast.makeText(activity, "Username or password incorrect",
                        Toast.LENGTH_SHORT);
                myToast.show();
            }
            else {
                Toast myToast = Toast.makeText(activity, result,
                        Toast.LENGTH_SHORT);
                myToast.show();
            }
        }
    }
}