package net.micode.notes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.micode.notes.R;
import net.micode.notes.model.user;


import org.litepal.LitePal;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

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

                List<user> all = LitePal.findAll(user.class);

                // 首先验证输入是否为空
                if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(userPwd) || TextUtils.isEmpty(secondPwd)) {
                    // 判断字符串是否为null或者""
                    Toast.makeText(RegisterActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    // 判断两次输入的密码是否匹配，匹配则写入数据库，并且结束当前活动，自动返回登录界面
                    if(userPwd.equals(secondPwd)) {
                        List<user> userInfoList = LitePal.where("userid = ?", userId).find(user.class);
                        if(userInfoList.size() > 0) {
                            Toast.makeText(RegisterActivity.this, "当前账号已被注册，请重新输入账号", Toast.LENGTH_SHORT).show();
                        } else {
                            user newuser = new user();
                            newuser.setUserid(userId);
                            newuser.setUserpassword(secondPwd);
                            newuser.setUserbirthday("待完善");
                            newuser.setUsersex("待完善");
                            newuser.setUserSignature("这个人很懒，TA什么也没留下。");
                            // 给其设置一个用户名
                            newuser.setUsername("用户" + (all.size() + 1));
                            newuser.save();
                            System.out.println(newuser);
                            Intent intent = new Intent();
                            intent.putExtra("register_status", "注册成功");
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "请确认输入密码与确认密码是否一致?", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}

