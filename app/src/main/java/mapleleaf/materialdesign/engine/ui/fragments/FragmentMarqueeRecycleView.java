package mapleleaf.materialdesign.engine.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import mapleleaf.materialdesign.engine.R;
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase;
import mapleleaf.materialdesign.engine.view.SunfushengMarqueeView;

/**
 * @author sunfusheng on 2018/2/6.
 */
public class FragmentMarqueeRecycleView extends UniversalFragmentBase {

    public FragmentMarqueeRecycleView() {
        super(R.layout.marquee_layout_recyclerview);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Item item = new Item();
            item.title = "item" + i;
            if (i == 3 || i == 27) {
                List<String> list = new ArrayList<>();
                for (int j = 1; j <= 10; j++) {
                    list.add("MarqueeView Item " + i + "-" + j);
                }
                item.list = list;
                item.showList = true;
            }
            items.add(item);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public static class Item {
        public String title;
        public List<String> list;
        public boolean showList;
    }

    static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private final List<Item> list;

        RecyclerViewAdapter(List<Item> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.marquee_item_layout, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            Item item = list.get(position);

            viewHolder.tvTitle.setText(item.title);
            viewHolder.tvTitle.setVisibility(item.showList ? View.GONE : View.VISIBLE);
            viewHolder.sunfushengMarqueeView.setVisibility(item.showList ? View.VISIBLE : View.GONE);
            if (item.showList) {
                viewHolder.sunfushengMarqueeView.startWithList(item.list);
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            if (holder.sunfushengMarqueeView != null) {
                holder.sunfushengMarqueeView.stopFlipping();
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            SunfushengMarqueeView sunfushengMarqueeView;

            ViewHolder(View view) {
                super(view);
                tvTitle = view.findViewById(R.id.tv_title);
                sunfushengMarqueeView = view.findViewById(R.id.marqueeView);
            }
        }
    }
}
