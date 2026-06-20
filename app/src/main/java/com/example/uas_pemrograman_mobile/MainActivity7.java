package com.example.uas_pemrograman_mobile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity7 extends AppCompatActivity {

    private ProgressBar pbWaterCircle;
    private TextView tvWaterProgressText;
    private RecyclerView rvIntakeTimeline;
    private View btnAddWaterManual;
    private ImageButton btnBack;
    private BottomNavigationView bottomNavigationView;

    // Views for Cup Selection
    private TextView tvSelectedCupText;
    private ImageView ivSelectedCupIcon;
    private View btnChangeCup;
    private int selectedCupAmount = 200;

    private SharedPreferences waterPrefs;
    private List<IntakeRecord> intakeList;
    private IntakeAdapter adapter;

    private int dailyGoal = 1200; 
    private int currentIntake = 0;
    private String todayDateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);

        waterPrefs = getSharedPreferences("WaterTrackingPrefs", MODE_PRIVATE);
        todayDateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        initViews();
        loadDailyGoal();
        setupRecyclerView();
        loadTodayData();
        setupBottomNavigation();

        btnAddWaterManual.setOnClickListener(v -> addWater(selectedCupAmount));
        btnChangeCup.setOnClickListener(v -> showCupSelectionDialog());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDailyGoal() {
        SharedPreferences activityPrefs = getSharedPreferences("DailyActivityLogs", MODE_PRIVATE);
        String savedGoal = activityPrefs.getString(todayDateKey + "_water_target", "2000");
        try {
            dailyGoal = Integer.parseInt(savedGoal);
        } catch (NumberFormatException e) {
            dailyGoal = 2000;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDailyGoal();
        updateUI();
        bottomNavigationView.setSelectedItemId(R.id.nav_water);
    }

    private void initViews() {
        pbWaterCircle = findViewById(R.id.pbWaterCircle);
        tvWaterProgressText = findViewById(R.id.tvWaterProgressText);
        rvIntakeTimeline = findViewById(R.id.rvIntakeTimeline);
        btnAddWaterManual = findViewById(R.id.btnAddWaterManual);
        btnBack = findViewById(R.id.btnBack);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        tvSelectedCupText = findViewById(R.id.tvSelectedCupText);
        ivSelectedCupIcon = findViewById(R.id.ivSelectedCupIcon);
        btnChangeCup = findViewById(R.id.btnChangeCup);
    }

    private void showCupSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cup, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        int[] buttonIds = {R.id.btnCup100, R.id.btnCup125, R.id.btnCup150, R.id.btnCup175, R.id.btnCup200, R.id.btnCup300, R.id.btnCup400};
        int[] amounts = {100, 125, 150, 175, 200, 300, 400};

        for (int i = 0; i < buttonIds.length; i++) {
            int amount = amounts[i];
            view.findViewById(buttonIds[i]).setOnClickListener(v -> {
                updateSelectedCup(amount);
                dialog.dismiss();
            });
        }

        view.findViewById(R.id.btnCupCustom).setOnClickListener(v -> {
            dialog.dismiss();
            showCustomAmountDialog();
        });

        view.findViewById(R.id.btnCancelDialog).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCustomAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Masukkan jumlah air (ml)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                updateSelectedCup(Integer.parseInt(val));
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateSelectedCup(int amount) {
        selectedCupAmount = amount;
        tvSelectedCupText.setText(amount + " ml");
        
        // Update Icon based on amount
        if (amount == 100) {
            ivSelectedCupIcon.setImageResource(R.drawable.satu_round_cup);
        } else if (amount == 125) {
            ivSelectedCupIcon.setImageResource(R.drawable.dua_square_mug);
        } else if (amount == 150) {
            ivSelectedCupIcon.setImageResource(R.drawable.tiga_water_glass);
        } else if (amount == 175) {
            ivSelectedCupIcon.setImageResource(R.drawable.empat_tall_glass);
        } else if (amount == 200) {
            ivSelectedCupIcon.setImageResource(R.drawable.lima_takeaway);
        } else if (amount == 300) {
            ivSelectedCupIcon.setImageResource(R.drawable.enam_thermos);
        } else if (amount == 400) {
            ivSelectedCupIcon.setImageResource(R.drawable.tujuh_bottle);
        } else {
            // Default for custom amount
            ivSelectedCupIcon.setImageResource(R.drawable.delapan_medical_mug);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_water);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_beranda) {
                startActivity(new Intent(this, MainActivity8.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_water) {
                return true;
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, MainActivity5.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, MainActivity6.class));
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

    private void setupRecyclerView() {
        intakeList = new ArrayList<>();
        adapter = new IntakeAdapter(intakeList, this::deleteIntake);
        rvIntakeTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvIntakeTimeline.setAdapter(adapter);
    }

    private void loadTodayData() {
        intakeList.clear();
        currentIntake = 0;

        Map<String, ?> allEntries = waterPrefs.getAll();
        TreeMap<String, Integer> sortedEntries = new TreeMap<>(Collections.reverseOrder());
        
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith(todayDateKey + "_")) {
                try {
                    int amount = Integer.parseInt(entry.getValue().toString());
                    sortedEntries.put(entry.getKey(), amount);
                    currentIntake += amount;
                } catch (Exception ignored) {}
            }
        }

        for (Map.Entry<String, Integer> entry : sortedEntries.entrySet()) {
            String fullKey = entry.getKey();
            String time = fullKey.substring(11, 16);
            int amount = entry.getValue();
            intakeList.add(new IntakeRecord(fullKey, time, (amount > 0 ? "+" : "") + amount + "ml", amount));
        }
        updateUI();
        updateDailyTotalForCalendar(0);
    }

    private void addWater(int amount) {
        saveWaterData(amount, "+" + amount + "ml Berhasil ditambahkan!");
    }

    private void saveWaterData(int amount, String message) {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat sdfFull = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdfTime.format(new Date());
        String fullKey = todayDateKey + "_" + sdfFull.format(new Date());

        waterPrefs.edit().putInt(fullKey, amount).apply();

        currentIntake += amount;
        intakeList.add(0, new IntakeRecord(fullKey, currentTime, (amount > 0 ? "+" : "") + amount + "ml", amount));
        adapter.notifyItemInserted(0);
        rvIntakeTimeline.scrollToPosition(0);
        
        updateUI();
        updateDailyTotalForCalendar(amount);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void deleteIntake(int position) {
        IntakeRecord record = intakeList.get(position);
        waterPrefs.edit().remove(record.fullKey).apply();
        
        currentIntake -= record.amountValue;
        updateDailyTotalForCalendar(-record.amountValue);
        
        intakeList.remove(position);
        adapter.notifyItemRemoved(position);
        updateUI();
        Toast.makeText(this, "Catatan dihapus", Toast.LENGTH_SHORT).show();
    }

    private void updateDailyTotalForCalendar(int addedAmount) {
        SharedPreferences dailyPrefs = getSharedPreferences("DailyActivityLogs", MODE_PRIVATE);
        String savedWaterStr = dailyPrefs.getString(todayDateKey + "_water", "0");
        int totalMl = 0;
        try {
            totalMl = Integer.parseInt(savedWaterStr);
        } catch (NumberFormatException e) {
            totalMl = 0;
        }
        
        int newTotalMl = Math.max(0, currentIntake); 
        dailyPrefs.edit().putString(todayDateKey + "_water", String.valueOf(newTotalMl)).apply();
    }

    private void updateUI() {
        pbWaterCircle.setMax(dailyGoal);
        pbWaterCircle.setProgress(Math.max(0, currentIntake));
        tvWaterProgressText.setText(Math.max(0, currentIntake) + "/" + dailyGoal + "ml");
    }

    public static class IntakeRecord {
        String fullKey;
        String time;
        String amountText;
        int amountValue;
        public IntakeRecord(String fullKey, String time, String amountText, int amountValue) {
            this.fullKey = fullKey;
            this.time = time;
            this.amountText = amountText;
            this.amountValue = amountValue;
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private static class IntakeAdapter extends RecyclerView.Adapter<IntakeAdapter.ViewHolder> {
        private List<IntakeRecord> records;
        private OnDeleteClickListener deleteListener;

        public IntakeAdapter(List<IntakeRecord> records, OnDeleteClickListener deleteListener) { 
            this.records = records; 
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intake, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            IntakeRecord record = records.get(position);
            holder.tvTime.setText(record.time);
            holder.tvAmount.setText(record.amountText);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() { return records.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime, tvAmount;
            ImageButton btnDelete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTime = itemView.findViewById(R.id.tvIntakeTime);
                tvAmount = itemView.findViewById(R.id.tvIntakeAmount);
                btnDelete = itemView.findViewById(R.id.btnDeleteIntake);
            }
        }
    }
}
