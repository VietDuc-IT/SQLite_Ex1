package com.levietduc.sqlite_ex1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.levietduc.sqlite_ex1.databinding.ActivityAddBinding;

public class AddActivity extends AppCompatActivity {
    ActivityAddBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add);

        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        addEvents();
    }

    private void addEvents() {
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Insert data into db
                ContentValues values = new ContentValues();
                values.put("ProductName",binding.edtProductName.getText().toString());
                values.put("ProductPrice",Double.parseDouble(binding.edtProductPrice.getText().toString()));
                long numbOfRows = MainActivity.db.insert(MainActivity.TBL_NAME,null,values);
                if(numbOfRows > 0){
                    Toast.makeText(AddActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddActivity.this, "Fail!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}