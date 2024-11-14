package com.example.baocaocuoiky;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Signin extends AppCompatActivity {
    ImageView imageView;
    EditText edituser1, editpass1, editrepass;
    Button btnloggin, btnsignin;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        dbHelper = new DatabaseHelper(this);

        imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.setImageResource(R.drawable.ava);

        edituser1 = (EditText) findViewById(R.id.edituser1);
        editpass1 = (EditText) findViewById(R.id.editpass1);
        editrepass = (EditText) findViewById(R.id.editrepass);

        btnloggin = (Button) findViewById(R.id.btnloggin1);
        btnsignin = (Button) findViewById(R.id.btnsignin1);

        btnloggin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edituser1.getText().toString().trim();
                String password = editpass1.getText().toString().trim();
                String repass = editrepass.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || repass.isEmpty()) {
                    Toast.makeText(Signin.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(repass)) {
                    Toast.makeText(Signin.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                } else if (dbHelper.isUsernameExists(username)) {
                    Toast.makeText(Signin.this, "Tên người dùng đã tồn tại", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isInserted = dbHelper.insertUser(username, password);
                    if (isInserted) {
                        Toast.makeText(Signin.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        // Chuyển sang màn hình đăng nhập hoặc màn hình khác nếu cần
                        Intent intent = new Intent(Signin.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Signin.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}