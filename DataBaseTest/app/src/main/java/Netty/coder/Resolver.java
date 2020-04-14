package Netty.coder;

import java.util.concurrent.atomic.AtomicInteger;
import Netty.coder.data.Message;
import Netty.coder.data.MessageTypeEnum;

public interface Resolver {
	public boolean support(Message message);
	public Message resolve(Message message);

}
