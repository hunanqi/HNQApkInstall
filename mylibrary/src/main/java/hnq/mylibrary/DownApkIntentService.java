package hnq.mylibrary;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Administrator on 2017/6/29.
 */

public class DownApkIntentService extends IntentService {
    private final  String  TAG="DownApkIntentService";
    private String path;
    private String name;
    private String   url ;
    private int   imageId ;
    private String   title ;
    private NotificationManager mNotificationManager;
    private  NotificationCompat.Builder builder;
    private int currnProgress;
    //处理通知信息
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what){
                case 1://下载进度
                   int arg1 = msg.arg1;
                        if (arg1>=0&&arg1<=100){
                            builder.setProgress(100,arg1,false);
                            mNotificationManager.notify(0,builder.build());
                    }
                    break;
                case  2://下载完成
                    mNotificationManager.cancel(0);
                    installApk();
                    Log.i("sss","下载完成");
                    break;
                case 3://初始化Notification
                    initNotification();
                    break;
            }
        }
    };
    public DownApkIntentService() {
        super("DownApkIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        url = extras.getString(DownApkConstant.URL);
        path = getExternalCacheDir().getAbsolutePath();
        name=extras.getString(DownApkConstant.NAME);
        imageId=extras.getInt(DownApkConstant.IMAGE_ID,0);
        title=extras.getString(DownApkConstant.TITLE);
        if (!isTrue())//若参数不完全，直接返回
            return;
        //初始化Notification
        Message message=new Message();
        message.what=3;
        handler.sendMessage(message);
        downloadApk(url,path,name);

    }

    /**
     * 下载文件
     * @param downloadUrl
     * @param path
     * @param name
     */
    private void downloadApk(String downloadUrl, String path, String name) {
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        file=new File(path+"/"+name);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStream ips = null;
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("GET");
            huc.setReadTimeout(10000);
            huc.setConnectTimeout(3000);
            ips = huc.getInputStream();
            int length = huc.getContentLength();//文件的总长度
            int loadLength=0;//下载长度
            // 拿到服务器返回的响应码
            int hand = huc.getResponseCode();
            if (hand == 200) {
                // 建立一个byte数组作为缓冲区，等下把读取到的数据储存在这个数组
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = ips.read(buffer)) != -1) {
                    loadLength+=len;
                    int progress= (int) (loadLength * 100.0F/ length);
                    if(currnProgress<progress){
                        Message msg=new Message();
                        msg.what=1;//向handler通知进度
                        msg.arg1=progress;
                        handler.sendMessage(msg);
                    }
                    currnProgress=progress;
                    fos.write(buffer, 0, len);
                }
                Message msg=new Message();
                msg.what=2;//下载完成
                handler.sendMessage(msg);
            } else {
                Log.i(TAG,"网络连接失败");
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (ips != null) {
                    ips.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查intent传过来的参数是否完全
     * @return
     */
    private boolean isTrue(){
        if (TextUtils.isEmpty(url)){
            Log.i(TAG,"url is null");
            return  false;
        }
        if (TextUtils.isEmpty(path)){
            Log.i(TAG,"path is null");
            return  false;
        }
        if (TextUtils.isEmpty(name)){
            Log.i(TAG,"name is null");
            return  false;
        }
        if (imageId==0){
            Log.i(TAG,"imageId is null");
            return  false;
        }
        return  true;
    }
    private void initNotification(){
        mNotificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder=new NotificationCompat.Builder(this).setSmallIcon(imageId)
                .setContentText("正在下载").setContentTitle(title);
         builder.setProgress(100,0,false);
        mNotificationManager.notify(0,builder.build());
    }

    private void installApk() {
        String apkFile = path+"/"+name;
        File apkfile = new File(apkFile);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT>=24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri =
                    FileProvider.getUriForFile(this, "hnq.mylibrary.apk.fileprovider", apkfile);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }else{
            i.setDataAndType(Uri.fromFile(apkfile),
                    "application/vnd.android.package-archive");
        }
        this.startActivity(i);
    }
}
