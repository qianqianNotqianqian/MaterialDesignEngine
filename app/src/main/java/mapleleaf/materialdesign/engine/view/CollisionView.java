package mapleleaf.materialdesign.engine.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import mapleleaf.materialdesign.engine.utils.CollisionPresenter;

/**
 * 碰撞view
 */
public class CollisionView extends FrameLayout {
    private CollisionPresenter collisionPresenter;

    public CollisionView(@NonNull Context context) {
        this(context, null);
    }

    public CollisionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollisionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //开启ondraw方法回调
        setWillNotDraw(false);

        collisionPresenter = new CollisionPresenter();
        collisionPresenter.setDensity(getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        collisionPresenter.updateBounds(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            //子viwe设置body
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                collisionPresenter.bindBody(view);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        collisionPresenter.startWorld();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            collisionPresenter.drawView(view);
        }
        invalidate();
    }

    public void onSensorChanged(float x, float y) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            collisionPresenter.applyLinearImpulse(x, y, view);
        }
    }
}
