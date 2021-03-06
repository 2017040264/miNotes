/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.gtask.remote.GTaskSyncService;
import net.micode.notes.model.WorkingNote;
import net.micode.notes.model.user;
import net.micode.notes.tool.ActivityCollector;
import net.micode.notes.tool.BackupUtils;
import net.micode.notes.tool.BaseActivity;
import net.micode.notes.tool.DataUtils;
import net.micode.notes.tool.ResourceParser;
import net.micode.notes.ui.NotesListAdapter.AppWidgetAttribute;
import net.micode.notes.widget.NoteWidgetProvider_2x;
import net.micode.notes.widget.NoteWidgetProvider_4x;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;
import cn.pedant.SweetAlert.SweetAlertDialog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import net.micode.notes.data.NotesDatabaseHelper;

public class NotesListActivity extends BaseActivity implements OnClickListener, OnItemLongClickListener {
    private static final int FOLDER_NOTE_LIST_QUERY_TOKEN = 0;

    private static final int FOLDER_LIST_QUERY_TOKEN      = 1;

    private static final int MENU_FOLDER_DELETE = 0;

    private static final int MENU_FOLDER_VIEW = 1;

    private static final int MENU_FOLDER_CHANGE_NAME = 2;

    private static final String PREFERENCE_ADD_INTRODUCTION = "net.micode.notes.introduction";



    private enum ListEditState {
        NOTE_LIST, SUB_FOLDER, CALL_RECORD_FOLDER
    };

    private ListEditState mState;

    private BackgroundQueryHandler mBackgroundQueryHandler;

    private NotesListAdapter mNotesListAdapter;

    private ListView mNotesListView;

    //private Button mAddNewNote;

    private FloatingActionButton mAddNewNote;

    // 主界面菜单定义按钮
    //private Button mMenuSet;

    private Toolbar toolbar;

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;

    // 采用静态变量来存储当前登录的账号
    public static String currentUserId="system";

    private TextView userNickName, userSignature;

    private ImageView userAvatar;

    // 记录读者账号，相当于Session来使用
    public String currentUserNickName, currentSignature, currentImagePath;

    private boolean mDispatch;

    private int mOriginY;

    private int mDispatchY;

    private TextView mTitleBar;

    private long mCurrentFolderId;

    private ContentResolver mContentResolver;

    private ModeCallback mModeCallBack;

    private static final String TAG = "NotesListActivity";

    public static final int NOTES_LISTVIEW_SCROLL_RATE = 30;

    private NoteItemData mFocusNoteDataItem;

    private static final String NORMAL_SELECTION = NoteColumns.PARENT_ID + "=?";

    private static final String ROOT_FOLDER_SELECTION = "(" + NoteColumns.TYPE + "<>"
            + Notes.TYPE_SYSTEM + " AND " + NoteColumns.PARENT_ID + "=?)" + " OR ("
            + NoteColumns.ID + "=" + Notes.ID_CALL_RECORD_FOLDER + " AND "
            + NoteColumns.NOTES_COUNT + ">0)";

    private final static int REQUEST_CODE_OPEN_NODE = 102;
    private final static int REQUEST_CODE_NEW_NODE  = 103;

    private NotesDatabaseHelper dbHelper;
    // Activity 启动调用的第一个函数。主要是一些初始化工作，但是不要过于复杂，否则启动过慢，影响用户体验。
    // saveInsanceState是保存的Activity状态，
    // onSaveInsanceState()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //  创建DatabaseHelper对象
        dbHelper=new NotesDatabaseHelper(NotesListActivity.this);

        toolbar = findViewById(R.id.toolbar);

        //注意：只需第一次创建或升级本地数据库，第二次运行就注释掉
        Connector.getDatabase();
        //Toast.makeText(NotesListActivity.this, "创建数据库成功", Toast.LENGTH_LONG).show();

        //获取抽屉布局实例
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //获取菜单控件实例
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //无法直接通过findViewById方法获取header布局id
        View v = navigationView.getHeaderView(0);


        initResources();

        /**
         * Insert an introduction when user firstly use this application
         * raw means orign
         */
        setAppInfoFromRawRes();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK
//                && (requestCode == REQUEST_CODE_OPEN_NODE || requestCode == REQUEST_CODE_NEW_NODE)) {
//            mNotesListAdapter.changeCursor(null);
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

