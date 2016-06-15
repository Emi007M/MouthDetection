package com.emilia.mouthdetection;

import android.graphics.Bitmap;

/**
 * Created by Emilia on 25.05.2016.
 */
public class NeuralNetwork {

    static int currentExpression;
    static int labels = 4;


    /**
     * Bridge to C language Neural Network
     * @param mouth cropped image of mouth
     * @return value of the label [0,labels], or -1 if not recognized
     */
    public static int getExpressionFromBitmap(Bitmap mouth){



        short [] test = new short[1];
        float [] out = new float[1];
        test[0] = 1;
        out = classify(test);

        currentExpression = (int)out[0];

        return currentExpression;
    }

    //adding bridge to C NN
    static {
        System.loadLibrary("neural-network");
    }
    public static native float [] classify(short [] bitmap);

}
