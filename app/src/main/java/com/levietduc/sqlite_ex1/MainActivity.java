package com.levietduc.sqlite_ex1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.levietduc.models.Product;
import com.levietduc.sqlite_ex1.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static SQLiteDatabase db;

    public static final String DB_NAME = "product_db.db";
    public static final String DB_PATH_SUFFIX = "/databases/";
    public static final String TBL_NAME = "Product";

    ArrayAdapter<Product> adapter;
    ArrayList<Product> products;
    Product selectedProduct = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        copyDB();
        openDB();
        //loadDataFromDB();
        addEvents();
        registerForContextMenu(binding.lvProduct);
    }

    private void addEvents() {
        binding.lvProduct.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedProduct = adapter.getItem(i);
                return false;
            }
        });
    }

    private void copyDB() {
        try{
            File dbFile = getDatabasePath(DB_NAME);
            if(!dbFile.exists()){
                if(processCopy()){
                    Toast.makeText(MainActivity.this,
                            "Copy database successful!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this,
                            "Copy database fail!", Toast.LENGTH_LONG).show();
                }
            }
        }catch (Exception e){
            Log.e("Error: ", e.toString());
        }
    }

    private boolean processCopy() {
        String dbPath = getApplicationInfo().dataDir + DB_PATH_SUFFIX +
                DB_NAME;
        try {
            InputStream inputStream = getAssets().open(DB_NAME);
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if(!f.exists()){
                f.mkdir();
            }
            OutputStream outputStream = new FileOutputStream(dbPath);
            byte[] buffer = new byte[1024]; int length;
            while((length=inputStream.read(buffer))>0){
                outputStream.write(buffer,0, length);
            }
            outputStream.flush(); outputStream.close(); inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openDB() {
        db = openOrCreateDatabase(DB_NAME,MODE_PRIVATE,null);
    }

    private void loadDataFromDB() {
        products = new ArrayList<>();
        //products.clear();
        Product p;
        // C1: Select data using rowQuery
        //Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_NAME, null);
        //Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_NAME + " WHERE ProductId=? OR ProductId=?", new String[]{"2","5"});

        // C2: Select data using query
        Cursor cursor = db.query(TBL_NAME,null,null,null,null,null,null);
        //Cursor cursor = db.query(TBL_NAME,null,"ProductId=? OR ProductId=?",new String[]{"1","4"},null,null,null);

        while (cursor.moveToNext()) {
            int pId = cursor.getInt(0);
            String pName = cursor.getString(1);
            double pPrice = cursor.getDouble(2);
            //To do Something ...
            p = new Product(pId,pName,pPrice);
            products.add(p);
            //Log.i("Data: ", pId + " - " + pName + " - " + pPrice);
        }
        cursor.close();

        adapter = new ArrayAdapter<Product>(this, android.R.layout.simple_list_item_1,products);

        binding.lvProduct.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        loadDataFromDB();
        super.onResume();
    }

    //==================MENU=======================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.option_menu,menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.mn_Add){
            //Open AddActivity
            Intent intent = new Intent(MainActivity.this,AddActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu,menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // ----------- Edit
        if(item.getItemId() == R.id.mn_Edit){
            //Open EditActivity
            Intent intent = new Intent(MainActivity.this,EditActivity.class);
            startActivity(intent);

            //Attach selectedProduct
            if(selectedProduct!=null){
                intent.putExtra("productInfo",selectedProduct);
                startActivity(intent);
            }
        }

        // ----------- Delete
        if(item.getItemId() == R.id.mn_Delete){

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Xác nhận xóa");
            builder.setMessage("Bạn có chắc muốn xóa sp"+selectedProduct.getProductName()+"'?");
            builder.setIcon(android.R.drawable.ic_delete);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int numbOdRow = db.delete(TBL_NAME,"ProductId=?",new String[]{String.valueOf(selectedProduct.getProductId())});
                    if(numbOdRow>0){
                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        loadDataFromDB();
                    }else {
                        Toast.makeText(MainActivity.this, "Fail!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        }
        return super.onContextItemSelected(item);
    }
}