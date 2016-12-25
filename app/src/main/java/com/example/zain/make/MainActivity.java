package com.example.zain.make;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by zain on 12/16/2016.
 */

public class MainActivity  extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        final Button switch_button = (Button) findViewById(R.id.button_ChangeCamera);

        final Button capture_button = (Button) findViewById(R.id.button_capture);

        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("You need what type of scren? ")
                        .setCancelable(false)
                        .setPositiveButton("Portrait", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id) {


                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                //Intent intent = new Intent(MainActivity.this, VideoCaptureActivity.class);


//                                dialog.cancel();
//                                finish();

                                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
//                                builder2.setTitle("hi!");
////                                //etc
//                                builder2.show();

                            }
                        })
                        .setNegativeButton("Landscape", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {



                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                //Intent intent = new Intent(MainActivity.this, VideoCaptureActivity.class);


//                                dialog.cancel();
//                                finish();

                                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                                builder.setTitle("hi!");
////                                //etc
//                                builder.show();
                            }
                        });




                AlertDialog alert = builder.create();
                alert.show();

                    }
                });

















        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VideoCaptureActivity.class);
                startActivity(intent);
            }
        });
    }
}



