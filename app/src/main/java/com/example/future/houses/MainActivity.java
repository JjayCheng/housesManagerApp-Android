package com.example.future.houses;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import android.content.ClipboardManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.os.Build;


public class MainActivity extends AppCompatActivity implements JsBridge {

    private WebView webView;
    private Handler handler;
    private String backUpFileName = "房源数据备份.txt";

    private static int REQUEST_PERMISSION_CODE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        webView = findViewById(R.id.mainwebview);

        webView.addJavascriptInterface(new JsInterface(this), "Android");
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.loadUrl("file:///android_asset/index.html");
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // webView.goBack();   //后退
                    webView.loadUrl("javascript: keyBack()");
                    return true;    //已处理
                }
            }
            return false;
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "聊天消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
            channelId = "subscribe";
            channelName = "订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

    }

    @Override
    public void callPhone(final String phoneNumber) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });
    }

    @Override
    public String getClipboardText() {
        String str = "";
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = clipboardManager.getPrimaryClip();
        ClipData.Item item = clipData.getItemAt(0);
        str = item.getText().toString();
        sendChatMsg("剪贴板数据:", str);
        return  str;
    }

    @Override
    public void exportData(final String data) {
        String sdCardDir =Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/tencent/MicroMsg/Download";
        File saveFile = new File(sdCardDir, backUpFileName);
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(saveFile);
            outStream.write(data.getBytes());
            sendChatMsg("已备份到微信文件夹", "发送给好友:(选择联系人-文件-选择:"+ backUpFileName +"-发送)");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "文件写入失败", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String importData() {
        String sdCardDir =Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/tencent/MicroMsg/Download";
        String result=null;
        try {
            int i = 0;
            final int MAXCOUNT = 200;
            String fileName = backUpFileName;
            String fileFullName = (i == 0 ?  "" : i + "_") + fileName;
            File f = new File(sdCardDir, fileName);
            if(f.exists()) {
                while(f.exists()) {
                    i++;
                    fileFullName = i + "_" + fileName;
                    f = new File(sdCardDir, fileFullName);
                }
                i--;
                fileFullName = (i == 0 ?  "" : i + "_") + fileName;
                f = new File(sdCardDir, fileFullName);
                sendChatMsg("发现文件", "数据源:" +  fileFullName);
            } else {
                sendChatMsg("未找到文件", "确保底文件:" + backUpFileName + "存在" );
            }
            int length=(int)f.length();
            byte[] buff=new byte[length];
            FileInputStream fin = new FileInputStream(f);
            fin.read(buff);
            fin.close();
            result = new String(buff,"UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"没有在微信里找到文件",Toast.LENGTH_SHORT).show();
        }
        return result;
    }


    // 通知相关
    public void sendChatMsg(String title, String content) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "chat")
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build();
        manager.notify(1, notification);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }
}
