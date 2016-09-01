package yzx.ook.lib;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

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

    private final OkHttpClient client;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<WeakReference<CancelAble>> callList = new ArrayList<>(0);

    public OKClient(OkHttpClient client){
        this.client = client;
    }


    /*=================== method======================*/


    public CancelAble get(String url , RequestParam params , OKRequestCallback callback){
        cleanCancelAbleList();

        String queryString = params == null?"":params.getQueryString();
        Request request = new Request.Builder().url(url+queryString).get().build();

        CancelAble cancelAble = new CancelAble();
        cancelAble.call = client.newCall(request);
        callList.add(new WeakReference<>(cancelAble));

        execCallback(cancelAble.call,callback);
        return cancelAble;
    }


    public CancelAble post(String url,RequestParam params , OKRequestCallback callback){
        cleanCancelAbleList();

        Request request = (params != null && params.hasFile()) ? getFilePostRequest(url,params) : getStringPostRequest(url,params);

        CancelAble cancelAble = new CancelAble();
        cancelAble.call = client.newCall(request);
        callList.add(new WeakReference<>(cancelAble));

        execCallback(cancelAble.call , callback);
        return cancelAble;
    }


    public CancelAble download(String url ,final File targetFile , boolean goon,final OKDownLoadCallback callback){
        cleanCancelAbleList();

        String rangeHeaderValue = null;
        if(goon && Util.isFileUseful(targetFile)){
            rangeHeaderValue = "byte="+targetFile.length()+"-";
        } else {
            targetFile.delete();
            goon = false;
        }

        final long hasDownLen = goon ? targetFile.length() : 0;

        Request.Builder builder = new Request.Builder().url(url).get();
        if(goon) builder.addHeader("RANGE", rangeHeaderValue);

        CancelAble cancelAble = new CancelAble();
        cancelAble.call = client.newCall(builder.build());
        callList.add(new WeakReference<>(cancelAble));

        cancelAble.call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                if(call.isCanceled()) return ;
                if(response.isSuccessful()){
                    InputStream input = response.body().byteStream();
                    final long total = input.available() + hasDownLen;
                    BufferedInputStream in = new BufferedInputStream(input);
                    byte[] buffer = new byte[1024];
                    long hasWriteLen = hasDownLen;
                    int len;
                    RandomAccessFile out = new RandomAccessFile(targetFile, "rwd");
                    out.seek(hasWriteLen);
                    long lastPublishTime = System.currentTimeMillis() - 333;
                    while((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        hasWriteLen += len;
                        long now;
                        if((now = System.currentTimeMillis()) - lastPublishTime > 333){
                            publishProgress(callback,total,hasWriteLen);
                            lastPublishTime = now;
                        }
                    }
                    publishProgress(callback,total,total);
                    in.close();
                    out.close();
                    mHandler.post(new Runnable() {
                        public void run() {
                            callback.onComplete(targetFile);
                        }
                    });
                }else
                    mHandler.post(new Runnable() {
                        public void run() {
                            callback.onError(OKDownLoadCallback.ERROR_NET);
                        }
                    });
            }
            public void onFailure(Call call, IOException e) {
                if(call.isCanceled()) return ;
                mHandler.post(new Runnable() {
                    public void run() {
                        callback.onError(OKDownLoadCallback.ERROR_SDCARD);
                    }
                });
            }
        });

        return cancelAble;
    }


    public void cancelAll(){
        for (WeakReference<CancelAble> item : callList)
            if(item.get()!=null)
                item.get().cancel();
        callList.clear();
    }


    /*=================== internal======================*/


    private void publishProgress(final OKDownLoadCallback callback, final long total , final long current){
            mHandler.post(new Runnable() {
                public void run() {
                    callback.onProgress((int)(1000 * current / total));
                }
            });
    }


    private void cleanCancelAbleList(){
        if(callList.size() <= client.dispatcher().getMaxRequests())
            return ;
        ArrayList<WeakReference> readyRemoveList = new ArrayList<>(callList.size());
        for (WeakReference<CancelAble> item : callList)
            if(item.get() == null)
                readyRemoveList.add(item);
        callList.removeAll(readyRemoveList);
    }


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
                if(call.isCanceled())  return ;

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
