package com.example.uas_pemrograman_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity6 extends AppCompatActivity {

    private RecyclerView rvHistory;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences dailyPrefs;
    private List<HistoryRecord> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        rvHistory = findViewById(R.id.rvHistory);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        dailyPrefs = getSharedPreferences("DailyActivityLogs", MODE_PRIVATE);

        loadHistoryData();
        setupRecyclerView();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_history) {
                return true;
            } else if (itemId == R.id.nav_beranda) {
                startActivity(new Intent(this, MainActivity8.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_water) {
                startActivity(new Intent(this, MainActivity7.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, MainActivity5.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity3.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_history);
    }

    private void loadHistoryData() {
        historyList = new ArrayList<>();
        Map<String, ?> allEntries = dailyPrefs.getAll();
        Map<String, HistoryRecord> recordsMap = new TreeMap<>(Collections.reverseOrder());

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() == null) continue;
            String value = entry.getValue().toString();

            if (key.endsWith("_water")) {
                String date = key.replace("_water", "");
                HistoryRecord record = recordsMap.getOrDefault(date, new HistoryRecord(date));
                record.setWater(value);
                recordsMap.put(date, record);
            } else if (key.endsWith("_activity")) {
                String date = key.replace("_activity", "");
                HistoryRecord record = recordsMap.getOrDefault(date, new HistoryRecord(date));
                record.setActivity(value);
                recordsMap.put(date, record);
            }
        }
        historyList.addAll(recordsMap.values());
    }

    private void setupRecyclerView() {
        HistoryAdapter adapter = new HistoryAdapter(historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
    }

    public static class HistoryRecord {
        private final String date;
        private String water = "0";
        private String activity = "-";

        public HistoryRecord(String date) { this.date = date; }
        public String getDate() { return date; }
        public String getWater() { return water; }
        public void setWater(String water) { this.water = water; }
        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<HistoryRecord> localDataSet;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvDate, tvWater, tvActivity;

            public ViewHolder(View view) {
                super(view);
                tvDate = view.findViewById(R.id.tvHistoryDate);
                tvWater = view.findViewById(R.id.tvHistoryWater);
                tvActivity = view.findViewById(R.id.tvHistoryActivity);
            }
        }

        public HistoryAdapter(List<HistoryRecord> dataSet) { localDataSet = dataSet; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_history, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            HistoryRecord record = localDataSet.get(position);
            viewHolder.tvDate.setText(record.getDate());
            viewHolder.tvWater.setText("Minum: " + record.getWater() + " ml");
            viewHolder.tvActivity.setText("Aktivitas: " + record.getActivity());
        }

        @Override
        public int getItemCount() { return localDataSet.size(); }
    }
}
