package com.example.derrick.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    SurfaceView svCamera;
    TextView tvData;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    final int reqPermissionId = 1001;
    private AsyncHttpClient client;
    private ArrayList<Event> al;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new AsyncHttpClient();

        client.get("http://10.0.2.2/FYP/getListOfEvents.php", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try{
                    Log.d("Result" , response.toString());
                    for (int i=0; i< response.length(); i ++){
                        JSONObject obj = response.getJSONObject(i);
                        int event_id = obj.getInt("event_id");
                        String event_name = obj.getString("event_name");

                        Event event = new Event(event_id, event_name);
                        al.add(event);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        svCamera = findViewById(R.id.svCamera);
        tvData = findViewById(R.id.tvData);



        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(500, 500)
                .build();

        svCamera.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, reqPermissionId);
                    return;
                }
                try {
                    cameraSource.start(svCamera.getHolder());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if(qrCodes.size() != 0){
                    tvData.post(new Runnable() {
                        @Override
                        public void run() {
                            String code = qrCodes.valueAt(0).displayValue;
                            for (int i = 0; i < al.size(); i++){
                                if (code == al.get(i).toString()){
                                    Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator.vibrate(1000);
                                    Toast.makeText(MainActivity.this, "Event Successfully Registered", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case reqPermissionId: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    try {
                        cameraSource.start(svCamera.getHolder());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }
}
