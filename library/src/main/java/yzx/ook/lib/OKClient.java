package yzx.ook.lib;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yzx on 2016/8/31
 */
public class OKClient {

    private  OkHttpClient client;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public OKClient(OkHttpClient client){
        this.client = client;
    }


    /*=================== method======================*/


    public void get(String url , RequestParam params , OKRequestCallback callback){
        String queryString = params == null?"":params.getQueryString();
        Request request = new Request.Builder().url(url+queryString).get().build();
        execCallback(client.newCall(request),callback);
    }

    public void post(String url,RequestParam params , OKRequestCallback callback){
        Request request = (params != null && params.hasFile()) ? getFilePostRequest(url,params) : getStringPostRequest(url,params);
        execCallback(client.newCall(request),callback);
    }


    /*=================== internal======================*/


    private Request getFilePostRequest(String url,RequestParam params){
        final MultipartBody.Builder builder = new MultipartBody.Builder("ook------------------------------------------ook").setType(MultipartBody.FORM);
        params.iteratorString(new RequestParam.KeyValueIteratorListener() {
            public void onIterator(String key, String value) {
              builder.addFormDataPart(key,value);
            }
        });
        params.iteratorFile(new RequestParam.KeyFileIteratorListener() {
            public void onIterator(String key, File file) {
               builder.addFormDataPart(key,file.getName(),
                       RequestBody.create(MediaType.parse("application/octet-stream") , file));
            }
        });
        return new Request.Builder().url(url).post(builder.build()).build();
    }

    private Request getStringPostRequest(String url,RequestParam params){
        final FormBody.Builder builder = new FormBody.Builder();
        if(params != null)
            params.iteratorString(new RequestParam.KeyValueIteratorListener() {
                public void onIterator(String key, String value) {
                    builder.add(key,value);
                }
            });
        return new Request.Builder().url(url).post(builder.build()).build();
    }


    private void execCallback(Call call,final OKRequestCallback callback){
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                if(callback == null) return ;

                final boolean isSuccess = response.isSuccessful();
                final String bodyString = response.body().string();
                final int code = response.code();

                mHandler.post(new Runnable() {
                    public void run() {
                        if(isSuccess)  callback.onSuccess(bodyString);
                        else                  callback.onFailure(code);
                    }
                });
            }
            public void onFailure(Call call, IOException e) {
                if(callback == null) return ;

                if(call.isCanceled()) return ;

                mHandler.post(new Runnable() {
                    public void run() {
                        callback.onFailure(0);
                    }
                });
            }
        });
    }

}
