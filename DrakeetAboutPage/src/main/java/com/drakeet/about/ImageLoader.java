package com.drakeet.about;

import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * @author drakeet
 */
public interface ImageLoader {

  void load(@NonNull ImageView imageView, @NonNull String url);
}
