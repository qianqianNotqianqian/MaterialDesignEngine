package mapleleaf.materialdesign.engine.ui.fragments;

import static mapleleaf.materialdesign.engine.utils.TopLevelFuncationKt.toast;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import mapleleaf.materialdesign.engine.view.SunfushengMarqueeView;
import mapleleaf.materialdesign.engine.model.SunfushengMarqueeCustomModel;

import java.util.ArrayList;
import java.util.List;

import mapleleaf.materialdesign.engine.R;

/**
 * @author by sunfusheng on 2017/8/8.
 */
public class FragmentMarqueeCommon extends Fragment {

    private SunfushengMarqueeView sunfushengMarqueeView;
    private SunfushengMarqueeView sunfushengMarqueeView1;
    private SunfushengMarqueeView sunfushengMarqueeView2;
    private SunfushengMarqueeView sunfushengMarqueeView3;
    private SunfushengMarqueeView sunfushengMarqueeView4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.marquee_fragment_tab, container, false);
        sunfushengMarqueeView = view.findViewById(R.id.marqueeView);
        sunfushengMarqueeView1 = view.findViewById(R.id.marqueeView1);
        sunfushengMarqueeView2 = view.findViewById(R.id.marqueeView2);
        sunfushengMarqueeView3 = view.findViewById(R.id.marqueeView3);
        sunfushengMarqueeView4 = view.findViewById(R.id.marqueeView4);

        List<CharSequence> list = new ArrayList<>();
        SpannableString ss1 = new SpannableString("1、MarqueeView开源项目");
        ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 2, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        list.add(ss1);
        SpannableString ss2 = new SpannableString("2、GitHub：sunfusheng");
        ss2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue)), 9, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        list.add(ss2);
        SpannableString ss3 = new SpannableString("3、个人博客：sunfusheng.com");
        ss3.setSpan(new URLSpan("http://sunfusheng.com/"), 7, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        list.add(ss3);
        list.add("4、新浪微博：@孙福生微博");
        //set Custom font
//        sunfushengMarqueeView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.huawenxinwei));

        sunfushengMarqueeView.startWithList(list);
        sunfushengMarqueeView.setOnItemClickListener((position, textView) -> Toast.makeText(getContext(), textView.getText() + "", Toast.LENGTH_SHORT).show());

        sunfushengMarqueeView1.startWithText(getString(R.string.marquee_texts), R.anim.anim_top_in, R.anim.anim_bottom_out);
        sunfushengMarqueeView1.setOnItemClickListener((position, textView) -> Toast.makeText(getContext(), String.valueOf(position) + ". " + textView.getText(), Toast.LENGTH_SHORT).show());

        sunfushengMarqueeView2.startWithText(getString(R.string.marquee_text));

        sunfushengMarqueeView3.startWithText(getString(R.string.marquee_texts));
        sunfushengMarqueeView3.setOnItemClickListener((position, textView) -> {
            CharSequence model = (CharSequence) sunfushengMarqueeView3.getMessages().get(position);
            Toast.makeText(getContext(), model, Toast.LENGTH_SHORT).show();
        });

        List<SunfushengMarqueeCustomModel> models = new ArrayList<>();
        models.add(new SunfushengMarqueeCustomModel(10000, "增加了新功能：", "设置自定义的Model数据类型"));
        models.add(new SunfushengMarqueeCustomModel(10001, "GitHub：sunfusheng", "新浪微博：@孙福生微博"));
        models.add(new SunfushengMarqueeCustomModel(10002, "MarqueeView开源项目", "个人博客：sunfusheng.com"));
        sunfushengMarqueeView4.startWithList(models);
        sunfushengMarqueeView4.setOnItemClickListener((position, textView) -> {
            SunfushengMarqueeCustomModel model = (SunfushengMarqueeCustomModel) sunfushengMarqueeView4.getMessages().get(position);
            toast("ID:" + model.id);
        });

        return view;
    }
}
