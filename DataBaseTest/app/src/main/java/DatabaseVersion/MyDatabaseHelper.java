package DatabaseVersion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    //数据库存日期格式
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd"); //格式
    //创建总的管理表
    public static final String CREATE_TABLE_MANAGER ="create table Manager("
            +"Id integer primary key autoincrement not null,"
            +"User text not null,"
            +"PassWord text not null,"
            +"NumberOfTable integer not null,"
            +"FirstTableName text not null,"
            +"LastTableName text not null)";
    //创建用户数据储存表
    public static final String CREATE_TABLE_BEGIN = "create table ";
    public static final String CREATE_TABLEDATA_END   =
            "(Id integer primary key autoincrement not null,"
            +"SPO2 int not null,"
            +"PR   int not null,"
            +"Time integer not null)";
    //创建用户管理表
    public static final String CREATE_TABLEUSER_END   =
            "(Id integer primary key autoincrement not null,"
            +"DataTableName text not null,"
            +"BuildTime integer not null)";
    //上下文
     private Context mContext;
     public MyDatabaseHelper(Context context,String name,
                             SQLiteDatabase.CursorFactory factory,
                             int version){
         super(context,name,factory,version);
         mContext = context;
     }
     @Override
    public void onCreate(SQLiteDatabase db){
         Cursor cursor = null;
         try{
             cursor = db.rawQuery("select Id from Manager",null);
             if(cursor.moveToNext()){
                 int count = cursor.getInt(0);
             }
             Toast.makeText(mContext, "总表已经存在了", Toast.LENGTH_SHORT).show();
         }catch(Exception e) {
             db.execSQL(CREATE_TABLE_MANAGER);
             Toast.makeText(mContext,"建立总表",Toast.LENGTH_SHORT).show();
         }
     }
     @Override
    public void onUpgrade(SQLiteDatabase db,int OldVersion,int NewVersion){
         db.execSQL("DROP TABLE IF EXISTS Manager");
         db.execSQL("DROP TABLE IF EXISTS jj20190907");
         db.execSQL("DROP TABLE IF EXISTS jj20190914");
         db.execSQL("DROP TABLE IF EXISTS jj20190915");
         db.execSQL("DROP TABLE IF EXISTS jj2019091420190914");
         db.execSQL("DROP TABLE IF EXISTS 靳虎20190915");
         db.execSQL("DROP TABLE IF EXISTS 靳虎");
         onCreate(db);
     }
     //创建新的数据表，并且更新总表相关信息
     public void newDataTable(SQLiteDatabase db,String username,boolean firstTableOfUser)
     {
         java.util.Date date=new java.util.Date();  //java的日期和时间类
         String str=sdf.format(date);              //获取当前日期
         long Writedatetime = date.getTime();      //获取当前时间
         String TableName =username+str;
         Cursor cursor = null;
         try{
             cursor = db.rawQuery("select Id from "+ TableName ,null);
             if(cursor.moveToNext()){
                 int count = cursor.getInt(0);
             }
             Toast.makeText(mContext, TableName + "已经存在了", Toast.LENGTH_SHORT).show();
         }catch(Exception e) {
             db.execSQL(CREATE_TABLE_BEGIN
                     + TableName + CREATE_TABLEDATA_END);
             Toast.makeText(mContext, TableName + "子表建立了", Toast.LENGTH_SHORT).show();
             //新建了一张表需要总表的表单数和最先最后表名更改
             if(firstTableOfUser == true){
                 ContentValues values = new ContentValues();
                 values.put("NumberOfTable",1);
                 values.put("FirstTableName",TableName);
                 values.put("LastTableName" ,TableName);
                 db.update("Manager",values,"User=?",
                         new String[] { username });
             }else{
                 ContentValues values = new ContentValues();
                 int number = this.getNumberOfTable(db,username);
                 values.put("NumberOfTable",number+1);
                 values.put("LastTableName" ,TableName);
                 db.update("Manager",values,"User=?",
                         new String[] { username });
             }
             this.newTableData(db,username,TableName,Writedatetime);
         }
     }
     //向子表中插入一条数据
    public void newData(SQLiteDatabase db,String tablename,int SPO2,int PR){
        ContentValues values = new ContentValues();
        values.put("SPO2",SPO2);
        values.put("PR",PR);
        Date date = new java.util.Date();   //java的日期和时间类
        long Writedatetime = date.getTime();
        values.put("Time",Writedatetime);
        db.insert(tablename,null,values);
        Toast.makeText(mContext, tablename + "子表插入数据成功", Toast.LENGTH_SHORT).show();
    }
    //新建一张用户管理表
    public  void newUserTable(SQLiteDatabase db,String username){
         String TableName = username ;
         Cursor cursor = null;
         try{
            cursor = db.rawQuery("select Id from "+ TableName ,null);
            if(cursor.moveToNext()){
                int count = cursor.getInt(0);
            }
            Toast.makeText(mContext, TableName + "已经存在了", Toast.LENGTH_SHORT).show();
         }catch(Exception e) {
            db.execSQL(CREATE_TABLE_BEGIN
                    + TableName + CREATE_TABLEUSER_END);
            Toast.makeText(mContext, TableName + "用户管理表建立了", Toast.LENGTH_SHORT).show();
            //新建了一张表需要总表的表单数和最先最后表名更改
         }
    }
    //向用户管理表中插入数据
    public void newTableData(SQLiteDatabase db,String tablename,String DataTableName,long Writedatetime){
        ContentValues values = new ContentValues();
        values.put("DataTableName",DataTableName);
        values.put("BuildTime",Writedatetime);
        db.insert(tablename,null,values);
        Toast.makeText(mContext, tablename + "用户管理表插入数据成功", Toast.LENGTH_SHORT).show();
    }
    //向总表里插入一条数据，并且给该用户新建一张用户管理表，一张数据表
    public void newUser(SQLiteDatabase db,String username,String password){
        ContentValues values = new ContentValues();
        values.put("User",username);;
        values.put("PassWord",password);
        values.put("NumberOfTable",0);
        values.put("FirstTableName",username);
        values.put("LastTableName" ,username);
        db.insert("Manager",null,values);
        Toast.makeText(mContext,  "Manager表添加新用户"+username+"成功", Toast.LENGTH_SHORT).show();
        this.newUserTable(db,username);                        //新建一张用户管理表
        this.newDataTable(db,username,true);  //新建一张用户数据表
    }
    //查询数据库总表中是否存在该用户
    public boolean isExistUserName(SQLiteDatabase db,String username){
         boolean HasUserName = false;
         Cursor cursor = db.query("Manager", null, "User=?",
                new String[] { username }, null, null, null);
         if(cursor != null) {
             while (cursor.moveToNext()) {
                 String user = cursor.getString(cursor.getColumnIndex("User"));
                 if (user.equals(username)) {
                     HasUserName = true;
                 }
             }
         }
         cursor.close();
         return HasUserName;
    }
    //查询用户数据表的张数
    public int getNumberOfTable(SQLiteDatabase db,String username){
         int numberOfTable = -1;
         Cursor cursor = db.query("Manager", null, "User=?",
                new String[] { username }, null, null, null);
         if(cursor != null) {
             while (cursor.moveToNext()) {
                numberOfTable = cursor.getInt(cursor.getColumnIndex("NumberOfTable"));
            }
        }
         cursor.close();
         return numberOfTable;
    }
    //查询用户密码
    public String getPassWord(SQLiteDatabase db,String username){
        String userPassword = null;
        Cursor cursor = db.query("Manager", null, "User=?",
                new String[] { username }, null, null, null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                userPassword = cursor.getString(cursor.getColumnIndex("PassWord"));
            }
        }
        cursor.close();
        return userPassword;
    }

    public ArrayList<ArrayList<String>> getUserAllDataTable(SQLiteDatabase db,String username){
        ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
        ArrayList<String> data = new ArrayList<String>();
        Cursor cursor = db.query(username,new String[] { "DataTableName" }, null,
                null, null, null, null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                String TableName = cursor.getString(cursor.getColumnIndex("DataTableName"));
                data.add(TableName);
            }
        }
        table.add(data);
        return table;

    }

}
