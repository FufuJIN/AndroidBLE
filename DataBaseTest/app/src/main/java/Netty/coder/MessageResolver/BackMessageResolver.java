package Netty.coder.MessageResolver;

import Netty.coder.data.Message;
import Netty.coder.data.MessageTypeEnum;
import Netty.coder.data.SessionIdManager;
import Netty.coder.Resolver;
public class BackMessageResolver implements Resolver{
    public static SessionIdManager sessionIdManager=new SessionIdManager();
    @Override
    public boolean support(Message message) {
        // TODO Auto-generated method stub
        return message.getMessageType() == MessageTypeEnum.BACK;
    }

    @Override
    public Message resolve(Message message) {
        // TODO Auto-generated method stub
        //打印相关调试信息
        System.out.println("BackMessageResolver消息：receive back message: " + System.currentTimeMillis());
        System.out.println("BackMessageResolver消息：注册返回消息体"+message.getBody());
        System.out.println("BackMessageResolver消息：注册返回sessionId:"+message.getSessionId());
        //设置sessionId
        sessionIdManager.setSessionId(message.getSessionId());
        System.out.println("BackMessageResolver消息：注册返回成功");
        Message empty = new Message();
        empty.setMessageType(MessageTypeEnum.EMPTY);
        empty.setSessionId(sessionIdManager.getSessionId());
        return empty;
    }

}
