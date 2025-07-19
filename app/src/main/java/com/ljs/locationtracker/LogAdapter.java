package com.ljs.locationtracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private List<LogEntry> logEntries = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static class LogEntry {
        public String message;
        public long timestamp;
        public String type; // "INFO", "ERROR", "SUCCESS"

        public LogEntry(String message, String type) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
            this.type = type;
        }
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogMessage;
        TextView tvLogTime;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogMessage = itemView.findViewById(R.id.tv_log_message);
            tvLogTime = itemView.findViewById(R.id.tv_log_time);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = logEntries.get(position);
        holder.tvLogMessage.setText(entry.message);
        holder.tvLogTime.setText(dateFormat.format(new Date(entry.timestamp)));
        
        // 根据日志类型设置不同的颜色，适配深色背景
        switch (entry.type) {
            case "ERROR":
                holder.tvLogMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
                break;
            case "SUCCESS":
                holder.tvLogMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green));
                break;
            default:
                holder.tvLogMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.log_text));
                break;
        }
        
        // 时间戳也使用浅色
        holder.tvLogTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.log_text));
        
        // 添加点击反馈
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加点击动画效果
                v.setScaleX(0.95f);
                v.setScaleY(0.95f);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                    }
                }, 100);
            }
        });
    }

    @Override
    public int getItemCount() {
        return logEntries.size();
    }

    public void addLog(String message, String type) {
        try {
            // 检查参数有效性
            if (message == null || message.trim().isEmpty()) {
                Log.w("LogAdapter", "尝试添加空日志消息");
                return;
            }
            
            if (type == null) {
                type = "INFO";
            }
            
            // 限制消息长度，防止内存溢出
            if (message.length() > 1000) {
                message = message.substring(0, 1000) + "...";
            }
            
            logEntries.add(0, new LogEntry(message, type)); // 添加到顶部
            notifyItemInserted(0);
            
            // 限制日志数量，最多显示50条，防止内存溢出
            if (logEntries.size() > 50) {
                int removedCount = logEntries.size() - 50;
                for (int i = 0; i < removedCount; i++) {
                    logEntries.remove(logEntries.size() - 1);
                }
                notifyItemRangeRemoved(50, removedCount);
            }
        } catch (Exception e) {
            Log.e("LogAdapter", "添加日志失败", e);
            LocationTrackerApplication.logError("LogAdapter添加日志失败", e);
        }
    }

    public void clearLogs() {
        logEntries.clear();
        notifyDataSetChanged();
    }

    public int getLogCount() {
        return logEntries.size();
    }
    
    /**
     * 获取所有日志的文本格式
     */
    public String getAllLogsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 位置上报 运行日志 ===\n");
        sb.append("时间: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date())).append("\n\n");
        
        for (int i = logEntries.size() - 1; i >= 0; i--) {
            LogEntry entry = logEntries.get(i);
            String timeStr = dateFormat.format(new Date(entry.timestamp));
            String typeStr = "[" + entry.type + "]";
            sb.append(timeStr).append(" ").append(typeStr).append(" ").append(entry.message).append("\n");
        }
        
        return sb.toString();
    }
} 