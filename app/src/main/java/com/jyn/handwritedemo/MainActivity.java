package com.jyn.handwritedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.softwaretest.InputViewActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image=findViewById(R.id.image);
        startActivityForResult(new Intent(this, InputViewActivity.class),100);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            ArrayList<String> list=data.getStringArrayListExtra("path");
            String  name=data.getStringExtra("name");
            List<Bitmap> bitmaps=new ArrayList<>();
            if (list!=null&&list.size()>0){

                for (String str:list){

                    bitmaps.add(BitmapFactory.decodeFile(str));

                }
            }
            if (name!=null){
                Toast.makeText(this,name+"",Toast.LENGTH_SHORT).show();
            }


            Bitmap newBmp = (Bitmap) bitmaps.get(0);
            for(int i = 1;i<bitmaps.size();i++) {
                Bitmap bmp = (Bitmap) bitmaps.get(i);
                newBmp = newBitmap(newBmp,bmp);
            }
            File rstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "resultImg.jpg");
            if (!rstFile.exists())
                try {
                    rstFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            // save(newBmp, rstFile, Bitmap.CompressFormat.JPEG, true);
            image.setImageBitmap(newBmp);

        }
    }


    public  Bitmap newBitmap(Bitmap bmp1, Bitmap bmp2) {
        Bitmap retBmp;
        int height =bmp1.getHeight() ;
        Paint paint = new Paint();
        paint.setColor(0xFFFFFFFF);
        if (bmp2.getHeight() != height) {
            //以第一张图片的宽度为标准，对第二张图片进行缩放。

            int w2 = bmp2.getWidth() * height / bmp2.getHeight();
            retBmp = Bitmap.createBitmap(bmp1.getWidth() + w2, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(retBmp);
            Bitmap newSizeBmp2 = resizeBitmap(bmp2, w2, height);
            canvas.drawBitmap(bmp1, 0, 0, paint);
            canvas.drawBitmap(newSizeBmp2, bmp1.getWidth(), 0, paint);
        } else {
            //两张图片宽度相等，则直接拼接。

            retBmp = Bitmap.createBitmap(bmp1.getWidth() + bmp2.getWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(retBmp);
            canvas.drawBitmap(bmp1, 0, 0, paint);
            canvas.drawBitmap(bmp2, bmp1.getWidth(), 0, paint);
        }


        return retBmp;
    }
    public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        float scaleWidth = ((float) newWidth) / bitmap.getWidth();
        float scaleHeight = ((float) newHeight) / bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bmpScale = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bmpScale;
    }

    /**
     * 保存图片到文件File。
     *
     * @param src     源图片
     * @param file    要保存到的文件
     * @param format  格式
     * @param recycle 是否回收
     * @return true 成功 false 失败
     */
    public static boolean save(Bitmap src, File file, Bitmap.CompressFormat format, boolean recycle) {
        if (isEmptyBitmap(src))
            return false;

        OutputStream os;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled())
                src.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Bitmap对象是否为空。
     */
    public static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
