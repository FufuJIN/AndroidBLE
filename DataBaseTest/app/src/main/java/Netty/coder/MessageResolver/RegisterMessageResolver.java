package Netty.coder.MessageResolver;

import Netty.coder.data.Message;
import Netty.coder.data.MessageTypeEnum;
import Netty.coder.database.DBconn;
import Netty.coder.Resolver;
import Netty.coder.Dao.*;

public class RegisterMessageResolver implements Resolver{

    @Override
    public boolean support(Message message) {
        return message.getMessageType() == MessageTypeEnum.REGISTER;
    }

    @Override
    public Message resolve(Message message) {
        // 接收到register消息后，查找数据库看用户是否存在，如果存在返回一个empty消息以及一个sessionID,body为exist
        //，如果不存在则注册一个用户，body为register success！
        //首先定义一下用户表的操作接口类
        UserDao user = new UserDaoImpl();
        //打印相关调试信息
        System.out.println("RegisterMessageResolver消息：receive register message: " + System.currentTimeMillis());
        System.out.println("RegisterMessageResolver消息：注册信息"+message.getAttachments());
        //定义服务器端返回消息
        Message back = new Message();
        //用户信息处理
        //检查用户情况
        if(!user.isExistUser(message.getAttachments("name"))) {
            //用户在服务器端的数据库中不存在，新增用户数据
            user.addUser(message);
            back.setBody("Register success！");
        }
        else {
            //用户在服务器端的数据库中已经存在
            back.setBody("The user is exist!");
        }
        int sessionId = user.getSessionId(message.getAttachments("name"));
        back.setMessageType(MessageTypeEnum.BACK);
        back.setSessionId(sessionId);
        return back;
    }

}
