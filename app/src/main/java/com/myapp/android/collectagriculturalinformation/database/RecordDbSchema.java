package com.myapp.android.collectagriculturalinformation.database;
/**
 * Description 实现数据库相关代码的统一管理
 */
public class RecordDbSchema {

    public static final class RecordTable {
        //数据库表名
        public static final String NAME = "records";
        //数据表字段
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String CONTACTS = "contacts";
            public static final String LOCATION = "location";
        }
    }

}
