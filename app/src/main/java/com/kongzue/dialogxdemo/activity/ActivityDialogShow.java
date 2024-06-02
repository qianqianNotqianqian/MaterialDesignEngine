package com.kongzue.dialogxdemo.activity;

import static com.kongzue.baseframework.BaseApp.dip2px;
import static com.kongzue.baseframework.BaseFrameworkSettings.log;
import static com.kongzue.dialogx.dialogs.PopTip.tip;
import static com.kongzue.dialogx.interfaces.BaseDialog.isNull;
import static mapleleaf.materialdesign.engine.utils.TopLevelFuncationKt.toast;

import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.kongzue.baseframework.util.CycleRunner;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.dialogs.CustomDialog;
import com.kongzue.dialogx.dialogs.FullScreenDialog;
import com.kongzue.dialogx.dialogs.GuideDialog;
import com.kongzue.dialogx.dialogs.InputDialog;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.MessageMenu;
import com.kongzue.dialogx.dialogs.PopMenu;
import com.kongzue.dialogx.dialogs.PopNotification;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.BottomDialogSlideEventLifecycleCallback;
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback;
import com.kongzue.dialogx.interfaces.DialogXAnimInterface;
import com.kongzue.dialogx.interfaces.DialogXRunnable;
import com.kongzue.dialogx.interfaces.MenuItemTextInfoInterceptor;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnBackgroundMaskClickListener;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack;
import com.kongzue.dialogx.interfaces.OnInputDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnMenuButtonClickListener;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;
import com.kongzue.dialogx.interfaces.OnMenuItemSelectListener;
import com.kongzue.dialogx.style.IOSStyle;
import com.kongzue.dialogx.style.KongzueStyle;
import com.kongzue.dialogx.style.MIUIStyle;
import com.kongzue.dialogx.style.MaterialStyle;
import com.kongzue.dialogx.util.TextInfo;
import com.kongzue.dialogxdemo.fragment.CustomFragment;
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle;

import java.util.Random;

import mapleleaf.materialdesign.engine.R;
import mapleleaf.materialdesign.engine.base.UniversalActivityBase;

public class ActivityDialogShow extends UniversalActivityBase {

    private Handler handler;
    private MaterialButtonToggleGroup grpStyle;
    private MaterialButton rdoMaterial;
    private MaterialButton rdoIos;
    private MaterialButton rdoKongzue;
    private MaterialButton rdoMiui;
    private MaterialButton rdoMaterialYou;
    private MaterialButtonToggleGroup grpTheme;
    private MaterialButton rdoAuto;
    private MaterialButton rdoLight;
    private MaterialButton rdoDark;
    private MaterialButtonToggleGroup grpMode;
    private MaterialButton rdoModeView;
    private MaterialButton rdoModeWindow;
    private MaterialButton rdoModeDialogFragment;
    private MaterialButton rdoModeFloatingActivity;
    private MaterialButton btnMessageDialog;
    private MaterialButton btnSelectDialog;
    private MaterialButton btnInputDialog;
    private MaterialButton btnSelectMessageMenu;
    private MaterialButton btnMutiSelectMessageMenu;
    private MaterialButton btnWaitDialog;
    private MaterialButton btnWaitAndTipDialog;
    private MaterialButton btnTipSuccess;
    private MaterialButton btnTipWarning;
    private MaterialButton btnTipError;
    private MaterialButton btnTipProgress;
    private MaterialButton btnPoptip;
    private MaterialButton btnPoptipBigMessage;
    private MaterialButton btnPoptipSuccess;
    private MaterialButton btnPoptipWarning;
    private MaterialButton btnPoptipError;
    private MaterialButton btnPopnotification;
    private MaterialButton btnPopnotificationBigMessage;
    private MaterialButton btnPopnotificationOverlay;
    private MaterialButton btnBottomDialog;
    private MaterialButton btnBottomMenu;
    private MaterialButton btnBottomReply;
    private MaterialButton btnBottomSelectMenu;
    private MaterialButton btnBottomMultiSelectMenu;
    private MaterialButton btnBottomCustomRecycleView;
    private MaterialButton btnCustomMessageDialog;
    private MaterialButton btnCustomInputDialog;
    private MaterialButton btnCustomBottomMenu;
    private MaterialButton btnCustomDialog;
    private MaterialButton btnCustomDialogAlign;
    private MaterialButton btnFullScreenDialogWebPage;
    private MaterialButton btnFullScreenDialogLogin;
    private MaterialButton btnFullScreenDialogFragment;
    private MaterialButton btnContextMenu;
    private TextView btnSelectMenu;
    private MaterialButton btnShowGuide;
    private MaterialButton btnShowGuideBaseView;
    private MaterialButton btnShowGuideBaseViewRectangle;
    private MaterialButton btnListDialog;
    private TextView txtVer;
    //用于模拟进度提示
    private CycleRunner cycleRunner;
    private float progress = 0;
    private int waitId;
    private TextView btnReplyCommit;
    private EditText editReplyCommit;
    private TextView btnCancel;
    private TextView btnSubmit;
    private RelativeLayout boxUserName;
    private EditText editUserName;
    private RelativeLayout boxPassword;
    private EditText editPassword;
    private TextView btnLicense;
    private TextView btnClose;
    private WebView webView;
    private final String[] singleSelectMenuText = new String[]{"拒绝", "询问", "始终允许", "仅在使用中允许"};
    private int selectMenuIndex;
    private final String[] multiSelectMenuText = new String[]{"上海", "北京", "广州", "深圳"};
    private int[] selectMenuIndexArray;
    private String multiSelectMenuResultCache;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initializeComponents(@Nullable Bundle savedInstanceState) {

        initViews();
        setEvents();
        switch (DialogX.implIMPLMode) {
            case VIEW:
                rdoModeView.setChecked(true);
                break;
            case WINDOW:
                rdoModeWindow.setChecked(true);
                break;
            case DIALOG_FRAGMENT:
                rdoModeDialogFragment.setChecked(true);
                break;
            case FLOATING_ACTIVITY:
                rdoModeFloatingActivity.setChecked(true);
                break;
        }

        handler = new Handler(getMainLooper());

        setToolbarTitle(getString(R.string.toolbar_title_activity_md_dialog));
    }

