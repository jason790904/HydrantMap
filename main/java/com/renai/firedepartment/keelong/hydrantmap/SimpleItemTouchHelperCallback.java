package com.renai.firedepartment.keelong.hydrantmap;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by b7918101 on 2017/10/18.
 */

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {


    private final ItemTouchHelperAdapter mAdapter;
    private boolean longPressDragEabled;
    private boolean itemViewSwipeEabled;

    public interface ItemTouchHelperAdapter {

        void onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
        longPressDragEabled =true;
        itemViewSwipeEabled = true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return longPressDragEabled;
    }

    public void setLongPressDragEnabled(boolean enabled) {
        longPressDragEabled = enabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return itemViewSwipeEabled;
    }

    public void setItemViewSwipeEnabled(boolean enabled) {
        itemViewSwipeEabled = enabled;
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
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
