package com.kongzue.dialogxdemo.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.util.views.DialogXBaseRelativeLayout;

import mapleleaf.materialdesign.engine.R;
import mapleleaf.materialdesign.engine.base.UniversalActivityBase;

public class ActivityTestMain extends UniversalActivityBase {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main_test;
    }

    @Override
    protected void initializeComponents(@Nullable Bundle savedInstanceState) {
//        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));

        DialogXBaseRelativeLayout.debugMode = true;
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        findViewById(R.id.btn_showDialog).setOnClickListener(view -> {

            BottomDialog.show("标题", "这里是对话框内容。")
                    .setCancelButton("取消", (dialog, v) -> false)
                    .setOkButton("确定", (dialog, v) -> false);

//            FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.layout_full_webview) {
//                private TextView btnClose;
//                private WebView webView;
//                @Override
//                public void onBind(final FullScreenDialog dialog, View v) {
//                    btnClose = v.findViewById(R.id.btn_close);
//                    webView = v.findViewById(R.id.webView);
//
//                    btnClose.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            dialog.dismiss();
//                        }
//                    });
//
//                    WebSettings webSettings = webView.getSettings();
//                    webSettings.setJavaScriptEnabled(true);
//                    webSettings.setLoadWithOverviewMode(true);
//                    webSettings.setUseWideViewPort(true);
//                    webSettings.setSupportZoom(false);
//                    webSettings.setAllowFileAccess(true);
//                    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//                    webSettings.setLoadsImagesAutomatically(true);
//                    webSettings.setDefaultTextEncodingName("utf-8");
//
//                    webView.setWebViewClient(new WebViewClient() {
//                        @Override
//                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                            try {
//                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                                startActivity(intent);
//                            } catch (ActivityNotFoundException e) {
//                                e.printStackTrace();
//                            }
//                            return true;
//                        }
//
//                        @Override
//                        public void onPageFinished(WebView view, String url) {
//                            super.onPageFinished(view, url);
//                        }
//                    });
//
//                    webView.loadUrl("https://github.com/kongzue/DialogX");
//                }
//            });
        });
    }

    @Override
    public void addMenuProvider(@NonNull MenuProvider provider, @NonNull LifecycleOwner owner, @NonNull Lifecycle.State state) {

    }
}
