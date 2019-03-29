package com.example.softwaretest;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by jiaoyanan on 2019/3/28.
 */

public class FileUtil {
    private final  static String FILE_PATH="image";




    /**
     * 获取文件目录
     * @return
     */
    public static  String  getAppFilePath(Context mContext){

        if (getSDPath()!=null){
            return checkDirPath(getAppBasePath(mContext)+ File.separator+FILE_PATH+File.separator);
        }
        return null;
    }



    /**
     * 检查文件是否存在
     */
    public static String checkDirPath(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return "";
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    private static  String getAppBasePath(Context mContext){
        return checkDirPath(getSDPath()+File.separator+mContext.getPackageName());
    }

    /**
     * 获取SD卡根目录
     * @return
     */
    public  static String getSDPath(){
        File sdDir = null;
        //判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if(sdCardExist) {
            //获取跟目录
            sdDir = Environment.getExternalStorageDirectory();
        }else {
            return null;
        }
        return sdDir.toString();
    }

    public static void copyBigDataToSD(String fileName, String strOutFileName,Context context) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(fileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }


    public  static String composeLocation(String fileName,Context context) {
        String dataLocation = "/data/data/" + context.getPackageName() + "/";
        return new StringBuilder().append(dataLocation).append(fileName).toString();
    }
}
