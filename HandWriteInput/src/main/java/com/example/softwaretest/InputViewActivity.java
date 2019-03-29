package com.example.softwaretest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.googlecode.openwnn.legacy.CLOUDSONG.OnPinyinQueryed;
import com.googlecode.openwnn.legacy.CLOUDSONG.PinyinQueryResult;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;


@SuppressLint("NewApi")
public class InputViewActivity extends FragmentActivity implements OnCandidateSelected, OnHandWritingRecognize, OnClickListener {

    HandWritingBoardLayout handWritingBoard;
    TextView inputShow;

    LinearLayout container;
    RelativeLayout candidateContainer;

    Button btnCleanHandWriting;
    ImageView btnDelete;


    CandidateView mCandidateView;
    private ArrayList<String> fileList = new ArrayList<String>();

    private StringBuilder currentInput = new StringBuilder();
    private CloudKeyboardInputManager ckManager;
    private List<WnnWord> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_view);

        container = (LinearLayout) findViewById(R.id.container);
        candidateContainer = (RelativeLayout) findViewById(R.id.candidateContainer);
        handWritingBoard = (HandWritingBoardLayout) findViewById(R.id.handwrtingboard);
        btnCleanHandWriting = (Button) findViewById(R.id.clean);
        inputShow = (TextView) findViewById(R.id.candidateselected);
        btnDelete = (ImageView) findViewById(R.id.delete);
        ckManager = new CloudKeyboardInputManager();
        mCandidateView = new CandidateView(this);
        mCandidateView.setOnCandidateSelected(this);

        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //lp1.addRule(RelativeLayout.LEFT_OF, R.id.btn_showMore);

        lp1.width = ViewGroup.LayoutParams.MATCH_PARENT;
        candidateContainer.addView(mCandidateView, lp1);

        handWritingBoard.setOnHandWritingRecognize(this);

        btnCleanHandWriting.setOnClickListener(this);
        btnCleanHandWriting.setOnClickListener(this);
        findViewById(R.id.finish).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        btnDelete.setOnClickListener(this);
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
            setResult(100, intent);

            finish();
        } else if (view.getId() == R.id.clean) {
            resetHandWritingRecognizeClicked();
        }


    }


    void showHandWriting() {
        handWritingBoard.setVisibility(View.VISIBLE);
        ckManager.delAll();
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

        mCandidateView.clear();

        try {
            String path = FileUtil.getAppFilePath(this.getApplicationContext()) + System.currentTimeMillis() + ".png";
            handWritingBoard.save(path, false, 100);
            fileList.add(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isHandWriting()) {
            resetHandWritingRecognize();
        } else {
            ckManager.candidateSelected(wnnWord);
        }


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


}
