package mapleleaf.materialdesign.engine.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.textview.marqueen.ComplexItemEntity;

import mapleleaf.materialdesign.engine.view.XUIMarqueeFactory;

/**
 * 复合字幕
 *
 * @author xuexiang
 * @since 2019/1/14 下午10:13
 */
public class ComplexViewMF extends XUIMarqueeFactory<RelativeLayout, ComplexItemEntity> {
    private LayoutInflater inflater;

    public ComplexViewMF(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RelativeLayout generateMarqueeItemView(ComplexItemEntity data) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.marqueen_layout_complex_view, null);
        ((TextView) view.findViewById(R.id.title)).setText(data.getTitle());
        ((TextView) view.findViewById(R.id.subTitle)).setText(data.getSubTitle());
        ((TextView) view.findViewById(R.id.time)).setText(data.getTime());
        return view;
    }
}