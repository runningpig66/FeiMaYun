package cn.aura.feimayun.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class MoveFrameLayoutIntercept extends android.widget.FrameLayout {
    private boolean canMove = true;//设置是否可以移动

    private int lastX;
    private int lastY;

    public MoveFrameLayoutIntercept(@NonNull Context context) {
        super(context);
        initView();
    }

    public MoveFrameLayoutIntercept(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MoveFrameLayoutIntercept(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = rawX;
                lastY = rawY;
                break;
            case MotionEvent.ACTION_MOVE:
                int offsetX = rawX - lastX;
                int offsetY = rawY - lastY;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                layoutParams.setMargins(
                        layoutParams.leftMargin += offsetX,
                        layoutParams.topMargin += offsetY,
                        layoutParams.rightMargin -= offsetX,
                        layoutParams.bottomMargin -= offsetY);
                setLayoutParams(layoutParams);
                lastX = rawX;
                lastY = rawY;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        if (canMove) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    //绝对坐标方式
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int rawX = (int) event.getRawX();
//        int rawY = (int) event.getRawY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                lastX = rawX;
//                lastY = rawY;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                // 计算偏移量
//                int offsetX = rawX - lastX;
//                int offsetY = rawY - lastY;
//                // 在当前left、top、right、bottom的基础上加上偏移量
////                layout(getLeft() + offsetX, getTop() + offsetY, getRight() + offsetX, getBottom() + offsetY);
//                offsetLeftAndRight(offsetX);
//                offsetTopAndBottom(offsetY);
//                // 重新设置初始坐标
//                lastX = rawX;
//                lastY = rawY;
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        if (canMove) {
//            return true;
//        } else {
//            return super.onTouchEvent(event);
//        }
//
//    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
}
