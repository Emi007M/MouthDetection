package com.emilia.mouthdetection;

import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by Emilia on 25.05.2016.
 */
public class NeuralNetwork {

    static int currentExpression;
    static int labels = 4;


    public static int getExpressionFromBitmap(Bitmap mouth){

        Random rand = new Random();
        currentExpression = rand.nextInt(labels);

        return currentExpression;
    }

}
