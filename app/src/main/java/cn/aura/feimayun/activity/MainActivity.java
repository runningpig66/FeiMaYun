package cn.aura.feimayun.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.fragment.FullCourseFragment;
import cn.aura.feimayun.fragment.HomePageFragment;
import cn.aura.feimayun.fragment.MessageCenterFragment;
import cn.aura.feimayun.fragment.MyStudiesFragment;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 描述：主活动
 */

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, EasyPermissions.PermissionCallbacks {
    public final static String[] PERMS_WRITE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static Handler mHandler;//检查版本更新
    public RadioGroup rg_bt;
    public boolean[] isRequestSuccess = new boolean[4];
    private FragmentManager fm;
    private RadioButton selector1_rb, selector2_rb, selector3_rb, selector4_rb;
    private HomePageFragment homePageFragment;
    private FullCourseFragment fullCourseFragment;
    private MyStudiesFragment myStudiesFragment;
    private MessageCenterFragment messageCenterFragment;
    private ImageView activity_main_layout_redpoint;//小红点
    private String fileName;
    private String targetUrl;
    private boolean isExit = false;
    private String saveName = "myApp.apk";

    //设置未读消息小圆点是否显示：true显示
    public void setRedPointVisiable(boolean isShow) {
        if (isShow) {
            activity_main_layout_redpoint.setVisibility(View.VISIBLE);
        } else {
            activity_main_layout_redpoint.setVisibility(View.GONE);
        }
    }

    @SuppressLint("HandlerLeak")
    private void handle() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!msg.obj.toString().equals("网络异常")) {
                    parseUpdate(msg.obj.toString());
                }
            }
        };
    }

    private void parseUpdate(String s) {
        Util.d("061401", s);
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 0) {
                String errno = jsonObject.getString("errno");
                if (errno.equals("E1001")) {//有新版本
                    String url = jsonObject.getString("url");//获取新版本下载地址

                    targetUrl = url.replaceFirst("yun", "us");

                    View view = LayoutInflater.from(this).inflate(R.layout.dialog_call, null);
                    TextView dialog_call_textview1 = view.findViewById(R.id.dialog_call_textview1);
                    TextView dialog_call_textview2 = view.findViewById(R.id.dialog_call_textview2);
                    dialog_call_textview1.setText("发现新版本");
                    dialog_call_textview2.setText("是否升级到最新版本？");
                    new TDialog.Builder(getSupportFragmentManager())
                            .setDialogView(view)
                            .setScreenWidthAspect(this, 0.7f)
                            .addOnClickListener(R.id.dialog_call_confirm, R.id.dialog_call_cancel)
                            .setOnViewClickListener(new OnViewClickListener() {
                                @Override
                                public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                    switch (view.getId()) {
                                        case R.id.dialog_call_cancel:
                                            tDialog.dismiss();
                                            break;
                                        case R.id.dialog_call_confirm:
                                            //开始下载新版本
                                            //申请权限
                                            if (EasyPermissions.hasPermissions(MainActivity.this, PERMS_WRITE)) {
                                                listener(downLoadApk(MainActivity.this, "下载更新", targetUrl));
                                            } else {
                                                EasyPermissions.requestPermissions(MainActivity.this, "下载更新需要开启部分权限",
                                                        0x1000, PERMS_WRITE);
                                            }
                                            tDialog.dismiss();
                                            break;
                                    }
                                }
                            })
                            .create()
                            .show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            handle();
            initView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @Override
    protected boolean needRegisterNetworkChangeObserver() {
        return true;
    }

    @Override
    public void onNetDisconnected() {
//        Toast.makeText(this, "网络断开", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetConnected(NetworkInfo networkInfo) {
        getRequestSuccess();
    }

    public void getRequestSuccess() {
        isRequestSuccess[0] = homePageFragment.isRequestSuccess;
        isRequestSuccess[1] = fullCourseFragment.isRequestSuccess;
        isRequestSuccess[2] = myStudiesFragment.isRequestSuccess;
        isRequestSuccess[3] = messageCenterFragment.isRequestSuccess;
        if (!isRequestSuccess[0] || !isRequestSuccess[1] || !isRequestSuccess[2] || !isRequestSuccess[3]) {//如果有页面没有下载成功
            if (!isRequestSuccess[0]) {
                homePageFragment.initData();
            }
            if (!isRequestSuccess[1]) {
                fullCourseFragment.initData();
            }
            if (!isRequestSuccess[2]) {
//                myStudiesFragment.initData();
            }
            if (!isRequestSuccess[3]) {
                messageCenterFragment.initData();
            }
        }
    }

    private void initView() {
        activity_main_layout_redpoint = findViewById(R.id.activity_main_layout_redpoint);//未读消息小圆点
        rg_bt = findViewById(R.id.rg_bt);
        selector1_rb = findViewById(R.id.selector1_rb);
        selector2_rb = findViewById(R.id.selector2_rb);
        selector3_rb = findViewById(R.id.selector3_rb);
        selector4_rb = findViewById(R.id.selector4_rb);
        rg_bt.setOnCheckedChangeListener(this);

        fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        defaultChildShow(fragmentTransaction);
        selector1_rb.setChecked(true);
    }

    private void initData() {
        //开始版本检查
        RequestURL.sendUpdate("https://app.feimayun.com/version/version", mHandler);
    }

    //设置默认加载页
    private void defaultChildShow(FragmentTransaction fragmentTransaction) {
        hideChildFragment(fragmentTransaction);
        if (homePageFragment == null) {
            //首次进入软件，加载主页的4个碎片页面，如果不加载，首页无法跳转
            homePageFragment = new HomePageFragment();
            fullCourseFragment = new FullCourseFragment();
            myStudiesFragment = new MyStudiesFragment();
            messageCenterFragment = new MessageCenterFragment();
            fragmentTransaction.add(R.id.main_content, homePageFragment);
            fragmentTransaction.add(R.id.main_content, fullCourseFragment);
            fragmentTransaction.add(R.id.main_content, myStudiesFragment);
            fragmentTransaction.add(R.id.main_content, messageCenterFragment);
        } else {
            //进入软件显示首页e
            fragmentTransaction.show(homePageFragment);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    //隐藏所有fragment
    private void hideChildFragment(FragmentTransaction fragmentTransaction) {
        if (homePageFragment != null) {
            fragmentTransaction.hide(homePageFragment);
        }
        if (fullCourseFragment != null) {
            fragmentTransaction.hide(fullCourseFragment);
        }
        if (myStudiesFragment != null) {
            fragmentTransaction.hide(myStudiesFragment);
        }
        if (messageCenterFragment != null) {
            fragmentTransaction.hide(messageCenterFragment);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        hideChildFragment(fragmentTransaction);
        switch (checkedId) {
            case R.id.selector1_rb:
                selector1_rb.setChecked(true);
                if (homePageFragment == null) {
                    homePageFragment = new HomePageFragment();
                    fragmentTransaction.add(R.id.main_content, homePageFragment);
                } else {
                    fragmentTransaction.show(homePageFragment);
                }
                break;
            case R.id.selector2_rb:
                selector2_rb.setChecked(true);
                if (fullCourseFragment == null) {
                    fullCourseFragment = new FullCourseFragment();
                    fragmentTransaction.add(R.id.main_content, fullCourseFragment);
                } else {
                    fragmentTransaction.show(fullCourseFragment);
                }
                break;
            case R.id.selector3_rb:
                selector3_rb.setChecked(true);
                if (myStudiesFragment == null) {
                    myStudiesFragment = new MyStudiesFragment();
                    fragmentTransaction.add(R.id.main_content, myStudiesFragment);
                } else {
                    fragmentTransaction.show(myStudiesFragment);
                }
                break;
            case R.id.selector4_rb:
                selector4_rb.setChecked(true);
                if (messageCenterFragment == null) {
                    messageCenterFragment = new MessageCenterFragment();
                    fragmentTransaction.add(R.id.main_content, messageCenterFragment);
                } else {
                    fragmentTransaction.show(messageCenterFragment);
                }

                //跳转到消息中心时，判断是否登录
                String uid = Util.getUid();
                if (uid.equals("")) {//如果未登录，则打开登录界面
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果用户按下了返回键
            exitByDoubleClick();
        }
        //返回false时，表示并没有完全处理完该事件，更希望其他回调方法继续对其进行处理，例如Activity中的回调方法。
        return false;
    }

    private void exitByDoubleClick() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "再按一次退出程序!", Toast.LENGTH_SHORT).show();
            Timer tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;//取消退出
                }
            }, 2000);// 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case 0x1000:
                if (EasyPermissions.hasPermissions(this, PERMS_WRITE)) {
                    listener(downLoadApk(MainActivity.this, "下载更新", targetUrl));
                }
                break;
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
//        Toast.makeText(this, "onPermissionsDenied:" + requestCode + ":" + perms.size(), Toast.LENGTH_SHORT).show();

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.

        switch (requestCode) {
            case 0x1000:
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    new AppSettingsDialog.Builder(this)
                            .setRequestCode(0x1000)
                            .setTitle("下载更新需要开启必要的权限")
                            .setRationale("您已拒绝开启部分权限，这将导致无法正常更新，是否打开设置界面开启权限？")
                            .setNegativeButton("取消")
                            .setPositiveButton("确认")
                            .build()
                            .show();
                } else {
                    Toast.makeText(this, "权限拒绝无法下载更新", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //下载apk
    public long downLoadApk(Context context, String title, String url) {
        deleteFile();

        Toast.makeText(context, "后台下载中...", Toast.LENGTH_SHORT).show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, saveName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //设置Notifucation信息
        request.setTitle(title);
        request.setDescription("下载完成后请点击打开");
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();
        // 设置文件类型，可以在下载结束后自动打开该文件
//        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType("application/vnd.android.package-archive");//一样的效果
        //实例化DownloadManager对象
        DownloadManager downloadManager = (DownloadManager) MyApplication.context.getSystemService(Context.DOWNLOAD_SERVICE);
        return Objects.requireNonNull(downloadManager).enqueue(request);
    }

    private void listener(final long Id) {
        //注册广播监听系统的下载事件完成
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                //这里是通过下面这个方法获取下载id的
                long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                //这里把传递的id和广播中获取的id进行对比是不是我们下载apk的那个id，如果是的话，就开始获取这个下载的路径
                if (ID == Id) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(Id);
                    Cursor cursor = Objects.requireNonNull(manager).query(query);
                    if (cursor.moveToNext()) {
                        //获取文件下载路径
                        fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        //如果文件名不为空，说明文件已经存在，则进行自动安装apk
                        if (fileName != null) {
                            boolean installAllowed;
                            if (Build.VERSION.SDK_INT >= 26) {//安卓8.0自动安装 VERSION_CODES.O
                                //判断应用是否有权限安装apk
                                installAllowed = getPackageManager().canRequestPackageInstalls();
                                if (!installAllowed) {//没有权限
                                    Toast.makeText(context, "安装应用需要打开未知来源权限，请开启权限", Toast.LENGTH_SHORT).show();
                                    startInstallPermissionSettingActivity();
                                } else {
                                    openAPK(fileName);
                                }
                            } else {
                                openAPK(fileName);
                            }
                        }
                    }
                    cursor.close();
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    //安装APK
    private void openAPK(String fileSavePath) {
        File file = new File(Objects.requireNonNull(Uri.parse(fileSavePath).getPath()));
        String filePath = file.getAbsolutePath();//获取绝对路径
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于7.0 需要内容提供器
            //生成文件的uri
            //包名+fileprovider
            data = FileProvider.getUriForFile(MainActivity.this, "cn.aura.app.fileprovider", new File(filePath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//给目标应用一个临时授权
        } else {
            data = Uri.fromFile(file);
        }

        intent.setDataAndType(data, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10086:
                if (resultCode == RESULT_OK) {
                    openAPK(fileName);
                }
                break;
            case 0x1000:
                if (EasyPermissions.hasPermissions(this, PERMS_WRITE)) {
                    listener(downLoadApk(MainActivity.this, "下载更新", targetUrl));
                } else {
                    Toast.makeText(this, "权限拒绝无法更新", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, 10086);
    }

    //     request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "ausee.apk");
    public void deleteFile() {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + saveName;
        File file = new File(downloadPath);
        String fileName = saveName;
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
        }
    }

}
