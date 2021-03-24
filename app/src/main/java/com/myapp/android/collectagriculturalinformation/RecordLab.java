package com.myapp.android.collectagriculturalinformation;

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

    public static RecordLab get(Context context) {
        if (sRecordLab == null) {
            sRecordLab = new RecordLab(context);
        }

        return sRecordLab;
    }

    private RecordLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new RecordBaseHelper(mContext)
                .getWritableDatabase();

    }

    public void addRecord(Record c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(RecordTable.NAME, null, values);
    }

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

    public File getPhotoFile(Record record) {
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, record.getPhotoFilename());
    }

    public void updateRecord(Record record) {
        String uuidString = record.getId().toString();
        ContentValues values = getContentValues(record);
        mDatabase.update(RecordTable.NAME, values,
                RecordTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

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

    private static ContentValues getContentValues(Record record) {
        ContentValues values = new ContentValues();
        values.put(UUID, record.getId().toString());
        values.put(TITLE, record.getTitle());
        values.put(DATE, record.getDate().getTime());
        values.put(SOLVED, record.isSolved() ? 1 : 0);
        values.put(CONTACTS, record.getContacts());
        return values;
    }


}
