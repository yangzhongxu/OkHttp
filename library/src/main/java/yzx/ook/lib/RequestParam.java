package yzx.ook.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yzx on 2016/8/31
 */
public class RequestParam {

    private final HashMap<String,List<String>> stringMap = new HashMap<>(0);
    private final ArrayList<FileWrapper> fileList = new ArrayList<>(0);


    /**
     *添加string参数,key可以重复,重复则按照array参数处理
     */
    public void add(String key,String value){
        List<String> pv = stringMap.get(key);
        if(pv == null)
            stringMap.put(key, pv = new ArrayList<>(1));
        pv.add(value);
    }

    /**
     * 添加文件参数
     */
    public void addFile(String key , File file){
        if(Util.isFileUseful(file))
            fileList.add(new FileWrapper(file,key));
    }


    /* ==========================internal========================== */


    /* internal */
    String getQueryString(){
        if(stringMap.isEmpty())
            return "";

        final StringBuilder sb = new StringBuilder("?");
        iteratorString(new KeyValueIteratorListener() {
            public void onIterator(String key, String value) {
                sb.append(key).append("=").append(value).append("&");
            }
        });

        if(sb.length() == 1) sb.deleteCharAt(0);
        else sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }


    /* is upload file */
    boolean hasFile(){
        return !fileList.isEmpty();
    }

    
    /* iterate string params */
    void iteratorString(KeyValueIteratorListener iteratorListener){
        if(iteratorListener != null && !stringMap.isEmpty())
            for (Map.Entry<String, List<String>> entry : stringMap.entrySet())
                for (String item : ((entry.getValue() != null) ?entry.getValue() : new ArrayList<String>(0)))
                    iteratorListener.onIterator(entry.getKey() , item);
    }

    /* iterator file params */
    void iteratorFile(KeyFileIteratorListener iteratorListener){
        if(iteratorListener == null || fileList.isEmpty())
            return;
        for (FileWrapper item : fileList)
            iteratorListener.onIterator(item.key,item.file);
    }


    /* ============================class============================ */


    interface KeyFileIteratorListener{
        void onIterator(String key,File file);
    }

    interface KeyValueIteratorListener{
        void onIterator(String key,String value);
    }

    class FileWrapper{
        public FileWrapper(File file,String key){this.file=file;this.key=key;}
        File file;
        String key;
    }

}
