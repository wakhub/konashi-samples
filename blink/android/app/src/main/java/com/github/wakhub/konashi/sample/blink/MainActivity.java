package com.github.wakhub.konashi.sample.blink;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.uxxu.konashi.lib.Konashi;
import com.uxxu.konashi.lib.KonashiListener;
import com.uxxu.konashi.lib.KonashiManager;

import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.Promise;

import info.izumin.android.bletia.BletiaException;

/**
 * Created by wak on 10/24/15.
 */
public final class MainActivity extends AppCompatActivity implements KonashiListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PIN = Konashi.PIO1;

    private static final int INTERVAL = 500;

    private KonashiManager konashiManager;

    private boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        konashiManager = new KonashiManager(this);
        konashiManager.addListener(this);

        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(linearLayout);

        Button findButton = new Button(this);
        findButton.setText("Find konashi");
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                konashiManager.find(MainActivity.this);
            }
        });
        linearLayout.addView(findButton);

        Button startButton = new Button(this);
        startButton.setText("Start Blink");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBlinkLoop();
            }
        });
        linearLayout.addView(startButton);
    }

    private void startBlinkLoop() {
        if (!konashiManager.isReady() || started) {
            return;
        }
        started = true;
        konashiManager.pinMode(PIN, Konashi.OUTPUT)
                .done(new DoneCallback<BluetoothGattCharacteristic>() {
                    @Override
                    public void onDone(BluetoothGattCharacteristic result) {
                        blinkLoop();
                    }
                });
    }

    private void blinkLoop() {
        Log.d(TAG, "HIGH");
        konashiManager.digitalWrite(PIN, Konashi.HIGH)
                .then(new DonePipe<BluetoothGattCharacteristic, BluetoothGattCharacteristic, BletiaException, Void>() {
                    @Override
                    public Promise<BluetoothGattCharacteristic, BletiaException, Void> pipeDone(BluetoothGattCharacteristic result) {
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                        }
                        Log.d(TAG, "LOW");
                        return konashiManager.digitalWrite(PIN, Konashi.LOW);
                    }
                })
                .done(new DoneCallback<BluetoothGattCharacteristic>() {
                    @Override
                    public void onDone(BluetoothGattCharacteristic result) {
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                        }
                        blinkLoop();
                    }
                });
    }

    @Override
    protected void onPause() {
        konashiManager.reset();
        super.onPause();
    }

    @Override
    public void onConnect(KonashiManager konashiManager) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnect(KonashiManager konashiManager) {

    }

    @Override
    public void onError(KonashiManager konashiManager, BletiaException e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdatePioOutput(KonashiManager konashiManager, int i) {

    }

    @Override
    public void onUpdateUartRx(KonashiManager konashiManager, byte[] bytes) {

    }

    @Override
    public void onUpdateBatteryLevel(KonashiManager konashiManager, int i) {

    }
}
