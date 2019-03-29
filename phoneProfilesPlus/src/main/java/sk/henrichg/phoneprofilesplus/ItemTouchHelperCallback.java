package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private final boolean mAllowSwipe;
    private final boolean mLogPressDrag;

    ItemTouchHelperCallback(ItemTouchHelperAdapter adapter,
                            @SuppressWarnings("SameParameterValue") boolean allowSwipe,
                            @SuppressWarnings("SameParameterValue") boolean longPressDrag) {
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
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

}
