package Netty.coder.Dao;

import Netty.coder.data.Message;

public interface DataDao {
    //向数据库中添加一条数据
    public boolean addUserData(Message message);

}

