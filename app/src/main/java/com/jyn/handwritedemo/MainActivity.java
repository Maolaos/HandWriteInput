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




        }
    }



}
