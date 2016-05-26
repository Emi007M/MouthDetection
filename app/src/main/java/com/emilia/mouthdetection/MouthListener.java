package com.emilia.mouthdetection;

/**
 * Created by Emilia on 25.05.2016.
 */
public class MouthListener {


    /**
     * Listener for DetectMouth
     * can be used by implementing MouthListener.Listener in Activity
     */
    public interface Listener {
        public void onFaceAmountChange(boolean state, int amount);
        public void onExpressionChange(boolean recognized, int expression);
    }

    private Listener mListener = null;
    public void registerListener (Listener listener) {
        mListener = listener;
    }

    // -----------------------------
    // the part that this class does


    /**
     *
     * @param isFace is there a face on preview
     * @param amount amount of found faces
     */
    public void facesChanged(boolean isFace, int amount) {
        if (mListener != null)
            mListener.onFaceAmountChange(isFace, amount);
    }

    /**
     *
     * @param recognized is the expression in range of labels
     * @param expression label of the expression
     */
    public void expressionChanged(boolean recognized, int expression){
        if(mListener != null)
            mListener.onExpressionChange(recognized,expression);
    }

}
