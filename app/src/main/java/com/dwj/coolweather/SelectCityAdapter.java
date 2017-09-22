package com.dwj.coolweather;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dwj.coolweather.bean.SelectCityItem;

import java.util.List;

import static com.dwj.coolweather.SelectCityActivity.REQUEST_CODE;

/**
 * Created by duWenJun on 17-9-9.
 */

public class SelectCityAdapter extends RecyclerView.Adapter<SelectCityAdapter.ViewHolder> {

    private static final String TAG = "SelectCityAdapter";
    private List<SelectCityItem> mLists;
    private Context mContext;

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mShow_time;
        private final TextView mShow_city;
        private final TextView mShow_tem;
        private final ImageView mSelect;
        private final View view;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            mShow_time = ((TextView) view.findViewById(R.id.show_time));
            mShow_city = ((TextView) view.findViewById(R.id.show_city));
            mShow_tem = ((TextView) view.findViewById(R.id.show_tem));
            mSelect = ((ImageView) view.findViewById(R.id.add));
        }
    }

    public SelectCityAdapter(List<SelectCityItem> lists) {
        this.mLists = lists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.city_list_item, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        SelectCityItem selectCityItem = mLists.get(position);
        if (selectCityItem.isLast()) {
            holder.mSelect.setVisibility(View.VISIBLE);
            holder.mShow_tem.setVisibility(View.GONE);
            holder.mShow_city.setVisibility(View.GONE);
            holder.mShow_time.setVisibility(View.GONE);
            //如果是最后一个 添加按钮的增加点击事件
            holder.mSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((SelectCityActivity) mContext).startActivityForResult(new Intent(((SelectCityActivity) mContext), SearchCityActivity.class), REQUEST_CODE);
                }
            });
        } else {
            //如果不是最后一个 添加item的点击事件
            holder.mShow_city.setText(selectCityItem.getCityName());
            holder.mShow_time.setText(selectCityItem.getTime());
            holder.mShow_tem.setText(selectCityItem.getTem());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //不用更新Adapter的列表数据 通过动态的获取holder的对应的adapter位置
                    //拿到变化之后的数据表对应的数据,获得正确的点击项的名称
                    Toast.makeText(mContext, " item position is " + mLists.get(holder.getAdapterPosition()).getCityName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

}
