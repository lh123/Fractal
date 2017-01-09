package com.lh.fractal;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.lh.permissionlibrary.RxPermission;

import java.io.File;
import java.io.IOException;

import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity implements FractalView.OnProgressChangeListener {

    private FractalView mSurface;
    private int mCheckedIndex = 1;
    private float mRe = 0.285f;
    private float mIm = 0.01f;

    private CircleProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurface = (FractalView) findViewById(R.id.surface);
        mSurface.beginDraw(0.285f, 0.01f, mCheckedIndex);
        mSurface.setOnProgressChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.progress);
        item.setActionView(R.layout.progress_bar);
        mProgressBar = (CircleProgressBar) item.getActionView().findViewById(R.id.progress_bar);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem juliaMenu = menu.findItem(R.id.julia);
        MenuItem mandelbrotMenu = menu.findItem(R.id.mandelbrot);
        MenuItem draw = menu.findItem(R.id.begin_draw);
        if (mCheckedIndex == 1) {
            juliaMenu.setChecked(true);
            mandelbrotMenu.setChecked(false);
            draw.setVisible(true);
        } else if (mCheckedIndex == 2) {
            juliaMenu.setChecked(false);
            mandelbrotMenu.setChecked(true);
            draw.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.begin_draw) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog, null);
            final EditText editTextRe = (EditText) view.findViewById(R.id.re);
            final EditText editTextIm = (EditText) view.findViewById(R.id.im);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("输入参数")
                    .setView(view)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mRe = parseFloat(editTextRe.getText().toString(), mRe);
                            mIm = parseFloat(editTextIm.getText().toString(), mIm);
                            mSurface.beginDraw(mRe, mIm, mCheckedIndex);
                        }
                    })
                    .setNegativeButton("取消", null);
            builder.show();
        } else if (item.getItemId() == R.id.save) {
            RxPermission.getInstance(this)
                    .requset(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                saveToFile();
                            } else {
                                Toast.makeText(getApplicationContext(), "保存文件需要写入Sdcard权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (item.getItemId() == R.id.julia) {
            mCheckedIndex = 1;
            if (!item.isChecked()) {
                item.setChecked(true);
                mSurface.beginDraw(mRe, mIm, mCheckedIndex);
            }
        } else if (item.getItemId() == R.id.mandelbrot) {
            mCheckedIndex = 2;
            if (!item.isChecked()) {
                item.setChecked(true);
                mSurface.beginDraw(mRe, mIm, mCheckedIndex);
            }
        } else if (item.getItemId() == R.id.reset_size) {
            mSurface.restSize();
        } else if (item.getItemId() == R.id.iterate_times) {
            final EditText editText = new EditText(this);
            editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("输入参数")
                    .setView(editText)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int times = parseInt(editText.getText().toString(), 64);
                            mSurface.setIterateTimes(times);
                        }
                    })
                    .setNegativeButton("取消", null);
            builder.show();
        }
        return true;
    }

    private void saveToFile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(path, "fractal.jpg");
        try {
            mSurface.saveToFile(file);
            Toast.makeText(this, "成功(" + file.getAbsolutePath() + ")", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "失败(" + e.getMessage() + ")", Toast.LENGTH_SHORT).show();
        }
    }

    private float parseFloat(String text, float defaultValue) {
        float result;
        try {
            result = Float.parseFloat(text);
        } catch (Exception e) {
            result = defaultValue;
        }
        return result;
    }

    private int parseInt(String text, int defaultValue) {
        int result;
        try {
            result = Integer.parseInt(text);
        } catch (Exception e) {
            result = defaultValue;
        }
        return result;
    }

    @Override
    public void onProgressChange(int max, int current) {
        if (mProgressBar != null) {
            mProgressBar.setMaxProgress(max);
            mProgressBar.setProgress(current);
        }
    }
}
