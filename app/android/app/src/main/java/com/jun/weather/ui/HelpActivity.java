package com.jun.weather.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jun.weather.R;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    @Override
    protected void onResume() {
        super.onResume();

        findViewById(R.id.btn_back).setOnClickListener((v) -> onBackPressed());

        CustomRecyclerAdapter customRecyclerAdapter = new CustomRecyclerAdapter();
        Data data1 = new Data();
        data1.title = "앱 사용법";
        Data data2 = new Data();
        data2.title = "라이센스";
        data2.content = getString(R.string.data_author)+"\n"+getString(R.string.icon_author);
        customRecyclerAdapter.dataList.add(data1);
        customRecyclerAdapter.dataList.add(data2);

        RecyclerView recyclerView = findViewById(R.id.list_help);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.help_item_divider, null));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(customRecyclerAdapter);
    }

    private static class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {
        public List<Data> dataList = new ArrayList<>();
        private Context context;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.component_help_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if(position == 0) {
                holder.layout_item_2.setVisibility(View.GONE);
                ImageView image1 = holder.layout_item_1.findViewById(R.id.item_image1);
                ImageView image2 = holder.layout_item_1.findViewById(R.id.item_image2);
                //Glide.with(this).load(nowWeatherModel.nowSkyDrawableId).into((ImageView) mActivity.findViewById(R.id.image_sky));

                Glide.with(context).load(
                        BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.help_image1))
                ).into(image1);

                Glide.with(context).load(
                        BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.help_image2))
                ).into(image2);

            } else if(position == 1) {
                holder.layout_item_1.setVisibility(View.GONE);
            }
            holder.onBind(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView title_help;
            private ImageButton btn_more;
            private TextView text_content;
            private ConstraintLayout layout_item_1, layout_item_2, layout_content;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title_help = itemView.findViewById(R.id.title_help);
                btn_more = itemView.findViewById(R.id.btn_more);
                text_content = itemView.findViewById(R.id.text_content);
                layout_content = itemView.findViewById(R.id.layout_content);
                layout_item_1 = itemView.findViewById(R.id.layout_item_1);
                layout_item_2 = itemView.findViewById(R.id.layout_item_2);

                layout_content.setVisibility(View.GONE);
                btn_more.setOnClickListener((v) -> {
                    if(layout_content.getVisibility() == View.GONE) {
                        btn_more.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up);
                        layout_content.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        layout_content.setVisibility(View.VISIBLE);
                                    }
                                });
                    } else {
                        btn_more.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down);
                        layout_content.setVisibility(View.GONE);
                        layout_content.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .setListener(null);
                    }
                });
            }

            public void onBind(Data data) {
                title_help.setText(data.title);
                if(data.content != null) {
                    text_content.setText(data.content);
                }
            }
        }
    }

    private static class Data {
        String title, content;
    }
}
