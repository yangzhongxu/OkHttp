package yzx.ook.lib;

import okhttp3.Call;

/**
 * Created by yzx on 2016/9/1
 */
public class CancelAble {

    Call call;


    public void cancel(){
        if(call != null){
            call.cancel();
            call = null;
        }
    }

}
