package com.snap.intelligence.musics.flow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snap.intelligence.musics.R;

import java.util.List;

public class FlowAdapter extends RecyclerView.Adapter<FlowAdapter.ViewHolder> {

    public List<FlowItem> data;
    private OnItemClickListener listener;

    public FlowAdapter(List<FlowItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FlowItem item = data.get(position);
        holder.tvText.setText(item.getText());
        
        // 整个Item点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
        
        // 删除按钮点击事件
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        ImageView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_text);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FlowItem item);
        void onDeleteClick(int position);
    }
}
