package com.kongzue.dialogxdemo.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.interfaces.OnBindView;

import mapleleaf.materialdesign.engine.R;

/**
 * AppCompatActivity 试验场，完成一些原生 UI 相关测试
 */
public class AppCompatActivityTest extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_compat_test);
    }
    
    public void btnTextClick(View view) {
        String s =  "你可以点击空白区域或返回键来关闭这个对话框";
        new BottomDialog("标题", "这里是对话框内容。\n" + s + "。\n底部对话框也支持自定义布局扩展使用方式。",
                new OnBindView<>(R.layout.layout_custom_reply) {
                    @Override
                    public void onBind(BottomDialog dialog, View v) {
                        v.setOnClickListener(v1 -> PopTip.show("TESTA!!"));
                    }
                })
                .show();
    }
}