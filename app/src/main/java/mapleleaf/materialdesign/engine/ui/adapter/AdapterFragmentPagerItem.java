package mapleleaf.materialdesign.engine.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by sunfusheng on 2017/7/20.
 */
public class AdapterFragmentPagerItem extends FragmentPagerAdapter {

    private Context context;
    private List<RecycleViewPagerItem> items;
    private SparseArray<Fragment> fragments = new SparseArray<>();
    private OnInstantiateFragmentListener listener;

    private AdapterFragmentPagerItem(Context context, FragmentManager fragmentManager, List<RecycleViewPagerItem> items) {
        super(fragmentManager);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, fragment);
        if (listener != null) {
            listener.onInstantiate(position, fragment, items.get(position).getArgs());
        }
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        return items.get(position).newInstance(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.remove(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public String getPageTitle(int position) {
        return items.get(position).getPageTitle();
    }

    public void setOnInstantiateFragmentListener(OnInstantiateFragmentListener listener) {
        this.listener = listener;
    }

    public interface OnInstantiateFragmentListener {
        void onInstantiate(int position, Fragment fragment, Bundle args);
    }

    public static class Builder {

        private Context context;
        private FragmentManager fragmentManager;
        private List<RecycleViewPagerItem> items = new ArrayList<>();

        public Builder(Context context, FragmentManager fragmentManager) {
            this.context = context;
            this.fragmentManager = fragmentManager;
        }

        public Builder add(RecycleViewPagerItem item) {
            items.add(item);
            return this;
        }

        public Builder add(int resId, Fragment fragment) {
            return add(context.getString(resId), fragment);
        }

        public Builder add(int resId, Class<? extends Fragment> clazz) {
            return add(context.getString(resId), clazz);
        }

        public Builder add(int resId, Class<? extends Fragment> clazz, Bundle args) {
            return add(context.getString(resId), clazz, args);
        }

        public Builder add(String title, Fragment fragment) {
            return add(RecycleViewPagerItem.create(title, fragment));
        }

        public Builder add(String title, Class<? extends Fragment> clazz) {
            return add(RecycleViewPagerItem.create(title, clazz));
        }

        public Builder add(String title, Class<? extends Fragment> clazz, Bundle args) {
            return add(RecycleViewPagerItem.create(title, clazz, args));
        }

        public AdapterFragmentPagerItem build() {
            return new AdapterFragmentPagerItem(context, fragmentManager, items);
        }
    }
}
