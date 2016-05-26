package com.emilia.mouthdetection;

import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by Emilia on 25.05.2016.
 */
public class NeuralNetwork {

    static int currentExpression;
    static int labels = 4;


    /**
     *
     * @param mouth cropped image of mouth
     * @return value of the label [0,labels], or -1 if not recognized
     */
    public static int getExpressionFromBitmap(Bitmap mouth){

        //TODO: implement neural network

        Random rand = new Random();
        currentExpression = rand.nextInt(labels);

        return currentExpression;
    }

}
