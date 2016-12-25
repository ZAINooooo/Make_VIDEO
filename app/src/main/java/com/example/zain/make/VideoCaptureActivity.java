
package com.example.zain.make;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import static com.example.zain.make.R.id.info;
import static com.example.zain.make.R.id.submit_area;
import static com.example.zain.make.R.id.timer;
import static com.example.zain.make.R.id.video;

public class VideoCaptureActivity extends Activity {
    private static final String TAG = "VideoCaptureActivity";


    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 1337;


    Camera camera;
    private Camera myCamera;


    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;


    private int what;


    private SurfaceHolder mHolder;


    ImageButton recordButton;

    TextView timerValue;


    int maxDuration = 7000;//7sec
    int frameRate = 1;

    int mCount;
    //TimerThread mTimer;


    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    private static Timer timer;

    private long startTime = 10000;
    private long interval = 1000;


    ImageButton stopButton;
    FrameLayout cameraPreviewFrame;
    CameraPreview cameraPreview;
    MediaRecorder mediaRecorder;


//    private final int maxDurationInMs = 20000;
//    private final long maxFileSizeInBytes = 500000;
//    private final int videoFramesPerSecond = 20;


    File file;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.video_capture);
        this.cameraPreviewFrame = (FrameLayout) super.findViewById(R.id.camera_preview);
        this.recordButton = (ImageButton) super.findViewById(R.id.recordButton);
        this.stopButton = (ImageButton) super.findViewById(R.id.stopButton);


        this.timerValue = (TextView) super.findViewById(R.id.timer);


        this.toggleButtons(false);
        // we'll enable this button once the camera is ready
        this.recordButton.setEnabled(false);


    }


    void toggleButtons(boolean recording) {
        this.recordButton.setEnabled(!recording);
        this.recordButton.setVisibility(recording ? View.GONE : View.VISIBLE);
        this.stopButton.setEnabled(recording);
        this.stopButton.setVisibility(recording ? View.VISIBLE : View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();


        // initialize the camera in background, as this may take a while
        new AsyncTask<Void, Void, Camera>() {

            @Override
            protected Camera doInBackground(Void... params) {
                try {

                    Camera camera = Camera.open();


                    Camera.Parameters parameters = camera.getParameters();


                    //setCameraDisplayOrientation(VideoCaptureActivity.this ,1, camera);

                    parameters.setRotation(0); //landscape
                    parameters.set("orientation", "LANDSCAPE");


//                    mCamera.setDisplayOrientation(0);
                    // parameters.setRotation(0);

                    return camera == null ? Camera.open(0) : camera;
                } catch (RuntimeException e) {
                    Log.wtf(TAG, "Failed to get camera", e);
                    return null;
                }
            }


            @Override
            protected void onPostExecute(Camera camera) {
                if (camera == null) {
                    Toast.makeText(VideoCaptureActivity.this, R.string.cannot_record, Toast.LENGTH_SHORT);
                } else {
                    VideoCaptureActivity.this.initCamera(camera);
                }
            }
        }.execute();
    }

    void initCamera(Camera camera) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(VideoCaptureActivity.this);
        builder.setMessage("You Will only be allowed to capture video for 30 seconds ").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {


//                Intent intent = new Intent(VideoCaptureActivity.this, PopSMSActivity.class);
//                VideoCaptureActivity.super.startActivity(intent);


            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


//                        Intent intent = new Intent(VideoCaptureActivity.this, VideoCaptureActivity.class);
//                        VideoCaptureActivity.super.startActivity(intent);


                    }
                });


        final AlertDialog alert = builder.create();
        alert.show();


//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                alert.dismiss();
//
//            }
//


        // we now have the camera


        this.camera = camera;
        // create a preview for our camera
        this.cameraPreview = new CameraPreview(VideoCaptureActivity.this, this.camera);


//set camera preview to Landscape


        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(90);    //landscape mode

            //camera.setDisplayOrientation(0);    //portrait

        }

        //myCamera = getCameraInstance();
        //setCameraDisplayOrientation(this,270,myCamera); // set the orientation here to enable portrait recording.

        // add the preview to our preview frame
        this.cameraPreviewFrame.addView(this.cameraPreview, 0);
        // enable just the record button
        this.recordButton.setEnabled(true);
    }

    void releaseCamera() {
        if (this.camera != null) {
            this.camera.lock(); // unnecessary in API >= 14
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
            this.cameraPreviewFrame.removeView(this.cameraPreview);
        }
    }

    void releaseMediaRecorder() {
        if (this.mediaRecorder != null) {
            this.mediaRecorder.reset(); // clear configuration (optional here)
            this.mediaRecorder.release();
            this.mediaRecorder = null;
        }
    }

    void releaseResources() {
        this.releaseMediaRecorder();
        this.releaseCamera();
    }


    @Override
    public void onPause() {
        super.onPause();
        this.releaseResources();


    }







    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + mins + ":" + String.format("%02d", secs) + ":" + String.format("%03d", milliseconds));


            customHandler.postDelayed(this, 0);


        }
    };


