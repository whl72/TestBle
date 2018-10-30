package com.example.sfd.test_1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.R.attr.data;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextPassword;
    private Button buttonRegister;
    private Button buttonQuery;
    private UserInfoDatabaseHelper dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextName = (EditText) findViewById(R.id.register_name_edit);
        editTextPassword = (EditText) findViewById(R.id.register_password_edit);
        buttonRegister = (Button) findViewById(R.id.register_finish_button);
        buttonQuery = (Button) findViewById(R.id.query_button);
        //创建SQL数据库，用于存放注册用户信息
        dbhelper = new UserInfoDatabaseHelper(this,
                "UserInfor.db", null, 1);


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameStr = editTextName.getText().toString();
                String passwordStr = editTextPassword.getText().toString();
                String saveStr = nameStr+" "+passwordStr;

                //文件形式保存，不推荐使用这种方式
//                saveInfo2File(saveStr);

                if((nameStr.length() == 0) || (passwordStr.length() == 0)){
//                    Toast.makeText(RegisterActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT)
//                            .show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("用户名或密码不能为空");
                    builder.setPositiveButton("确定", null);
                    builder.show();
                }else{
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(RegisterActivity.this);
                    builder.setIcon(R.drawable.loginbg);
                    builder.setTitle("温馨提示");
                    builder.setMessage("确定要创建该用户名？");
                    builder.setCancelable(true);

                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String nameStr = editTextName.getText().toString();
                            String passwordStr = editTextPassword.getText().toString();

                            //数据库形式保存，这种方式适合保存数据结构比较复杂的情况
                            SQLiteDatabase db = dbhelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("user", nameStr);
                            values.put("password", passwordStr);
//                values.put("phone", "15023456789");
                            db.insert("UserInfor", null, values);
                            values.clear();
//                    Toast.makeText(RegisterActivity.this, "插入成功", Toast.LENGTH_SHORT)
//                            .show();

                            AlertDialog.Builder builder2 = new AlertDialog.Builder(RegisterActivity.this);
                            builder2.setTitle("提示");
                            builder2.setMessage("注册成功！");
                            builder2.setPositiveButton("确定", null);
                            builder2.show();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.create().show();
                }

            }
        });

        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbhelper.getWritableDatabase();
                Cursor cursor = db.query("UserInfor", null, null, null,
                        null, null, null);
                if(cursor.moveToFirst()){
                    do{
                        String user = cursor.getString(cursor.getColumnIndex("user"));
                        String password = cursor.getString(cursor.getColumnIndex(
                                "password"));
                        String str = "用户："+user+"  密码："+password;
                        Log.d("RegisterActivity", "user: "+user);
                        Log.d("RegisterActivity", "password: "+password);
                        Toast.makeText(RegisterActivity.this, str,
                                Toast.LENGTH_SHORT).show();
                    }while (cursor.moveToNext());
                }
                cursor.close();
            }
        });
    }

    public void saveInfo2File(String saveStr){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("UserInformation", Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(saveStr);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


