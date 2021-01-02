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

package net.micode.notes.model;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;

import java.util.ArrayList;


public class Note {
    // ContenrValues 是Andriod内部类
    // ContentValues 和HashTable类似都是一种存储的机制 但是两者最大的区别就在于，
    // contenvalues只能存储基本类型的数据，像string，int之类的，不能存储对象这种东西，而HashTable却可以存储对象。
    private ContentValues mNoteDiffValues;

    // NoteData Note的内部类
    private NoteData mNoteData;

    private static final String TAG = "Note";

    /**
     * Create a new note id for adding a new note to databases
     * synchronized是Java中的关键字，是一种同步锁
     * 修改一个静态的方法，其作用的范围是整个静态方法，作用的对象是这个类的所有对象
     */
    public static synchronized long getNewNoteId(Context context, long folderId) {
        // Create a new note in the database
        // 在往数据库中插入数据的时候，首先应该有一个ContentValues的对象
        ContentValues values = new ContentValues();

        // 获得系统时间，单位为毫秒
        long createdTime = System.currentTimeMillis();

        // Created data for note or folder
        values.put(NoteColumns.CREATED_DATE, createdTime);

        // Latest modified(调整) date
        values.put(NoteColumns.MODIFIED_DATE, createdTime);

        // The file type: folder or note
        // TYPE_NOTE,TYPE_FOLDER,TYPE_SYSTEM 三种
        values.put(NoteColumns.TYPE, Notes.TYPE_NOTE);

        // Sign to indicate local modified or not 是否局部修改标志
        values.put(NoteColumns.LOCAL_MODIFIED, 1);

        // The parent's id for note or folder 父类的id
        values.put(NoteColumns.PARENT_ID, folderId);

        // Uri to query all notes and folders
        Uri uri = context.getContentResolver().insert(Notes.CONTENT_NOTE_URI, values);

        long noteId = 0;
        try {
            // uri.getPathSegments() : Gets the decoded path segments. Lsit<String>类型
            // Long.valueOf(参数)是将参数转换成long的包装类——Long；也就是把基本数据类型转换成包装类
            noteId = Long.valueOf(uri.getPathSegments().get(1));
        } catch (NumberFormatException e) {
            // tring TAG = "Note"
            Log.e(TAG, "Get note id error :" + e.toString());
            noteId = 0;
        }

        if (noteId == -1) {
            throw new IllegalStateException("Wrong note id:" + noteId);
        }

        return noteId;
    } // end getNewNoteId


    // 构造类
    public Note() {

        // android 内部类，是一个ArrayMap类型
        // 存储数据
        mNoteDiffValues = new ContentValues();

        mNoteData = new NoteData();
    }


    public void setNoteValue(String key, String value) {
        mNoteDiffValues.put(key, value);
        mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
        mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
    }

    public void setTextData(String key, String value) {
        mNoteData.setTextData(key, value);
    }

    public void setTextDataId(long id) {
        mNoteData.setTextDataId(id);
    }

    public long getTextDataId() {
        return mNoteData.mTextDataId;
    }

    public void setCallDataId(long id) {
        mNoteData.setCallDataId(id);
    }

    public void setCallData(String key, String value) {
        mNoteData.setCallData(key, value);
    }

    // 这个函数存在递归？没有。
    // return 中的mNoteData.isLocalModified()函数是NoreData类的，而该函数属于Note类
    public boolean isLocalModified() {
        return mNoteDiffValues.size() > 0 || mNoteData.isLocalModified();
    }

    /**
     * 该函数用于同步Note
     * @param context
     * @param noteId
     * @return boolean
     */

    public boolean syncNote(Context context, long noteId) {

        if (noteId <= 0) {
            throw new IllegalArgumentException("Wrong note id:" + noteId);
        }

        // 未修改，不用同步，返回true
        if (!isLocalModified()) {
            return true;
        }

        /**
         * In theory(理论上), once data changed, the note should be updated on {@link NoteColumns#LOCAL_MODIFIED} and
         * {@link NoteColumns#MODIFIED_DATE}. For data safety, though update note fails, we also update the
         * note data info
         *
         * context.getContentResolver().update()
         * Update row(s) in a content URI. If the content provider supports transactions the update will be atomic.
         *
         * ContentUris.withAppendedId(Uri contentUri, long id)
         *Appends the given ID to the end of the path.
         */
        if (context.getContentResolver().update(
                ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), mNoteDiffValues, null,
                null) == 0) {
            Log.e(TAG, "Update note error, should not happen");
            // Do not return, fall through
        }
        mNoteDiffValues.clear();

