package Netty.coder.Dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Netty.coder.database.DBconn;
import Netty.coder.data.Message;

public class UserDaoImpl implements UserDao{

    @Override
    //查找该用户是否存在
    public boolean isExistUser(String name) {
        // TODO Auto-generated method stub
        boolean flag = false;
        try {
            DBconn.init();
            ResultSet rs = DBconn.selectSql("select * from alluser where name='"+name+"'");
            while(rs.next()){
                if(rs.getString("name").equals(name)){
                    flag = true;
                }
            }
            DBconn.closeConn();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public int getSessionId(String name) {
        // TODO Auto-generated method stub
        try {
            DBconn.init();
            ResultSet rs = DBconn.selectSql("select id from alluser where name='"+name+"'");
            while(rs.next()){
                return rs.getInt("id");
            }
            DBconn.closeConn();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean addUser(Message message) {
        // TODO Auto-generated method stub
        boolean flag = false;
        DBconn.init();
        int i =DBconn.addUpdDel("insert into alluser(Name,Pwd,Sex,Age,Home) " +
                "values('"+message.getAttachments("name")+"','"+message.getAttachments("password")
                +"','"+message.getAttachments("sex")+"','"+message.getAttachments("age")+"','"
                +message.getAttachments("home")+"')");
        if(i>0){
            flag = true;
        }
        DBconn.closeConn();
        return flag;
    }

    @Override
    public String getUserName(int id) {
        // TODO Auto-generated method stub
        try {
            DBconn.init();
            ResultSet rs = DBconn.selectSql("select name from alluser where id='"+id+"'");
            while(rs.next()){
                return rs.getString("name");
            }
            DBconn.closeConn();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
