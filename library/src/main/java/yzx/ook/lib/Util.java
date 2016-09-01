package yzx.ook.lib;

import java.io.File;

/**
 * Created by yzx on 2016/8/31
 */
 class Util {

    static boolean isFileUseful(File file){
            return file != null && file.exists() && file.canRead() && !file.isDirectory();
    }

}
