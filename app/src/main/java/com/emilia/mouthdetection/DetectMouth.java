package com.emilia.mouthdetection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Emilia on 25.05.2016.
 */
public class DetectMouth extends ViewGroup implements SurfaceHolder.Callback, Camera.FaceDetectionListener {

    Activity mActivity;
    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;

    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;
    int sampleSize;
    Camera mCamera;
    int camId;

    Matrix matrix = new Matrix();
    ImageView mouthBox;

    boolean isCapturing;
    boolean isPreview = false;

    int facesAmount;
    private boolean newFace = false;
    private RectF currentFace;
    int currentExpression;


    MouthListener fcListener;




    public DetectMouth(Context context) {
        super(context);
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceView.setVisibility(View.INVISIBLE);
    }
    public DetectMouth(Context context, AttributeSet attr) {
        super(context, attr);
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceView.setVisibility(View.INVISIBLE);
    }


    /**
     * handles everything :D
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                 mCamera.setPreviewDisplay(holder);
                if(isCapturing ) try{
                    mCamera.startFaceDetection();
                    mCamera.setFaceDetectionListener(this);
                } catch (RuntimeException e){
                    Log.d("FaceDetection", "couldn't start");
                }


                mCamera.setPreviewCallback(new Camera.PreviewCallback() {

                    //on single frame try to capture mouth
                    public synchronized void onPreviewFrame(byte[] data, Camera camera) {

                        if(!newFace) {
                            mouthBox.setImageResource(R.drawable.blind);
                            return;
                        }

                        // Convert to JPG
                        Camera.Size previewSize = camera.getParameters().getPreviewSize();
                        YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
                        byte[] jdata = baos.toByteArray();


                        // mouth preview options
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inDither = false;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inPreferQualityOverSpeed = false;
                        //options.inSampleSize = sampleSize;

                        Bitmap image = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, options);

                        //translate values
                        prepareMatrix(matrix, 0, image.getWidth(), image.getHeight(), false);
                        matrix.mapRect(currentFace);

                        //find mouth
                        Bitmap mouthImg = MouthCrop.getMouthFromBitmap(image, true, currentFace);

                        //set mouth preview
                        if(isPreview && image!=null) mouthBox.setImageBitmap(mouthImg);


                        checkExpression(mouthImg);


                        camera.addCallbackBuffer(data);
                        newFace = false;

                        return;
                    }
                });



            }
        } catch (IOException exception) {
            Log.e("DetectMouth", "IOException caused by setPreviewDisplay()", exception);
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
//        if(mCamera==null) return;
//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//        requestLayout();
//
//        mCamera.setParameters(parameters);
//        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }



    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Log.d("facedetection", "Faces Found: " + faces.length );
        setFacesAmount(faces.length);

        //find biggest face
        if(faces.length>0){
            newFace = true;
            currentFace = new RectF();
            int size = 0;
            for (Camera.Face f: faces) {
                if(f.rect.width()>size){
                    size = f.rect.width();
                    currentFace.set(f.rect);
                }
            }

        }

    }





    ///----------


    /**
     * add pointer to mouthBox
     * @param c
     * @param v
     * @param id
     */
    public void setMouthBox(Camera c, ImageView v, int id){
        mCamera = c;
        Camera.Size s = mCamera.getParameters().getPictureSize();
        mouthBox = v;
        camId = id;
        sampleSize = s.width/256; //recovering a subsampled picture with width of 256px
        setCamera(c);

        //parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        //mCamera.setParameters(parameters);
        mCamera.startPreview();
        mouthBox.setVisibility(INVISIBLE);
    }



