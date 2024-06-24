package mapleleaf.materialdesign.engine.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import mapleleaf.materialdesign.engine.R;
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2021/8/17 13:50
 */
public class FragmentCustomView extends UniversalFragmentBase {

    private static int index = 0;
    private View.OnClickListener addButtonClickListener;

    public FragmentCustomView() {
        super(R.layout.fragment_custom);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        TextView txtInfo = rootView.findViewById(R.id.txt_info);
        MaterialButton btnAddDialog = rootView.findViewById(R.id.btn_addDialog);
        txtInfo.setText("这是第：" + (index++) + " 个 Fragment");
        btnAddDialog.setOnClickListener(addButtonClickListener);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public View.OnClickListener getAddButtonClickListener() {
        return addButtonClickListener;
    }

    public FragmentCustomView setAddButtonClickListener(View.OnClickListener addButtonClickListener) {
        this.addButtonClickListener = addButtonClickListener;
        return this;
    }
}
