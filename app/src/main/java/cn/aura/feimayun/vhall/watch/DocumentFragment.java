package cn.aura.feimayun.vhall.watch;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.MessageServer;
import com.vhall.business.widget.PPTView;
import com.vhall.business.widget.WhiteBoardView;

import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.vhall.BasePresenter;
import cn.aura.feimayun.vhall.util.VhallUtil;

/**
 * 文档页的Fragment HAVE DONE
 */
public class DocumentFragment extends Fragment implements WatchContract.DocumentView, View.OnClickListener {
    public long playerCurrentPosition = 0L; // 当前的进度
    public long playerDuration;
    public String playerDurationTimeStr = "00:00:00";
    public WatchPlaybackPresenter presenter;
    private RelativeLayout document_layout1;
    private ImageView doc_image_action_back;//返回键
    private ImageView doc_click_ppt_live;//PPT切换
    private ImageView doc_click_rtmp_orientation;//水平切换
    private ImageView doc_click_rtmp_watch;//播放键
    //    private ProgressBar pb;
    private SeekBar seekbar;
    private TextView tv_current_time;
    private TextView tv_end_time;
    private PPTView iv_doc;
    private WhiteBoardView board;
    private String url = "";
    private boolean loadingVideo = false;
    private boolean loadingComment = false;
    private WatchActivity activity;
    private int type;

    public static DocumentFragment newInstance() {
        DocumentFragment articleFragment = new DocumentFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vhall_document_fragment, null);
        doc_image_action_back = view.findViewById(R.id.doc_image_action_back);
        doc_click_ppt_live = view.findViewById(R.id.doc_click_ppt_live);
        doc_click_rtmp_orientation = view.findViewById(R.id.doc_click_rtmp_orientation);
        doc_click_rtmp_watch = view.findViewById(R.id.doc_click_rtmp_watch);
        doc_image_action_back.setOnClickListener(this);
        doc_click_ppt_live.setOnClickListener(this);
        doc_click_rtmp_orientation.setOnClickListener(this);
        doc_click_rtmp_watch.setOnClickListener(this);
        seekbar = view.findViewById(R.id.seekbar);
        tv_current_time = view.findViewById(R.id.tv_current_time);
        tv_end_time = view.findViewById(R.id.tv_end_time);
//        pb = view.findViewById(R.id.pb);
        document_layout1 = view.findViewById(R.id.document_layout1);

        int type = activity.getType();

        if (type == VhallUtil.WATCH_PLAYBACK) {
            doc_click_rtmp_watch.setVisibility(View.VISIBLE);
            document_layout1.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iv_doc = (PPTView) getView().findViewById(R.id.iv_doc);
        board = (WhiteBoardView) getView().findViewById(R.id.board);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                activity.getPlaybackPresenter().onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                activity.getPlaybackPresenter().onStopTrackingTouch(seekBar);
            }
        });
    }

    @Override
    public void paintBoard(MessageServer.MsgInfo msgInfo) {
        board.setStep(msgInfo);
    }

    @Override
    public void paintBoard(String key, List<MessageServer.MsgInfo> msgInfos) {
        board.setSteps(key, msgInfos);
    }

    @Override
    public void paintPPT(MessageServer.MsgInfo msgInfo) {
        iv_doc.setStep(msgInfo);
    }

    @Override
    public void paintPPT(String key, List<MessageServer.MsgInfo> msgInfos) {
        iv_doc.setSteps(key, msgInfos);
    }

    @Override
    public void setPresenter(BasePresenter presenter) {

    }

    public void setVisiable(boolean canSee) {
        int type = activity.getType();
        if (canSee) {
//            pb.setVisibility(View.VISIBLE);
            if (type == VhallUtil.WATCH_PLAYBACK) {
                document_layout1.setVisibility(View.VISIBLE);
                doc_click_rtmp_watch.setVisibility(View.VISIBLE);
            }
            doc_image_action_back.setVisibility(View.VISIBLE);
            doc_click_ppt_live.setVisibility(View.VISIBLE);
            doc_click_rtmp_orientation.setVisibility(View.VISIBLE);

        } else {
//            pb.setVisibility(View.INVISIBLE);
            if (type == VhallUtil.WATCH_PLAYBACK) {
                document_layout1.setVisibility(View.INVISIBLE);
                doc_click_rtmp_watch.setVisibility(View.GONE);
            }
            doc_image_action_back.setVisibility(View.GONE);
            doc_click_ppt_live.setVisibility(View.GONE);
            doc_click_rtmp_orientation.setVisibility(View.GONE);

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (WatchActivity) context;
    }

    public void setPlayIcon(boolean isStop) {
        if (isStop) {
            if (doc_click_rtmp_watch != null) {
                doc_click_rtmp_watch.setImageResource(R.drawable.vhall_icon_live_play);
            }
        } else {
            if (doc_click_rtmp_watch != null) {
                doc_click_rtmp_watch.setImageResource(R.drawable.vhall_icon_live_pause);
            }
        }
    }

    public void setProgressLabel(String currentTime, String max) {
        tv_current_time.setText(currentTime);
        tv_end_time.setText(max);
    }

    public void setSeekbarMax(int max) {
        seekbar.setMax(max);
    }

    public void setSeekbarCurrentPosition(int position) {
        seekbar.setProgress(position);
    }

//    public void showProgressbar(boolean show) {
//        if (show)
//            pb.setVisibility(View.VISIBLE);
//        else
//            pb.setVisibility(View.GONE);
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.doc_image_action_back) {
            activity.onBackPressed();
        } else if (id == R.id.doc_click_ppt_live) {
            if (activity != null) {
                activity.setPlace();
            } else {
                Toast.makeText(activity, "程序异常,请重新打开界面", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.doc_click_rtmp_orientation) {
            type = activity.getType();
            if (type == VhallUtil.WATCH_LIVE) {
                activity.getWatchLivePresenter().changeOriention();
            } else if (type == VhallUtil.WATCH_PLAYBACK) {
                activity.getPlaybackPresenter().changeScreenOri();
            }
        } else if (id == R.id.doc_click_rtmp_watch) {
            int type = activity.getType();
            if (type == VhallUtil.WATCH_LIVE) {
                WatchLivePresenter presenter = activity.getWatchLivePresenter();
                presenter.onWatchBtnClick();
            } else if (type == VhallUtil.WATCH_PLAYBACK) {
                WatchPlaybackPresenter presenter = activity.getPlaybackPresenter();
                presenter.onPlayClick();
            }
        }
    }
}
