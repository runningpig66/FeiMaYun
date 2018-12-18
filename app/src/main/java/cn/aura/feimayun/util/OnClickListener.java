package cn.aura.feimayun.util;

import android.view.View;

public abstract class OnClickListener implements View.OnClickListener {

    private final int SPACE_TIME = 2000;
    private long lastClickTime = 0;

    public boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        boolean isDoubleClick;
        if (currentTime - lastClickTime > SPACE_TIME) {
            isDoubleClick = false;
        } else {
            isDoubleClick = true;
        }
        lastClickTime = currentTime;
        return isDoubleClick;
    }
}
