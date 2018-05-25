package org.tensorflow.demo.drone;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import org.tensorflow.demo.ProcessingActivty;
import org.tensorflow.demo.R;

import java.util.ArrayList;

public class BebopActivity extends Activity {
    private static final String TAG = "BebopActivity";
    private BebopDrone mBebopDrone;

    private ProgressDialog mConnectionProgressDialog;
    private ProgressDialog mDownloadProgressDialog;

    private H264VideoView mVideoView;

    private TextView mBatteryLabel;
    private Button mTakeOffLandBt;
    private Button mDownloadBt;
    private boolean mStreamOn;
    private boolean mAuto;
    private boolean mManual;

    private int mNbMaxDownload;
    private int mCurrentDownloadIndex;

    /** Visaje starts*/
    static ArrayList<String> photos = new ArrayList();
    private int mPathDuration;
    private int mPath;

    private void processImages(String mediaName){

    }





    private void startAnalysis(){

        mDownloadProgressDialog = new ProgressDialog(BebopActivity.this);
        mDownloadProgressDialog.setIndeterminate(false);
        mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadProgressDialog.setMessage("Proccesing  Images");
        mDownloadProgressDialog.setMax(10);
        mDownloadProgressDialog.setSecondaryProgress(4);
        mDownloadProgressDialog.setCancelable(false);
        /*
        mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBebopDrone.cancelGetLastFlightMedias();
            }
        });
        */
        mDownloadProgressDialog.show();

        for(int i=0;i<photos.size();i++){
            processImages(photos.get(i));
            mDownloadProgressDialog.setSecondaryProgress(i+1);

        }

        mDownloadProgressDialog.dismiss();

        AsyncTask<Void,Integer,Integer> asyncTask = new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] objects) {
                for(int i=0;i<photos.size();i++){
                    processImages(photos.get(i));
                    publishProgress(new Object[]{i + 1});
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                mDownloadProgressDialog.setSecondaryProgress((Integer) values[0]);
            }

            @Override
            protected void onPostExecute(Object o) {
                mDownloadProgressDialog.dismiss();
            }
        };

        //asyncTask.execute();







    }

    /** Visaje ends*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bebop);

        initIHM();

        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);
        mBebopDrone = new BebopDrone(this, service);
        mBebopDrone.addListener(mBebopListener);




    }

    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the bebop drone is connecting
        if ((mBebopDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mBebopDrone.getConnectionState())))
        {
            mConnectionProgressDialog = new ProgressDialog(this);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            // if the connection to the Bebop fails, finish the activity
            if (!mBebopDrone.connect()) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mBebopDrone != null)
        {
            mConnectionProgressDialog = new ProgressDialog(this);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            if (!mBebopDrone.disconnect()) {
                finish();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        mBebopDrone.dispose();
        super.onDestroy();
    }

    static int count = 0;
    private void initIHM() {

        hideAutoMode();
        showManualMode();
        mVideoView = (H264VideoView) findViewById(R.id.videoView);

        findViewById(R.id.emergencyBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.emergency();
            }
        });

        mTakeOffLandBt = (Button) findViewById(R.id.takeOffOrLandBt);
        mTakeOffLandBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                switch (mBebopDrone.getFlyingState()) {
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                        if(mManual)
                            mBebopDrone.takeOff();
                        else
                            moveDrone(4);

                        break;
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                        mBebopDrone.land();
                        break;
                    default:
                }
            }
        });

        findViewById(R.id.takePictureBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.takePicture();
            }
        });

        mDownloadBt = (Button)findViewById(R.id.downloadBt);
        mDownloadBt.setEnabled(false);
        mDownloadBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.getLastFlightMedias();

                mDownloadProgressDialog = new ProgressDialog(BebopActivity.this);
                mDownloadProgressDialog.setIndeterminate(true);
                mDownloadProgressDialog.setMessage("Fetching medias");
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBebopDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        });

        findViewById(R.id.gazUpBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.gazDownBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.yawLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.yawRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.backBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rollLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        Log.e("TOUCH", ++count+"");
                        mBebopDrone.setRoll((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rollRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });




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

        ((RadioGroup)findViewById(R.id.pathTypes)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.customPath:
                        mPath = 0;
                        break;
                    case R.id.linePath:
                        mPath = 1;
                        break;
                    case R.id.gridPath:
                        mPath = 2;
                        break;
                }
            }
        });

        ((SeekBar)findViewById(R.id.durationSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {



            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPathDuration = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mBatteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    private void showAutoMode() {
        mAuto = true;
        findViewById(R.id.manualBt).setVisibility(View.VISIBLE);
        findViewById(R.id.pathTypelbl).setVisibility(View.VISIBLE);
        findViewById(R.id.pathTypes).setVisibility(View.VISIBLE);
        findViewById(R.id.pathDurationlbl).setVisibility(View.VISIBLE);
        findViewById(R.id.durationSeekBar).setVisibility(View.VISIBLE);
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


    }

    private String getDif(long ini){
        return ":"+(System.currentTimeMillis()- ini)/1000;
    }
    private void moveDrone(final int dist){




        Handler handler = new Handler(Looper.getMainLooper());

        Runnable[] runnables= new Runnable[10];


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(BebopActivity.this, "Path Completed", Toast.LENGTH_SHORT).show();


            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(BebopActivity.this, ProcessingActivty.class);
                startActivity(intent);


            }
        }, 3000);


        if(1==1) return;
        int t=0;
        final long inicio=System.currentTimeMillis();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(BebopActivity.this, "Iniciando recorrido"+getDif(inicio), Toast.LENGTH_SHORT).show();


            }
        }, t);

        t+=1500;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Arrancando"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.takeOff();
            }
        }, t);

        t+=3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Subiendo"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setGaz((byte) 50);
            }
        },t);



        t+= 3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Para"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setGaz((byte) 0);
            }
        },t);

        t+=3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Derecha"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setRoll((byte) 50);
                mBebopDrone.setFlag((byte) 1);
            }
        },t);

        t+=3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Para"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setRoll((byte) 0);
                mBebopDrone.setFlag((byte) 0);
            }
        }, t);

        t+=3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Derecha"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setRoll((byte) 50);
                mBebopDrone.setFlag((byte) 1);

            }
        }, t);

        t+=3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Para"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setRoll((byte) 0);
                mBebopDrone.setFlag((byte) 0);
            }
        },t);

        t+=3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Bajando"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setGaz((byte) -50);
            }
        },t);

        t+=3000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BebopActivity.this, "Para"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setGaz((byte) 0);
            }
        },t);


        /*
        for(int i=1;i<15;i++) {
            final int x = i;
            handler.postDelayed(new Runnable() {
                public void run() {
                    // acciones que se ejecutan tras los milisegundos
                    int _dist = x % 2  == 0 ? 0: dist;
                    int flag = x % 2;
                    Toast.makeText(BebopActivity.this, "CMueve:"+_dist+"Flag:"+flag, Toast.LENGTH_SHORT).show();
                    mBebopDrone.setFlag((byte) flag);
                    mBebopDrone.setRoll((byte) _dist);

                }
            }, 300);
        }
        */
        t+=3000;
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                Toast.makeText(BebopActivity.this, "Atteriza:"+getDif(inicio), Toast.LENGTH_SHORT).show();
                mBebopDrone.setRoll((byte) 0);
                mBebopDrone.setFlag((byte) 0);
                mBebopDrone.land();
            }
        }, t);





    }


    private final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state)
            {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();
                    finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPilotingStateChanged(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    mTakeOffLandBt.setText("Take off");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(true);
                    break;
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    mTakeOffLandBt.setText("Land");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(false);
                    break;
                default:
                    mTakeOffLandBt.setEnabled(false);
                    mDownloadBt.setEnabled(false);
            }
        }

        @Override
        public void onPictureTaken(ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
            Log.i(TAG, "Picture has been taken");
        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
            mVideoView.configureDecoder(codec);
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            //if( mStreamOn ){
                mVideoView.displayFrame(frame);
            //}

        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {
            mDownloadProgressDialog.dismiss();

            mNbMaxDownload = nbMedias;
            mCurrentDownloadIndex = 1;

            if (nbMedias > 0) {
                mDownloadProgressDialog = new ProgressDialog(BebopActivity.this);
                mDownloadProgressDialog.setIndeterminate(false);
                mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDownloadProgressDialog.setMessage("Downloading medias");
                mDownloadProgressDialog.setMax(mNbMaxDownload * 100);
                mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);
                mDownloadProgressDialog.setProgress(0);
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBebopDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {
            mDownloadProgressDialog.setProgress(((mCurrentDownloadIndex - 1) * 100) + progress);
        }

        @Override
        public void onDownloadComplete(String mediaName) {
            Log.e(TAG, "SAJLAKSjkl"+mediaName);
            photos.add(mediaName);


            mCurrentDownloadIndex++;
            mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);

            if (mCurrentDownloadIndex > mNbMaxDownload) {

                mDownloadProgressDialog.dismiss();
                mDownloadProgressDialog = null;
                startAnalysis();
            }
        }
    };
}