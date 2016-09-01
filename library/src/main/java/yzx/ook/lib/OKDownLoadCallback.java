package yzx.ook.lib;

import java.io.File;

/**
 * Created by yzx on 2016/9/1
 */
public interface OKDownLoadCallback {

     int ERROR_SDCARD = 1;
     int ERROR_NET = 2;


    void onComplete(File file);

    void onError(int errCode);

    void onProgress(int current); //0 - 1000

}
