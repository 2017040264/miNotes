package net.micode.notes.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
<<<<<<< HEAD

import androidx.appcompat.app.ActionBar;
=======
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
>>>>>>> c5b774f9aa1bbc59e6b6a973c871e7209e5a9335
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import net.micode.notes.R;
import net.micode.notes.model.user;
import android.database.sqlite.SQLiteDatabase;
import net.micode.notes.data.NotesDatabaseHelper;
import net.micode.notes.data.NotesDatabaseHelper.TABLE;

//import org.litepal.LitePal;

import java.util.List;

public class RegisterActivity extends AppCompatActivity{

    //顶部标题栏的toolbar
    private Toolbar registerToolbar;
    // 注册账号，即userid
    private EditText reg_userAccount;
    // 注册密码，即userpassword
    private EditText reg_userPwd;
    // 确认密码，验证两次输入的密码是否一致
    private EditText reg_confirm_userPwd;
    // 注册按钮
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerToolbar = findViewById(R.id.register_toolbar);
        registerToolbar.setTitle("小米便签");
        setSupportActionBar(registerToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_return_left2);
        }

        reg_userAccount = findViewById(R.id.register_userAccount);
        reg_userPwd = findViewById(R.id.register_pwd);
        reg_confirm_userPwd = findViewById(R.id.confirm_pwd);
        registerBtn = findViewById(R.id.register_click);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userId = reg_userAccount.getText().toString();
                String userPwd = reg_userPwd.getText().toString();
                String secondPwd = reg_confirm_userPwd.getText().toString();

                //List<user> all = LitePal.findAll(user.class);

                // 首先验证输入是否为空
                if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(userPwd) || TextUtils.isEmpty(secondPwd)) {
                    // 判断字符串是否为null或者""
                    Toast.makeText(RegisterActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    NotesDatabaseHelper dbHelper=new NotesDatabaseHelper(RegisterActivity.this);
                    SQLiteDatabase  sqliteDatabase = dbHelper.getWritableDatabase();
                    // 判断两次输入的密码是否匹配，匹配则写入数据库，并且结束当前活动，自动返回登录界面
                    if(userPwd.equals(secondPwd)) {
                        Cursor cursor = sqliteDatabase.rawQuery("select * from user where userid=?",new String[]{userId});
                        Cursor cursor1=sqliteDatabase.rawQuery("select * from user",null);
                        if(cursor.getCount()>0)
                        {
                            Toast.makeText(RegisterActivity.this, "当前账号已被注册，请重新输入账号", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // a. 创建ContentValues对象
                            ContentValues values = new ContentValues();
                            // b. 向该对象中插入键值对
                            values.put("userid", userId);
                            values.put("username", "用户"+(cursor1.getCount()+1));
                            values.put("userpassword",secondPwd);
                            values.put("userbirthday","待完善");
                            values.put("usersex","待完善");
                            values.put("userSignature","加油，奥利给！");
                            // c. 插入数据到数据库当中：insert()
                            sqliteDatabase.insert("user", null, values);

                            //关闭数据库
                            sqliteDatabase.close();
                            Intent intent = new Intent();
                            intent.putExtra("register_status", "注册成功");
                            setResult(RESULT_OK, intent);
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }
//                        List<user> userInfoList = LitePal.where("userid = ?", userId).find(user.class);
//                        if(userInfoList.size() > 0) {
//                            Toast.makeText(RegisterActivity.this, "当前账号已被注册，请重新输入账号", Toast.LENGTH_SHORT).show();
//                        } else {
//                            user newuser = new user();
//                            newuser.setUserid(userId);
//                            newuser.setUserpassword(secondPwd);
//                            newuser.setUserbirthday("待完善");
//                            newuser.setUsersex("待完善");
//                            newuser.setUserSignature("这个人很懒，TA什么也没留下。");
//                            // 给其设置一个用户名
//                            newuser.setUsername("用户" + (all.size() + 1));
//                            newuser.save();
//                            System.out.println(newuser);
//                            Intent intent = new Intent();
//                            intent.putExtra("register_status", "注册成功");
//                            setResult(RESULT_OK, intent);
//                            finish();
//                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "请确认输入密码与确认密码是否一致?", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

<<<<<<< HEAD
    //顶部标题栏的返回按钮
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
=======

>>>>>>> c5b774f9aa1bbc59e6b6a973c871e7209e5a9335
}

