# 小米便签数据库分析

### note表：

```
该表存储的是所有note和folder的信息。
parent_id=0,type=0:表示主界面的note
parent_id=0,type=1:表示主界面的folder(folder下无folder)
parent_id!=0,type=0:表示folder下的note
type=2的是系统文件
```

SQL语句：

```sqlite
private static final String CREATE_NOTE_TABLE_SQL =
        "CREATE TABLE " + TABLE.NOTE + "(" +
            NoteColumns.ID + " INTEGER PRIMARY KEY," +
            NoteColumns.PARENT_ID + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.ALERTED_DATE + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.BG_COLOR_ID + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
            NoteColumns.HAS_ATTACHMENT + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
            NoteColumns.NOTES_COUNT + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.SNIPPET + " TEXT NOT NULL DEFAULT ''," +
            NoteColumns.TYPE + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.WIDGET_ID + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.WIDGET_TYPE + " INTEGER NOT NULL DEFAULT -1," +
            NoteColumns.SYNC_ID + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.LOCAL_MODIFIED + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.ORIGIN_PARENT_ID + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.GTASK_ID + " TEXT NOT NULL DEFAULT ''," +
            NoteColumns.VERSION + " INTEGER NOT NULL DEFAULT 0" +
        ")";
```

| 名称             | 类型   | 含义                                                         |
| ---------------- | ------ | ------------------------------------------------------------ |
| _id              | long   | The unique ID for a row                                      |
| parent_id        | long   | The parent's id for note or folder                           |
| alert_data       | long   | Alert date                                                   |
| bg_color_id      | long   | Note's background color's id                                 |
| created_date     | long   | Created data for note or folder                              |
| has_attachment   | long   | For text note, it doesn't has attachment, for multi-media note, it has at least one attachment |
| modified_date    | long   | Latest modified date                                         |
| notes_count      | long   | Folder's count of notes                                      |
| snippet          | string | Folder's name or text content of note                        |
| type             | long   | The file type: folder or note                                |
| widget_id        | long   | Note's widget id                                             |
| widget_type      | long   | Note's widget type                                           |
| sync_id          | long   | The last sync id                                             |
| local_modified   | long   | Sign to indicate local modified or not                       |
| origin_parent_id | long   | Original parent id before moving into temporary folder       |
| gtask_id         | String | The gtask id                                                 |
| version          | long   | The version code                                             |

parent_id=0表示在主界面; 若在folder下，则为folder的id

type=0表示是note，type=1表示是个folder

### data表：

```
该表存储的是所有note的信息
```

SQL语句：

```sql
private static final String CREATE_DATA_TABLE_SQL =
        "CREATE TABLE " + TABLE.DATA + "(" +
            DataColumns.ID + " INTEGER PRIMARY KEY," +
            DataColumns.MIME_TYPE + " TEXT NOT NULL," +
            DataColumns.NOTE_ID + " INTEGER NOT NULL DEFAULT 0," +
            NoteColumns.CREATED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
            NoteColumns.MODIFIED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)," +
            DataColumns.CONTENT + " TEXT NOT NULL DEFAULT ''," +
            DataColumns.DATA1 + " INTEGER," +
            DataColumns.DATA2 + " INTEGER," +
            DataColumns.DATA3 + " TEXT NOT NULL DEFAULT ''," +
            DataColumns.DATA4 + " TEXT NOT NULL DEFAULT ''," +
            DataColumns.DATA5 + " TEXT NOT NULL DEFAULT ''" +
        ")";
```

| 名称          | 类型   | 含义                                                         |
| ------------- | ------ | ------------------------------------------------------------ |
| _id           | long   | The unique ID for a row                                      |
| mime_type     | String | The MIME type of the item represented by this row  此行表示的项的MIME类型 |
| note_id       | long   | The reference id to note that this data belongs to(Note表中的id) |
| created_date  | long   | Created data for note or folder                              |
| modified_date | long   | Latest modified date                                         |
| content       | String | Data's content                                               |
| data1         | long   | Generic data column(通用数据列), the meaning is {@link #MIME_TYPE} specific, used for   integer data type |
| data2         | long   | Generic data column, the meaning is {@link #MIME_TYPE} specific, used for  integer data type |
| data3         | string | Generic data column, the meaning is {@link #MIME_TYPE} specific, used for  TEXT data type |
| data4         | string | Generic data column, the meaning is {@link #MIME_TYPE} specific, used for  TEXT data type |
| data5         | string | Generic data column, the meaning is {@link #MIME_TYPE} specific, used for  TEXT data type |

```
MIME:Multipurpose Internet Mail Extensions  多用途互联网邮件扩展类型
```

litepal支持的数据类型:int、short、long、float、double、boolean、String和Date

上述两个表中的“_id”，在litepal中可以自动创建，也就不再声明。添加“userid”字段，表示属于哪个用户。

-----------------------------------------------------------------------------------------------------------------------------------------------------

倘若自己建表：

user:用户的个人信息

folder:用户标识，folder标识（parent_id,文件夹嵌套文件夹、为0则在主界面）、folder属性

note:用户标识、folder标识（parent_id,文件夹下的note、为0则在主界面）、note的属性

源代码中的建表现在有些地方不清楚，但是应该比我想的好吧。迫于实力，跟着源码中的来吧，尽量尝试将数据库从SQLite改为Ltepal.







