package com.drakeet.about.provided;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.drakeet.about.ImageLoader;
import com.squareup.picasso.Picasso;

/**
 * @author drakeet
 */
public class PicassoImageLoader implements ImageLoader {

  @Override
  public void load(@NonNull ImageView imageView, @NonNull String url) {
    Picasso.get().load(url).into(imageView);
  }
}
