package app;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emilia.mouthdetection.DetectMouth;
import com.emilia.mouthdetection.MouthListener;
import com.emilia.mouthdetection.R;

/**
 * Created by  Emilia
 */
public class MainActivity extends Activity implements MouthListener.Listener{
    /**
     * Simple implementation of Camera Plugin
     * which enables to join the project with the C++ implementation of the neural network
     */

    Activity context;
    ImageView mouthBox;
    TextView faces_txt;
    TextView expressions_txt;
    CheckBox preview_check;
    CheckBox capture_switch;
    DetectMouth mPreview;

    int camId=-1;
    Camera mCamera;

    MouthListener fcListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;


        mouthBox = (ImageView) findViewById(R.id.imageView);
        faces_txt = (TextView) findViewById(R.id.faces);
        expressions_txt = (TextView) findViewById(R.id.expressions);
        preview_check = (CheckBox) findViewById(R.id.preview_check);
        capture_switch = (CheckBox) findViewById(R.id.capturing);

        mouthBox.setVisibility(View.INVISIBLE);



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



        //register listener for DetectMouth;
        fcListener = new MouthListener();
        fcListener.registerListener(this);


    }

    /**
     * Listener for DetectMouth, updates faces_txt
     * @param isFace is there at least 1 face found
     * @param amount of recognized faces
     */
    @Override
    public void onFaceAmountChange(boolean isFace, int amount) {
        if (isFace) {
            faces_txt.setText("Faces fund: "+amount);
        } else {
            faces_txt.setText("Faces fund: "+amount);
            Toast.makeText(this, getString(R.string.no_faces), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Listener for DetectMouth, updates expressions_txt
     * @param recognized
     * @param expression
     */
    @Override
    public void onExpressionChange(boolean recognized, int expression) {
        if(recognized){
            expressions_txt.setText("Expression: "+expression);
        }
        else{
            expressions_txt.setText("Expression: -");
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

    /**
     * finds and opens front camera
     */
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


    /**
     * initializes DetectMouth
     */
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