//        new CountDownTimer(5000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished)
//            {
//
//            }
//
//            @Override
//            public void onFinish()
//            {
//
//              mediaRecorder.stop();
//
//              Toast.makeText(VideoCaptureActivity. this, "stopepd", Toast.LENGTH_SHORT).show();
//
//
//              //mediaRecorder.release();
//
//            }
//        } .start();





        // gets called by the button press
    public void startRecording(final View v) {
        Log.d(TAG, "startRecording()");


        //start timer when recording starts
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);





        //camera.setDisplayOrientation(0); //landscape mode , camera k lihaaz se

        camera.setDisplayOrientation(90); //portrait mode , camera k lihaaz se


        // we need to unlock the camera so that mediaRecorder can use it
        this.camera.unlock(); // unnecessary in API >= 14
        // now we can initialize the media recorder and set it up with our
        // camera

//        this.setCameraDisplayOrientation(VideoCaptureActivity.this,90,myCamera);


        this.mediaRecorder = new MediaRecorder();



       // 1:-
//        customHandler = new Handler();
//        Runnable r =new Runnable() {
//            @Override
//            public void run()
//            {
//
//                mediaRecorder.stop();
//                //mediaRecorder.release();
//            }
//        }; customHandler.postDelayed(r , 9000);



         //2:
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        mediaRecorder.stop();
////                        mediaRecorder.reset();
////                        mediaRecorder.release();
//                    }
//                });
//            }
//        }, 10000);

//3.
//        new CountDownTimer(5000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished)
//            {
//
//            }
//
//            @Override
//            public void onFinish()
//            {
//
//                mediaRecorder.stop();
//
//                Toast.makeText(VideoCaptureActivity. this, "stopped", Toast.LENGTH_SHORT).show();
////                Intent intent = new Intent(VideoCaptureActivity.this , MainActivity.class);
////                startActivity(intent);
//
//
//
//              //mediaRecorder.release();
//            }
//
//
//
//        } .start();



            String state = android.os.Environment.getExternalStorageState();

        //if No SD card executed
        if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            try {
                throw new IOException("SD Card is not mounted.  It is " + state + ".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        this.mediaRecorder.setCamera(this.camera);
        this.mediaRecorder.setAudioSamplingRate(8000);
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        this.mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        this.mediaRecorder.setOutputFile(this.initFile().getAbsolutePath());


        this.mediaRecorder.setMaxDuration(10000); // 10 seconds
        this.mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra)
            {

                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
                {
                    Log.v("VIDEOCAPTURE", "Maximum Duration Reached");
                    Toast.makeText(VideoCaptureActivity.this , "Stopped" , Toast.LENGTH_SHORT).show();
//                    timer.cancel();
//                    timer.purge();
                      //mediaRecorder.stop();
                     //mediaRecorder.release();

//                    mr.reset();



                }
            }
        });







        this.mediaRecorder.setPreviewDisplay(this.cameraPreview.getHolder().getSurface());


        try {





            //showing media recorder output in landscape
            //this.mediaRecorder.setOrientationHint(90);

            //showing media recorder output in landscape
            this.mediaRecorder.setOrientationHint(0);


            //this.mediaRecorder.setMaxDuration(5000);
            //this.mediaRecorder.setOrientationHint(90);
            this.mediaRecorder.prepare();
            // start the actual recording
            // throws IllegalStateException if not prepared

            this.mediaRecorder.start();
            Toast.makeText(this, R.string.recording, Toast.LENGTH_SHORT).show();
            // enable the stop button by indicating that we are recording
            this.toggleButtons(true);
        } catch (Exception e) {
            Log.wtf(TAG, "Failed to prepare MediaRecorder", e);
            Toast.makeText(this, R.string.cannot_record, Toast.LENGTH_SHORT).show();
            this.releaseMediaRecorder();

        }
    }


    private void setCameraDisplayOrientation(VideoCaptureActivity videoCaptureActivity, int i, Camera myCamera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(i, info);

        //set up the rotation in camera capture
        int rotation = videoCaptureActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    // gets called by the button press
    public void stopRecording(View v) {
        Log.d(TAG, "stopRecording()");
        assert this.mediaRecorder != null;
        try {



            this.mediaRecorder.stop();
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            // we are no longer recording
            this.toggleButtons(false);
        } catch (RuntimeException e) {
            // the recording did not succeed
            Log.w(TAG, "Failed to record", e);
            if (this.file != null && this.file.exists() && this.file.delete()) {
                Log.d(TAG, "Deleted " + this.file.getAbsolutePath());


                this.mediaRecorder.release();
                this.mediaRecorder.reset();


            }
            return;


        }
        if (this.file == null || !this.file.exists()) {
            Log.w(TAG, "File does not exist after stop: " + this.file.getAbsolutePath());
        } else {
            Log.d(TAG, "Going to display the video: " + this.file.getAbsolutePath());
            Intent intent = new Intent(this, VideoPlaybackActivity.class);
            intent.setData(Uri.fromFile(file));
            super.startActivity(intent);
        }
    }

    private File initFile() {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), this
                .getClass().getPackage().getName());
        if (!dir.exists() && !dir.mkdirs()) {
            Log.wtf(TAG, "Failed to create storage directory: " + dir.getAbsolutePath());
            Toast.makeText(VideoCaptureActivity.this, R.string.cannot_record, Toast.LENGTH_SHORT);
            this.file = null;
        } else {
            this.file = new File(dir.getAbsolutePath(), new SimpleDateFormat("'IMG_'yyyyMMddHHmmss'.m4v'").format(new Date()));
        }
        return this.file;
    }
}

































































































































































