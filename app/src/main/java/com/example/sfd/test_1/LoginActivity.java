package com.example.sfd.test_1;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUserName;
    private EditText editTextPassWord;
//    private UserInfoDatabaseHelper ddbhelper;
    private UserInfoDatabaseHelper dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserName = (EditText) findViewById(R.id.username_edit);
        editTextPassWord = (EditText) findViewById(R.id.password_edit);
        Button buttonEnter = (Button) findViewById(R.id.enter_button);
        Button buttonRegister = (Button) findViewById(R.id.register_button);

        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.enter_button:
                    String name_str = editTextUserName.getText().toString();
                    String paswrd_str = editTextPassWord.getText().toString();
                    String str = "用户："+name_str+"  密码："+paswrd_str;
//                    Toast.makeText(LoginActivity.this, str,
//                            Toast.LENGTH_SHORT).show();

                    //String inputext = load();

                    check_infor(name_str, paswrd_str);

                        break;
                    default:
                        break;
                }
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                //这里将用户和密码保存到本地
//                Toast.makeText(LoginActivity.this, "注册成功！",
//                        Toast.LENGTH_SHORT).show();

                Intent mintent = new Intent();
                mintent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(mintent);
            }
        });
    }

    public String load() {
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder content = new StringBuilder();
        try {
            inputStream = openFileInput("UserInformation");
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                content.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(bufferedReader != null){
                try{
                    bufferedReader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    public  String check_infor(String userName, String userPassword){
        dbhelper = new UserInfoDatabaseHelper(this,
                "UserInfor.db", null, 1);
        Log.d("RegisterActivity", "step1");
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        Log.d("RegisterActivity", "step2");
        Cursor cursor = db.query("UserInfor", null, null, null,
                null, null, null);
        Log.d("RegisterActivity", "step3");
        if(cursor.moveToFirst()){
            do{
                String user = cursor.getString(cursor.getColumnIndex("user"));
                String password = cursor.getString(cursor.getColumnIndex(
                        "password"));
                String str = "用户："+user+"  密码："+password;
                Log.d("RegisterActivity", "user: "+user);
                Log.d("RegisterActivity", "password: "+password);
//                Toast.makeText(LoginActivity.this, str,
//                        Toast.LENGTH_SHORT).show();

                if((userName.equals(user)) && (userPassword.equals(password))){
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("提示")
                            .setMessage("登录成功！")
                            .setPositiveButton("确定", null)
                            .show();
                    return "OK";
                }

            }while (cursor.moveToNext());
        }
        cursor.close();

        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("提示")
                .setMessage("用户名或密码错误！")
                .setPositiveButton("确定", null)
                .show();
        return "NG";
    }
}
