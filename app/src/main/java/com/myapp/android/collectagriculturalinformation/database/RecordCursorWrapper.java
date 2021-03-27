package com.myapp.android.collectagriculturalinformation.database;
/**
 * Description Cursor可以封装数据表中的原始字段值
 * 使用CursorWrapper封装Cursor对象，防止重复写代码
 */
import android.database.Cursor;
import android.database.CursorWrapper;
import com.myapp.android.collectagriculturalinformation.database.RecordDbSchema.RecordTable;
import com.myapp.android.collectagriculturalinformation.Record;

import java.util.Date;
import java.util.UUID;

public class RecordCursorWrapper extends CursorWrapper {

    public RecordCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Record getRecord() {
        String uuidString = getString(getColumnIndex(RecordTable.Cols.UUID));
        String title = getString(getColumnIndex(RecordTable.Cols.TITLE));
        long date = getLong(getColumnIndex(RecordTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(RecordTable.Cols.SOLVED));
        String contacts = getString(getColumnIndex(RecordTable.Cols.CONTACTS));

        Record record = new Record(UUID.fromString(uuidString));
        record.setTitle(title);
        record.setDate(new Date(date));
        record.setSolved(isSolved != 0);
        record.setContacts(contacts);

        return record;
    }

}
