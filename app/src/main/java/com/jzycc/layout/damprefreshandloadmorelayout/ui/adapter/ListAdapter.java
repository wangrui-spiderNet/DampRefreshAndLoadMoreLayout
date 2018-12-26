package com.jzycc.layout.damprefreshandloadmorelayout.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jzycc.layout.damprefreshandloadmorelayout.R;
import com.jzycc.layout.damprefreshandloadmorelayout.model.ZhiHuDto;

import java.util.List;

/**
 * author Jzy(Xiaohuntun)
 * date 18-8-28
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{

    private Context mContext;
    private List<ZhiHuDto> mList;

    public ListAdapter(Context mContext, List<ZhiHuDto> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list,viewGroup,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.tvItem.setText(mList.get(i).getTitle());
        Glide.with(mContext)
                .load(mList.get(i).getImageUrl())
                .into(viewHolder.ivItem);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivItem;
        private TextView tvItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItem = (ImageView)itemView.findViewById(R.id.iv_image);
            tvItem = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }

}
