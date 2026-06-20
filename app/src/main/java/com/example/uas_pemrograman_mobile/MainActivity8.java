package com.example.uas_pemrograman_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity8 extends AppCompatActivity {

    private AutoCompleteTextView actvActivityType;
    private MaterialCardView cardDetailInput;
    private TextView tvDetailTitle, tvWaterRecommendation;
    private TextInputLayout tilDetailValue;
    private TextInputEditText etDetailValue;
    private Button btnSaveActivity;
    private BottomNavigationView bottomNavigationView;
    
    private SharedPreferences activityPrefs;
    private String todayDateKey;
    private String selectedActivity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main8);

        // Jalankan pengingat minum air setiap 1 jam
        WaterReminderHelper.scheduleWaterReminder(this);

        activityPrefs = getSharedPreferences("DailyActivityLogs", MODE_PRIVATE);
        todayDateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        initViews();
        setupActivityDropdown();
        loadSavedData();
        setupBottomNavigation();

        btnSaveActivity.setOnClickListener(v -> saveRencana());
    }

    private void initViews() {
        actvActivityType = findViewById(R.id.actvActivityType);
        cardDetailInput = findViewById(R.id.cardDetailInput);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvWaterRecommendation = findViewById(R.id.tvWaterRecommendation);
        tilDetailValue = findViewById(R.id.tilDetailValue);
        etDetailValue = findViewById(R.id.etDetailValue);
        btnSaveActivity = findViewById(R.id.btnSaveActivity);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupActivityDropdown() {
        String[] activities = {
                "Duduk/aktivitas ringan (per jam)",
                "Berjalan santai (per 30 menit)",
                "Pekerjaan rumah ringan (per jam)",
                "Bekerja di kantor (per jam)",
                "Olahraga ringan (per 30-60 menit)",
                "Jogging atau bersepeda santai (per jam)",
                "Latihan intensitas sedang-tinggi (per jam)",
                "Bekerja di luar ruangan / panas (per jam)",
                "Mendaki gunung / aktivitas berat (>2 jam)",
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activities);
        actvActivityType.setAdapter(adapter);

        actvActivityType.setOnItemClickListener((parent, view, position, id) -> {
            selectedActivity = (String) parent.getItemAtPosition(position);
            cardDetailInput.setVisibility(View.VISIBLE);
            updateInputLabels(selectedActivity);
        });
    }

    private void updateInputLabels(String activity) {
        etDetailValue.setText(""); // Reset input
        if (activity.contains("Berjalan santai")) {
            tvDetailTitle.setText("Durasi Berjalan");
            tilDetailValue.setHint("Jumlah Sesi (1 sesi = 30 mnt)");
        } else if (activity.contains("Pekerjaan rumah")) {
            tvDetailTitle.setText("Durasi Pekerjaan Rumah");
            tilDetailValue.setHint("Jumlah Jam");
        } else if (activity.contains("Olahraga ringan")) {
            tvDetailTitle.setText("Durasi Olahraga");
            tilDetailValue.setHint("Jumlah Sesi (1 sesi = 45 mnt)");
        } else if (activity.contains("Jogging") || activity.contains("Latihan") || activity.contains("luar ruangan") || 
                   activity.contains("Duduk") || activity.contains("kantor") || activity.contains("Mendaki")) {
            tvDetailTitle.setText("Durasi Aktivitas");
            tilDetailValue.setHint("Jumlah Jam");
        } else {
            tvDetailTitle.setText("Keterangan");
            tilDetailValue.setHint("Status");
            etDetailValue.setText("1");
        }
    }

    private void loadSavedData() {
        String waterReq = activityPrefs.getString(todayDateKey + "_water_target", "2000");
        tvWaterRecommendation.setText(waterReq + " ml");
    }

    private void saveRencana() {
        if (selectedActivity.isEmpty()) {
            Toast.makeText(this, "Pilih aktivitas terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String detailStr = etDetailValue.getText().toString();
        if (detailStr.isEmpty()) {
            Toast.makeText(this, "Masukkan detail durasi/sesi", Toast.LENGTH_SHORT).show();
            return;
        }

        double detailValue = 0;
        try {
            detailValue = Double.parseDouble(detailStr);
        } catch (NumberFormatException e) {
            detailValue = 1;
        }

        int baseTarget = 2000;
        int extra = 0;
        String unit = " jam";

        // Logika perhitungan berdasarkan tabel
        if (selectedActivity.contains("Duduk")) {
            extra = (int) (detailValue * 50); // 50ml per jam duduk
        } else if (selectedActivity.contains("kantor")) {
            extra = (int) (detailValue * 100); // 100ml per jam di kantor
        } else if (selectedActivity.contains("Berjalan santai")) {
            extra = (int) (detailValue * 300);
            unit = " sesi";
        } else if (selectedActivity.contains("Pekerjaan rumah")) {
            extra = (int) (detailValue * 375);
        } else if (selectedActivity.contains("Olahraga ringan")) {
            extra = (int) (detailValue * 425);
            unit = " sesi";
        } else if (selectedActivity.contains("Jogging") || selectedActivity.contains("bersepeda")) {
            extra = (int) (detailValue * 625);
        } else if (selectedActivity.contains("Latihan intensitas")) {
            extra = (int) (detailValue * 850);
        } else if (selectedActivity.contains("luar ruangan")) {
            extra = (int) (detailValue * 750);
        } else if (selectedActivity.contains("Mendaki")) {
            extra = (int) (detailValue * 750);
        }

        int totalTarget = baseTarget + extra;
        String waterReq = String.valueOf(totalTarget);

        SharedPreferences.Editor editor = activityPrefs.edit();
        editor.putString(todayDateKey + "_water_target", waterReq);
        
        // Simpan format ringkasan: "Aktivitas (X jam/sesi)"
        String cleanActivity = selectedActivity.replaceAll("\\(.*?\\)", "").trim();
        editor.putString(todayDateKey + "_activity", cleanActivity + " (" + detailStr + unit + ")");
        editor.apply();

        tvWaterRecommendation.setText(waterReq + " ml");
        Toast.makeText(this, "Target air diperbarui: " + waterReq + " ml", Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_beranda);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_beranda) {
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
}