        // 修改但是没能push给ContentResolver,同步失败
        if (mNoteData.isLocalModified()
                && (mNoteData.pushIntoContentResolver(context, noteId) == null)) {
            return false;
        }

        return true;
    }

    private class NoteData {

        private long mTextDataId;

        // 存储结构
        private ContentValues mTextDataValues;

        private long mCallDataId;

        private ContentValues mCallDataValues;

        private static final String TAG = "NoteData";

        // 构造函数
        public NoteData() {
            mTextDataValues = new ContentValues();
            mCallDataValues = new ContentValues();
            mTextDataId = 0;
            mCallDataId = 0;
        }

        boolean isLocalModified() {
            return mTextDataValues.size() > 0 || mCallDataValues.size() > 0;
        }

        void setTextDataId(long id) {
            if(id <= 0) {
                throw new IllegalArgumentException("Text data id should larger than 0");
            }
            mTextDataId = id;
        }

        void setCallDataId(long id) {
            if (id <= 0) {
                throw new IllegalArgumentException("Call data id should larger than 0");
            }
            mCallDataId = id;
        }

        void setCallData(String key, String value) {
            mCallDataValues.put(key, value);
            // Sign to indicate local modified or not
            mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
            mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
        }

        void setTextData(String key, String value) {
            mTextDataValues.put(key, value);
            mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
            mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
        }

        Uri pushIntoContentResolver(Context context, long noteId) {

            /**
             * Check for safety
             */
            if (noteId <= 0) {
                throw new IllegalArgumentException("Wrong note id:" + noteId);
            }

            //ContentProviderOperation 类： 数据库批量操作方法
            ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
            // ContentProviderOperation.Builder 类: Used to add parameters to a ContentProviderOperation
            ContentProviderOperation.Builder builder = null;

            if(mTextDataValues.size() > 0) {

                mTextDataValues.put(DataColumns.NOTE_ID, noteId);

                if (mTextDataId == 0) {
                    // String MIME_TYPE
                    //CONTENT_ITEM_TYPE = "vnd.android.cursor.item/text_note"
                    mTextDataValues.put(DataColumns.MIME_TYPE, TextNote.CONTENT_ITEM_TYPE);

                    // 插入数据
                    Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                            mTextDataValues);
                    try {
                        setTextDataId(Long.valueOf(uri.getPathSegments().get(1)));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Insert new text data fail with noteId" + noteId);
                        mTextDataValues.clear();
                        return null;
                    }
                } else {
                    // CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/data")
                    // ContentProviderOperation.newUpdate():
                    // Create a Builder suitable for building an operation that will invoke ContentProvider#update.
                    builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mTextDataId));

                    //  withValues (ContentValues  value)
                     // This method will replace any previously defined values for the contained keys,
                    // but it will not replace any back-reference requests
                    builder.withValues(mTextDataValues);

                    // build()
                    // Create a ContentProviderOperation from this Builder.
                    operationList.add(builder.build());
                }
                mTextDataValues.clear();
            }

            if(mCallDataValues.size() > 0) {
                mCallDataValues.put(DataColumns.NOTE_ID, noteId);
                if (mCallDataId == 0) {
                    mCallDataValues.put(DataColumns.MIME_TYPE, CallNote.CONTENT_ITEM_TYPE);
                    Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                            mCallDataValues);
                    try {
                        setCallDataId(Long.valueOf(uri.getPathSegments().get(1)));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Insert new call data fail with noteId" + noteId);
                        mCallDataValues.clear();
                        return null;
                    }
                } else {
                    builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mCallDataId));
                    builder.withValues(mCallDataValues);
                    operationList.add(builder.build());
                }
                mCallDataValues.clear();
            }

            if (operationList.size() > 0) {
                try {
                    // AUTHORITY = "micode_notes"
                    ContentProviderResult[] results = context.getContentResolver().applyBatch(
                            Notes.AUTHORITY, operationList);
                    // CONTENT_NOTE_URI = Uri.parse("content://" + AUTHORITY + "/note"
                    return (results == null || results.length == 0 || results[0] == null) ? null
                            : ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId);
                } catch (RemoteException e) {
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    return null;
                } catch (OperationApplicationException e) {
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    return null;
                }
            }
            return null;

        }// end pushIntoContentResolver

    } //end class NoteData

}
