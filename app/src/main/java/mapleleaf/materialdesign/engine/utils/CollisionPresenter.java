package mapleleaf.materialdesign.engine.utils;

import android.view.View;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import mapleleaf.materialdesign.engine.R;

/**
 * Presenter层
 */
public class CollisionPresenter {
    private CollisionModel collisionModel;

    public CollisionPresenter() {
        collisionModel = new CollisionModel();
    }

    /**
     * 设置边界
     *
     * @param width
     * @param height
     */
    public void updateBounds(int width, int height) {
        collisionModel.updateBounds(width, height);
    }

    /**
     * 绑定刚体
     *
     * @param view
     */
    public void bindBody(View view) {
        Body body = collisionModel.createBody(view.getX(), view.getY(), view.getWidth() / 2f);
        view.setTag(R.id.view_body_tag, body);
    }

    /**
     * 设置View的坐标和旋转
     *
     * @param view
     */
    public void drawView(View view) {
        Body body = (Body) view.getTag(R.id.view_body_tag);
        //拿到坐标
        float[] pos = collisionModel.getCoordinate(body);
        if (pos != null) {
            //左上角坐标
            view.setX((int) (pos[0] - view.getWidth() / 2f));
            view.setY((int) (pos[1] - view.getWidth() / 2f));
        }

        //设置旋转
        float angle = collisionModel.getAngle(body);
        if (angle != 0f) {
            view.setRotation(angle);
        }
    }

    /**
     * 开启世界
     */
    public void startWorld() {
        collisionModel.startWorld();
    }

    /**
     * 设置重力
     *
     * @param x
     * @param y
     * @param view
     */
    public void applyLinearImpulse(float x, float y, View view) {
        Body body = (Body) view.getTag(R.id.view_body_tag);
        if (body == null) {
            return;
        }

        Vec2 impluse = new Vec2(x, y);
        body.applyLinearImpulse(impluse, body.getPosition(), true); //给body做线性运动 true 运动完之后停止
    }

    public void setDensity(float density) {
        collisionModel.setmDesity(density);
    }

}
