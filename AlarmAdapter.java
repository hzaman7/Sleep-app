package com.example.sleepapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<AlarmModel> alarmList;

    public AlarmAdapter(List<AlarmModel> alarmList) {
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmModel alarm = alarmList.get(position);
        holder.alarm_date.setText(alarm.getDate());
        holder.timeText.setText(alarm.sleepHours + " hrs and " + alarm.sleepMinutes + " mins");
        holder.labelText.setText(alarm.getTitle());
        if(alarm.isAlarm()){
            holder.wasSmartAlarmTV.setText("Smart Alarm");
        }else{
            holder.wasSmartAlarmTV.setText("No Alarm");
        }


    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, alarm_date,labelText,wasSmartAlarmTV;
        Switch toggleSwitch;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.alarm_time);
            labelText = itemView.findViewById(R.id.alarm_label);
            alarm_date = itemView.findViewById(R.id.alarm_date);
            wasSmartAlarmTV = itemView.findViewById(R.id.wasSmartAlarmTV);
        }
    }
}
