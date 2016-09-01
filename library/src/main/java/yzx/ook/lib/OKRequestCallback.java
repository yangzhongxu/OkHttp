package yzx.ook.lib;

/**
 * Created by yzx on 2016/8/31
 */
public interface OKRequestCallback {

    void onSuccess(String data);

    void onFailure(int httpCode);

}
