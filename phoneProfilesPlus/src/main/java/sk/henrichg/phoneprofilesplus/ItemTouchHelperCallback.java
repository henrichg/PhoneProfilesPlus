package sk.henrichg.phoneprofilesplus;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private boolean mAllowSwipe;
    private boolean mLogPressDrag;

    ItemTouchHelperCallback(ItemTouchHelperAdapter adapter, boolean allowSwipe, boolean longPressDrag) {
        mAdapter = adapter;
        mAllowSwipe = allowSwipe;
        mLogPressDrag = longPressDrag;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return mLogPressDrag;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return mAllowSwipe;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

}
