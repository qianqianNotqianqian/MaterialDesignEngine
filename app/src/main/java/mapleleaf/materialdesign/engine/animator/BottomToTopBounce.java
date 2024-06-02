package mapleleaf.materialdesign.engine.animator;

import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Interpolator;

import org.jetbrains.annotations.NotNull;

public class BottomToTopBounce implements ItemAnimator {
    @NotNull
    @Override
    public Animator animator(@NotNull View view) {
        Animator translationY =
                ObjectAnimator.ofFloat(view, "translationY", view.getRootView().getHeight(), 0f);

        translationY.setDuration(800);
        translationY.setInterpolator(new MyInterpolator3());

        return translationY;
    }

    private static class MyInterpolator3 implements Interpolator {
        @Override
        public float getInterpolation(float input) {
            float factor = 0.7f;
            return (float) (pow(2.0, -10.0 * input) * sin((input - factor / 4) * (2 * PI) / factor) + 1);
        }
    }
}
