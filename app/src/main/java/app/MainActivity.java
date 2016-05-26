package app;

import android.app.Activity;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.emilia.mouthdetection.DetectMouth;
import com.emilia.mouthdetection.FaceCounterListener;
import com.emilia.mouthdetection.R;

public class MainActivity extends AppCompatActivity implements FaceCounterListener.Listener{

    Activity context;
    ImageView mouthBox;
    public TextView faces_txt;
    CheckBox preview_check;
    Switch capture_switch;
    DetectMouth mPreview;

    int camId=-1;
    Camera mCamera;

    FaceCounterListener fcListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;


        mouthBox = (ImageView) findViewById(R.id.imageView);
        faces_txt = (TextView) findViewById(R.id.faces);
        preview_check = (CheckBox) findViewById(R.id.preview_check);
        capture_switch = (Switch) findViewById(R.id.capturing);




        //switch listener
        capture_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {

                    prepareDetectMouth();

                    mPreview.startCapture();
                    mPreview.setFaceCounterListener(fcListener);
                }
                else {
                    mPreview.stopCapture();
                }
            }
        });


        //checkbox listener
        preview_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    prepareDetectMouth();
                    mPreview.startPreview();
                }
                else {

                    mPreview.stopPreview();
                    if(mPreview.isCapturing()){ //has to be like this, otherwise face capturing freezes until starting preview again
                        mPreview.stopCapture();
                        prepareDetectMouth();
                        mPreview.startCapture();
                    }
                }
            }
        });



        //register listener for face amount;
        fcListener = new FaceCounterListener();
        fcListener.registerListener(this);



    }

    /* called just like onCreate at some point in time */
    @Override
    public void onFaceAmountChange(boolean isFace, int amount) {
        if (isFace) {
            faces_txt.setText("Faces fund: "+amount);
        } else {
            faces_txt.setText("Faces fund: "+amount);
            Toast.makeText(this, getString(R.string.no_faces), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();



        if(capture_switch.isChecked()) {
            findFrontCamera();

            mPreview.setMouthBox(mCamera, mouthBox, camId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    protected void findFrontCamera(){
        int numCams = Camera.getNumberOfCameras();

        if(numCams > 0){

            if(camId==-1) {
                //search for front camera
                for (int i = 0; i < numCams; i++) {
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        camId = i;
                        break;
                    }
                }
            }

            try{
                mCamera = Camera.open(camId);
                //mPreview.setCamera(mCamera);
            } catch (RuntimeException ex){
                Toast.makeText(this, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
            }
        }

        Log.d("Camera ID", Integer.toString(camId));
    }


    private void prepareDetectMouth(){
        if(mCamera==null)
            findFrontCamera();
        if(mPreview==null) {
            mPreview = new DetectMouth(context);
            mPreview = (DetectMouth) findViewById(R.id.preview);
        }

        mPreview.setMouthBox(mCamera, mouthBox, camId);
    }




}
