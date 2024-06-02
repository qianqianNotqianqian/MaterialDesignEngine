package mapleleaf.materialdesign.engine.utils;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.Random;

/**
 * 小球运动坐标的model层
 */
public class CollisionModel {
    private final Random mRandom = new Random();
    /**
     * 世界，即容器，没有边界的
     */
    private World mWorld;
    private float dt = 1f / 60f; //模拟世界的频率
    private int mVelocityIterations = 5; //速率迭代器
    private int mPosiontIterations = 20; //迭代次数
    /**
     * 边界宽高
     */
    private int mWidth, mHeight;
    /**
     * 刚体的密度
     */
    private float mDesity = 0.5f;
    /**
     * 刚体的摩擦系数
     */
    private float mFriction = 0.8f;
    /**
     * 刚体的补偿系数
     */
    private float mRestitution = 0.5f;
    private float mRatio = 50;//坐标映射比例

    /**
     * 创建世界
     */
    public void createNewWorld() {
        //竖直向下的重力向量
        mWorld = new World(new Vec2(0f, 10.0f));
    }

    /**
     * 开启世界
     */
    public void startWorld() {
        if (mWorld != null) {
            mWorld.step(dt, mVelocityIterations, mPosiontIterations);
        }
    }

    /**
     * 由于世界时没有边界的，我们又要在边界有碰撞效果，所以使用刚体设置边界
     *
     * @param width
     * @param height
     */
    public void updateBounds(int width, int height) {
        createNewWorld();
        mWidth = width;
        mHeight = height;

        //创建静态刚体
        BodyDef bodyDef = new BodyDef();
        bodyDef.setType(BodyType.STATIC);//表示这个刚体是静态的

        //左侧和右侧的
        //定义的形状，PolygonShape为多边形，所以可表示矩形
        PolygonShape shape = new PolygonShape();
        //确定为矩形,左侧和右侧刚体的高度为最大边界高度，宽度为1
        float boxWidth = switchPositionToBody(mRatio);
        float boxHeight = switchPositionToBody(mHeight);
        shape.setAsBox(boxWidth, boxHeight);

        //描述
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = mDesity;
        fixtureDef.friction = mFriction;//摩擦系数
        fixtureDef.restitution = mRestitution; //补偿系数

        //确定左侧刚体的位置
        bodyDef.position.set(-boxWidth, 0);//左侧为-1，0
        //通过世界创建刚体
        Body body = mWorld.createBody(bodyDef);
        //赋予刚体属性
        body.createFixture(fixtureDef);

        //确定右侧刚体的位置
        bodyDef.position.set(switchPositionToBody(mWidth) + boxWidth, 0);//右侧为mWidth+1，0
        //通过世界创建刚体并赋予属性
        mWorld.createBody(bodyDef).createFixture(fixtureDef);

        //上侧和下侧的刚体
        //左侧和右侧刚体的高度为1，宽度为最大宽度
        boxWidth = switchPositionToBody(mWidth);
        boxHeight = switchPositionToBody(mRatio);
        shape.setAsBox(boxWidth, boxHeight);
        //重新赋值下形状
        fixtureDef.shape = shape;

        //确定上侧刚体的位置
        bodyDef.position.set(0, -boxHeight);//上侧为0，-1
        //通过世界创建刚体并赋予属性
        mWorld.createBody(bodyDef).createFixture(fixtureDef);

        //确定下侧刚体的位置
        bodyDef.position.set(0, switchPositionToBody(mHeight) + boxHeight);//下侧为0，mHeight+1
        //通过世界创建刚体并赋予属性
        mWorld.createBody(bodyDef).createFixture(fixtureDef);
    }

    /**
     * 根据坐标创建刚体
     *
     * @return
     */
    public Body createBody(float x, float y, float radius) {
        //创建动态刚体
        BodyDef bodyDef = new BodyDef();
        bodyDef.setType(BodyType.DYNAMIC);//表示这个刚体是动态的

        //定义的形状，CircleShape为圆形
        Shape shape = new CircleShape();
        //设置半径
        shape.setRadius(switchPositionToBody(radius));

        //描述
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.setShape(shape);
        fixtureDef.density = mDesity;
        fixtureDef.friction = mFriction;//摩擦系数
        fixtureDef.restitution = mRestitution; //补偿系数

        //确定刚体的位置
        bodyDef.position.set(switchPositionToBody(x + radius), switchPositionToBody(y + radius));
        //通过世界创建刚体
        Body body = mWorld.createBody(bodyDef);
        //赋予刚体属性
        body.createFixture(fixtureDef);

        body.setLinearVelocity(new Vec2(mRandom.nextFloat(), mRandom.nextFloat()));
        return body;
    }

    /**
     * 获取坐标
     *
     * @param body
     * @return
     */
    public float[] getCoordinate(Body body) {
        if (body != null) {
            return new float[]{switchPositionToView(body.getPosition().x), switchPositionToView(body.getPosition().y)};
        }

        return null;
    }

    /**
     * 获取弧度
     *
     * @param body
     * @return
     */
    public float getAngle(Body body) {
        if (body != null) {
            return (float) ((body.getAngle() / Math.PI * 180) % 360);
        }

        return 0;
    }

    //view坐标映射为物理的坐标
    private float switchPositionToBody(float viewPosition) {
        return viewPosition / mRatio;
    }

    //物理的坐标映射为iew坐标映射
    private float switchPositionToView(float bodyPosition) {
        return bodyPosition * mRatio;
    }

    public float getmDesity() {
        return mDesity;
    }

    public void setmDesity(float mDesity) {
        this.mDesity = mDesity;
    }

    public float getmFriction() {
        return mFriction;
    }

    public void setmFriction(float mFriction) {
        this.mFriction = mFriction;
    }

    public float getmRestitution() {
        return mRestitution;
    }

    public void setmRestitution(float mRestitution) {
        this.mRestitution = mRestitution;
    }
}
