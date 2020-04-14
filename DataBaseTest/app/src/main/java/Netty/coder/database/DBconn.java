package Netty.coder.database;

import java.sql.*;
public class DBconn {
    //驱动名称
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"; //mysql8.0以下使用com.mysql.jdbc.Driver
    //数据库的URL以及时区
    static final String URL = "jdbc:mysql://localhost:3306/test?serverTimezone=UTC";
    //用户名以及密码
    static final String USERNAME = "root";
    static final String PASSWORD = "password";

    static Connection  conn = null;
    static ResultSet rs = null;
    //继承了Statement类
    static PreparedStatement ps =null;

    //连接数据库初始化
    public static void init(){
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(URL,USERNAME,PASSWORD);
        } catch (Exception e) {
            System.out.println("init [SQL驱动程序初始化失败！]");
            e.printStackTrace();
        }
    }

    //执行增删改语句
    public static int addUpdDel(String sql){  //增删改语句
        int i = 0;
        try {
            PreparedStatement ps =  conn.prepareStatement(sql);
            i =  ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("sql数据库增删改异常");
            e.printStackTrace();
        }
        return i;
    }
    //执行查询语句
    public static ResultSet selectSql(String sql){  //查询语句
        try {
            ps =  conn.prepareStatement(sql);
            rs =  ps.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println("sql数据库查询异常");
            e.printStackTrace();
        }
        return rs;
    }
    //断开数据库连接
    public static void closeConn(){
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println("sql数据库关闭异常");
            e.printStackTrace();
        }
    }
    //增加数据
    public static boolean addData(String name,String Pwd,String sex,String age,String home) {
        boolean flag = false;
        DBconn.init();
        int i =DBconn.addUpdDel("insert into alluser(Name,Pwd,Sex,Age,Home) " +
                "values('"+name+"','"+Pwd+"','"+sex+"','"+age+"','"+home+"')");
        if(i>0){
            flag = true;
        }
        DBconn.closeConn();
        return flag;
    }

}