package com.dwj.coolweather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by duWenJun on 17-9-6.
 *
 */

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyHolder> {

    private List<HourlyForecastItem> list;
    private Context mContext;

    public WeatherAdapter(List<HourlyForecastItem> list) {
        this.list = list;
    }

     class MyHolder extends RecyclerView.ViewHolder {

        TextView time;
        ImageView icon;
        TextView tem;

         MyHolder(View view) {
            super(view);
            time = ((TextView) view.findViewById(R.id.time));
            icon = (ImageView) view.findViewById(R.id.weather_icon);
            tem = (TextView) view.findViewById(R.id.tem);
        }

    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.recycleview_item, parent, false);
        return new MyHolder(inflate);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        HourlyForecastItem item = list.get(position);
        holder.time.setText(item.getHour());
        holder.icon.setImageResource(item.getIconId());
        holder.tem.setText(item.getTem());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