    /**
     * translating image from camera to display
     * @param matrix
     * @param displayOrientation
     * @param viewWidth
     * @param viewHeight
     * @param mirror
     */
    public static void prepareMatrix(Matrix matrix, int displayOrientation,
                                     int viewWidth, int viewHeight, boolean mirror) {

        matrix.setScale(mirror ? -1 : 1, 1);
        // Need mirror for front camera.
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);

    }


    /**
     * Set camera parameters
     * @param camera
     */
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();

            Camera.Parameters params = mCamera.getParameters();
            params.set("jpeg-quality", 70);
            params.setPictureFormat(PixelFormat.JPEG);

            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            Camera.Size size = sizes.get(Integer.valueOf((sizes.size()-1)/2)); //choose a medium resolution
            params.setPictureSize(size.width, size.height);
            //camera.setDisplayOrientation(90);

            List<Camera.Size> sizes2 = params.getSupportedPreviewSizes();
            Camera.Size size2 = sizes2.get(0);

            params.setPreviewSize(size2.width, size2.height);
            //camera.setPreviewDisplay(mHolder);

            //set color efects to none
            params.setColorEffect(Camera.Parameters.EFFECT_NONE);

            //set antibanding to none
            if (params.getAntibanding() != null) {
                params.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
            }

            // set white ballance
            if (params.getWhiteBalance() != null) {
                params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
            }

            //set flash
            if (params.getFlashMode() != null) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }

            //set zoom
            if (params.isZoomSupported()) {
                params.setZoom(0);
            }

            //set focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);

            mCamera.setParameters(params);
            mCamera.setFaceDetectionListener(this);
        }

    }




    //--EXPRESSIONS (NN)

    /**
     * Send image to NN and get the expression from it
     * @param mouth image of mouth
     */
    private void checkExpression(Bitmap mouth){
        setCurrentExpression(NeuralNetwork.getExpressionFromBitmap(mouth));
    }

    /**
     *
     * @return last recognized expression
     */
    public int getCurrentExpression() {
        return currentExpression;
    }

    /**
     * set label for expression
     * @param currentExpression
     */
    private void setCurrentExpression(int currentExpression) {
        this.currentExpression = currentExpression;
        Log.d("EXPRESSION", Integer.toString(currentExpression));

        boolean isExpression = currentExpression!=-1;
        fcListener.expressionChanged(isExpression,currentExpression);
    }




    //---FACES

    /**
     *
     * @return how many faces are now on frame
     */
    public int getFacesAmount(){
        return facesAmount;
    }

    /**
     *
     * @param facesAmount set amount of faces
     */
    private void setFacesAmount(int facesAmount) {
        this.facesAmount = facesAmount;

        fcListener.facesChanged(isFace(), facesAmount);
    }

    /**
     *
     * @return if there is any face
     */
    public boolean isFace(){
        return (facesAmount>0);
    }





   //---TRIGGERS

    /**
     * set Listener
     * @param faceCounterListener
     */
    public void setFaceCounterListener(MouthListener faceCounterListener) {
        fcListener = faceCounterListener;
    }



    /**
     * open preview from camera
     */
    public void startPreview(){
        isPreview = true;
        surfaceCreated(mHolder);

        mSurfaceView.setVisibility(View.VISIBLE);
        if(isCapturing) mouthBox.setVisibility(VISIBLE);
    }

    /**
     * close preview from camera
     */
    public void stopPreview(){
        isPreview = false;
        mSurfaceView.setVisibility(View.INVISIBLE);
        mouthBox.setVisibility(INVISIBLE);

        if(isCapturing) {
            //surfaceDestroyed(mHolder);
            //setMouthBox(mCamera, mouthBox, camId);
            //surfaceCreated(mHolder);
//            stopCapture();
//            startCapture();

        }
        else
            surfaceDestroyed(mHolder);

    }



    /**
     * start detecting faces
     */
    public void startCapture() {
        Log.d("Capture", "Start");
        isCapturing = true;
        surfaceCreated(mHolder);
        if(isPreview) mouthBox.setVisibility(VISIBLE);
    }

    /**
     * stop detecting faces
     */
    public void stopCapture() {
        Log.d("Capture", "Stop");
        isCapturing = false;

        try{
            mCamera.stopFaceDetection();
        } catch (RuntimeException e){}

        if(!isPreview)
            surfaceDestroyed(mHolder);
        else {
            //stopPreview();
            startPreview();
        }

    }

    /**
     *
     * @return if is now detecting faces
     */
    public boolean isCapturing(){
        return isCapturing;
    }


}
