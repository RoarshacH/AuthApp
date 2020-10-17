package com.example.authapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private DatabaseReference notificationDatabase;

    FirebaseUser fUser;
    DatabaseReference reference;
    Intent intent;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.Q)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = getIntent();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        String uID = fUser.getUid();
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        authUser(); // only activate when the user sends a auth request
    }

    protected void authUser(){
        ImageButton authBtn = findViewById(R.id.btnAuth);
        TextView txtInfo = findViewById(R.id.txtInfo);
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()){
            case BiometricManager.BIOMETRIC_SUCCESS:
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                txtInfo.setText("Hardware Needed For Biometric Auth is Unavilable");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                txtInfo.setText("No Fingerprint Registered Please add a Fingerprint in device Settings");
                authBtn.setVisibility(View.GONE);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                txtInfo.setText("Hardware Needed For Biometric Auth is Unavilable in Your Device Please use the One time password to log in");
                authBtn.setVisibility(View.GONE);
                break;
        }

        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in to the website using your biometric credential")
                .setNegativeButtonText("Use account Alternate Login")
                .build();

        authBtn.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });
    }

    protected void sendMessage(String message, String sender, String receiver){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        databaseReference.child("Requests/"+sender).push().setValue(hashMap);

    }
}