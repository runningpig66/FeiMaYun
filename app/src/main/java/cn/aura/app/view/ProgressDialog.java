package cn.aura.app.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import cn.aura.app.R;

public class ProgressDialog {
    private Dialog progressDialog;
    private AnimationDrawable animationDrawable;
    private Context context;

    public ProgressDialog(Context context) {
        this.context = context;
    }

    private void init() {
        progressDialog = new Dialog(context, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(true);

        ImageView bar = progressDialog.findViewById(R.id.loadingPrigressBar);
        bar.setImageResource(R.drawable.progress_drawable_white);
        animationDrawable = (AnimationDrawable) bar.getDrawable();
    }

    public void show() {
        init();
        animationDrawable.start();
        progressDialog.show();
    }

    public void dismiss() {
        animationDrawable.stop();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void destory() {
        progressDialog = null;
        animationDrawable = null;
    }

}
