package com.drakeet.about;

import android.view.View;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

/**
 * @author drakeet
 */
public interface OnRecommendationClickedListener {

  @CheckResult
  boolean onRecommendationClicked(@NonNull View itemView, @NonNull Recommendation recommendation);
}
