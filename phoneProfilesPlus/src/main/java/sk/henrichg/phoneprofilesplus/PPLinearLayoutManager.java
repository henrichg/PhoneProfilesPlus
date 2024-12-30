package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PPLinearLayoutManager extends LinearLayoutManager {

    public PPLinearLayoutManager(Context context) {
        super(context);
    }

    public PPLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public PPLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e){
            Log.e("PPLinearLayoutManager.onLayoutChildren", Log.getStackTraceString(e));
        }
    }
}