package mapleleaf.materialdesign.engine.model;

import mapleleaf.materialdesign.engine.interfaces.IMarqueeItem;

/**
 * @author by sunfusheng on 2019-04-25
 */
public class SunfushengMarqueeCustomModel implements IMarqueeItem {

    public int id;
    public String title;
    public String content;

    public SunfushengMarqueeCustomModel(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    @Override
    public CharSequence marqueeMessage() {
        return title + "\n" + content;
    }
}
