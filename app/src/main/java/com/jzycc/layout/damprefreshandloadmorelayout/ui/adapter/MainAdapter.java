package com.jzycc.layout.damprefreshandloadmorelayout.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jzycc.layout.damprefreshandloadmorelayout.R;

import java.util.List;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-17
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<String> mList;
    private Context context;
    public static interface OnClickItemListener{
        void click(int position);
    }

    private OnClickItemListener onClickItemListener = null;

    public void setOnClickItemListener(OnClickItemListener onClickItemListener){
        this.onClickItemListener = onClickItemListener;
    }

    public MainAdapter(List<String> mList, Context context) {
        this.mList = mList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main,parent,false);
        MainAdapter.ViewHolder holder = new MainAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvItem.setText(mList.get(position));
        holder.cvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItemListener.click(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cvItem;
        private TextView tvItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvItem = (CardView) itemView.findViewById(R.id.cv_item);
            tvItem = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }
}

