package Netty.coder.Dao;

import Netty.coder.database.DBconn;
import Netty.coder.data.Message;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataDaoImpl implements DataDao{

    @Override
    public boolean addUserData(Message message) {
        // TODO Auto-generated method stub
        boolean flag = false;
        UserDao user = new UserDaoImpl();
        String name = user.getUserName(message.getSessionId());
        DBconn.init();
        int i =DBconn.addUpdDel("insert into data(cid,username,SPO2,PR,datatime) " +
                "values('"+message.getSessionId()+"','"+name+"','"+message.getAttachments("SPO2")
                +"','"+message.getAttachments("PR")
                +"','"+message.getAttachments("TIME")+"')");
        if(i>0){
            flag = true;
        }
        DBconn.closeConn();
        return flag;
    }

}