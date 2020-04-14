package Netty.coder.Dao;

import Netty.coder.data.*;
public interface UserDao {
    //查找数据库中是否存在该用户
    public boolean isExistUser(String name);
    //查找用户在数据库中的id作为SessionId
    public int getSessionId(String name);
    //根据ID查找用户名
    public String getUserName(int id);
    //将新用户添加到数据库中
    public boolean addUser(Message message);
}