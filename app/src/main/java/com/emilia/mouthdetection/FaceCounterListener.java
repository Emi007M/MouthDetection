package com.emilia.mouthdetection;

/**
 * Created by Emilia on 25.05.2016.
 */
public class FaceCounterListener {


    // all the listener stuff below
    public interface Listener {
        public void onFaceAmountChange(boolean state, int amount);
    }

    private Listener mListener = null;
    public void registerListener (Listener listener) {
        mListener = listener;
    }

    // -----------------------------
    // the part that this class does

    private boolean isFace = true;
    public void alarm(boolean f, int amount) {

        isFace=f;

        if (mListener != null)
            mListener.onFaceAmountChange(isFace, amount);
    }

}
