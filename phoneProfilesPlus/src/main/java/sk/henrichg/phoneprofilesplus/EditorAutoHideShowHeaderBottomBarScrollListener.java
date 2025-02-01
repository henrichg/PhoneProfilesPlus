package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

abstract class EditorAutoHideShowHeaderBottomBarScrollListener extends RecyclerView.OnScrollListener {

    //final int fragmentType;

    //private static final int THRESHOLD = 200;
    //private int scrolledDistance = 0;
    //private boolean controlsVisible = true;
    //private int distanceShow = 0;
    //private int distanceHide = 0;

    EditorAutoHideShowHeaderBottomBarScrollListener(/*int fragmentType*/) {
        super();
        //this.fragmentType = fragmentType;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

//        Log.e("EditorAutoHideShowHeaderBottomBarScrollListener.onScrolled", "dx="+dx);
//        Log.e("EditorAutoHideShowHeaderBottomBarScrollListener.onScrolled", "dy="+dy);

        //int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
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
            if (dy < -2) {
                // scrolled up (this shows items at bottom)

                //distanceHide = 0;
                //if (distanceShow > THRESHOLD)
                //    distanceShow -= dy;
                //else {
                //    distanceShow = 0;
                    onShow();
                //}
            }
            if (dy > 0) {
                // scrolled down (this shows items at top)

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

    /*
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        //possible states
        //RecyclerView.SCROLL_STATE_IDLE
        //RecyclerView.SCROLL_STATE_DRAGGING <-- This state may be what you are looking for.
        //RecyclerView.SCROLL_STATE_SETTLING

        //if (newState == RecyclerView.SCROLL_STATE_IDLE)
        //    Log.e("HidingRecyclerViewScrollListener.onScrollStateChanged", "newStatex=SCROLL_STATE_IDLE");
        //if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
        //    Log.e("HidingRecyclerViewScrollListener.onScrollStateChanged", "newStatex=SCROLL_STATE_DRAGGING");
        //if (newState == RecyclerView.SCROLL_STATE_SETTLING)
        //    Log.e("HidingRecyclerViewScrollListener.onScrollStateChanged", "newStatex=SCROLL_STATE_SETTLING");

        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                int firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition();

                //show header if first item is first visible position and header is hidden
                if (firstVisibleItem == 0) {
                    View item = layoutManager.findViewByPosition(firstVisibleItem);
                    if (item != null) {
                        if (fragmentType == 1) {
                            EditorProfileListViewHolder viewHolder = (EditorProfileListViewHolder) recyclerView.getChildViewHolder(item);
                            if (viewHolder.editorFragment.activatedProfileHeader.getVisibility() != View.VISIBLE)
                                viewHolder.editorFragment.activatedProfileHeader.setVisibility(View.VISIBLE);
                        } else {
                            EditorEventListViewHolder viewHolder = (EditorEventListViewHolder) recyclerView.getChildViewHolder(item);
                            if (viewHolder.editorFragment.activatedProfileHeader.getVisibility() != View.VISIBLE)
                                viewHolder.editorFragment.activatedProfileHeader.setVisibility(View.VISIBLE);
                        }
                    }
                }

                //show bottom bar if last item is last visible position and bootom bar is hidden
                int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                if (recyclerView.getAdapter() != null) {
                    int lastItemId = recyclerView.getAdapter().getItemCount() - 1;
                    if (lastVisibleItem == lastItemId) {
                        View item = layoutManager.findViewByPosition(lastVisibleItem);
                        if (item != null) {
                            if (fragmentType == 1) {
                                EditorProfileListViewHolder viewHolder = (EditorProfileListViewHolder) recyclerView.getChildViewHolder(item);
                                if (viewHolder.editorFragment.bottomToolbar.getVisibility() != View.VISIBLE)
                                    viewHolder.editorFragment.bottomToolbar.setVisibility(View.VISIBLE);
                            } else {
                                EditorEventListViewHolder viewHolder = (EditorEventListViewHolder) recyclerView.getChildViewHolder(item);
                                if (viewHolder.editorFragment.bottomToolbar.getVisibility() != View.VISIBLE)
                                    viewHolder.editorFragment.bottomToolbar.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        }
    }
    */

    abstract void onHide();
    abstract void onShow();

}
