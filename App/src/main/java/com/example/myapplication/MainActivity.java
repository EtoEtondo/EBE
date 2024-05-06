package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    //needed Elements
    Boolean toggle = false;
    Boolean toggle2 = true;
    Bitmap bm;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PreviewView previewView;
    TextView textViewy;
    LocalModel localModel;
    CustomObjectDetectorOptions customObjectDetectorOptions;
    ObjectDetector objectDetector;
    User user;
    Integer cnt = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //calling the View-Elements
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewView = findViewById(R.id.previewView);
        textViewy = (TextView) findViewById(R.id.textView);
        textViewy.setText("Model-Label");

        //this can be called on the login screen, creating new User in DB
        user = new User("ethem", "0000");
        user.writeUser();

        //Camera setup
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try { //checking and asking for camera premission
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        //calling the local ML Model
        localModel = new LocalModel.Builder().setAssetFilePath("model.tflite").build();

        // Live detection and tracking
        customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(1)
                .build();
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
    }

    //Camera Preview
    @SuppressLint("UnsafeOptInUsageError")
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //Analyzer for recognizing the face
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(720,1280))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this) , new ImageAnalysis.Analyzer() {
// Analyze Mediapictures
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                Image mediaImage = imageProxy.getImage();

                //Check Camera input with ML Model via Object Detection
                if (mediaImage != null) {
                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    objectDetector.process(image)
                            .addOnFailureListener(e -> {textViewy.setText("listener error");})
                            .addOnSuccessListener(results -> {
                                for (DetectedObject detectedObject : results) { //checking detected object
                                    Rect boundingBox = detectedObject.getBoundingBox();
                                    Integer trackingId = detectedObject.getTrackingId();
                                    for (DetectedObject.Label label : detectedObject.getLabels()) {
                                        String text = label.getText();
                                        int index = label.getIndex();
                                        float confidence = label.getConfidence();
                                        textViewy.setText(text);

                                        //updating DB with Incidents and URL
                                        user.reportIncident();
                                        if(!text.equals(user.username)){ //for wrong or unknown user
                                            cnt += 1;
                                            if(cnt >= 15){
                                                user.state = true;
                                                if(toggle){ //to dont take multiple screenshots
                                                    if(toggle2){
                                                        bm = previewView.getBitmap();
                                                        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                                        byte[] byteArray = stream.toByteArray();
                                                        bm.recycle();
                                                        String dURL = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                                        user.setDataURL(dURL);
                                                        toggle2 = false;
                                                    }
                                                    toggle = false;
                                                }else {
                                                    toggle = true;
                                                }
                                            }
                                        }else{ //correct user
                                            cnt = 0;
                                            user.state = false;
                                            user.setDataURL("kein Foto");
                                            if (toggle2 == false){
                                                toggle2 = true;
                                                if(bm != null && stream != null){
                                                    bm.recycle();
                                                    bm = null;
                                                    stream.reset();
                                                }
                                            }
                                        }
                                    }
                                }
                            }).addOnCompleteListener(results -> {imageProxy.close();});
                }
            }
        });

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
}