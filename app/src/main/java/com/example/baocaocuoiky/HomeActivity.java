package com.example.baocaocuoiky;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {
    TextView userInitialsView;
    ListView lvRoomList;
    Button DatPhong;
    ArrayList<String> roomList;
    ArrayAdapter<String> roomAdapter;
    SQLiteDatabase dataPhong;
    String selectedRoomId = null;

    String selectedDate = "";
    String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        userInitialsView = findViewById(R.id.userInitialsView);
        lvRoomList = findViewById(R.id.lvRoomList);
        DatPhong = findViewById(R.id.btnDatPhong);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        if (username != null && !username.isEmpty()) {
            String initials = username.substring(0, Math.min(2, username.length())).toUpperCase();
            userInitialsView.setText(initials);
        }

        roomList = new ArrayList<>();
        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomList);
        lvRoomList.setAdapter(roomAdapter);

        dataPhong = openOrCreateDatabase("QlyPhong.db", MODE_PRIVATE, null);
        loadRoomData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lvRoomList.setOnItemClickListener((parent, view, position, id) -> {
            String roomData = roomList.get(position);
            selectedRoomId = getRoomIdFromData(roomData);

            if (roomData.contains("Trạng thái: Bận")) {
                DatPhong.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Phòng này đã được đặt!", Toast.LENGTH_SHORT).show();
            } else {
                DatPhong.setVisibility(View.VISIBLE);
            }
        });

        DatPhong.setOnClickListener(v -> showDateTimePicker());
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(HomeActivity.this, (view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;

            showTimePicker();
        }, currentYear, currentMonth, currentDay);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(HomeActivity.this, (view, hourOfDay, minute) -> {
            selectedTime = hourOfDay + ":" + minute;

            placeBooking();
        }, currentHour, currentMinute, true);

        timePickerDialog.show();
    }

    private void placeBooking() {
        if (selectedRoomId != null && !selectedDate.isEmpty() && !selectedTime.isEmpty()) {
            updateRoomStatus(selectedRoomId, "Bận");

            ContentValues bookingDetails = new ContentValues();
            bookingDetails.put("maphong", selectedRoomId);
            bookingDetails.put("ngaydat", selectedDate);
            bookingDetails.put("giothue", selectedTime);

            Toast.makeText(HomeActivity.this, "Đặt phòng thành công!", Toast.LENGTH_SHORT).show();
            loadRoomData();
            DatPhong.setVisibility(View.GONE);
            selectedRoomId = null;
        } else {
            Toast.makeText(HomeActivity.this, "Vui lòng chọn đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRoomData() {
        roomList.clear();
        Cursor cursor = dataPhong.query("tblphong", null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String roomData = "Mã phòng: " + cursor.getString(cursor.getColumnIndex("maphong")) + "\n" +
                        "Giá tiền: " + cursor.getDouble(cursor.getColumnIndex("GiaTien")) + "\n" +
                        "Trạng thái: " + cursor.getString(cursor.getColumnIndex("TrangThai"));
                roomList.add(roomData);
            }
            cursor.close();
        }
        roomAdapter.notifyDataSetChanged();
    }

    private String getRoomIdFromData(String roomData) {
        String[] lines = roomData.split("\n");
        if (lines.length > 0) {
            return lines[0].replace("Mã phòng: ", "").trim();
        }
        return null;
    }

    private void updateRoomStatus(String roomId, String newStatus) {
        ContentValues values = new ContentValues();
        values.put("TrangThai", newStatus);
        dataPhong.update("tblphong", values, "maphong = ?", new String[]{roomId});
    }
}