    public void setEvents() {
        grpMode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                BaseDialog.cleanAll();
                if (checkedId == R.id.rdo_mode_view) {
                    DialogX.implIMPLMode = DialogX.IMPL_MODE.VIEW;
                } else if (checkedId == R.id.rdo_mode_window) {
                    DialogX.implIMPLMode = DialogX.IMPL_MODE.WINDOW;
                } else if (checkedId == R.id.rdo_mode_dialogFragment) {
                    DialogX.implIMPLMode = DialogX.IMPL_MODE.DIALOG_FRAGMENT;
                } else if (checkedId == R.id.rdo_mode_floatingActivity) {
                    DialogX.implIMPLMode = DialogX.IMPL_MODE.FLOATING_ACTIVITY;
                }
            }
        });

        grpTheme.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.rdo_auto) {
                    DialogX.globalTheme = DialogX.THEME.AUTO;
                } else if (checkedId == R.id.rdo_light) {
                    DialogX.globalTheme = DialogX.THEME.LIGHT;
                } else if (checkedId == R.id.rdo_dark) {
                    DialogX.globalTheme = DialogX.THEME.DARK;
                }
            }
        });

        grpStyle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                DialogX.cancelButtonText = "取消";
                DialogX.titleTextInfo = null;
                DialogX.buttonTextInfo = null;
                if (checkedId == R.id.rdo_material) {
                    DialogX.globalStyle = MaterialStyle.style();
                    DialogX.cancelButtonText = "";
                } else if (checkedId == R.id.rdo_kongzue) {
                    DialogX.globalStyle = KongzueStyle.style();
                } else if (checkedId == R.id.rdo_ios) {
                    DialogX.globalStyle = IOSStyle.style();
                } else if (checkedId == R.id.rdo_miui) {
                    DialogX.globalStyle = MIUIStyle.style();
                } else if (checkedId == R.id.rdo_material_you) {
                    DialogX.globalStyle = MaterialYouStyle.style();
                }
            }
        });

        btnContextMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopMenu.show("添加", "编辑", "删除", "分享")
                        .disableMenu("编辑", "删除")
                        .setIconResIds(R.mipmap.img_dialogx_demo_add, R.mipmap.img_dialogx_demo_edit, R.mipmap.img_dialogx_demo_delete, R.mipmap.img_dialogx_demo_share)
                        .setOnMenuItemClickListener(new OnMenuItemClickListener<PopMenu>() {
                            @Override
                            public boolean onClick(PopMenu dialog, CharSequence text, int index) {
                                if (index == 0) {
                                    dialog.setMenuList(new String[]{"产品A", "产品B", "产品C"});
                                    return true;
                                }
                                return false;
                            }
                        });
            }
        });

        btnSelectMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                PopMenu.show(view, new String[]{"选项1", "选项2", "选项3"})
                        .setOnMenuItemClickListener(new OnMenuItemClickListener<PopMenu>() {
                            @Override
                            public boolean onClick(PopMenu dialog, CharSequence text, int index) {
                                btnSelectMenu.setText(text);
                                return false;
                            }
                        });
            }
        });

        btnFullScreenDialogFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomFragment customFragment = new CustomFragment()
                        .setAddButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                btnFullScreenDialogFragment.callOnClick();
                            }
                        });
                FullScreenDialog.build(new OnBindView<FullScreenDialog>(customFragment) {
                            @Override
                            public void onBind(FullScreenDialog dialog, View v) {

                            }
                        })
                        .hideActivityContentView(true)
                        .show();
            }
        });

        btnMessageDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageDialog.show("标题", "这里是正文内容。", "确定")
                        .onShow(new DialogXRunnable<MessageDialog>() {
                            @Override
                            public void run(MessageDialog dialog) {
                                tip("onShow");
                            }
                        })
                        .onDismiss(new DialogXRunnable<MessageDialog>() {
                            @Override
                            public void run(MessageDialog dialog) {
                                tip("onDismiss");
                            }
                        })
                        .setTitleIcon(R.mipmap.img_demo_avatar)
                        .setOkButton(new OnDialogButtonClickListener<MessageDialog>() {
                            @Override
                            public boolean onClick(MessageDialog baseDialog, View v) {
                                PopTip.show("点击确定按钮");
                                return true;
                            }
                        });
            }
        });

        btnSelectDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialog messageDialog = new MessageDialog("多选对话框", "移除App会将它从主屏幕移除并保留其所有数据。", "删除App", "取消", "移至App资源库")
                        .setButtonOrientation(LinearLayout.VERTICAL);
                if (!rdoMiui.isChecked()) {
                    messageDialog.setOkTextInfo(new TextInfo().setFontColor(Color.parseColor("#EB5545")).setBold(true));
                }
                messageDialog.show();
            }
        });

        btnInputDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new InputDialog("标题", "正文内容", "确定", "取消", "正在输入的文字")
                        .setInputText("Hello World")
                        .setOkButton(new OnInputDialogButtonClickListener<InputDialog>() {
                            @Override
                            public boolean onClick(InputDialog baseDialog, View v, String inputStr) {
                                PopTip.show("输入的内容：" + inputStr);
                                return false;
                            }
                        })
                        .show();
            }
        });

        btnSelectMessageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageMenu.show(singleSelectMenuText)
                        .setShowSelectedBackgroundTips(rdoMiui.isChecked())
                        .setMessage("这里是权限确认的文本说明，这是一个演示菜单")
                        .setTitle("获得权限标题")
                        .setOnMenuItemClickListener(new OnMenuItemSelectListener<>() {
                            @Override
                            public void onOneItemSelect(MessageMenu dialog, CharSequence text, int index, boolean select) {
                                selectMenuIndex = index;
                            }
                        })
                        .setCancelButton("确定", new OnMenuButtonClickListener<MessageMenu>() {
                            @Override
                            public boolean onClick(MessageMenu baseDialog, View v) {
                                PopTip.show("已选择：" + singleSelectMenuText[selectMenuIndex]);
                                return false;
                            }
                        })
                        .setSelection(selectMenuIndex);
            }
        });
        btnMutiSelectMessageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageMenu.show(multiSelectMenuText)
                        .setMessage("这里是选择城市的模拟范例，这是一个演示菜单")
                        .setTitle("请选择城市")
                        .setOnMenuItemClickListener(new OnMenuItemSelectListener<>() {
                            @Override
                            public void onMultiItemSelect(MessageMenu dialog, CharSequence[] text, int[] index) {
                                multiSelectMenuResultCache = "";
                                for (CharSequence c : text) {
                                    multiSelectMenuResultCache = multiSelectMenuResultCache + " " + c;
                                }
                                selectMenuIndexArray = index;
                            }
                        })
                        .setOkButton("确定", new OnMenuButtonClickListener<MessageMenu>() {
                            @Override
                            public boolean onClick(MessageMenu dialog, View v) {
                                PopTip.show("已选择：" + multiSelectMenuResultCache);
                                return false;
                            }
                        })
                        .setSelection(selectMenuIndexArray);
            }
        });

        btnWaitDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WaitDialog.show("Please Wait!")
                        .setOnBackPressedListener(new OnBackPressedListener<WaitDialog>() {
                            @Override
                            public boolean onBackPressed(WaitDialog dialog) {
                                PopTip.show("按下返回");
                                return false;
                            }
                        });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WaitDialog.dismiss();
                    }
                }, 1500);
            }
        });

        btnWaitAndTipDialog.setOnClickListener(new View.OnClickListener() {

            boolean closeFlag = false;

            @Override
            public void onClick(View v) {
                closeFlag = false;
                WaitDialog.show("Please Wait!").setOnBackPressedListener(new OnBackPressedListener<WaitDialog>() {
                    @Override
                    public boolean onBackPressed(WaitDialog dialog) {
                        PopTip.show("按下返回", "关闭").setButton(new OnDialogButtonClickListener<PopTip>() {
                            @Override
                            public boolean onClick(PopTip baseDialog, View v) {
                                closeFlag = true;
                                WaitDialog.dismiss();
                                return false;
                            }
                        });
                        return false;
                    }
                });
                if (!closeFlag) {
                    handler.postDelayed(() -> {
                        if (!closeFlag) {
                            TipDialog.show("完成！", WaitDialog.TYPE.SUCCESS);
                        }
                    }, 1500 + new Random().nextInt(1000));
                }
            }
        });

        btnTipSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TipDialog.show("Success!", WaitDialog.TYPE.SUCCESS);
            }
        });

        btnTipWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TipDialog.show("Warning!", WaitDialog.TYPE.WARNING);
            }
        });

        btnTipError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TipDialog.show("Error!", WaitDialog.TYPE.ERROR);
            }
        });

        btnTipProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waitId = 0;
                progress = 0;
                WaitDialog.show("假装连接...").setOnBackPressedListener(new OnBackPressedListener<WaitDialog>() {
                    @Override
                    public boolean onBackPressed(WaitDialog dialog) {
                        MessageDialog.show("正在进行", "是否取消？", "是", "否").setOkButton((OnDialogButtonClickListener) (baseDialog, v1) -> {
                            waitId = -1;
                            WaitDialog.dismiss();
                            return false;
                        });
                        return false;
                    }
                });
                handler.postDelayed(() -> {
                    if (waitId != 0) {
                        return;
                    }
                    // 替换 runOnMainCycle 方法的调用
                    Runnable cycleRunner = new Runnable() {
                        float progress = 0f;

                        @Override
                        public void run() {
                            if (waitId != 0) {
                                handler.removeCallbacks(this);
                                return;
                            }
                            progress += 0.1f;
                            if (progress < 1f) {
                                WaitDialog.show("假装加载" + ((int) (progress * 100)) + "%", progress);
                                handler.postDelayed(this, 1000);
                            } else {
                                TipDialog.show("加载完成", WaitDialog.TYPE.SUCCESS);
                                handler.removeCallbacks(this);
                            }
                        }
                    };
                    handler.post(cycleRunner);
                }, 3000);
            }
        });

        btnBottomDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = rdoMaterial.isChecked() ? "你可以向下滑动来关闭这个对话框" : "你可以点击空白区域或返回键来关闭这个对话框";
                new BottomDialog("标题", "这里是对话框内容。\n" + s + "。\n底部对话框也支持自定义布局扩展使用方式。",
                        new OnBindView<BottomDialog>(R.layout.layout_custom_view) {
                            @Override
                            public void onBind(BottomDialog dialog, View v) {
                                if (dialog.getDialogImpl().imgTab != null) {
                                    ((ViewGroup) dialog.getDialogImpl().imgTab.getParent()).removeView(dialog.getDialogImpl().imgTab);
                                }
                                v.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        PopTip.show("Click Custom View");
                                    }
                                });
                            }
                        })
                        .setDialogLifecycleCallback(new BottomDialogSlideEventLifecycleCallback<BottomDialog>() {
                            @Override
                            public boolean onSlideClose(BottomDialog dialog) {
                                log("#onSlideClose");
                                return super.onSlideClose(dialog);
                            }

                            @Override
                            public boolean onSlideTouchEvent(BottomDialog dialog, View v, MotionEvent event) {
                                log("#onSlideTouchEvent: action=" + event.getAction() + " y=" + event.getY());
                                return super.onSlideTouchEvent(dialog, v, event);
                            }
                        })
                        .show();
            }
        });

        btnBottomMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rdoMaterial.isChecked()) {
                    //Material 可滑动展开 BottomMenu 演示
                    BottomMenu.build()
                            .setBottomDialogMaxHeight(0.6f)
                            .setMenuList(new String[]{"添加", "查看", "编辑", "删除", "分享", "评论", "下载", "收藏", "赞！", "不喜欢", "所属专辑", "复制链接", "类似推荐", "添加", "查看", "编辑", "删除", "分享", "评论", "下载", "收藏", "赞！", "不喜欢", "所属专辑", "复制链接", "类似推荐"})
                            .setOnIconChangeCallBack(new OnIconChangeCallBack<BottomMenu>(true) {
                                @Override
                                public int getIcon(BottomMenu bottomMenu, int index, String menuText) {
                                    switch (menuText) {
                                        case "添加":
                                            return R.mipmap.img_dialogx_demo_add;
                                        case "查看":
                                            return R.mipmap.img_dialogx_demo_view;
                                        case "编辑":
                                            return R.mipmap.img_dialogx_demo_edit;
                                        case "删除":
                                            return R.mipmap.img_dialogx_demo_delete;
                                        case "分享":
                                            return R.mipmap.img_dialogx_demo_share;
                                        case "评论":
                                            return R.mipmap.img_dialogx_demo_comment;
                                        case "下载":
                                            return R.mipmap.img_dialogx_demo_download;
                                        case "收藏":
                                            return R.mipmap.img_dialogx_demo_favorite;
                                        case "赞！":
                                            return R.mipmap.img_dialogx_demo_good;
                                        case "不喜欢":
                                            return R.mipmap.img_dialogx_demo_dislike;
                                        case "所属专辑":
                                            return R.mipmap.img_dialogx_demo_album;
                                        case "复制链接":
                                            return R.mipmap.img_dialogx_demo_link;
                                        case "类似推荐":
                                            return R.mipmap.img_dialogx_demo_recommend;
                                    }
                                    return 0;
                                }
                            })
                            .setOnMenuItemClickListener(new OnMenuItemClickListener<BottomMenu>() {
                                @Override
                                public boolean onClick(BottomMenu dialog, CharSequence text, int index) {
                                    PopTip.show(text);
                                    return false;
                                }
                            })
                            .show();

//                      测试用代码
//                    BottomMenu.show("添加", "查看", "编辑")
//                            .setIconResIds(R.mipmap.img_dialogx_demo_add,
//                                    R.mipmap.img_dialogx_demo_view,
//                                    R.mipmap.img_dialogx_demo_edit
//                            );
                } else {
                    BottomMenu.show(new String[]{"新标签页中打开", "稍后阅读", "复制链接网址"})
                            .setMessage("http://www.kongzue.com/DialogX")
                            .setMenuItemTextInfoInterceptor(new MenuItemTextInfoInterceptor<BottomMenu>() {
                                @Override
                                public TextInfo menuItemTextInfo(BottomMenu dialog, int index, String menuText) {
                                    if (index == 2) {
                                        return new TextInfo()
                                                .setFontColor(Color.RED)
                                                .setBold(true);
                                    }
                                    return null;
                                }
                            })
                            .setOnMenuItemClickListener(new OnMenuItemClickListener<BottomMenu>() {
                                @Override
                                public boolean onClick(BottomMenu dialog, CharSequence text, int index) {
                                    PopTip.show(text);
                                    return false;
                                }
                            });
                }
            }
        });

        btnBottomReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomDialog.show(new OnBindView<BottomDialog>(rdoDark.isChecked() ? R.layout.layout_custom_reply_dark : R.layout.layout_custom_reply) {
                            @Override
                            public void onBind(final BottomDialog dialog, View v) {
                                btnReplyCommit = v.findViewById(R.id.btn_reply_commit);
                                editReplyCommit = v.findViewById(R.id.edit_reply_commit);
                                btnReplyCommit.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        PopTip.show("提交内容：\n" + editReplyCommit.getText().toString());
                                    }
                                });
                                editReplyCommit.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        showIME(editReplyCommit);
                                    }
                                }, 300);
                            }
                        })
                        .setAllowInterceptTouch(false);
            }
        });

        btnCustomMessageDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialog.show("这里是标题", "此对话框演示的是自定义对话框内部布局的效果", "确定", "取消")
                        .setDialogLifecycleCallback(new BottomDialogSlideEventLifecycleCallback<MessageDialog>() {
                            @Override
                            public void onShow(MessageDialog dialog) {
                                super.onShow(dialog);
                                dialog.getDialogImpl().txtDialogTip.setPadding(0, dip2px(20), 0, 0);
                            }
                        })
                        .setCustomView(new OnBindView<>(R.layout.layout_custom_view) {
                            @Override
                            public void onBind(MessageDialog dialog, View v) {

                            }
                        });
            }
        });

        btnCustomInputDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputDialog.show("这里是标题", "此对话框演示的是自定义对话框内部布局的效果", "确定", "取消")
                        .setCustomView(new OnBindView<MessageDialog>(R.layout.layout_custom_view) {
                            @Override
                            public void onBind(MessageDialog dialog, View v) {

                            }
                        });
            }
        });

        //仅使用渐变动画的实现示例
        DialogXAnimInterface<BottomDialog> alphaDialogAnimation = new DialogXAnimInterface<BottomDialog>() {

            @Override
            //入场动画
            public void doShowAnim(BottomDialog dialog, ViewGroup dialogBodyView) {
                //设置好位置，将默认预设会在屏幕底外部恢复为屏幕内
                dialog.getDialogImpl().boxBkg.setY(dialog.getDialogImpl().boxRoot.getUnsafePlace().top);
                //创建 0f~1f 的数值动画
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        //修改背景遮罩透明度
                        dialog.getDialogImpl().boxRoot.setBkgAlpha(value);
                        //修改内容透明度
                        dialog.getDialogImpl().bkg.setAlpha(value);
                    }
                });
                //使用真正的动画时长
                animator.setDuration(dialog.getDialogImpl().getEnterAnimationDuration());
                animator.start();
            }

            @Override
            //出场动画
            public void doExitAnim(BottomDialog dialog, ViewGroup dialogBodyView) {
                ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        //这里可以直接修改整体透明度淡出
                        dialog.getDialogImpl().boxRoot.setAlpha(value);
                    }
                });
                animator.setDuration(dialog.getDialogImpl().getExitAnimationDuration());
                animator.start();
            }
        };

        btnCustomBottomMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomMenu.show(new String[]{"新标签页中打开", "稍后阅读", "复制链接网址"})
                        .setMessage("http://www.kongzue.com/DialogX")
                        .setOnMenuItemClickListener(new OnMenuItemClickListener<BottomMenu>() {
                            @Override
                            public boolean onClick(BottomMenu dialog, CharSequence text, int index) {
                                PopTip.show(text);
                                return false;
                            }
                        })
                        //.setDialogXAnimImpl(alphaDialogAnimation)
                        .setCustomView(new OnBindView<BottomDialog>(R.layout.layout_custom_view) {
                            @Override
                            public void onBind(BottomDialog dialog, View v) {

                            }
                        });
            }
        });

        btnShowGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuideDialog.show(R.mipmap.img_guide_tip);
            }
        });

        btnShowGuideBaseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuideDialog.show(btnFullScreenDialogLogin, R.mipmap.img_tip_login)
                        .setBaseViewMarginTop(-dip2px(30));
            }
        });

        btnShowGuideBaseViewRectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuideDialog.show(btnCustomDialogAlign,
                                GuideDialog.STAGE_LIGHT_TYPE.RECTANGLE,
                                R.mipmap.img_tip_login_clicktest)
                        .setStageLightFilletRadius(dip2px(5))
                        .setBaseViewMarginTop(-dip2px(30))
                        .setOnBackgroundMaskClickListener(new OnBackgroundMaskClickListener<CustomDialog>() {
                            @Override
                            public boolean onClick(CustomDialog dialog, View v) {
                                toast("点击了外围遮罩");
                                return false;
                            }
                        })
                        .setOnStageLightPathClickListener(new OnDialogButtonClickListener<GuideDialog>() {
                            @Override
                            public boolean onClick(GuideDialog dialog, View v) {
                                toast("点击了原按钮");
                                btnCustomDialogAlign.callOnClick();
                                return false;
                            }
                        });
            }
        });

        btnListDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogX.showDialogList(
                        MessageDialog.build().setTitle("提示").setMessage("这是一组消息对话框队列").setOkButton("开始").setCancelButton("取消")
                                .setCancelButton(new OnDialogButtonClickListener<MessageDialog>() {
                                    @Override
                                    public boolean onClick(MessageDialog dialog, View v) {
                                        dialog.cleanDialogList();
                                        return false;
                                    }
                                }),
                        PopTip.build().setMessage("每个对话框会依次显示"),
                        PopNotification.build().setTitle("通知提示").setMessage("直到上一个对话框消失"),
                        InputDialog.build().setTitle("请注意").setMessage("你必须使用 .build() 方法构建，并保证不要自己执行 .show() 方法").setInputText("输入文字").setOkButton("知道了"),
                        TipDialog.build().setMessageContent("准备结束...").setTipType(WaitDialog.TYPE.SUCCESS),
                        BottomDialog.build().setTitle("结束").setMessage("下滑以结束旅程，祝你编码愉快！").setCustomView(new OnBindView<BottomDialog>(R.layout.layout_custom_dialog) {
                            @Override
                            public void onBind(BottomDialog dialog, View v) {
                                ImageView btnOk;
                                btnOk = v.findViewById(R.id.btn_ok);
                                btnOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        })
                );
            }
        });

        btnFullScreenDialogLogin.setOnClickListener(new View.OnClickListener() {

            /**
             * 采用异步加载布局防止卡顿测试
             */

            OnBindView<FullScreenDialog> onBindView;

            @Override
            public void onClick(View v) {
                onBindView = new OnBindView<>(R.layout.layout_full_login, true) {
                    @Override
                    public void onBind(FullScreenDialog dialog, View v) {
                        btnCancel = v.findViewById(R.id.btn_cancel);
                        btnSubmit = v.findViewById(R.id.btn_submit);
                        boxUserName = v.findViewById(R.id.box_userName);
                        editUserName = v.findViewById(R.id.edit_userName);
                        boxPassword = v.findViewById(R.id.box_password);
                        editPassword = v.findViewById(R.id.edit_password);
                        btnLicense = v.findViewById(R.id.btn_license);

                        initFullScreenLoginDemo(dialog);
                    }
                };
                FullScreenDialog.show(onBindView);
            }
        });

        btnFullScreenDialogWebPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenDialog.show(new OnBindView<FullScreenDialog>(R.layout.layout_full_webview) {
                    @Override
                    public void onBind(final FullScreenDialog dialog, View v) {
                        btnClose = v.findViewById(R.id.btn_close);
                        webView = v.findViewById(R.id.webView);

                        btnClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        webSettings.setLoadWithOverviewMode(true);
                        webSettings.setUseWideViewPort(true);
                        webSettings.setSupportZoom(false);
                        webSettings.setAllowFileAccess(true);
                        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                        webSettings.setLoadsImagesAutomatically(true);
                        webSettings.setDefaultTextEncodingName("utf-8");

                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }

                            @Override
                            public void onPageFinished(WebView view, String url) {
                                super.onPageFinished(view, url);
                            }
                        });

                        webView.loadUrl("https://github.com/kongzue/DialogX");
                    }
                }).setBottomNonSafetyAreaBySelf(false);
            }
        });

        btnCustomDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog.show(new OnBindView<CustomDialog>(R.layout.layout_custom_dialog) {
                            @Override
                            public void onBind(final CustomDialog dialog, View v) {
                                ImageView btnOk;
                                btnOk = v.findViewById(R.id.btn_ok);
                                btnOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        })
//                        .setAlign(CustomDialog.ALIGN.LEFT)
                        //.setAnimResId(R.anim.anim_right_in, R.anim.anim_right_out)
                        .setMaskColor(getResources().getColor(com.kongzue.dialogx.R.color.black30))
                //实现完全自定义动画效果
//                        .setDialogXAnimImpl(new DialogXAnimInterface<CustomDialog>() {
//                            @Override
//                            public void doShowAnim(CustomDialog customDialog, ViewGroup dialogBodyView) {
//                                Animation enterAnim;
//
//                                int enterAnimResId = com.kongzue.dialogx.R.anim.anim_dialogx_top_enter;
//                                enterAnim = AnimationUtils.loadAnimation(me, enterAnimResId);
//                                enterAnim.setInterpolator(new DecelerateInterpolator(2f));
//
//                                long enterAnimDurationTemp = enterAnim.getDuration();
//
//                                enterAnim.setDuration(enterAnimDurationTemp);
//                                customDialog.getDialogImpl().boxCustom.startAnimation(enterAnim);
//
//                                ValueAnimator bkgAlpha = ValueAnimator.ofFloat(0f, 1f);
//                                bkgAlpha.setDuration(enterAnimDurationTemp);
//                                bkgAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                    @Override
//                                    public void onAnimationUpdate(ValueAnimator animation) {
//                                        if (customDialog.getDialogImpl() == null || customDialog.getDialogImpl().boxRoot == null) {
//                                            return;
//                                        }
//                                        customDialog.getDialogImpl().boxRoot.setBkgAlpha((Float) animation.getAnimatedValue());
//                                    }
//                                });
//                                bkgAlpha.start();
//                            }
//
//                            @Override
//                            public void doExitAnim(CustomDialog customDialog, ViewGroup dialogBodyView) {
//                                int exitAnimResIdTemp = com.kongzue.dialogx.R.anim.anim_dialogx_default_exit;
//
//                                Animation exitAnim = AnimationUtils.loadAnimation(me, exitAnimResIdTemp);
//                                customDialog.getDialogImpl().boxCustom.startAnimation(exitAnim);
//
//                                ValueAnimator bkgAlpha = ValueAnimator.ofFloat(1f, 0f);
//                                bkgAlpha.setDuration(exitAnim.getDuration());
//                                bkgAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                    @Override
//                                    public void onAnimationUpdate(ValueAnimator animation) {
//                                        if (customDialog.getDialogImpl() == null || customDialog.getDialogImpl().boxRoot == null) {
//                                            return;
//                                        }
//                                        customDialog.getDialogImpl().boxRoot.setBkgAlpha((Float) animation.getAnimatedValue());
//                                    }
//                                });
//                                bkgAlpha.start();
//                            }
//                        })
                ;
            }
        });

        btnCustomDialogAlign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog.show(new OnBindView<CustomDialog>(R.layout.layout_custom_dialog_align) {

                            private TextView btnSelectPositive;

                            @Override
                            public void onBind(final CustomDialog dialog, View v) {
                                btnSelectPositive = v.findViewById(R.id.btn_selectPositive);
                                btnSelectPositive.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PopTip.show("我知道了");
                                        dialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setCancelable(false)
                        .setMaskColor(getResources().getColor(com.kongzue.dialogx.R.color.black30))
                        .setEnterAnimResId(R.anim.anim_custom_pop_enter)
                        .setExitAnimResId(R.anim.anim_custom_pop_exit)
                        .setAlignBaseViewGravity(btnCustomDialogAlign, Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                        .setBaseViewMarginBottom(-dip2px(45))
                        .show();
            }
        });

        btnPoptip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopTip.show("这是一个提示");
            }
        });

        btnPoptipBigMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rdoIos.isChecked()) {
                    PopTip.show(R.mipmap.img_air_pods_pro, "AirPods Pro 已连接").setTintIcon(false).showLong();
                } else {
                    PopTip.show(R.mipmap.img_mail_line_white, "邮件已发送", "撤回").setButton(new OnDialogButtonClickListener<PopTip>() {
                        @Override
                        public boolean onClick(PopTip popTip, View v) {
                            //点击“撤回”按钮回调
                            toast("邮件已撤回");
                            return false;
                        }
                    }).setTintIcon(true).showLong();
                }
            }
        });

        btnPoptipSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopTip.show("操作已完成").iconSuccess();
            }
        });

        btnPoptipWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopTip.show("存储空间不足").setButton("立即清理", new OnDialogButtonClickListener<PopTip>() {
                    @Override
                    public boolean onClick(PopTip baseDialog, View v) {
                        toast("点击了立即清理");
                        return false;
                    }
                }).iconWarning();
            }
        });

        btnPoptipError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopTip.show("无法连接网络").iconError();
            }
        });

        btnBottomSelectMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomMenu.show(singleSelectMenuText)
                        .setShowSelectedBackgroundTips(rdoMiui.isChecked())
                        .setMessage("这里是权限确认的文本说明，这是一个演示菜单")
                        .setTitle("获得权限标题")
                        .setOnMenuItemClickListener(new OnMenuItemSelectListener<BottomMenu>() {
                            @Override
                            public void onOneItemSelect(BottomMenu dialog, CharSequence text, int index, boolean select) {
                                selectMenuIndex = index;
                            }
                        })
                        .setCancelButton("确定", new OnMenuButtonClickListener<BottomMenu>() {
                            @Override
                            public boolean onClick(BottomMenu baseDialog, View v) {
                                PopTip.show("已选择：" + singleSelectMenuText[selectMenuIndex]);
                                return false;
                            }
                        })
                        .setSelection(selectMenuIndex);
            }
        });

        btnBottomMultiSelectMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomMenu.show(multiSelectMenuText)
                        .setMessage("这里是选择城市的模拟范例，这是一个演示菜单")
                        .setTitle("请选择城市")
                        .setOnMenuItemClickListener(new OnMenuItemSelectListener<BottomMenu>() {
                            @Override
                            public void onMultiItemSelect(BottomMenu dialog, CharSequence[] text, int[] index) {
                                multiSelectMenuResultCache = "";
                                for (CharSequence c : text) {
                                    multiSelectMenuResultCache = multiSelectMenuResultCache + " " + c;
                                }
                                selectMenuIndexArray = index;
                            }
                        })
                        .setOkButton("确定", new OnMenuButtonClickListener<BottomMenu>() {
                            @Override
                            public boolean onClick(BottomMenu dialog, View v) {
                                PopTip.show("已选择：" + multiSelectMenuResultCache);
                                return false;
                            }
                        })
