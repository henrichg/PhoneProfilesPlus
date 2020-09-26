package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

abstract class HidingRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    //private static final int THRESHOLD = 200;
    //private int scrolledDistance = 0;
    //private boolean controlsVisible = true;
    //private int distanceShow = 0;
    //private int distanceHide = 0;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        //int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
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

        /*if (firstVisibleItem == 0) {
            onShow();
        }
        else {*/
            if (dy < 0) {
                //distanceHide = 0;
                //if (distanceShow > THRESHOLD)
                //    distanceShow -= dy;
                //else {
                //    distanceShow = 0;
                    onShow();
                //}
            }
            if (dy > 0) {
                //distanceShow = 0;
                //if (distanceHide < THRESHOLD)
                //    distanceHide += dy;
                //else {
                //    distanceHide = 0;
                    onHide();
                //}
            }
        //}
    }

    abstract void onHide();
    abstract void onShow();

}
