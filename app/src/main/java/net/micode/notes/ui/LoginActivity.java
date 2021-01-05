package net.micode.notes.ui;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.micode.notes.R;
import net.micode.notes.model.user;

import org.litepal.LitePal;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // 用户账号，也就是id
    private EditText userAccount;
    // 用户密码
    private EditText userPwd;
    // 登录按钮
    private Button loginBtn;
    // 注册按钮
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 根据id找值
        userAccount = findViewById(R.id.login_userAccount);
        userPwd = findViewById(R.id.login_pwd);

        // 在点击编辑资料时，提醒先登录
        Intent intent = getIntent();
        String status = intent.getStringExtra("loginStatus");
        if(status != null) {
            Toast.makeText(LoginActivity.this, status, Toast.LENGTH_SHORT).show();
        }

        loginBtn = findViewById(R.id.login_on);
        registerBtn = findViewById(R.id.login_register);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numId = userAccount.getText().toString();
                String pwd = userPwd.getText().toString();
                // 先判断输入不能为空
                if(TextUtils.isEmpty(numId) || TextUtils.isEmpty(pwd)) {
                    Toast.makeText(LoginActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    //此处用litepal去查询数据库，查询用户输入的账号和密码是否登录成功，其中账号是唯一标识
                    List<user> userInfos = LitePal.where("userAccount = ?", numId).find(user.class);
                    System.out.println(userInfos);
                    Log.d("登录界面", "onClick: " + userInfos);
                    if(userInfos.size() == 0) {
                        // 提示用户注册
                        Toast.makeText(LoginActivity.this, "账号不存在，请先注册！", Toast.LENGTH_SHORT).show();
                    } else {
                        // 验证密码是否正确
                        if(!pwd.equals(userInfos.get(0).getUserpassword())) {
                            Toast.makeText(LoginActivity.this, "请确认是否输入正确的密码?", Toast.LENGTH_SHORT).show();
                        } else {
                            // 登录成功，返回到主界面，主界面要保存登录的账号，便于查询读者信息，主界面使用onActivityResult来接收得到的账号
                            Intent intent = new Intent(LoginActivity.this, NotesListActivity.class);
                            intent.putExtra("userID", numId);
                            intent.putExtra("userNick", userInfos.get(0).getUsername());
                            intent.putExtra("userSign", userInfos.get(0).getUserSignature());
                            intent.putExtra("imagePath", userInfos.get(0).getUserimagePath());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //此处跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                // 注册请求码是2
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                if(resultCode == RESULT_OK) {
                    Toast.makeText(LoginActivity.this, data.getStringExtra("register_status"), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 登录界面销毁时存储账号
        String inputIdText = userAccount.getText().toString();
        save(inputIdText);
        System.out.println("活动毁灭之前是否传值" + inputIdText);
    }

    // 存储账号，方便下次启动app时，直接读取账号，并初始化数据
    public void save(String inputText) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

