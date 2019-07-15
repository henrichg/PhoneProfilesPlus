package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

abstract class HidingRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    //private static final int HIDE_THRESHOLD = 20;
    //private int scrolledDistance = 0;
    //private boolean controlsVisible = true;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        //noinspection ConstantConditions
        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        //Log.e("HidingRecyclerViewScrollListener.onScrolled", "firstVisibleItem="+firstVisibleItem);
        //show views if first item is first visible position and views are hidden
        /*if (firstVisibleItem == 0) {
            if(!controlsVisible) {
                onShow();
                controlsVisible = true;
            }
        } else {
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                controlsVisible = true;
                scrolledDistance = 0;
            }
        }*/

        /*if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
            scrolledDistance += dy;
        }*/

        if (firstVisibleItem == 0) {
            onShow();
        }
        else {
            //if (dy < 0)
            //    onShow();
            if (dy > 0)
                onHide();
        }
    }

    abstract void onHide();
    abstract void onShow();

}
