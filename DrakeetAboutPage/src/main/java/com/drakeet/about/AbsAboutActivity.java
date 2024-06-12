package com.drakeet.about;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.drakeet.multitype.MultiTypeAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsAboutActivity extends AppCompatActivity {

  private Toolbar toolbar;
  private CollapsingToolbarLayout collapsingToolbar;
  private LinearLayout headerContentLayout;

  private List<Object> items;
  private MultiTypeAdapter adapter;
  private Context context;
  private TextView slogan, version;
  private RecyclerView recyclerView;
  private @Nullable ImageLoader imageLoader;
  private boolean initialized;
  private @Nullable OnRecommendationClickedListener onRecommendationClickedListener;
  private @Nullable OnContributorClickedListener onContributorClickedListener;

  protected abstract void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version, @NonNull CollapsingToolbarLayout collapsingToolbar );
  protected abstract void onItemsCreated(@NonNull List<Object> items);

  public void setImageLoader(@NonNull ImageLoader imageLoader) {
    this.imageLoader = imageLoader;
    if (initialized) {
      adapter.notifyDataSetChanged();
    }
  }

  public @Nullable ImageLoader getImageLoader() {
    return imageLoader;
  }

  @LayoutRes
  protected int layoutRes() {
    return R.layout.about_page_main_activity;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(layoutRes());
    toolbar = findViewById(R.id.toolbar);
    ImageView icon = findViewById(R.id.icon);
    slogan = findViewById(R.id.slogan);
    version = findViewById(R.id.version);
    collapsingToolbar = findViewById(R.id.collapsing_toolbar);
    headerContentLayout = findViewById(R.id.header_content_layout);
    onCreateHeader(icon, slogan, version, collapsingToolbar);
    setSupportActionBar(toolbar);
    extractToolbarColor(this, toolbar);
    context = this;

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }
    onApplyPresetAttrs();
    recyclerView = findViewById(R.id.recycleView);
      applyEdgeToEdge();
  }

  public static void extractToolbarColor(@NonNull AppCompatActivity activity, @NonNull Toolbar toolbar) {
    int toolbarColor = extractToolbarColorFromDrawable(toolbar);
    if (toolbarColor == Color.TRANSPARENT) {
      toolbarColor = extractToolbarColorFromMaterialShapeDrawable(toolbar);
    }
    applyStatusBarColor(activity, toolbarColor);
  }

  private static int extractToolbarColorFromDrawable(@NonNull Toolbar toolbar) {
    int toolbarColor = Color.TRANSPARENT;
    if (toolbar.getBackground() instanceof ColorDrawable) {
      toolbarColor = ((ColorDrawable) toolbar.getBackground()).getColor();
    }
    return toolbarColor;
  }

  private static int extractToolbarColorFromMaterialShapeDrawable(@NonNull Toolbar toolbar) {
    int toolbarColor = Color.TRANSPARENT;
    if (toolbar instanceof MaterialToolbar) {
      MaterialShapeDrawable materialShapeDrawable = (MaterialShapeDrawable) toolbar.getBackground();
      if (materialShapeDrawable != null) {
        toolbarColor = materialShapeDrawable.getFillColor().getDefaultColor();
      }
    }
    return toolbarColor;
  }

  private static void applyStatusBarColor(@NonNull AppCompatActivity activity, int color) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = activity.getWindow();
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(color);

      // 根据状态栏背景颜色选择文字颜色
      int textColor = isColorDark(color) ? Color.BLACK : Color.WHITE;
      View decorView = window.getDecorView();
      int flags = decorView.getSystemUiVisibility();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // 清除浅色状态栏标志位
      }
      flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // 设置深色状态栏标志位
      decorView.setSystemUiVisibility(flags);
    }
  }

  private static boolean isColorDark(int color) {
    double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
    return darkness >= 0.5;
  }
  private boolean givenInsetsToDecorView = false;

  private void applyEdgeToEdge() {
    Window window = getWindow();
    int navigationBarColor = ContextCompat.getColor(this, R.color.about_page_navigationBarColor);
    window.setNavigationBarColor(navigationBarColor);

    final AppBarLayout appBarLayout = findViewById(R.id.header_layout);
    final View decorView = window.getDecorView();
    final int originalRecyclerViewPaddingBottom =recyclerView.getPaddingBottom();

    givenInsetsToDecorView = false;
    WindowCompat.setDecorFitsSystemWindows(window, false);
    ViewCompat.setOnApplyWindowInsetsListener(decorView, new OnApplyWindowInsetsListener() {
      @NonNull
      @Override
      public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat windowInsets) {
        Insets navigationBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
        boolean isGestureNavigation = navigationBarsInsets.bottom <= 24 * getResources().getDisplayMetrics().density;

        if (!isGestureNavigation) {
          ViewCompat.onApplyWindowInsets(decorView, windowInsets);
          givenInsetsToDecorView = true;
        } else if (givenInsetsToDecorView) {
          ViewCompat.onApplyWindowInsets(
              decorView,
              new WindowInsetsCompat.Builder()
                  .setInsets(
                      WindowInsetsCompat.Type.navigationBars(),
                      Insets.of(navigationBarsInsets.left, navigationBarsInsets.top, navigationBarsInsets.right, 0)
                  )
                  .build()
          );
        }
        decorView.setPadding(windowInsets.getSystemWindowInsetLeft(), decorView.getPaddingTop(), windowInsets.getSystemWindowInsetRight(), decorView.getPaddingBottom());
        appBarLayout.setPadding(appBarLayout.getPaddingLeft(), windowInsets.getSystemWindowInsetTop(), appBarLayout.getPaddingRight(), appBarLayout.getPaddingBottom());
        recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), originalRecyclerViewPaddingBottom + navigationBarsInsets.bottom);
        return windowInsets;
      }
    });
  }

  @Override @SuppressWarnings("deprecation")
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    adapter = new MultiTypeAdapter();
    adapter.register(Category.class, new CategoryViewBinder());
    adapter.register(Card.class, new CardViewBinder());
    adapter.register(Line.class, new LineViewBinder());
    adapter.register(Contributor.class, new ContributorViewBinder(this));
    adapter.register(License.class, new LicenseViewBinder());
    adapter.register(Recommendation.class, new RecommendationViewBinder(this));
    items = new ArrayList<>();
    onItemsCreated(items);
    adapter.setItems(items);
    adapter.setHasStableIds(true);
    recyclerView.addItemDecoration(new DividerItemDecoration(adapter));
    recyclerView.setAdapter(adapter);
    initialized = true;
  }

  private void onApplyPresetAttrs() {
    final TypedArray a = obtainStyledAttributes(R.styleable.AbsAboutActivity);
    Drawable headerBackground = a.getDrawable(R.styleable.AbsAboutActivity_aboutPageHeaderBackground);
    if (headerBackground != null) {
      setHeaderBackground(headerBackground);
    }
    Drawable headerContentScrim = a.getDrawable(R.styleable.AbsAboutActivity_aboutPageHeaderContentScrim);
    if (headerContentScrim != null) {
      setHeaderContentScrim(headerContentScrim);
    }
    @ColorInt
    int headerTextColor = a.getColor(R.styleable.AbsAboutActivity_aboutPageHeaderTextColor, -1);
    if (headerTextColor != -1) {
      setHeaderTextColor(headerTextColor);
    }
    Drawable navigationIcon = a.getDrawable(R.styleable.AbsAboutActivity_aboutPageNavigationIcon);
    if (navigationIcon != null) {
      setNavigationIcon(navigationIcon);
    }
    a.recycle();
  }

  /**
   * Use {@link #setHeaderBackground(int)} instead.
   *
   * @param resId The resource id of header background
   */
  @Deprecated
  public void setHeaderBackgroundResource(@DrawableRes int resId) {
    setHeaderBackground(resId);
  }

  public void setHeaderBackground(@DrawableRes int resId) {
    setHeaderBackground(ContextCompat.getDrawable(this, resId));
  }

  public void setHeaderBackground(@NonNull Drawable drawable) {
    ViewCompat.setBackground(headerContentLayout, drawable);
  }

  /**
   * Set the drawable to use for the content scrim from resources. Providing null will disable
   * the scrim functionality.
   *
   * @param drawable the drawable to display
   */
  public void setHeaderContentScrim(@NonNull Drawable drawable) {
    collapsingToolbar.setContentScrim(drawable);
  }

  public void setHeaderContentScrim(@DrawableRes int resId) {
    setHeaderContentScrim(ContextCompat.getDrawable(this, resId));
  }

  public void setHeaderTextColor(@ColorInt int color) {
    collapsingToolbar.setCollapsedTitleTextColor(color);
    slogan.setTextColor(color);
    version.setTextColor(color);
  }

  /**
   * Set the icon to use for the toolbar's navigation button.
   *
   * @param resId Resource ID of a drawable to set
   */
  public void setNavigationIcon(@DrawableRes int resId) {
    toolbar.setNavigationIcon(resId);
  }

  public void setNavigationIcon(@NonNull Drawable drawable) {
    toolbar.setNavigationIcon(drawable);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    if (menuItem.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(menuItem);
  }

  @Override
  public void setTitle(@NonNull CharSequence title) {
    collapsingToolbar.setTitle(title);
  }

  public Toolbar getToolbar() {
    return toolbar;
  }

  public CollapsingToolbarLayout getCollapsingToolbar() {
    return collapsingToolbar;
  }

  public List<Object> getItems() {
    return items;
  }

  public MultiTypeAdapter getAdapter() {
    return adapter;
  }

  public TextView getSloganTextView() {
    return slogan;
  }

  public TextView getVersionTextView() {
    return version;
  }

  public void setOnRecommendationClickedListener(@Nullable OnRecommendationClickedListener listener) {
    this.onRecommendationClickedListener = listener;
  }

  public @Nullable OnRecommendationClickedListener getOnRecommendationClickedListener() {
    return onRecommendationClickedListener;
  }

  public void setOnContributorClickedListener(@Nullable OnContributorClickedListener listener) {
    this.onContributorClickedListener = listener;
  }

  public @Nullable OnContributorClickedListener getOnContributorClickedListener() {
    return onContributorClickedListener;
  }
}
