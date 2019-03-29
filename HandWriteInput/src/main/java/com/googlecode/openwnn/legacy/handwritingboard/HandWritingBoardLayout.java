/*
 * View3.java
 *
 * Version info
 *
 * Create time
 *
 * Last modify time
 *
 * Copyright (c) 2010 FOXCONN Technology Group All rights reserved
 */
package com.googlecode.openwnn.legacy.handwritingboard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsoluteLayout;

import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.wwengine.hw.WWHandWrite;

/**
 * View3.java
 * <p>
 */
/**
 * description
 * 
 * @author cairuizhi
 */
public class HandWritingBoardLayout extends AbsoluteLayout {
	private static final String TAG = "SelfAbsoluteLayout";

	private OnHandWritingRecognize mOnHandWritingRecognize;

	private float mx;
	private float my;
	private Path mPath;
	private Paint mPaint;
	private Paint mPaintText;
	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	private static char[] mResult1;
	private static short[] mTracks;
	private static int mCount;
	private static Context mContext;
	private static int mFontSize;
	private String mSavePath = null;
	private Bitmap cacheBitmap;
	private Canvas mCanvas;
	/**
	 * @param context
	 */
	public HandWritingBoardLayout(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public HandWritingBoardLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		ApplicationInfo mAppInfo = mContext.getApplicationInfo();

		init();
		hw_init();
	}

	private void hw_init() {

		byte[] hwData = readData(mContext.getAssets(), "hwdata.bin");
		if (hwData == null) {
			return;
		}

		WWHandWrite.apkBinding(mContext);

		if (WWHandWrite.hwInit(hwData, 0) != 0) {
			return;
		}

		mResult1 = new char[256];
		mTracks = new short[1024];
		mCount = 0;
	}

	private void init() {

		// why only set background then invalidate() valid
		this.setBackgroundColor(0xFFFFFFFF);

		{
			mPath = new Path();
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setDither(true);
			mPaint.setColor(0xFFFF0000);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(6);
		}

		mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintText.setColor(0xFFFF0000);
		mPaintText.setTextSize(mFontSize);
		mPaintText.setTextAlign(Paint.Align.LEFT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(cacheBitmap, 0, 0, mPaint);
		canvas.drawPath(mPath, mPaint);

		// canvas.drawText(mResult.toString(), 10, 100, mPaintText);
		// canvas.drawText(mResult1, 0, 10, 5, 20 + mFontSize/2, mPaintText);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(cacheBitmap);
		mCanvas.drawColor(0xFFFFFFFF);
		//isTouched = false;
	}

	private void touch_start(float x, float y) {
		mPath.moveTo(x, y);
		mX = x;
		mY = y;

		mTracks[mCount++] = (short) x;
		mTracks[mCount++] = (short) y;
	}

	private void touch_move(float x, float y) {
		{
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}
		mTracks[mCount++] = (short) x;
		mTracks[mCount++] = (short) y;
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);

		mTracks[mCount++] = -1;
		mTracks[mCount++] = 0;
		mCanvas.drawPath(mPath,mPaint);
		recognize();
	}

	private void recognize() {
		short[] mTracksTemp;
		int countTemp = mCount;

		mTracksTemp = mTracks.clone();
		mTracksTemp[countTemp++] = -1;
		mTracksTemp[countTemp++] = -1;

		WWHandWrite.hwRecognize(mTracksTemp, mResult1, 10, 0xFFFF);

		if (mOnHandWritingRecognize != null) {

			mOnHandWritingRecognize.handWritingRecognized(convertCharToWnnWord(mResult1));
		}

	}

