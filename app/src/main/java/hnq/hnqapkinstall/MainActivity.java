package hnq.hnqapkinstall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import hnq.mylibrary.DownApkConstant;
import hnq.mylibrary.DownApkIntentService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn001).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownApkIntentService.class);
                Bundle bundle = new Bundle();
                bundle.putString(DownApkConstant.URL, "http://imtt.dd.qq.com/16891/F93BBCA5868A3D335CF3DE847C8B4687.apk?fsname=com.tianxia.hnq.erweima_1.0_1.apk&csr=1bbd");
                bundle.putString(DownApkConstant.NAME, "sss.apk");
                bundle.putInt(DownApkConstant.IMAGE_ID, R.mipmap.ic_launcher);
                bundle.putString(DownApkConstant.TITLE, "应用更新");
                intent.putExtras(bundle);
                startService(intent);
            }
        });
    }
}
