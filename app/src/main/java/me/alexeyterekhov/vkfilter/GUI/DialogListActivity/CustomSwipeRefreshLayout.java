package me.alexeyterekhov.vkfilter.GUI.DialogListActivity;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    private RecyclerView list;
    private Runnable onDown = null;

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRecyclerView(RecyclerView listView) {
        this.list = listView;
    }

    @Override
    public boolean canChildScrollUp() {
        if (list != null) {
            LinearLayoutManager man = (LinearLayoutManager) list.getLayoutManager();
            if (man != null) {
                if (man.findFirstVisibleItemPosition() == 0) {
                    if (list.getChildAt(0) != null) {
                        if (list.getChildAt(0).getTop() < -1)
                            return true;
                    } else
                        return false;
                } else
                    return true;
            }
        }
        return false;
    }

    public void setOnDownAction(Runnable onDownAction) {
        onDown = onDownAction;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && onDown != null)
            onDown.run();
        return super.onTouchEvent(event);
    }
}
