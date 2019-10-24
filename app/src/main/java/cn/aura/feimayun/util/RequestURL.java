package cn.aura.feimayun.util;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

/**
 * 描述：访问网络的模板类
 */
public class RequestURL {
    public static final String version = "18";
    private static final String osName = "Android";
    private static final String verName = "2.2";
    private static boolean isTest = false;//测试  ？
    private static String apidString = isTest ? "school.feimayun.com" : "yun.aura.cn";

    //在子线程中访问网络，GET访问
    public static void sendGET(final String urlPath, final Handler handler, final AppCompatActivity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String aptk;
                String apud = Util.getUid();
                String secret = Util.getSecret();
                if (apud.equals("")) {
                    aptk = new Md5().getDateToken();
                } else {
                    aptk = new Md5().getUidToken(apud);
                }
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlPath);
                    //打开连接
                    connection = (HttpURLConnection) url.openConnection();
                    //接收模式
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("apud", apud);
                    connection.setRequestProperty("aptk", aptk);
                    connection.setRequestProperty("secket", secret);
                    connection.setRequestProperty("osName", osName);
                    connection.setRequestProperty("verName", verName);
                    connection.setRequestProperty("version", version);
                    connection.setRequestProperty("apid", apidString);
                    //连接超时，单位毫秒
                    connection.setConnectTimeout(4000);
                    //读取超时，单位毫秒
                    connection.setReadTimeout(4000);

                    //开始连接
                    connection.connect();
                    //判断请求是否成功
//                    if (connection.getResponseCode() == 200) {
                    //得到输入流
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    //response存放从服务器接收到的字符串
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    //TODO 判断是否其他设备登录
                    JSONTokener jsonTokener = new JSONTokener(response.toString());
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    int status = jsonObject.optInt("status");
                    int exit = jsonObject.optInt("exit");
                    String msg = jsonObject.optString("msg");
                    if (status == 0 && exit == 1) {
                        SharedPreferences.Editor editor = activity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                        editor.clear().apply();
                        Util.showLogoutDialog(activity);
                        sendGET(urlPath, handler, activity);//重新请求一次
                    } else {
                        //创建消息并发送
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.sendToTarget();
                    }
//                    }
                } catch (IOException e) {
                    Message message = handler.obtainMessage();
                    message.obj = "网络异常";
                    message.sendToTarget();
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
//    Map<String, String> paramsMap = new HashMap<>();
//                        paramsMap.put("phone",phone1);
//                        paramsMap.put("nick_name",nickname1);
//                        paramsMap.put("code",msg1);
//                        paramsMap.put("passwd",password1);
//                        RequestURL.sendPOST("https://app.feimayun.com/Login/register",handleRegister,paramsMap);

    /**
     * 在子线程中访问网络，POST访问
     *
     * @param urlPath   请求访问的URL
     * @param handler   处理网络返回结果的handler
     * @param paramsMap post请求参数
     */
    public static void sendPOST(final String urlPath, final Handler handler, final Map<String, String> paramsMap, final AppCompatActivity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String aptk;
                String apud = Util.getUid();
                String secret = Util.getSecret();
                if (apud.equals("")) {
                    aptk = new Md5().getDateToken();
                } else {
                    aptk = new Md5().getUidToken(apud);
                }

                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlPath);
                    //打开连接
                    connection = (HttpURLConnection) url.openConnection();
                    //接收模式
                    connection.setRequestMethod("POST");
                    //必须设置false，否则会自动redirect到重定向后的地址
                    connection.setInstanceFollowRedirects(false);
                    //连接超时，单位毫秒
                    connection.setConnectTimeout(4000);
                    //读取超时，单位毫秒
                    connection.setReadTimeout(4000);
                    connection.setRequestProperty("secket", secret);
                    connection.setRequestProperty("apud", apud);
                    connection.setRequestProperty("aptk", aptk);
                    connection.setRequestProperty("osName", osName);
                    connection.setRequestProperty("verName", verName);
                    connection.setRequestProperty("version", version);
                    connection.setRequestProperty("apid", apidString);

                    //发送POST请求必须设置如下两行：需要输出，需要输入
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);

                    //开始连接
                    connection.connect();
                    //判断请求是否成功
