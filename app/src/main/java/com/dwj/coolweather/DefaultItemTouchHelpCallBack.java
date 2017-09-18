package com.dwj.coolweather;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by duWenJun on 17-9-18.
 */

public class DefaultItemTouchHelpCallBack extends ItemTouchHelper.Callback {

    private boolean isSwipeEnable = false;
    private boolean isLongPressDragEnabled = false;

    private final OnItemTouchCallBackListener mCallBackListener;

    public DefaultItemTouchHelpCallBack(OnItemTouchCallBackListener callBackListener) {
        this.mCallBackListener = callBackListener;
    }

    /**
     * 实现两个接口 用来设置是否可以执行拖拽和滑动删除的动作
     */


    public void setSwipeEnabled(boolean isSwipeEnable) {
        this.isSwipeEnable = isSwipeEnable;
    }

    public void setLongPressDragEnabled(boolean isLongPressDragEnabled) {
        this.isLongPressDragEnabled = isLongPressDragEnabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return this.isSwipeEnable;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return this.isLongPressDragEnabled;
    }

    /**
     * 当用户拖拽或者滑动Item的时候需要我们告诉系统滑动或者拖拽的方向
     *
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {// GridLayoutManager
            // flag如果值是0，相当于这个功能被关闭
            int dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //滑动功能关闭
            int swipeFlag = 0;
            // create make
            return makeMovementFlags(dragFlag, swipeFlag);
        } else if (layoutManager instanceof LinearLayoutManager) {// linearLayoutManager
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int orientation = linearLayoutManager.getOrientation();

            int dragFlag = 0;
            int swipeFlag = 0;

            // 为了方便理解，相当于分为横着的ListView和竖着的ListView
            if (orientation == LinearLayoutManager.HORIZONTAL) {// 如果是横向的布局
                swipeFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (orientation == LinearLayoutManager.VERTICAL) {// 如果是竖向的布局，相当于ListView
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                swipeFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            }
            return makeMovementFlags(dragFlag, swipeFlag);
        }
        return 0;
    }

    /**
     * 当Item被拖拽的时候被回调
     *
     * @param recyclerView     recyclerView
     * @param viewHolder    拖拽的ViewHolder
     * @param target 目的地的viewHolder
     * @return
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (mCallBackListener != null) {
            return mCallBackListener.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (mCallBackListener != null) {
            mCallBackListener.onSwiped(viewHolder.getAdapterPosition());
        }
    }

    public interface OnItemTouchCallBackListener {
        void onSwiped(int position);

        boolean onMove(int srcPosition, int targetPosition);
    }
}
