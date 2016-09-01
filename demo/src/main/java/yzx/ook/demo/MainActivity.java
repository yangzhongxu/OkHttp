package yzx.ook.demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import okhttp3.OkHttpClient;
import yzx.ook.lib.CancelAble;
import yzx.ook.lib.OKClient;
import yzx.ook.lib.OKRequestCallback;
import yzx.ook.lib.RequestParam;

public class MainActivity extends AppCompatActivity {

    private OKClient client = new OKClient(new OkHttpClient());
    private String url = "http://192.168.10.159:8080/test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        client.cancelAll();
        super.onDestroy();
    }


    /* get 请求 */
    public void get(View view){
        RequestParam param = new RequestParam();
        param.add("fuck","fuck1");
        param.add("fuck","fuck2");

        /* 这个CancelAble对象, 可以实现本次请求的取消  -> cancelAble.cancel() */
        CancelAble cancelAble = client.get(url, param, new OKRequestCallback() {
            public void onSuccess(String data) {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }

            public void onFailure(int httpCode) {
                Toast.makeText(MainActivity.this, "" + httpCode, Toast.LENGTH_SHORT).show();
            }
        });

    }


    /* post 请求 */
    public void post(View view){
        RequestParam param = new RequestParam();
        param.add("fuck","fuck1");
        param.add("fuck","fuck2");
        param.add("fuck333","fuck333");

        CancelAble cancelAble = client.post(url, param, new OKRequestCallback() {
            public void onSuccess(String data) {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }

            public void onFailure(int httpCode) {
                Toast.makeText(MainActivity.this, "" + httpCode, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /* 上传文件 */
    public void postFile(View view){
        RequestParam param = new RequestParam();
        param.add("a","aaa");
        param.add("b","bbb");
        param.addFile("f1",new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/a.jpg"));
        param.addFile("f1",new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tiwang.mp3"));

        CancelAble cancelAble = client.post(url, param, new OKRequestCallback() {
            public void onSuccess(String data) {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
            public void onFailure(int httpCode) {
                Toast.makeText(MainActivity.this, httpCode+"", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
