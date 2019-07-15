package sk.henrichg.phoneprofilesplus;

import android.widget.AbsListView;

abstract class HidingAbsListViewScrollListener implements AbsListView.OnScrollListener {

    private int mLastFirstVisibleItem;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        final int currentFirstVisibleItem = view.getFirstVisiblePosition();

        if (currentFirstVisibleItem == 0)
            onShow();
        else {
            if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                // scrolling up
                onHide();
            }/* else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                // scrolling down
                onShow();
            }*/
        }

        mLastFirstVisibleItem = currentFirstVisibleItem;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    abstract void onHide();
    abstract void onShow();

}
