package com.example.baocaocuoiky;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    EditText editMaphong, editGiatien, editTrangthai;
    Button btnThem, btnSua, btnTruyvan, btnuploadAnh;
    ImageView imageView;
    ListView lvDanhsach;
    ArrayList<String> mylist;
    ArrayAdapter<String> myadapter;
    private List<String> list;
    private Spinner spinner;
    private static final int PICK_IMAGE_REQUEST = 1;
    SQLiteDatabase dataPhong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        editMaphong = (EditText) findViewById(R.id.editMaphong);
        editGiatien = (EditText) findViewById(R.id.editGiatien);
        editTrangthai = (EditText) findViewById(R.id.editTrangthai);

        btnuploadAnh = (Button) findViewById(R.id.btnuploadAnh);
        btnThem = (Button) findViewById(R.id.btnThem);
        btnSua = (Button) findViewById(R.id.btnSua);
        btnTruyvan = (Button) findViewById(R.id.btnTruyvan);

        lvDanhsach = (ListView) findViewById(R.id.lvDanhsach);
        imageView = (ImageView) findViewById(R.id.imgUpload);

        spinner =(Spinner) findViewById(R.id.spntrangthai);
        list = new ArrayList<>();
        list.add("Rảnh");
        list.add("Bận");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editTrangthai.setText(list.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnuploadAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
            }
        });

        mylist = new ArrayList<>();
        myadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mylist);
        lvDanhsach.setAdapter(myadapter);
        dataPhong =openOrCreateDatabase("QlyPhong.db", MODE_PRIVATE, null);
        try{
            String sql = "CREATE TABLE tblphong(maphong TEXT, GiaTien REAL, Anh BLOB, TrangThai TEXT)";
            dataPhong.execSQL(sql);
        }catch (Exception e){
            Log.e("Error", "Table đã tồn tại");
        }
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String MaPhong = editMaphong.getText().toString().trim();
                double giaTien = Double.parseDouble(editGiatien.getText().toString().trim());
                byte[] anh = imageViewToByte(imageView);
                String trangThai = spinner.getSelectedItem().toString();
                ContentValues myvalue = new ContentValues();
                myvalue.put("Maphong", MaPhong);
                myvalue.put("GiaTien", giaTien);
                myvalue.put("Anh", anh);
                myvalue.put("TrangThai", trangThai);
                String msg = "";
                if (dataPhong.insert("tblphong", null, myvalue) == -1) {
                    msg = "Thêm thông tin thất bại";
                } else {
                    msg = "Thêm thông tin thành công";
                }
                Toast.makeText(AdminActivity.this, msg, Toast.LENGTH_SHORT).show();
                editMaphong.setText("");
                editGiatien.setText("");
                editTrangthai.setText("");
                spinner.setSelection(0);
                editMaphong.requestFocus();
                imageView.setImageResource(0);
            }
        });

        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String MaPhong = editMaphong.getText().toString().trim();
                String GiaTienText = editGiatien.getText().toString().trim();
                String TrangThai = spinner.getSelectedItem().toString();

                if (MaPhong.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Vui lòng nhập mã phòng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (GiaTienText.isEmpty() && TrangThai.equals("Rảnh")) {
                    Toast.makeText(AdminActivity.this, "Vui lòng điền ít nhất một thông tin để sửa!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double giaTien = 0;
                if (!GiaTienText.isEmpty()) {
                    try {
                        giaTien = Double.parseDouble(GiaTienText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(AdminActivity.this, "Giá tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                byte[] anh = imageViewToByte(imageView);

                String updateSQL = "UPDATE tblphong SET GiaTien = ?, Anh = ?, TrangThai = ? WHERE Maphong = ?";
                SQLiteStatement statement = dataPhong.compileStatement(updateSQL);

                statement.bindDouble(1, giaTien);
                statement.bindBlob(2, anh);
                statement.bindString(3, TrangThai);
                statement.bindString(4, MaPhong);

                try {
                    int rowsAffected = statement.executeUpdateDelete();

                    if (rowsAffected > 0) {
                        Toast.makeText(AdminActivity.this, "Cập nhật phòng thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminActivity.this, "Không tìm thấy phòng với mã phòng đã nhập!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(AdminActivity.this, "Đã xảy ra lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
                }

                editMaphong.setText("");
                editGiatien.setText("");
                spinner.setSelection(0);
                editTrangthai.setText("");
                imageView.setImageResource(0);
                editMaphong.requestFocus();
            }
        });


        btnTruyvan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mylist.clear();
                Cursor c = dataPhong.query("tblphong", null, null, null, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        @SuppressLint("Range") String data = "Mã phòng: " + c.getString(c.getColumnIndex("maphong")) + "\n" +
                                "Giá tiền: " + c.getDouble(c.getColumnIndex("GiaTien")) + "\n" +
                                "Trạng thái: " + c.getString(c.getColumnIndex("TrangThai"));
                        mylist.add(data);
                    }
                    c.close();
                }
                myadapter.notifyDataSetChanged();
                Toast.makeText(AdminActivity.this, "Truy vấn dữ liệu thành công!", Toast.LENGTH_SHORT).show();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}