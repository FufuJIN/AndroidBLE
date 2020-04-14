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

public class LoginActivity extends AppCompatActivity {
    private DatabaseVersionManager  mDatabaseVersionManager = new DatabaseVersionManager();
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase   db;

    private TextView mTxtTitle;                           //登陆界面标题
    private Button   mBtnLogin,mBtnBack;                  //登陆，返回按钮
    private EditText mETxtUserName,mETxtPaw;              //用户名和密码输入框
    private String   UserName,PassWord,Md5Password;
    private TextView mTxtRegister,mTxtForgetPassword;      //注册和忘记密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    private void init(){
        //打开数据库
        dbHelper =new MyDatabaseHelper(LoginActivity.this,"PULSE.db",null, mDatabaseVersionManager.VERSION);
        db = dbHelper.getWritableDatabase();
        //标题
        mTxtTitle = findViewById(R.id.TitleText);
        mTxtTitle.setText("登陆");
        //按钮
        mBtnBack = findViewById(R.id.title_back);
        mBtnLogin = findViewById(R.id.btn_login);
        //可编辑文本
        mETxtUserName = findViewById(R.id.et_user_name);
        mETxtPaw = findViewById(R.id.et_psw);
        //文本
        mTxtRegister = findViewById(R.id.tv_register);
        mTxtForgetPassword = findViewById(R.id.tv_find_psw);
        //返回按键直接退出程序
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.this.finish();
            }
        });
        //立即注册控件的点击事件
        mTxtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //为了跳转到注册界面，并实现注册功能
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        //忘记密码控件的点击事件
        mTxtForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "抱歉，该功能暂未开放", Toast.LENGTH_SHORT).show();
            }
        });
        //登陆按钮的点击事件
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserName = mETxtUserName.getText().toString().trim();
                PassWord = mETxtPaw.getText().toString().trim();
                String ssPassWord = MD5Utils.md5(PassWord);  //输入密码的加密后字符串
                if(TextUtils.isEmpty(UserName)){
                    Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(PassWord)){
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Md5Password = dbHelper.getPassWord(db,UserName);
                    if(Md5Password ==null){
                        Toast.makeText(LoginActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        if (ssPassWord.equals(Md5Password)) {
                            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                            mDatabaseVersionManager.setUserNow(UserName);
                            Intent data = new Intent(LoginActivity.this, MainActivity.class);
                            //datad.putExtra( ); name , value ;
                            data.putExtra(MainActivity.EXTRAS_LOGIN_STATUS, true);
                            data.putExtra(MainActivity.EXTRAS_USER_NAME, UserName);
                            //跳转到主界面，登录成功的状态传递到 MainActivity 中
                            startActivity(data);
                        } else if (!ssPassWord.equals(Md5Password)) {
                            Toast.makeText(LoginActivity.this, "用户名和密码不一致", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            }
        });
    }

    @Override
    //显示数据， onActivityResult
    //startActivityForResult(intent, 1); 从注册界面中获取数据
    //int requestCode , int resultCode , Intent data
    // LoginActivity -> startActivityForResult -> onActivityResult();
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            //是获取注册界面回传过来的用户名
            // getExtra().getString("***");
            String userName=data.getStringExtra("userName");
            if(!TextUtils.isEmpty(userName)){
                //设置用户名到 et_user_name 控件
                mETxtUserName.setText(userName);
                //et_user_name控件的setSelection()方法来设置光标位置
                mETxtUserName.setSelection(userName.length());
            }
        }
    }
}
