package com.dwj.coolweather;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dwj.coolweather.bean.SelectCityItem;

import java.util.List;

import static com.dwj.coolweather.SelectCityActivity.REQUEST_CODE;

/**
 * Created by duWenJun on 17-9-9.
 */

public class SelectCityAdapter extends RecyclerView.Adapter<SelectCityAdapter.ViewHolder>{

    private static final String TAG = "SelectCityAdapter";
    private List<SelectCityItem> lists;
    private Context mContext;
    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mShow_time;
        private final TextView mShow_city;
        private final TextView mShow_tem;
        private final ImageView mSelect;
        ViewHolder(View view) {
            super(view);
            mShow_time = ((TextView) view.findViewById(R.id.show_time));
            mShow_city = ((TextView) view.findViewById(R.id.show_city));
            mShow_tem = ((TextView) view.findViewById(R.id.show_tem));
            mSelect = ((ImageView) view.findViewById(R.id.add));
            mSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((SelectCityActivity) mContext).startActivityForResult(new Intent(((SelectCityActivity) mContext), SearchCityActivity.class), REQUEST_CODE);
                }
            });
        }
    }

    public SelectCityAdapter(List<SelectCityItem> lists) {
        this.lists = lists;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        SelectCityItem selectCityItem = lists.get(position);
        if (selectCityItem.isLast()) {
            holder.mSelect.setVisibility(View.VISIBLE);
            holder.mShow_tem.setVisibility(View.GONE);
            holder.mShow_city.setVisibility(View.GONE);
            holder.mShow_time.setVisibility(View.GONE);
        } else {
            holder.mShow_city.setText(selectCityItem.getCityName());
            holder.mShow_time.setText(selectCityItem.getTime());
            holder.mShow_tem.setText(selectCityItem.getTem());
        }
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

}
