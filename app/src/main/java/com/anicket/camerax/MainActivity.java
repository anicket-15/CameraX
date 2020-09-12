package com.anicket.camerax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final String[] PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            "android.permission.CAMERA",
    };


    Camera camera;
    Preview preview;
    ImageCapture imageCapture;
    PreviewView cameraView;
    CameraSelector cameraSelector;
    // 0 for rear facing and 1 for front facing
    int currentFacingSide;

    public MainActivity(){
        currentFacingSide = 0;
    }

    // onCLick Listeners
    public void switchCameraSide(View view) {
        startCamera();
    }

    public void capturePicture(View view) {
        takePhoto();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)== PermissionChecker.PERMISSION_GRANTED){
            startCamera();
         }
         else{
             ActivityCompat.requestPermissions(this,PERMISSIONS,101);
         }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)== PermissionChecker.PERMISSION_GRANTED){
            startCamera();
        }
        else{
            ActivityCompat.requestPermissions(this,PERMISSIONS,101);
            Toast.makeText(this, "Please accept the required permissions", Toast.LENGTH_LONG).show();
        }
    }

    private void startCamera() {
        cameraView = findViewById(R.id.cameraView);
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();
        preview.setSurfaceProvider(cameraView.createSurfaceProvider());
        if(currentFacingSide==0){
            cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
            currentFacingSide=1;
        }
        else{
            cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
            currentFacingSide=0;
        }
        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture);
    }



    private void takePhoto() {
        File photoFile = new File( getExternalFilesDir(null)+ "/" + System.currentTimeMillis() + ".jpg");
        Log.i("imagePath",getExternalFilesDir(null) + "/" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.d("imagesaved","The image is saved at "+getExternalFilesDir(null));
                Toast.makeText(MainActivity.this, "Image Saved Successfully..!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.i("errorImage",exception.getMessage());
                Toast.makeText(MainActivity.this, "Image saving failed.. Please grant permissions..!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