	public void reset_recognize() {

		if (mCanvas != null) {
			mCanvas.drawColor(0xFFFFFFFF, PorterDuff.Mode.CLEAR);
		}
		mCount = 0;
		mResult1 = new char[256];
		{
			mPath.reset();
		}
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			invalidate();
			break;

		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
			touch_up();
			invalidate();
			break;
		}
		return true;
		// return super.onTouchEvent(event);
	}

	// //////////////////////////////////////////////////////////////
	// readData
	// //////////////////////////////////////////////////////////////
	private static byte[] readData(AssetManager am, String name) {
		try {

			InputStream is = am.open(name);
			if (is == null) {
				return null;
			}

			int length = is.available();
			if (length <= 0) {
				return null;
			}

			byte[] buf = new byte[length];
			if (buf == null) {
				return null;
			}

			if (is.read(buf, 0, length) == -1) {
				return null;
			}

			is.close();

			return buf;

		} catch (Exception ex) {
			return null;
		}
	}

	public void setOnHandWritingRecognize(OnHandWritingRecognize handWritingRecognize) {
		this.mOnHandWritingRecognize = handWritingRecognize;
	}

	private ArrayList<WnnWord> convertCharToWnnWord(char[] result) {
		ArrayList<WnnWord> words = new ArrayList<WnnWord>();
		int length = result.length;
		for (int i = 0; i < length; ++i) {
			WnnWord wnnWord = new WnnWord(String.valueOf(result[i]), "");
			words.add(wnnWord);
		}
		return words;
	}

	/**
	 * 保存图片
	 *
	 * @param path 保存的地址
	 * @param clearBlank 是否清除空白区域
	 * @param blank 空白区域留空距离
	 * @throws IOException
	 */
	public void save(String path, boolean clearBlank, int blank) throws IOException {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		mSavePath = path;
		Bitmap bitmap = cacheBitmap;
		if (clearBlank) {
			bitmap = clearBlank(bitmap, blank);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		byte[] buffer = bos.toByteArray();
		if (buffer != null) {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			OutputStream os = new FileOutputStream(file);
			os.write(buffer);
			os.close();
			bos.close();
		}
	}

	/**
	 * 获取Bitmap缓存
	 */
	public Bitmap getBitmap() {
		setDrawingCacheEnabled(true);
		buildDrawingCache();
		Bitmap bitmap = getDrawingCache();
		setDrawingCacheEnabled(false);
		return bitmap;
	}

	/**
	 * 逐行扫描，清除边界空白
	 *
	 * @param blank 边界留多少个像素
	 */
	private Bitmap clearBlank(Bitmap bmp, int blank) {
		int height = bmp.getHeight();
		int width = bmp.getWidth();
		int top = 0, left = 0, right = 0, bottom = 0;
		int[] pixs = new int[width];
		boolean isStop;
		//扫描上边距不等于背景颜色的第一个点
		for (int i = 0; i < height; i++) {
			bmp.getPixels(pixs, 0, width, 0, i, width, 1);
			isStop = false;
			for (int pix :
					pixs) {
				if (pix != 0xFFFF0000) {
					top = i;
					isStop = true;
					break;
				}
			}
			if (isStop) {
				break;
			}
		}
		//扫描下边距不等于背景颜色的第一个点
		for (int i = height - 1; i >= 0; i--) {
			bmp.getPixels(pixs, 0, width, 0, i, width, 1);
			isStop = false;
			for (int pix :
					pixs) {
				if (pix != 0xFFFF0000) {
					bottom = i;
					isStop = true;
					break;
				}
			}
			if (isStop) {
				break;
			}
		}
		pixs = new int[height];
		//扫描左边距不等于背景颜色的第一个点
		for (int x = 0; x < width; x++) {
			bmp.getPixels(pixs, 0, 1, x, 0, 1, height);
			isStop = false;
			for (int pix : pixs) {
				if (pix != 0xFFFF0000) {
					left = x;
					isStop = true;
					break;
				}
			}
			if (isStop) {
				break;
			}
		}
		//扫描右边距不等于背景颜色的第一个点
		for (int x = width - 1; x > 0; x--) {
			bmp.getPixels(pixs, 0, 1, x, 0, 1, height);
			isStop = false;
			for (int pix : pixs) {
				if (pix != 0xFFFF0000) {
					right = x;
					isStop = true;
					break;
				}
			}
			if (isStop) {
				break;
			}
		}
		if (blank < 0) {
			blank = 0;
		}
		//计算加上保留空白距离之后的图像大小
		left = left - blank > 0 ? left - blank : 0;
		top = top - blank > 0 ? top - blank : 0;
		right = right + blank > width - 1 ? width - 1 : right + blank;
		bottom = bottom + blank > height - 1 ? height - 1 : bottom + blank;
		return Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
	}
}
