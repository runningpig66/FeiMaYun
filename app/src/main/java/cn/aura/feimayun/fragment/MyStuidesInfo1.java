package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.MyStudiesInfo1_RecyclerView_Adapter;
import cn.aura.feimayun.bean.MyStuidesInfo1Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.ProgressDialog;

public class MyStuidesInfo1 extends Fragment {
    private static Handler handleNetwork;
    private ProgressDialog progressDialog;
    private AppCompatActivity context;
    //    private List<Map<String, String>> dataList = new ArrayList<>();
    private List<MyStuidesInfo1Bean.DataBeanX.DataBean> dataBeanList;
    private RelativeLayout mystudiesinfo1_layout1;
    private RecyclerView mystudiesinfo1_recyclerview;
    private String pkid;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(context, "请检查网络连接_Error35", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseJson(msg.obj.toString());
                }
            }
        };
    }

    private void parseJson(String s) {
//        s = "{\"status\":1,\"msg\":\"success\",\"data\":{\"teach_type\":3,\"pkid\":1030,\"data\":[{},{\"lid\":\"747\",\"name\":\"\\u5927\\u6570\\u636e\\u5b9e\\u6218-\\u57fa\\u7840\\u77e5\\u8bc6\",\"bg_url\":\"https:\\/\\/img01.feimayun.com\\/wx\\/manage\\/kc\\/2019\\/2019-01\\/20190117140312_76352_560x310.jpg\",\"type\":2,\"total\":3,\"learned\":1,\"rate\":\"5%\",\"stat\":1,\"typer\":\"\\u7ee7\\u7eed\\u89c2\\u770b\"},{\"lid\":\"751\",\"name\":\"\\u5927\\u6570\\u636e\\u5b9e\\u6218-\\u5ef6\\u4f38\\u89c6\\u9891\",\"bg_url\":\"https:\\/\\/img01.feimayun.com\\/wx\\/manage\\/kc\\/2019\\/2019-01\\/20190117140337_14648_560x310.jpg\",\"type\":2,\"total\":3,\"learned\":2,\"rate\":\"8%\",\"stat\":1,\"typer\":\"\\u7ee7\\u7eed\\u89c2\\u770b\"},{\"lid\":\"749\",\"name\":\"\\u5927\\u6570\\u636e\\u5b9e\\u6218-\\u8fdb\\u9636\\u89c6\\u9891\",\"bg_url\":\"https:\\/\\/img01.feimayun.com\\/wx\\/manage\\/kc\\/2019\\/2019-01\\/20190117140324_4122_560x310.jpg\",\"type\":2,\"total\":9,\"learned\":1,\"rate\":\"1%\",\"stat\":1,\"typer\":\"\\u7ee7\\u7eed\\u89c2\\u770b\"},{\"lid\":\"1029\",\"name\":\"BDP\\u7b2c22\\u671f\",\"bg_url\":\"https:\\/\\/img01.feimayun.com\\/wx\\/manage\\/kc\\/2019\\/2019-02\\/20190222085101_60943_560x310.png\",\"type\":2,\"total\":0,\"learned\":0,\"rate\":\"\\u672a\\u5f00\\u59cb\",\"stat\":0,\"typer\":\"\\u5f00\\u59cb\\u89c2\\u770b\"}]}}\n";
//        Util.d("021401", s);
        Gson gson = new Gson();
        MyStuidesInfo1Bean myStuidesInfo1Bean = gson.fromJson(s, MyStuidesInfo1Bean.class);
        int status = myStuidesInfo1Bean.getStatus();
        if (status == 1) {
            MyStuidesInfo1Bean.DataBeanX dataBeanX = myStuidesInfo1Bean.getData();
            if (dataBeanX != null) {
                dataBeanList = dataBeanX.getData();
                pkid = String.valueOf(dataBeanX.getPkid());
            }
        }

        if (dataBeanList != null && !dataBeanList.isEmpty()) {
            mystudiesinfo1_recyclerview.setVisibility(View.VISIBLE);
            mystudiesinfo1_layout1.setVisibility(View.GONE);
            initRecyclerView();
        } else {
            mystudiesinfo1_recyclerview.setVisibility(View.GONE);
            mystudiesinfo1_layout1.setVisibility(View.VISIBLE);
        }
//        "status":1,
//                "msg":"success",
//                "data":{
//            "teach_type":3,
//                    "pkid":832
//        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

//        JSONTokener jsonTokener = new JSONTokener(s);
//        try {
//            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//            int status = jsonObject.getInt("status");
//            if (status == 1) {
//                JSONObject dataObject = jsonObject.getJSONObject("data");
////                int teach_type = dataObject.getInt("teach_type");
////                int pkid = dataObject.getInt("pkid");
//                if (dataObject.has("data")) {
//                    JSONArray dataArray = dataObject.getJSONArray("data");
//                    dataList.clear();
//                    for (int i = 0; i < dataArray.length(); i++) {
//                        JSONObject dataInnerObject = dataArray.getJSONObject(i);
//                        Map<String, String> dataInnerMap = new HashMap<>();
//                        int type = dataInnerObject.getInt("type");
//                        if (type == 1) {//直播
//                            dataInnerMap.put("name", dataInnerObject.getString("name"));
//                            dataInnerMap.put("lid", dataInnerObject.getString("lid"));
//                            dataInnerMap.put("bg_url", dataInnerObject.getString("bg_url"));
//                            dataInnerMap.put("start_ts", dataInnerObject.getString("start_ts"));
//                            dataInnerMap.put("end_ts", dataInnerObject.getString("end_ts"));
//                            dataInnerMap.put("webinar_id", dataInnerObject.getString("webinar_id"));
//                            dataInnerMap.put("type", dataInnerObject.getString("type"));
//                            dataInnerMap.put("liveStatus", dataInnerObject.getString("liveStatus"));
//                            dataInnerMap.put("stat", dataInnerObject.getString("stat"));
//                            dataList.add(dataInnerMap);
//                        } else if (type == 2) {//录播
//                            dataInnerMap.put("lid", dataInnerObject.getString("lid"));
//                            dataInnerMap.put("name", dataInnerObject.getString("name"));
//                            dataInnerMap.put("bg_url", dataInnerObject.getString("bg_url"));
//                            dataInnerMap.put("type", dataInnerObject.getString("type"));
//                            dataInnerMap.put("total", dataInnerObject.getString("total"));
//                            dataInnerMap.put("learned", dataInnerObject.getString("learned"));
//                            dataInnerMap.put("rate", dataInnerObject.getString("rate"));
//                            dataInnerMap.put("stat", dataInnerObject.getString("stat"));
//                            dataInnerMap.put("typer", dataInnerObject.getString("typer"));
//                            dataList.add(dataInnerMap);
//                        }
//                    }
//                    initRecyclerView();
//                } else {
//                    Toast.makeText(context, "暂无视频", Toast.LENGTH_SHORT).show();
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            progressDialog.dismiss();
//        } finally {
//            progressDialog.dismiss();
//        }
    }

    private void initRecyclerView() {
        MyStudiesInfo1_RecyclerView_Adapter adapter = new MyStudiesInfo1_RecyclerView_Adapter(context, dataBeanList, pkid);
        mystudiesinfo1_recyclerview.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (AppCompatActivity) context;
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            if (isFirstIn) {
//                isFirstIn = false;
//            } else {
//                if (dataList.isEmpty()) {
//                    Toast.makeText(context, "暂无视频", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handle();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(context);
        progressDialog.show();

        String lid = Objects.requireNonNull(getArguments()).getString("lid");
        Map<String, String> map = new HashMap<>();
        map.put("uid", Util.getUid());
        map.put("lid", lid);
//        Log.i("021402", "uid:" + Util.getUid() + ", lid:" + lid);
        RequestURL.sendPOST("https://app.feimayun.com/User/myLesson", handleNetwork, map, context);

        View view = inflater.inflate(R.layout.fragment_mystudiesinfo1, container, false);
        mystudiesinfo1_recyclerview = view.findViewById(R.id.mystudiesinfo1_recyclerview);
        mystudiesinfo1_layout1 = view.findViewById(R.id.mystudiesinfo1_layout1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mystudiesinfo1_recyclerview.setLayoutManager(layoutManager);
        return view;
    }

}