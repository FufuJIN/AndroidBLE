package com.example.databasetest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import DatabaseVersion.DatabaseVersionManager;
import DatabaseVersion.MyDatabaseHelper;
import utils.MD5Utils;

public class RegisterActivity extends AppCompatActivity {
    //数据库
    private DatabaseVersionManager mDatabaseVersionManager= new DatabaseVersionManager();
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private TextView mTxtTitle;         //注册界面标题
    private Button   mBtnBack;      //注册界面返回按钮
    private Button   mBtnRegister;  //注册界面注册按钮
    private EditText mETxtUserName,mETxtPsw,mETxtPswAgain;
    private String   userName,psw,pswAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }
    private void init(){
        //打开数据库
        dbHelper =new MyDatabaseHelper(RegisterActivity.this,"PULSE.db",null, mDatabaseVersionManager.VERSION);
        db = dbHelper.getWritableDatabase();

        mTxtTitle = findViewById(R.id.TitleText);
        mTxtTitle.setText("欢迎注册");

        mBtnBack     = findViewById(R.id.title_back);
        mBtnRegister = findViewById(R.id.btn_register);
        mETxtUserName =findViewById(R.id.et_user_name);
        mETxtPsw      =findViewById(R.id.et_psw);
        mETxtPswAgain =findViewById(R.id.et_psw_again);
        //返回按钮
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.this.finish();
            }
        });
        //注册按钮
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEditString();
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(!psw.equals(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }else if(dbHelper.isExistUserName(db,userName)){//数据库中是否存在该用户
                    Toast.makeText(RegisterActivity.this, "此账户名已经存在", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    //把账号、密码和账号标识保存到数据库里面
                    //代码没写
                    String md5Psw = MD5Utils.md5(psw);//把密码用MD5加密
                    dbHelper.newUser(db,userName,md5Psw);
                    //注册成功后把账号传递到LoginActivity.java中
                    // 返回值到loginActivity显示
                    Intent data = new Intent();
                    data.putExtra("userName", userName);
                    setResult(RESULT_OK, data);
                    //RESULT_OK为Activity系统常量，状态码为-1，
                    //表示此页面下的内容操作成功将data返回到上一页面，如果是用back返回过去的则不存在用setResult传递data值
                    RegisterActivity.this.finish();
                }
            }
        });
    }
    /**
     * 获取控件中的字符串
     */
    private void getEditString(){
        userName = mETxtUserName.getText().toString().trim();
        psw      = mETxtPsw.getText().toString().trim();
        pswAgain = mETxtPswAgain.getText().toString().trim();
    }

}
