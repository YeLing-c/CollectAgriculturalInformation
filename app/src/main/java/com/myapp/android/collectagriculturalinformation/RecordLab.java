package com.myapp.android.collectagriculturalinformation;
/**
 * Description 数据集中存储池，存储Record对象
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.myapp.android.collectagriculturalinformation.database.RecordBaseHelper;
import com.myapp.android.collectagriculturalinformation.database.RecordCursorWrapper;
import com.myapp.android.collectagriculturalinformation.database.RecordDbSchema.RecordTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.myapp.android.collectagriculturalinformation.database.RecordDbSchema.RecordTable.Cols.*;

public class RecordLab {

    private static RecordLab sRecordLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    /**
     * 调用get方法创建RecordLab对象
     */
    public static RecordLab get(Context context) {
        if (sRecordLab == null) {
            sRecordLab = new RecordLab(context);
        }

        return sRecordLab;
    }

    /**
     * 创建数据库
     */
    private RecordLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new RecordBaseHelper(mContext)
                .getWritableDatabase();

    }

    /**
     * 向数据库写入数据
     */
    public void addRecord(Record c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(RecordTable.NAME, null, values);
    }

    /**
     * 遍历查询出所有的Record，返回Record数组对象
     */
    public List<Record> getRecords() {
        List<Record> records = new ArrayList<>();
        RecordCursorWrapper cursor = queryRecords(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                records.add(cursor.getRecord());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return records;
    }

    /**
     * 取出首条记录
     */
    public Record getRecord(UUID id) {
        RecordCursorWrapper cursor = queryRecords(
                RecordTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getRecord();
        } finally {
            cursor.close();
        }
    }

    /**
     * 返回指向某个具体位置的File对象
     */
    public File getPhotoFile(Record record) {
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, record.getPhotoFilename());
    }

    /**
     * 更新数据库记录
     */
    public void updateRecord(Record record) {
        String uuidString = record.getId().toString();
        ContentValues values = getContentValues(record);
        mDatabase.update(RecordTable.NAME, values,
                RecordTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    /**
     * 调用数据库的query方法查询RecordTable中的数据
     */
    private RecordCursorWrapper queryRecords(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                RecordTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new RecordCursorWrapper(cursor);
    }

    /**
     * 创建ContentValues实例，将Record记录转换为ContentValues
     * ContentValues是负责处理数据库写入和更新操作的辅助类。
     * 它是一个键值存储类，类似前面的Bundle。键是数据表字段。
     */
    private static ContentValues getContentValues(Record record) {
        ContentValues values = new ContentValues();
        values.put(UUID, record.getId().toString());
        values.put(TITLE, record.getTitle());
        values.put(DATE, record.getDate().getTime());
        values.put(SOLVED, record.isSolved() ? 1 : 0);
        values.put(CONTACTS, record.getContacts());
        values.put(LOCATION, record.getLocation());
        return values;
    }


}
