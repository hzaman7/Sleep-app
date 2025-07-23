package com.example.sleepapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;

public class TrackingActivity extends AppCompatActivity {

    private TextView heartRateText;
    private MessageClient.OnMessageReceivedListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        heartRateText = findViewById(R.id.heart_rate);
        heartRateText.setText("-- BPM"); // placeholder until we get a message

        Button stopTrackingButton = findViewById(R.id.btn_stop_tracking);
        stopTrackingButton.setOnClickListener(v ->
                Toast.makeText(this, "Sleep tracking stopped", Toast.LENGTH_SHORT).show()
        );

        // Bottom‐nav wiring (unchanged)…
        findViewById(R.id.nav_alarm).setOnClickListener(v ->
                startActivity(new Intent(this, AlarmActivity.class)));
        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.nav_tracking).setOnClickListener(v ->
                Toast.makeText(this, "You're on Tracking", Toast.LENGTH_SHORT).show());
        findViewById(R.id.nav_sounds).setOnClickListener(v ->
                startActivity(new Intent(this, SoundsActivity.class)));

        // Prepare the MessageClient listener
        messageListener = new MessageClient.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(MessageEvent event) {
                if ("/heart_rate".equals(event.getPath())) {
                    String bpm = new String(event.getData(), StandardCharsets.UTF_8);
                    runOnUiThread(() ->
                            heartRateText.setText("Heart Rate: " + bpm + " BPM")
                    );
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register to listen for messages from the watch
        Wearable.getMessageClient(this)
                .addListener(messageListener)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully registered
                    }
                });
    }

    @Override
    protected void onPause() {
        // Unregister to avoid leaks
        Wearable.getMessageClient(this)
                .removeListener(messageListener)
                .addOnSuccessListener(aVoid -> {
                    // Successfully unregistered
                });
        super.onPause();
    }
}
