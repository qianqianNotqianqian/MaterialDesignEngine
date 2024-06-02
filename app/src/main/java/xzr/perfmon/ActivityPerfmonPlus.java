package xzr.perfmon;

import static mapleleaf.materialdesign.engine.utils.TopLevelFuncationKt.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Objects;

import mapleleaf.materialdesign.engine.R;

public class ActivityPerfmonPlus extends Activity {
    ScrollView mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = new ScrollView(this);
        setContentView(mainView);

        if (!FloatingWindow.doExit) {
            toast(getResources().getString(R.string.please_close_app_first));
            finish();
            return;
        }

        //Move from service:onCreate in order to show the supporting list
        RefreshingDateThread.cpunum = JniTools.getCpuNum();
        FloatingWindow.linen = Support.CheckSupport();
        SharedPreferencesUtil.init(this);

        if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SKIP_FIRST_SCREEN, SharedPreferencesUtil.DEFAULT_SKIP_FIRST_SCREEN)) {
            permissionCheck();
            finish();
            return;
        }

        addView();

    }

    @SuppressLint("SetTextI18n")
    void addView() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        mainView.addView(main);
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_cpufreq_mo) + Tools.bool2Text(Support.support_cpufreq, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_cpuload_mo) + Tools.bool2Text(Support.support_cpuload, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_gpufreq_mo) + Tools.bool2Text(Support.support_adrenofreq, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_gpuload_mo) + Tools.bool2Text(Support.support_adrenofreq, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_cpubw_mo) + Tools.bool2Text(Support.support_cpubw, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_gpubw_mo) + Tools.bool2Text(Support.support_gpubw, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_llcbw_mo) + Tools.bool2Text(Support.support_llcbw, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_m4mfreq_mo) + Tools.bool2Text(Support.support_m4m, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_thermal_mo) + Tools.bool2Text(Support.support_temp, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_mem_mo) + Tools.bool2Text(Support.support_mem, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_current_mo) + Tools.bool2Text(Support.support_current, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.support_fps_mo) + Tools.bool2Text(Support.support_fps, this));
            main.addView(textView);
        }
        {
            TextView textView = new TextView(this);
            textView.setText(R.string.not_supported_reason);
            main.addView(textView);
        }
        {
            Button button = new Button(this);
            button.setText(R.string.show_floating_window);
            main.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    permissionCheck();
                    finish();
                }
            });
        }
        {
            Button button = new Button(this);
            button.setText(R.string.perfmon_plus_settings);
            main.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PerfmonPlusSettings.createDialog(ActivityPerfmonPlus.this);
                }
            });
        }
        {
            Button button = new Button(this);
            button.setText(R.string.permissive_selinux);
            main.addView(button);
            button.setOnClickListener(view -> {
                try {
                    Process process = new ProcessBuilder("su").redirectErrorStream(true).start();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
                    outputStreamWriter.write("setenforce 0\nexit\n");
                    outputStreamWriter.flush();
                    StringBuilder log = new StringBuilder();
                    String cache;
                    while ((cache = bufferedReader.readLine()) != null) {
                        log.append(cache).append("\n");
                    }
                    if (log.toString().equals("")) {
                        toast(getString(R.string.permissive_done));
                    } else {
                        toast(log.toString());
                    }
                    finish();
                } catch (Exception e) {
                    toast(getString(R.string.permission_denied));
                }
            });
            button.setOnLongClickListener(view -> {
                try {
                    Process process = new ProcessBuilder("su").redirectErrorStream(true).start();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
                    outputStreamWriter.write("setenforce 1\nexit\n");
                    outputStreamWriter.flush();
                    StringBuilder log = new StringBuilder();
                    String cache;
                    while ((cache = bufferedReader.readLine()) != null) {
                        log.append(cache).append("\n");
                    }
                    if (log.toString().equals("")) {
                        toast(getString(R.string.enforce_done));
                    } else {
                        toast(log.toString());
                    }
                    finish();
                } catch (Exception e) {
                    toast(getString(R.string.permission_denied));
                }
                return true;
            });
        }
        {
            TextView textView = new TextView(this);
            textView.setText(R.string.permissive_selinux_description);
            main.addView(textView);
        }
        {
            LinearLayout line = new LinearLayout(ActivityPerfmonPlus.this);
            main.addView(line);
            {
                TextView textView = new TextView(this);
                textView.setText(R.string.visit_github);
                line.addView(textView);
                textView.setOnClickListener(view -> {
                    Uri uri = Uri.parse("https://github.com/libxzr/PerfMon-Plus");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
            }

            {
                TextView textView = new TextView(this);
                textView.setText(R.string.visit_coolapk);
                line.addView(textView);
                textView.setOnClickListener(view -> {
                    Uri uri = Uri.parse("https://www.coolapk.com/apk/xzr.perfmon");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
            }
        }
    }

    void permissionCheck() {
        if (Settings.canDrawOverlays(ActivityPerfmonPlus.this)) {
            Intent intent = new Intent(ActivityPerfmonPlus.this, FloatingWindow.class);
            startService(intent);
        } else {
            try {
                Class<Settings> clazz = Settings.class;
                Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
                Intent intent = new Intent(Objects.requireNonNull(field.get(null)).toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                this.startActivity(intent);
            } catch (Exception e) {
                //
            }
        }
    }
}