    private void setAppInfoFromRawRes() {
        // SharedPreferences是一种轻量级的数据存储方式，采用键值对的存储方式。
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        // private static final String PREFERENCE_ADD_INTRODUCTION = "net.micode.notes.introduction"
        if (!sp.getBoolean(PREFERENCE_ADD_INTRODUCTION, false)) {


            //StringBuilder类也代表可变字符串对象。
            // 实际上，StringBuilder和StringBuffer基本相似，两个类的构造器和方法也基本相同。
            // 不同的是：StringBuffer是线程安全的，而StringBuilder则没有实现线程安全功能，所以性能略高
            StringBuilder sb = new StringBuilder();

            InputStream in = null;

            try {
                // getResources().openRawResource() 这两个函数都是android的内部函数，用来读取资源
                 in = getResources().openRawResource(R.raw.introduction);

                if (in != null) {

                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(isr);

                    char [] buf = new char[1024];
                    int len = 0;
                    while ((len = br.read(buf)) > 0) {
                        sb.append(buf, 0, len);
                    }
                } else {
                    // private static final String TAG = "NotesListActivity";
                    Log.e(TAG, "Read introduction file error");
                    return;
                }
            } catch (IOException e) {
                //IO exception io异常
                e.printStackTrace();
                return;
            } finally {
                if(in != null) {
                    try {
                        //关闭输入流
                        in.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            // WorkingNote.java文件
            // ID_ROOT_FOLDER = 0
            // INVALID_APPWIDGET_ID = 0
            // TYPE_WIDGET_INVALIDE  = -1,无效的widget,默认不创建widget
             WorkingNote note = WorkingNote.createEmptyNote(this, Notes.ID_ROOT_FOLDER,
                    AppWidgetManager.INVALID_APPWIDGET_ID, Notes.TYPE_WIDGET_INVALIDE,
                    ResourceParser.RED);

            note.setWorkingText(sb.toString());

            if (note.saveNote()) {
                // private static final String PREFERENCE_ADD_INTRODUCTION = "net.micode.notes.introduction";
                // editor.putBoolean(“married”,false); //以键值对形式存储

                // Set a boolean value in the preferences editor, to be written back once commit() or apply() are called.
                // commit方法将添加的数据提交
                sp.edit().putBoolean(PREFERENCE_ADD_INTRODUCTION, true).commit();
            } else {
                Log.e(TAG, "Save introduction note error");
                return;
            }
        }
    } //end setAppInfoFromRawRes

    @Override
    protected void onStart() {
        super.onStart();
        setSupportActionBar(toolbar);

        //获取到ActionBar的实例
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //通过HomeAsUp来让导航按钮显示出来
            actionBar.setDisplayHomeAsUpEnabled(true);
            //设置Indicator来添加一个点击图标（默认图标是一个返回的箭头）
            actionBar.setHomeAsUpIndicator(R.drawable.ic__menu);
        }

        //设置默认选中第一个
        navigationView.setCheckedItem(R.id.nav_edit);
        //设置菜单项的监听事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //逻辑页面处理
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_edit:
                        //每个菜单项的点击事件，通过Intent实现点击item简单实现活动页面的跳转。
                        if (!TextUtils.isEmpty(currentUserId)&&!currentUserId.equals("system")) {
                            Intent editIntent = new Intent(NotesListActivity.this, UserDetailActivity.class);
                            editIntent.putExtra("user_edit_id", currentUserId);
                            startActivityForResult(editIntent, 3);
                        } else {
                            Intent loginIntent = new Intent(NotesListActivity.this, LoginActivity.class);
                            loginIntent.putExtra("loginStatus", "请先登录后才能操作！");
                            startActivityForResult(loginIntent, 1);
                        }
                        break;


                    case R.id.nav_exit:
                        SweetAlertDialog mDialog = new SweetAlertDialog(NotesListActivity.this, SweetAlertDialog.NORMAL_TYPE)
                                .setTitleText("提示")
                                .setContentText("您是否要退出？")
                                .setCustomImage(null)
                                .setCancelText("取消")
                                .setConfirmText("确定")
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                    }
                                }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        ActivityCollector.finishAll();
                                    }
                                });
                        mDialog.show();
                        break;
                    case R.id.nav_switch:
                        // 切换账号
                        Intent intent = new Intent(NotesListActivity.this, LoginActivity.class);
                        // 登录请求码是1
                        startActivityForResult(intent, 1);
                        break;
                    default:
                }
                return true;
            }
        });

        // 加载上次登录的账号，起到记住会话的功能
        String inputText = load();
        if (!TextUtils.isEmpty(inputText) && TextUtils.isEmpty(currentUserId)) {
            currentUserId = inputText;
        }
        View v = navigationView.getHeaderView(0);
        userNickName = v.findViewById(R.id.text_nickname);
        userSignature = v.findViewById(R.id.text_signature);
        userAvatar = v.findViewById(R.id.icon_image);
        if (!TextUtils.isEmpty(currentUserId)) {
            SQLiteDatabase  sqliteDatabase = dbHelper.getWritableDatabase();
            Cursor cursor = sqliteDatabase.rawQuery("select * from user where userid=?",new String[]{currentUserId});
            if(cursor.getCount()>0)
            {
                cursor.moveToFirst();
                //List<user> userInfos = LitePal.where("userid = ?", currentUserId).find(user.class);
                userNickName.setText(cursor.getString(cursor.getColumnIndex("username")));
                userSignature.setText(cursor.getString(cursor.getColumnIndex("userSignature")));
                currentImagePath = cursor.getString(cursor.getColumnIndex("userimagePath"));
                //System.out.println("主界面初始化数据：" + userInfos);
                diplayImage(currentImagePath);
            }
            else {
                userNickName.setText("请先登录");

                userAvatar.setImageResource(R.drawable.user);
            }

        } else {
            userNickName.setText("请先登录");

            userAvatar.setImageResource(R.drawable.user);
        }

        startAsyncNotesListQuery();
    }

    // 加载数据
    public String load() {
        //读取我们之前存储到data文件中的账号，方便下次启动app时直接使用
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput("data");
            System.out.println("是否读到文件内容" + in);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    // 通过登录来接收值
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK
                && (requestCode == REQUEST_CODE_OPEN_NODE || requestCode == REQUEST_CODE_NEW_NODE)) {
            mNotesListAdapter.changeCursor(null);
       } else {
            super.onActivityResult(requestCode, resultCode, data);
            View v = navigationView.getHeaderView(0);

            userNickName = v.findViewById(R.id.text_nickname);
            userSignature = v.findViewById(R.id.text_signature);
            userAvatar = v.findViewById(R.id.icon_image);

            switch (requestCode) {
                case 1: // 切换账号登录后来主界面
                    if (resultCode == RESULT_OK) {
                        Toast.makeText(NotesListActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        currentUserId = data.getStringExtra("userID");
                        currentUserNickName = data.getStringExtra("userNick");
                        currentSignature = data.getStringExtra("userSign");
                        currentImagePath = data.getStringExtra("imagePath");
                        Log.d(TAG, "当前用户的账号为：" + currentUserId);
                        Log.d(TAG, "当前用户的昵称为：" + currentUserNickName);
                        Log.d(TAG, "当前用户的个性签名为： " + currentSignature);
                        Log.d(TAG, "当前用户的头像地址为: " + currentImagePath);
                        userNickName.setText(currentUserNickName);
                        userSignature.setText(currentSignature);
                        diplayImage(currentImagePath);
                    }
                    break;
                case 3: // 从个人信息返回来的数据，要更新导航栏中的数据，包括昵称，签名，图片路径
                    if (resultCode == RESULT_OK) {
                        currentUserNickName = data.getStringExtra("nickName");
                        currentSignature = data.getStringExtra("signature");
                        currentImagePath = data.getStringExtra("imagePath");
                        Log.d(TAG, "当前用户的昵称为：" + currentUserNickName);
                        Log.d(TAG, "当前用户的个性签名为： " + currentSignature);
                        Log.d(TAG, "当前用户的图片路径为： " + currentImagePath);
                        System.out.println("当前用户的图片路径7777777为： " + currentImagePath);
                        userNickName.setText(currentUserNickName);
                        userSignature.setText(currentSignature);
                        diplayImage(currentImagePath);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // 解析、展示图片
    private void diplayImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            userAvatar.setImageBitmap(bitmap);
        } else {
            userAvatar.setImageResource(R.drawable.user);
        }
    }

    // 主要是对变量的一些初始化
    private void initResources() {



        // 使用ContentResolver调用ContentProvider提供的接口，对ContentProvider中的数据进行添加、删除、修改和查询操作时。
        mContentResolver = this.getContentResolver();

        mBackgroundQueryHandler = new BackgroundQueryHandler(this.getContentResolver());

        // long 类型
        mCurrentFolderId = Notes.ID_ROOT_FOLDER;

        // ListView 对应layout\note_ilist.xml的listview
        mNotesListView = (ListView) findViewById(R.id.notes_list);

        mNotesListView.addFooterView(LayoutInflater.from(this).inflate(R.layout.note_list_footer, null),
                null, false);

        // 参数是个监听器
        mNotesListView.setOnItemClickListener(new OnListItemClickListener());
        // this代表当前类，在这里指的就是：setOnItemLongClickListener()
        mNotesListView.setOnItemLongClickListener(this);

        // 布局适配器
        mNotesListAdapter = new NotesListAdapter(this);
        mNotesListView.setAdapter(mNotesListAdapter);

        // 添加note按钮 FloatingActionButton
        mAddNewNote =(FloatingActionButton) findViewById(R.id.fab1);
        //mAddNewNote =(Button) findViewById(R.id.fab);
        mAddNewNote.setOnClickListener(this);
        mAddNewNote.setOnTouchListener(new NewNoteOnTouchListener());



        // 主页菜单按钮初始化，通过id绑定note_list中的button
        //mMenuSet = (Button) findViewById(R.id.btn_set);

        mDispatch = false;
        mDispatchY = 0;
        mOriginY = 0;

        // textview  layout\note_list.xml中的TextView组件
        mTitleBar = (TextView) findViewById(R.id.tv_title_bar);

        // ListEditState有三种参数（状态）
        mState = ListEditState.NOTE_LIST;

        //  多选操作？？
        mModeCallBack = new ModeCallback();
    }

    private class ModeCallback implements ListView.MultiChoiceModeListener, OnMenuItemClickListener {
        private DropdownMenu mDropDownMenu;
        private ActionMode mActionMode;
        private Menu menu;
        private MenuItem mMoveMenu;

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.note_list_options, menu);
            this.menu = menu;
            menu.findItem(R.id.delete).setOnMenuItemClickListener(this);
            mMoveMenu = menu.findItem(R.id.move);
            if (mFocusNoteDataItem.getParentId() == Notes.ID_CALL_RECORD_FOLDER
                    || DataUtils.getUserFolderCount(mContentResolver) == 0) {
                mMoveMenu.setVisible(false);
            } else {
                mMoveMenu.setVisible(true);
                mMoveMenu.setOnMenuItemClickListener(this);
            }
            mActionMode = mode;
            mNotesListAdapter.setChoiceMode(true);
            mNotesListView.setLongClickable(false);
            mAddNewNote.setVisibility(View.GONE);

            //设置按钮隐藏，布局文件中也要android:visibility="gone"，否则无效
           // mMenuSet.setVisibility(View.GONE);

            View customView = LayoutInflater.from(NotesListActivity.this).inflate(
                    R.layout.note_list_dropdown_menu, null);
            mode.setCustomView(customView);
            mDropDownMenu = new DropdownMenu(NotesListActivity.this,
                    (Button) customView.findViewById(R.id.selection_menu),
                    R.menu.note_list_dropdown);
            mDropDownMenu.setOnDropdownMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                public boolean onMenuItemClick(MenuItem item) {
                    mNotesListAdapter.selectAll(!mNotesListAdapter.isAllSelected());
                    updateMenu();
                    return true;
                }

            });
            return true;
        }

        private void updateMenu() {
            int selectedCount = mNotesListAdapter.getSelectedCount();
            // Update dropdown menu
            String format = getResources().getString(R.string.menu_select_title, selectedCount);
            mDropDownMenu.setTitle(format);
            MenuItem item = mDropDownMenu.findItem(R.id.action_select_all);
            if (item != null) {
                if (mNotesListAdapter.isAllSelected()) {
                    item.setChecked(true);
                    item.setTitle(R.string.menu_deselect_all);
                } else {
                    item.setChecked(false);
                    item.setTitle(R.string.menu_select_all);
                }
            }
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // TODO Auto-generated method stub
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            mNotesListAdapter.setChoiceMode(false);
            mNotesListView.clearChoices();
            mNotesListView.setItemChecked(0,true);
            mNotesListView.setItemChecked(0,false);
            
            mNotesListView.setLongClickable(true);
            System.out.println("-----------------onDestroyActionMode------------------");
            mNotesListAdapter.notifyDataSetChanged();
//            closeOptionsMenu();
//            mNotesListView.invalidate();
            mAddNewNote.setVisibility(View.VISIBLE);

            // 设置按钮可见
           // mMenuSet.setVisibility(View.VISIBLE);
            
            
            
//            mNotesListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            
//            menu.clear();
//            menu.close();
        }
        
        

        public void finishActionMode() {
            mActionMode.finish();
            mActionMode = null;
            mNotesListAdapter.setChoiceMode(false);
            mNotesListView.setLongClickable(true);
        }
        
        

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {
            mNotesListAdapter.setCheckedItem(position, checked);
            updateMenu();
        }

        public boolean onMenuItemClick(MenuItem item) {
            if (mNotesListAdapter.getSelectedCount() == 0) {
                Toast.makeText(NotesListActivity.this, getString(R.string.menu_select_none),
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            switch (item.getItemId()) {
                case R.id.delete:
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                    builder.setTitle(getString(R.string.alert_title_delete));
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setMessage(getString(R.string.alert_message_delete_notes,
                                             mNotesListAdapter.getSelectedCount()));
                    builder.setPositiveButton(android.R.string.ok,
                                             new DialogInterface.OnClickListener() {
                                                 public void onClick(DialogInterface dialog,
                                                         int which) {
                                                     batchDelete();
                                                 }
                                             });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.show();
                    break;
                case R.id.move:
                    startQueryDestinationFolders();
                    break;
                default:
                    return false;
            }
            return true;
        }
    }
    

    private class NewNoteOnTouchListener implements OnTouchListener {

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {// 当屏幕检测到第一个触点按下之后就会触发到这个事件

                    // 得到窗口
                    Display display = getWindowManager().getDefaultDisplay();
                    // 整个窗口的高度，包括状态栏、标题栏和布局
                    int screenHeight = display.getHeight();
                    // 获得悬浮按钮控件的高度
                    int newNoteViewHeight = mAddNewNote.getHeight();
                    int start = screenHeight - newNoteViewHeight;
                    int eventY = start + (int) event.getY();
                    /**
                     * Minus TitleBar's height
                     */
                    if (mState == ListEditState.SUB_FOLDER) {
                        eventY -= mTitleBar.getHeight();
                        start -= mTitleBar.getHeight();
                    }
                    /**
                     * HACKME:When click the transparent part of "New Note" button, dispatch
                     * the event to the list view behind this button. The transparent part of
                     * "New Note" button could be expressed by formula y=-0.12x+94（Unit:pixel）
                     * and the line top of the button. The coordinate based on left of the "New
                     * Note" button. The 94 represents maximum height of the transparent part.
                     * Notice that, if the background of the button changes, the formula should
                     * also change. This is very bad, just for the UI designer's strong requirement.
                     */
                    if (event.getY() < (event.getX() * (-0.12) + 94)) {
                        View view = mNotesListView.getChildAt(mNotesListView.getChildCount() - 1
                                - mNotesListView.getFooterViewsCount());
                        if (view != null && view.getBottom() > start
                                && (view.getTop() < (start + 94))) {
                            mOriginY = (int) event.getY();
                            mDispatchY = eventY;
                            event.setLocation(event.getX(), mDispatchY);
                            mDispatch = true;
                            return mNotesListView.dispatchTouchEvent(event);
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    //当触点在屏幕上移动时触发，触点在屏幕上停留也是会触发的，
                    // 主要是由于它的灵敏度很高，
                    // 而我们的手指又不可能完全静止（即使我们感觉不到移动，但其实我们的手指也在不停地抖动）
                    if (mDispatch) {
                        mDispatchY += (int) event.getY() - mOriginY;
                        event.setLocation(event.getX(), mDispatchY);
                        return mNotesListView.dispatchTouchEvent(event);
                    }
                    break;
                }
                default: {
                    if (mDispatch) {
                        event.setLocation(event.getX(), mDispatchY);
                        mDispatch = false;
                        return mNotesListView.dispatchTouchEvent(event);
                    }
                    break;
                }
            }
            return false;
        }

    };

    private void startAsyncNotesListQuery() {
        String selection = (mCurrentFolderId == Notes.ID_ROOT_FOLDER) ? ROOT_FOLDER_SELECTION
                : NORMAL_SELECTION;
        mBackgroundQueryHandler.startQuery(FOLDER_NOTE_LIST_QUERY_TOKEN, null,
                Notes.CONTENT_NOTE_URI, NoteItemData.PROJECTION, selection, new String[] {
                    String.valueOf(mCurrentFolderId)
                }, NoteColumns.TYPE + " DESC," + NoteColumns.MODIFIED_DATE + " DESC");
    }

    private final class BackgroundQueryHandler extends AsyncQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case FOLDER_NOTE_LIST_QUERY_TOKEN:
                    mNotesListAdapter.changeCursor(cursor);
                    break;
                case FOLDER_LIST_QUERY_TOKEN:
                    if (cursor != null && cursor.getCount() > 0) {
                        showFolderListMenu(cursor);
                    } else {
                        Log.e(TAG, "Query folder failed");
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private void showFolderListMenu(Cursor cursor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
        builder.setTitle(R.string.menu_title_select_folder);
        final FoldersListAdapter adapter = new FoldersListAdapter(this, cursor);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                DataUtils.batchMoveToFolder(mContentResolver,
                        mNotesListAdapter.getSelectedItemIds(), adapter.getItemId(which));
                Toast.makeText(
                        NotesListActivity.this,
                        getString(R.string.format_move_notes_to_folder,
                                mNotesListAdapter.getSelectedCount(),
                                adapter.getFolderName(NotesListActivity.this, which)),
                        Toast.LENGTH_SHORT).show();
                mModeCallBack.finishActionMode();
            }
        });
        builder.show();
    }

    private void createNewNote() {
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(Notes.INTENT_EXTRA_FOLDER_ID, mCurrentFolderId);
        this.startActivityForResult(intent, REQUEST_CODE_NEW_NODE);
    }

    private void batchDelete() {
        new AsyncTask<Void, Void, HashSet<AppWidgetAttribute>>() {
            protected HashSet<AppWidgetAttribute> doInBackground(Void... unused) {
                HashSet<AppWidgetAttribute> widgets = mNotesListAdapter.getSelectedWidget();
                if (!isSyncMode()) {
                    // if not synced, delete notes directly
                    if (DataUtils.batchDeleteNotes(mContentResolver, mNotesListAdapter
                            .getSelectedItemIds())) {
                    } else {
                        Log.e(TAG, "Delete notes error, should not happens");
                    }
                } else {
                    // in sync mode, we'll move the deleted note into the trash
                    // folder
                    if (!DataUtils.batchMoveToFolder(mContentResolver, mNotesListAdapter
                            .getSelectedItemIds(), Notes.ID_TRASH_FOLER)) {
                        Log.e(TAG, "Move notes to trash folder error, should not happens");
                    }
                }
                return widgets;
            }

            @Override
            protected void onPostExecute(HashSet<AppWidgetAttribute> widgets) {
                if (widgets != null) {
                    for (AppWidgetAttribute widget : widgets) {
                        if (widget.widgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                                && widget.widgetType != Notes.TYPE_WIDGET_INVALIDE) {
                            updateWidget(widget.widgetId, widget.widgetType);
                        }
                    }
                }
                mModeCallBack.finishActionMode();
            }
        }.execute();
    }

    private void deleteFolder(long folderId) {
        if (folderId == Notes.ID_ROOT_FOLDER) {
            Log.e(TAG, "Wrong folder id, should not happen " + folderId);
            return;
        }

        HashSet<Long> ids = new HashSet<Long>();
        ids.add(folderId);
        HashSet<AppWidgetAttribute> widgets = DataUtils.getFolderNoteWidget(mContentResolver,
                folderId);
        if (!isSyncMode()) {
            // if not synced, delete folder directly
            DataUtils.batchDeleteNotes(mContentResolver, ids);
        } else {
            // in sync mode, we'll move the deleted folder into the trash folder
            DataUtils.batchMoveToFolder(mContentResolver, ids, Notes.ID_TRASH_FOLER);
        }
        if (widgets != null) {
            for (AppWidgetAttribute widget : widgets) {
                if (widget.widgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                        && widget.widgetType != Notes.TYPE_WIDGET_INVALIDE) {
                    updateWidget(widget.widgetId, widget.widgetType);
                }
            }
        }
    }

    private void openNode(NoteItemData data) {
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_UID, data.getId());
        this.startActivityForResult(intent, REQUEST_CODE_OPEN_NODE);
    }

    private void openFolder(NoteItemData data) {
        mCurrentFolderId = data.getId();
        startAsyncNotesListQuery();
        if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
            mState = ListEditState.CALL_RECORD_FOLDER;
            mAddNewNote.setVisibility(View.GONE);
            //隐藏
          //  mMenuSet.setVisibility(View.GONE);

        } else {
            mState = ListEditState.SUB_FOLDER;
        }
        if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
            mTitleBar.setText(R.string.call_record_folder_name);
        } else {
            mTitleBar.setText(data.getSnippet());
        }
        mTitleBar.setVisibility(View.VISIBLE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab1:
                createNewNote();
                break;
            default:
                break;
        }
    }

    private void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void hideSoftInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showCreateOrModifyFolderDialog(final boolean create) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        final EditText etName = (EditText) view.findViewById(R.id.et_foler_name);
        showSoftInput();
        if (!create) {
            if (mFocusNoteDataItem != null) {
                etName.setText(mFocusNoteDataItem.getSnippet());
                builder.setTitle(getString(R.string.menu_folder_change_name));
            } else {
                Log.e(TAG, "The long click data item is null");
                return;
            }
        } else {
            etName.setText("");
            builder.setTitle(this.getString(R.string.menu_create_folder));
        }

        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                hideSoftInput(etName);
            }
        });

        final Dialog dialog = builder.setView(view).show();
        final Button positive = (Button)dialog.findViewById(android.R.id.button1);
        positive.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                hideSoftInput(etName);
                String name = etName.getText().toString();

                //如果已经存在同名的文件夹
                if (DataUtils.checkVisibleFolderName(mContentResolver, name)) {
                    Toast.makeText(NotesListActivity.this, getString(R.string.folder_exist, name),
                            Toast.LENGTH_LONG).show();
                    etName.setSelection(0, etName.length());
                    return;
                }
                if (!create) {
                    if (!TextUtils.isEmpty(name)) {
                        ContentValues values = new ContentValues();
                        values.put(NoteColumns.SNIPPET, name);
                        values.put(NoteColumns.TYPE, Notes.TYPE_FOLDER);
                        values.put(NoteColumns.LOCAL_MODIFIED, 1);
                        mContentResolver.update(Notes.CONTENT_NOTE_URI, values, NoteColumns.ID
                                + "=?", new String[] {
                            String.valueOf(mFocusNoteDataItem.getId())
                        });
                    }
                } else if (!TextUtils.isEmpty(name)) {
                    ContentValues values = new ContentValues();
                    values.put(NoteColumns.SNIPPET, name);
                    values.put(NoteColumns.TYPE, Notes.TYPE_FOLDER);
                    mContentResolver.insert(Notes.CONTENT_NOTE_URI, values);
                }
                dialog.dismiss();
            }
        });

        if (TextUtils.isEmpty(etName.getText())) {
            positive.setEnabled(false);
        }
        /**
         * When the name edit text is null, disable the positive button
         */
        etName.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(etName.getText())) {
                    positive.setEnabled(false);
                } else {
                    positive.setEnabled(true);
                }
            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onBackPressed() {

    	System.out.println("-------onBackPressed---00000");
        switch (mState) {
            case SUB_FOLDER:
                mCurrentFolderId = Notes.ID_ROOT_FOLDER;
                mState = ListEditState.NOTE_LIST;
                startAsyncNotesListQuery();
                mTitleBar.setVisibility(View.GONE);
                break;
            case CALL_RECORD_FOLDER:
                mCurrentFolderId = Notes.ID_ROOT_FOLDER;
                mState = ListEditState.NOTE_LIST;
                mAddNewNote.setVisibility(View.VISIBLE);
                //可见。
                //mMenuSet.setVisibility(View.VISIBLE);

                mTitleBar.setVisibility(View.GONE);
                startAsyncNotesListQuery();
                break;
            case NOTE_LIST:
            	System.out.println("-------onBackPressed---");
                super.onBackPressed();
                break;
            default:
                break;
        }
    }

    private void updateWidget(int appWidgetId, int appWidgetType) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        if (appWidgetType == Notes.TYPE_WIDGET_2X) {
            intent.setClass(this, NoteWidgetProvider_2x.class);
        } else if (appWidgetType == Notes.TYPE_WIDGET_4X) {
            intent.setClass(this, NoteWidgetProvider_4x.class);
        } else {
            Log.e(TAG, "Unspported widget type");
            return;
        }

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
            appWidgetId
        });

        sendBroadcast(intent);
        setResult(RESULT_OK, intent);
    }

    private final OnCreateContextMenuListener mFolderOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (mFocusNoteDataItem != null) {
                menu.setHeaderTitle(mFocusNoteDataItem.getSnippet());
                menu.add(0, MENU_FOLDER_VIEW, 0, R.string.menu_folder_view);
                menu.add(0, MENU_FOLDER_DELETE, 0, R.string.menu_folder_delete);
                menu.add(0, MENU_FOLDER_CHANGE_NAME, 0, R.string.menu_folder_change_name);
            }
        }
    };

    @Override
    public void onContextMenuClosed(Menu menu) {
        if (mNotesListView != null) {
            mNotesListView.setOnCreateContextMenuListener(null);
        }
        super.onContextMenuClosed(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mFocusNoteDataItem == null) {
            Log.e(TAG, "The long click data item is null");
            return false;
        }
        switch (item.getItemId()) {
            case MENU_FOLDER_VIEW:
                openFolder(mFocusNoteDataItem);
                break;
            case MENU_FOLDER_DELETE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.alert_title_delete));
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(getString(R.string.alert_message_delete_folder));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFolder(mFocusNoteDataItem.getId());
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
                break;
            case MENU_FOLDER_CHANGE_NAME:
                showCreateOrModifyFolderDialog(false);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mState == ListEditState.NOTE_LIST) {
            getMenuInflater().inflate(R.menu.note_list, menu);
            // set sync or sync_cancel
            menu.findItem(R.id.menu_sync).setTitle(
                    GTaskSyncService.isSyncing() ? R.string.menu_sync_cancel : R.string.menu_sync);
        } else if (mState == ListEditState.SUB_FOLDER) {
            getMenuInflater().inflate(R.menu.sub_folder, menu);
        } else if (mState == ListEditState.CALL_RECORD_FOLDER) {
            getMenuInflater().inflate(R.menu.call_record_folder, menu);
        } else {
            Log.e(TAG, "Wrong state:" + mState);
        }
        return true;
    }

    /*左边菜单实现*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                //打开侧滑栏，注意要与xml中保持一致START
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            }
            case R.id.menu_new_folder: {
                showCreateOrModifyFolderDialog(true);
                break;
            }
            case R.id.menu_export_text: {
                exportNoteToText();
                break;
            }
            case R.id.menu_sync: {
                if (isSyncMode()) {
                    if (TextUtils.equals(item.getTitle(), getString(R.string.menu_sync))) {
                        GTaskSyncService.startSync(this);
                    } else {
                        GTaskSyncService.cancelSync(this);
                    }
                } else {
                    startPreferenceActivity();
                }
                break;
            }
            case R.id.menu_setting: {
                startPreferenceActivity();
                break;
            }
            case R.id.menu_new_note: {
                createNewNote();
                break;
            }
            case R.id.menu_search:
                onSearchRequested();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null /* appData */, false);
        return true;
    }

    private void exportNoteToText() {
        final BackupUtils backup = BackupUtils.getInstance(NotesListActivity.this);
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... unused) {
                return backup.exportToText();
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result == BackupUtils.STATE_SD_CARD_UNMOUONTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                    builder.setTitle(NotesListActivity.this
                            .getString(R.string.failed_sdcard_export));
                    builder.setMessage(NotesListActivity.this
                            .getString(R.string.error_sdcard_unmounted));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                } else if (result == BackupUtils.STATE_SUCCESS) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                    builder.setTitle(NotesListActivity.this
                            .getString(R.string.success_sdcard_export));
                    builder.setMessage(NotesListActivity.this.getString(
                            R.string.format_exported_file_location, backup
                                    .getExportedTextFileName(), backup.getExportedTextFileDir()));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                } else if (result == BackupUtils.STATE_SYSTEM_ERROR) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                    builder.setTitle(NotesListActivity.this
                            .getString(R.string.failed_sdcard_export));
                    builder.setMessage(NotesListActivity.this
                            .getString(R.string.error_sdcard_export));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
            }

        }.execute();
    }

    private boolean isSyncMode() {
        return NotesPreferenceActivity.getSyncAccountName(this).trim().length() > 0;
    }



    private void startPreferenceActivity() {
        Activity from = getParent() != null ? getParent() : this;
        Intent intent = new Intent(from, NotesPreferenceActivity.class);
        from.startActivityIfNeeded(intent, -1);
    }





    private class OnListItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (view instanceof NotesListItem) {
                NoteItemData item = ((NotesListItem) view).getItemData();
                if (mNotesListAdapter.isInChoiceMode()) {
                    if (item.getType() == Notes.TYPE_NOTE) {
                        position = position - mNotesListView.getHeaderViewsCount();
                        mModeCallBack.onItemCheckedStateChanged(null, position, id,
                                !mNotesListAdapter.isSelectedItem(position));
                    }
                    return;
                }

                switch (mState) {
                    case NOTE_LIST:
                        if (item.getType() == Notes.TYPE_FOLDER
                                || item.getType() == Notes.TYPE_SYSTEM) {
                            openFolder(item);
                        } else if (item.getType() == Notes.TYPE_NOTE) {
                            openNode(item);
                        } else {
                            Log.e(TAG, "Wrong note type in NOTE_LIST");
                        }
                        break;
                    case SUB_FOLDER:
                    case CALL_RECORD_FOLDER:
                        if (item.getType() == Notes.TYPE_NOTE) {
                            openNode(item);
                        } else {
                            Log.e(TAG, "Wrong note type in SUB_FOLDER");
                        }
                        break;
                    default:
                        break;
                }
            }
        }

    }
    /*查询目标文件夹*/
    private void startQueryDestinationFolders() {
        String selection = NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>? AND " + NoteColumns.ID + "<>?";
        selection = (mState == ListEditState.NOTE_LIST) ? selection:
            "(" + selection + ") OR (" + NoteColumns.ID + "=" + Notes.ID_ROOT_FOLDER + ")";

        mBackgroundQueryHandler.startQuery(FOLDER_LIST_QUERY_TOKEN,
                null,
                Notes.CONTENT_NOTE_URI,
                FoldersListAdapter.PROJECTION,
                selection,
                new String[] {
                        String.valueOf(Notes.TYPE_FOLDER),
                        String.valueOf(Notes.ID_TRASH_FOLER),
                        String.valueOf(mCurrentFolderId)
                },
                NoteColumns.MODIFIED_DATE + " DESC");
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof NotesListItem) {
            mFocusNoteDataItem = ((NotesListItem) view).getItemData();
            if (mFocusNoteDataItem.getType() == Notes.TYPE_NOTE && !mNotesListAdapter.isInChoiceMode()) {
                if (mNotesListView.startActionMode(mModeCallBack) != null) {
                    mModeCallBack.onItemCheckedStateChanged(null, position, id, true);
                    mNotesListView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                } else {
                    Log.e(TAG, "startActionMode fails");
                }
            } else if (mFocusNoteDataItem.getType() == Notes.TYPE_FOLDER) {
                mNotesListView.setOnCreateContextMenuListener(mFolderOnCreateContextMenuListener);
            }
        }
        return false;
    }


//    public void OnOpenMenu(View view) {
//		openOptionsMenu();
//	}
    /*右上角菜单按钮*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_list, menu);
        return true;
    }
}
