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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class CameraActivity extends AppCompatActivity {
    private String EMAIL = "17010457@myrp.edu.sg";
    SurfaceView svCamera;
    TextView tvData;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    final int reqPermissionId = 1001;
    private AsyncHttpClient client;
    private ArrayList<Event> al;
    Boolean isRegistered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        client = new AsyncHttpClient();

        svCamera = findViewById(R.id.svCamera);
        tvData = findViewById(R.id.tvData);

        String[] email = EMAIL.split("@");
        final String student_id = email[0];

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        svCamera.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, reqPermissionId);
                    return;
                }
                try {
                    cameraSource.start(holder);

                } catch (IOException e) {
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
                if (qrCodes.size() != 0) {
                    if (!isRegistered) {
                        tvData.post(new Runnable() {
                            @Override
                            public void run() {
                                final String code = qrCodes.valueAt(0).displayValue;
                                Log.i("code", code);
                                String[] parameters = code.split("_");
                                String event_id = parameters[0];
                                String event_title = parameters[1];
                                Log.i("params", event_id + " & " + event_title);
                                RequestParams params = new RequestParams();
                                params.add("event_id", event_id);
                                params.add("event_title", event_title);
                                params.put("student_id", Integer.parseInt(student_id));
                                client.post("http://fyp-demo.000webhostapp.com/registerAttendance.php", params, new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        try {
                                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                            vibrator.vibrate(100);
                                            Log.i("JSON Results: ", response.toString());
                                            Toast.makeText(CameraActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                            tvData.setText("Success");
                                            finish();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(CameraActivity.this, "Invalid Code", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        super.onFailure(statusCode, headers, responseString, throwable);
                                        Log.d("Error", responseString);
                                        Toast.makeText(CameraActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        });
                        isRegistered = true;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