//                        .setCancelButton("确定", new OnDialogButtonClickListener<BottomDialog>() {
//                            @Override
//                            public boolean onClick(BottomDialog baseDialog, View v) {
//                                PopTip.show("已选择：" + multiSelectMenuResultCache);
//                                return false;
//                            }
//                        })
                        .setSelection(selectMenuIndexArray);
            }
        });
    }

    private void initFullScreenLoginDemo(final FullScreenDialog fullScreenDialog) {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreenDialog.dismiss();
            }
        });

        btnCancel.setText("取消");
        btnSubmit.setText("下一步");

        btnLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopTip.show("点击用户服务条款");
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNull(editUserName.getText().toString().trim())) {
                    hideIME(null);
                    TipDialog.show("请输入账号", TipDialog.TYPE.WARNING);
                    return;
                }

                boxUserName.animate().x(-getDisplayWidth()).setDuration(300);
                boxPassword.setX(getDisplayWidth());
                boxPassword.setVisibility(View.VISIBLE);
                boxPassword.animate().x(0).setDuration(300);

                editPassword.setFocusable(true);
                editPassword.requestFocus();

                btnCancel.setText("上一步");
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boxUserName.animate().x(0).setDuration(300);
                        boxPassword.animate().x(getDisplayWidth()).setDuration(300);

                        editUserName.setFocusable(true);
                        editUserName.requestFocus();

                        initFullScreenLoginDemo(fullScreenDialog);
                    }
                });

                btnSubmit.setText("登录");
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideIME(null);
                        if (isNull(editPassword.getText().toString().trim())) {
                            TipDialog.show("请输入密码", TipDialog.TYPE.WARNING);
                            return;
                        }
                        WaitDialog.show("登录中...");
                        handler.postDelayed(() -> {
                            TipDialog.show("登录成功", TipDialog.TYPE.SUCCESS)
                                    .setDialogLifecycleCallback(new DialogLifecycleCallback<WaitDialog>() {
                                        @Override
                                        public void onDismiss(WaitDialog dialog) {
                                            fullScreenDialog.dismiss();
                                        }
                                    });
                        }, 2000);
                    }
                });
            }
        });
    }

    public void hideIME(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public int getDisplayWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }


    @Override
    public void onBackPressed() {
        log("#MainActivity.onBackPressed");
        super.onBackPressed();
    }

    public void showIME(EditText editText) {
        if (editText == null) {
            return;
        }
        editText.requestFocus();
        editText.setFocusableInTouchMode(true);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void initViews() {
        grpStyle = findViewById(R.id.grp_style);
        rdoMaterial = findViewById(R.id.rdo_material);
        rdoIos = findViewById(R.id.rdo_ios);
        rdoKongzue = findViewById(R.id.rdo_kongzue);
        rdoMiui = findViewById(R.id.rdo_miui);
        rdoMaterialYou = findViewById(R.id.rdo_material_you);
        grpTheme = findViewById(R.id.grp_theme);
        rdoAuto = findViewById(R.id.rdo_auto);
        rdoLight = findViewById(R.id.rdo_light);
        rdoDark = findViewById(R.id.rdo_dark);
        grpMode = findViewById(R.id.grp_mode);
        rdoModeView = findViewById(R.id.rdo_mode_view);
        rdoModeWindow = findViewById(R.id.rdo_mode_window);
        rdoModeDialogFragment = findViewById(R.id.rdo_mode_dialogFragment);
        rdoModeFloatingActivity = findViewById(R.id.rdo_mode_floatingActivity);
        btnMessageDialog = findViewById(R.id.btn_messageDialog);
        btnSelectDialog = findViewById(R.id.btn_selectDialog);
        btnInputDialog = findViewById(R.id.btn_inputDialog);
        btnSelectMessageMenu = findViewById(R.id.btn_select_menu);
        btnMutiSelectMessageMenu = findViewById(R.id.btn_multiSelect_menu);
        btnWaitDialog = findViewById(R.id.btn_waitDialog);
        btnWaitAndTipDialog = findViewById(R.id.btn_waitAndTipDialog);
        btnTipSuccess = findViewById(R.id.btn_tipSuccess);
        btnTipWarning = findViewById(R.id.btn_tipWarning);
        btnTipError = findViewById(R.id.btn_tipError);
        btnTipProgress = findViewById(R.id.btn_tipProgress);
        btnPoptip = findViewById(R.id.btn_poptip);
        btnPoptipBigMessage = findViewById(R.id.btn_poptip_bigMessage);
        btnPoptipSuccess = findViewById(R.id.btn_poptip_success);
        btnPoptipWarning = findViewById(R.id.btn_poptip_warning);
        btnPoptipError = findViewById(R.id.btn_poptip_error);
        btnPopnotification = findViewById(R.id.btn_popnotification);
        btnPopnotificationBigMessage = findViewById(R.id.btn_popnotification_bigMessage);
        btnPopnotificationOverlay = findViewById(R.id.btn_popnotification_overlay);
        btnBottomDialog = findViewById(R.id.btn_bottom_dialog);
        btnBottomMenu = findViewById(R.id.btn_bottom_menu);
        btnBottomReply = findViewById(R.id.btn_bottom_reply);
        btnBottomSelectMenu = findViewById(R.id.btn_bottom_select_menu);
        btnBottomMultiSelectMenu = findViewById(R.id.btn_bottom_multiSelect_menu);
        btnBottomCustomRecycleView = findViewById(R.id.btn_bottom_custom_recycleView);
        btnCustomMessageDialog = findViewById(R.id.btn_customMessageDialog);
        btnCustomInputDialog = findViewById(R.id.btn_customInputDialog);
        btnCustomBottomMenu = findViewById(R.id.btn_customBottomMenu);
        btnCustomDialog = findViewById(R.id.btn_customDialog);
        btnCustomDialogAlign = findViewById(R.id.btn_customDialogAlign);
        btnFullScreenDialogWebPage = findViewById(R.id.btn_fullScreenDialog_webPage);
        btnFullScreenDialogLogin = findViewById(R.id.btn_fullScreenDialog_login);
        btnFullScreenDialogFragment = findViewById(R.id.btn_fullScreenDialog_fragment);
        btnContextMenu = findViewById(R.id.btn_contextMenu);
        btnSelectMenu = findViewById(R.id.btn_selectMenu);
        btnShowGuide = findViewById(R.id.btn_showGuide);
        btnShowGuideBaseView = findViewById(R.id.btn_showGuideBaseView);
        btnShowGuideBaseViewRectangle = findViewById(R.id.btn_showGuideBaseViewRectangle);
        btnListDialog = findViewById(R.id.btn_listDialog);
    }

}