//                    if (connection.getResponseCode() == 200) {

                    //获取URLConnection对象对应的输出流
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                        builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                    }
                    if (builder.length() > 0) {

                    }
                    builder.deleteCharAt(builder.length() - 1);

                    out.write(builder.toString().getBytes());//注意中文
                    //flush输出流的缓冲
                    out.flush();

                    //得到输入流
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    //response存放从服务器接收到的字符串
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    //TODO 判断是否其他设备登录
                    JSONTokener jsonTokener = new JSONTokener(response.toString());
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    int status = jsonObject.optInt("status");
                    int exit = jsonObject.optInt("exit");
                    String msg = jsonObject.optString("msg");
                    if (status == 0 && exit == 1) {
                        SharedPreferences.Editor editor = activity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                        editor.clear().apply();
                        Util.showLogoutDialog(activity);
                        paramsMap.put("uid", "");
                        sendPOST(urlPath, handler, paramsMap, activity);//重新请求一次
                    } else {
                        //创建消息并发送
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.sendToTarget();
                    }
//                    }
                } catch (IOException e) {
                    Message message = handler.obtainMessage();
                    message.obj = "网络异常";
                    message.sendToTarget();
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void uploadFile(final List<String> filePathList, final String url, final Handler handler, final AppCompatActivity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String aptk;
                String apud = Util.getUid();
                String secret = Util.getSecret();
                if (apud.equals("")) {
                    aptk = new Md5().getDateToken();
                } else {
                    aptk = new Md5().getUidToken(apud);
                }

                String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
                String PREFIX = "--", LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data"; //内容类型
                try {
                    URL httpUrl = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setDoInput(true); //允许输入流
                    conn.setDoOutput(true); //允许输出流
                    conn.setUseCaches(false); //不允许使用缓存
                    conn.setRequestMethod("POST"); //请求方式
                    conn.setRequestProperty("Charset", "UTF-8");
                    //设置编码
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                    conn.setRequestProperty("apud", apud);
                    conn.setRequestProperty("aptk", aptk);
                    conn.setRequestProperty("osName", osName);
                    conn.setRequestProperty("verName", verName);
                    conn.setRequestProperty("version", version);
                    conn.setRequestProperty("apid", apidString);

                    //     if(files.size()!= 0) {
                    //当文件不为空，把文件包装并且上传
                    OutputStream outputSteam = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(outputSteam);
                    for (int i = 0; i < filePathList.size(); i++) {
                        File file = new File(filePathList.get(i));
                        //sb.append("Content-Type: application/octet-stream; charset="+ "UTF-8" +LINE_END);
                        String sb = PREFIX +
                                BOUNDARY +
                                LINE_END +
                                "Content-Disposition: form-data; name=\"upfile[]\"; filename=\"" + file.getName() + "\"" + LINE_END +
                                LINE_END;
                        dos.write(sb.getBytes());

                        //图片上传，需要压缩一下
                        int requestWidth = (int) (1024 / 2.625);//计算1024像素的dp
                        //这里传入的宽高是dp值
                        Bitmap bitmap = StaticUtil.decodeSampledBitmapFromFile(filePathList.get(i), requestWidth, requestWidth);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, dos);
                        //将bitmap转换成byte数组
//                        byte[] bytes = StaticUtil.Bitmap2Bytes(bitmap);
//                        dos.write(bytes);
//                        InputStream is = new FileInputStream(files.get(i));
//                        byte[] bytes = new byte[1024];
//                        int len = -1;
//                        while ((len = is.read(bytes)) != -1) {
//                            dos.write(bytes, 0, len);
//                        }
                        dos.write(LINE_END.getBytes());
//                        is.close();
                    }
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    dos.write(end_data);
                    dos.flush();

                    //得到输入流
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    //response存放从服务器接收到的字符串
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    //TODO 判断是否其他设备登录
                    JSONTokener jsonTokener = new JSONTokener(response.toString());
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    int status = jsonObject.optInt("status");
                    int exit = jsonObject.optInt("exit");
                    String msg = jsonObject.optString("msg");
                    if (status == 0 && exit == 1) {
                        SharedPreferences.Editor editor = activity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                        editor.clear().apply();
                        Util.showLogoutDialog(activity);
                        uploadFile(filePathList, url, handler, activity);//重新请求一次
                    } else {
                        //创建消息并发送
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.sendToTarget();
                    }
                } catch (Exception e) {
                    Message message = handler.obtainMessage();
                    message.obj = "网络异常";
                    message.sendToTarget();
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static void uploadFile2(final List<String> filePathList, final String url, final Handler handler, final AppCompatActivity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String aptk;
                String apud = Util.getUid();
                String secret = Util.getSecret();
                if (apud.equals("")) {
                    aptk = new Md5().getDateToken();
                } else {
                    aptk = new Md5().getUidToken(apud);
                }

                String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
                String PREFIX = "--", LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data"; //内容类型
                try {
                    URL httpUrl = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setDoInput(true); //允许输入流
                    conn.setDoOutput(true); //允许输出流
                    conn.setUseCaches(false); //不允许使用缓存
                    conn.setRequestMethod("POST"); //请求方式
                    conn.setRequestProperty("Charset", "UTF-8");
                    //设置编码
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                    conn.setRequestProperty("apud", apud);
                    conn.setRequestProperty("aptk", aptk);
                    conn.setRequestProperty("osName", osName);
                    conn.setRequestProperty("verName", verName);
                    conn.setRequestProperty("version", version);
                    conn.setRequestProperty("apid", apidString);

                    //     if(files.size()!= 0) {
                    //当文件不为空，把文件包装并且上传
                    OutputStream outputSteam = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(outputSteam);
                    for (int i = 0; i < filePathList.size(); i++) {
                        File file = new File(filePathList.get(i));
                        //sb.append("Content-Type: application/octet-stream; charset="+ "UTF-8" +LINE_END);
                        String sb = PREFIX +
                                BOUNDARY +
                                LINE_END +
                                "Content-Disposition: form-data; name=\"upfile\"; filename=\"" + file.getName() + "\"" + LINE_END +
                                LINE_END;
                        dos.write(sb.getBytes());

                        //图片上传，需要压缩一下
                        int requestWidth = (int) (1024 / 2.625);//计算1024像素的dp
                        //这里传入的宽高是dp值
                        Bitmap bitmap = StaticUtil.decodeSampledBitmapFromFile(filePathList.get(i), requestWidth, requestWidth);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, dos);
                        //将bitmap转换成byte数组
//                        byte[] bytes = StaticUtil.Bitmap2Bytes(bitmap);
//                        dos.write(bytes);
//                        InputStream is = new FileInputStream(files.get(i));
//                        byte[] bytes = new byte[1024];
//                        int len = -1;
//                        while ((len = is.read(bytes)) != -1) {
//                            dos.write(bytes, 0, len);
//                        }
                        dos.write(LINE_END.getBytes());
//                        is.close();
                    }
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    dos.write(end_data);
                    dos.flush();

                    //得到输入流
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    //response存放从服务器接收到的字符串
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    //TODO 判断是否其他设备登录
                    JSONTokener jsonTokener = new JSONTokener(response.toString());
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    int status = jsonObject.optInt("status");
                    int exit = jsonObject.optInt("exit");
                    String msg = jsonObject.optString("msg");
                    if (status == 0 && exit == 1) {
                        SharedPreferences.Editor editor = activity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                        editor.clear().apply();
                        Util.showLogoutDialog(activity);
                        uploadFile2(filePathList, url, handler, activity);//重新请求一次
                    } else {
                        //创建消息并发送
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.sendToTarget();
                    }
                } catch (Exception e) {
                    Message message = handler.obtainMessage();
                    message.obj = "网络异常";
                    message.sendToTarget();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendUpdate(final String urlPath, final Handler handler, final AppCompatActivity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String aptk;
                String apud = Util.getUid();
                String secret = Util.getSecret();
                if (apud.equals("")) {
                    aptk = new Md5().getDateToken();
                } else {
                    aptk = new Md5().getUidToken(apud);
                }

                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlPath);
                    //打开连接
                    connection = (HttpURLConnection) url.openConnection();
                    //接收模式
                    connection.setRequestMethod("GET");

                    connection.setRequestProperty("apud", apud);
                    connection.setRequestProperty("aptk", aptk);
                    connection.setRequestProperty("osName", osName);
                    connection.setRequestProperty("verName", verName);
                    connection.setRequestProperty("version", version);
                    connection.setRequestProperty("apid", apidString);
                    //连接超时，单位毫秒
                    connection.setConnectTimeout(4000);
                    //读取超时，单位毫秒
                    connection.setReadTimeout(4000);

                    //开始连接
                    connection.connect();
                    //判断请求是否成功
//                    if (connection.getResponseCode() == 200) {

                    //得到输入流
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    //response存放从服务器接收到的字符串
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    //TODO 判断是否其他设备登录
                    JSONTokener jsonTokener = new JSONTokener(response.toString());
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    int status = jsonObject.optInt("status");
                    int exit = jsonObject.optInt("exit");
                    String msg = jsonObject.optString("msg");
                    if (status == 0 && exit == 1) {
                        SharedPreferences.Editor editor = activity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                        editor.clear().apply();
                        Util.showLogoutDialog(activity);
                        sendUpdate(urlPath, handler, activity);//重新请求一次
                    } else {
                        //创建消息并发送
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.sendToTarget();
                    }
//                    }
                } catch (IOException e) {
                    Message message = handler.obtainMessage();
                    message.obj = "网络异常";
                    message.sendToTarget();
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    //在子线程中访问网络，GET访问，path传参
    public static void sendGetPath(final String urlPath, final Handler handler, final Map<String, String> paramsMap, final AppCompatActivity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String aptk;
                String apud = Util.getUid();
                String secret = Util.getSecret();
                if (apud.equals("")) {
                    aptk = new Md5().getDateToken();
                } else {
                    aptk = new Md5().getUidToken(apud);
                }
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    StringBuilder params = new StringBuilder();
                    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                        params.append(entry.getKey());
                        params.append("=");
                        params.append(entry.getValue());
                        params.append("&");
                    }
                    if (params.length() > 0) {
                        params.deleteCharAt(params.lastIndexOf("&"));
                    }
                    URL url = new URL(urlPath + (params.length() > 0 ? "?" + params.toString() : ""));
                    //打开连接
                    connection = (HttpURLConnection) url.openConnection();
                    //接收模式
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("apud", apud);
                    connection.setRequestProperty("aptk", aptk);
                    connection.setRequestProperty("secket", secret);
                    connection.setRequestProperty("osName", osName);
                    connection.setRequestProperty("verName", verName);
                    connection.setRequestProperty("version", version);
                    connection.setRequestProperty("apid", apidString);
                    //连接超时，单位毫秒
                    connection.setConnectTimeout(4000);
                    //读取超时，单位毫秒
                    connection.setReadTimeout(4000);
                    //开始连接
                    connection.connect();
                    //判断请求是否成功
//                    if (connection.getResponseCode() == 200) {

                    //得到输入流
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    //response存放从服务器接收到的字符串
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    //TODO 判断是否其他设备登录
                    JSONTokener jsonTokener = new JSONTokener(response.toString());
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    int status = jsonObject.optInt("status");
                    int exit = jsonObject.optInt("exit");
                    String msg = jsonObject.optString("msg");
                    if (status == 0 && exit == 1) {
                        SharedPreferences.Editor editor = activity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                        editor.clear().apply();
                        Util.showLogoutDialog(activity);
                        sendGetPath(urlPath, handler, paramsMap, activity);//重新请求一次
                    } else {
                        //创建消息并发送
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.sendToTarget();
                    }
//                    }
                } catch (IOException e) {
                    Message message = handler.obtainMessage();
                    message.obj = "网络异常";
                    message.sendToTarget();
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }


}



