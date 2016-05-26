package com.emilia.mouthdetection;

import android.graphics.Bitmap;
import android.graphics.RectF;


public class MouthCrop {
	

	
	public static Bitmap getMouthFromBitmap(Bitmap bitmap, boolean recycle, RectF rect){

		//crop to bottom middle 1/4 square
		int x = (int)(rect.left+rect.width()*0.25);
		int y = (int)(rect.top+rect.height()*0.5);
		int height = (int)(rect.height()*0.5);
		int width = height;

		Bitmap croppedMouth = Bitmap.createBitmap(bitmap, x, y, width, height);

		if(recycle){
        	recycleBitmap(bitmap);
        }
        
		return croppedMouth;

	}
	
	private static void recycleBitmap(Bitmap bitmap){
		bitmap.recycle();
	}
}
