package org.tensorflow.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class DroneTempActivity extends Activity {

    private boolean mStreamOn;
    private boolean mAuto;
    private boolean mManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bebop);

        hideAutoMode();
        showManualMode();


        //save();



        findViewById(R.id.takeOffOrLandBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveDrone();
            }
        });


        float t=500;
        double firstLevel = 0.3 + (0.95-0.3) *( (t/33000));
        int percentage = (int) Math.floor((1-firstLevel)*100);

        ((TextView)findViewById(R.id.batteryLabel)).setText(percentage+"%");

        findViewById(R.id.manualBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAutoMode();
                showManualMode();

            }
        });

        findViewById(R.id.autoBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideManualMode();
                showAutoMode();
            }
        });



        final TextView repetitionsLabel = findViewById(R.id.repetitionsLabel);

        ((SeekBar)findViewById(R.id.durationSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress=0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
                repetitionsLabel.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private void showAutoMode() {
        mAuto = true;
        findViewById(R.id.manualBt).setVisibility(View.VISIBLE);
        findViewById(R.id.pathTypelbl).setVisibility(View.VISIBLE);
        findViewById(R.id.pathTypes).setVisibility(View.VISIBLE);
        findViewById(R.id.pathDurationlbl).setVisibility(View.VISIBLE);
        findViewById(R.id.durationSeekBar).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.takeOffOrLandBt)).setText("START PATH");

    }


    private void hideAutoMode() {
        mAuto = false;
        findViewById(R.id.manualBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.pathTypelbl).setVisibility(View.INVISIBLE);
        findViewById(R.id.pathTypes).setVisibility(View.INVISIBLE);
        findViewById(R.id.pathDurationlbl).setVisibility(View.INVISIBLE);
        findViewById(R.id.durationSeekBar).setVisibility(View.INVISIBLE);
    }

    private void hideManualMode(){
        mManual = false;
        mStreamOn = false;

        findViewById(R.id.autoBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.videoView).setVisibility(View.INVISIBLE);

        findViewById(R.id.takePictureBt).setVisibility(View.INVISIBLE);


        findViewById(R.id.forwardBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.gazUpBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.rollLeftBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.yawLeftBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.rollRightBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.yawRightBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.backBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.gazDownBt).setVisibility(View.INVISIBLE);
        findViewById(R.id.downloadBt).setVisibility(View.INVISIBLE);


    }


    private void showManualMode(){

        mManual = true;
        mStreamOn = true;
        findViewById(R.id.autoBt).setVisibility(View.VISIBLE);
        findViewById(R.id.videoView).setVisibility(View.VISIBLE);
        findViewById(R.id.takePictureBt).setVisibility(View.VISIBLE);

        findViewById(R.id.forwardBt).setVisibility(View.VISIBLE);
        findViewById(R.id.gazUpBt).setVisibility(View.VISIBLE);
        findViewById(R.id.rollLeftBt).setVisibility(View.VISIBLE);
        findViewById(R.id.yawLeftBt).setVisibility(View.VISIBLE);
        findViewById(R.id.rollRightBt).setVisibility(View.VISIBLE);
        findViewById(R.id.yawRightBt).setVisibility(View.VISIBLE);
        findViewById(R.id.backBt).setVisibility(View.VISIBLE);
        findViewById(R.id.gazDownBt).setVisibility(View.VISIBLE);
        findViewById(R.id.downloadBt).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.takeOffOrLandBt)).setText("TAKE OFF");


    }

    private void moveDrone(){

        if( mManual ){
            Toast.makeText(DroneTempActivity.this, "Taking Off...", Toast.LENGTH_SHORT).show();
            return;
        }



        final Handler handler = new Handler(Looper.getMainLooper());


        final TextView batteryLabel = ((TextView)findViewById(R.id.batteryLabel));


        Toast.makeText(DroneTempActivity.this, "Starting Path", Toast.LENGTH_SHORT).show();
        ((Button)findViewById(R.id.takeOffOrLandBt)).setEnabled(false);

        int t=500;



        for(;t<=33000;t+=4000){
            final float _t = t;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    double firstLevel = 0.3 + (0.95-0.3) *( (_t/33000));
                    int percentage = (int) Math.floor((1-firstLevel)*100);


                    batteryLabel.setText(percentage+"%");
                }
            },t);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DroneTempActivity.this, "Moving...", Toast.LENGTH_SHORT).show();
                }
            },t);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DroneTempActivity.this, "Ending Path", Toast.LENGTH_SHORT).show();
                ((Button)findViewById(R.id.takeOffOrLandBt)).setEnabled(true);
            }
        },t);





    }
}


