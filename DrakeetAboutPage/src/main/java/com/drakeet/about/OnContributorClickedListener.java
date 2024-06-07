package com.drakeet.about;

import android.view.View;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

public interface OnContributorClickedListener {

  @CheckResult
  boolean onContributorClicked(@NonNull View itemView, @NonNull Contributor contributor);
}
