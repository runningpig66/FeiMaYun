package cn.aura.feimayun.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import cn.aura.feimayun.R;

/**
 * 创建自定义dialog，主要学习其实现原理
 */
public class SelfDialog extends Dialog {
    private TextView yes;//确定按钮
    private TextView no;//取消按钮
    private TextView titleTv;//消息标题文本
    private TextView messageTv;//消息提示文本
    private Boolean cancelable = false;
    private Boolean canceledOnTouchOutside = false;

    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器

    public SelfDialog(Context context) {
        super(context, R.style.MyDialog);
        setContentView(R.layout.dialog_call);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(canceledOnTouchOutside);
        setCancelable(cancelable);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }

    public void setMyCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public void setMyCanceledOnTouchOutside(boolean b) {
        this.canceledOnTouchOutside = b;
    }

    /**
     * 设置取消按钮的显示内容和监听
     */
    public void setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (!TextUtils.isEmpty(str)) {
            no.setText(str);
        }
        this.noOnclickListener = onNoOnclickListener;
    }

    /**
     * 设置确定按钮的显示内容和监听
     */
    public void setYesOnclickListener(String str, onYesOnclickListener onYesOnclickListener) {
        if (!TextUtils.isEmpty(str)) {
            yes.setText(str);
        }
        this.yesOnclickListener = onYesOnclickListener;
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        yes = findViewById(R.id.dialog_call_confirm);
        no = findViewById(R.id.dialog_call_cancel);
        titleTv = findViewById(R.id.dialog_call_textview1);
        messageTv = findViewById(R.id.dialog_call_textview2);
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null) {
                    noOnclickListener.onNoClick();
                }
            }
        });
    }

    /**
     * 从外界Activity为Dialog设置标题
     */
    public void setTitle(String title) {
        titleTv.setText(title);
    }

    /**
     * 从外界Activity为Dialog设置message
     */
    public void setMessage(String message) {
        messageTv.setText(message);
    }

    /**
     * 设置确定按钮和取消按钮被点击的接口
     */
    public interface onYesOnclickListener {
        void onYesClick();
    }

    public interface onNoOnclickListener {
        void onNoClick();
    }
}