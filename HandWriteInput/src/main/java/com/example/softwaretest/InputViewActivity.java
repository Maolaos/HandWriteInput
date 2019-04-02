package com.example.softwaretest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.CloudKeyboardInputManager;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;


@SuppressLint("NewApi")
public class InputViewActivity extends   Activity implements OnCandidateSelected, OnHandWritingRecognize, OnClickListener {


    HandWritingBoardLayout handWritingBoard;
    TextView inputShow;

    LinearLayout container;
    RelativeLayout candidateContainer;

    Button btnCleanHandWriting;
    ImageView btnDelete;


    CandidateView mCandidateView;
    private ArrayList<String> fileList = new ArrayList<String>();

    private StringBuilder currentInput = new StringBuilder();
    //private CloudKeyboardInputManager ckManager;
    private List<WnnWord> requestList;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
             if (msg.what==0102){
                 try {
                     String path = FileUtil.getAppFilePath(InputViewActivity.this) + System.currentTimeMillis() + ".png";
                     handWritingBoard.save(path, true, 10);
                     fileList.add(path);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 if (isHandWriting()) {
                     resetHandWritingRecognize();
                 }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.input_view);



        container = (LinearLayout) findViewById(R.id.container);
        candidateContainer = (RelativeLayout) findViewById(R.id.candidateContainer);
        handWritingBoard = (HandWritingBoardLayout) findViewById(R.id.handwrtingboard);
        btnCleanHandWriting = (Button) findViewById(R.id.clean);
        inputShow = (TextView) findViewById(R.id.candidateselected);
        btnDelete = (ImageView) findViewById(R.id.delete);
       // ckManager = new CloudKeyboardInputManager();
        mCandidateView = new CandidateView(this);
        mCandidateView.setOnCandidateSelected(this);

        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //lp1.addRule(RelativeLayout.LEFT_OF, R.id.btn_showMore);

        lp1.width = ViewGroup.LayoutParams.MATCH_PARENT;
        candidateContainer.addView(mCandidateView, lp1);
        //mCandidateView.setVisibility(View.GONE);
        handWritingBoard.setOnHandWritingRecognize(this);

        btnCleanHandWriting.setOnClickListener(this);
        btnCleanHandWriting.setOnClickListener(this);
        findViewById(R.id.finish).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        btnDelete.setOnClickListener(this);



        try {
            File file1 = new File(FileUtil.composeLocation("writableZHCN.dic",this));
            File file2 = new File(FileUtil.composeLocation("writableZHCN.dic-journal",this));
            if (!file1.exists()) {
                FileUtil.copyBigDataToSD("writableZHCN.dic", file1.getName(),this);
            }
            if (!file2.exists()) {
                FileUtil.copyBigDataToSD("writableZHCN.dic-journal", file2.getName(),this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



    showHandWriting();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    if (!aBoolean) {
                        finish();
                    }
                }
            });
        }*/


    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.delete) {
            if (currentInput.length() > 0) {
                currentInput = new StringBuilder(currentInput.substring(0, currentInput.length() - 1));
                inputShow.setText(currentInput);
            }
            if (currentInput.length() == 0) {
                btnDelete.setVisibility(View.GONE);
            }
        } else if (view.getId() == R.id.cancel) {
            finish();
        } else if (view.getId() == R.id.finish) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("path", fileList);
            intent.putExtra("name", inputShow.getText().toString());
            setResult(100, intent);

            finish();
        } else if (view.getId() == R.id.clean) {
            resetHandWritingRecognizeClicked();
        }


    }


    void showHandWriting() {
        handWritingBoard.setVisibility(View.VISIBLE);
       // ckManager.delAll();
        mCandidateView.clear();

    }


    void resetHandWritingRecognizeClicked() {
        resetHandWritingRecognize();
        mCandidateView.clear();
    }

    @Override
    public void candidateSelected(WnnWord wnnWord) {
        String candidate = null;
        if (wnnWord != null) {
            candidate = wnnWord.candidate;
        }
        if (!TextUtils.isEmpty(candidate)) {
            appendCandidate(candidate);
            inputShow.setText(currentInput.toString());
            btnDelete.setVisibility(View.VISIBLE);
        }
        handler.sendEmptyMessage(0102);





    }


    private void appendCandidate(String candidate) {
        currentInput.append(candidate);
        // currentIndex += candidate.length();
    }

    @Override
    public void handWritingRecognized(ArrayList<WnnWord> result) {
        if (requestList == null) {
            requestList = new ArrayList<WnnWord>();
        } else {
            requestList.clear();
        }

        for (WnnWord ww : result) {
            if (CommonUtil.isChinese(ww.candidate)) {
                requestList.add(ww);
            }

        }
        mCandidateView.setVisibility(View.VISIBLE);
        mCandidateView.setSuggestions(requestList, false, false);
    }

    // TODO 整理一下

    private void resetHandWritingRecognize() {
        handWritingBoard.reset_recognize();
    }

    /*
     * 删除和上屏都要区分手写和字母输入；
     */
    private boolean isHandWriting() {
        return handWritingBoard.getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
