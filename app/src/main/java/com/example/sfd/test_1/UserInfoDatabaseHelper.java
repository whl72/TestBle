package com.example.sfd.test_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by SFD on 2017/12/13.
 */

public class UserInfoDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_TABLE = "create table UserInfor ("
            + "id integer primary key autoincrement, "
            + "user text, "
            + "password text，"
            + "phone text)";
    private Context mContext;

    public UserInfoDatabaseHelper(Context context, String user,
                  SQLiteDatabase.CursorFactory factory, int version){
        super(context, user, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        
    }
}
