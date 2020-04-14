package Netty.coder;

import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import Netty.coder.data.Constants;
import Netty.coder.data.Message;
import Netty.coder.data.MessageTypeEnum;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message>{
	@Override
	protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
		// 将Message转换成二进制数据
		if (message.getMessageType() != MessageTypeEnum.EMPTY) {
			out.writeInt(Constants.MAGIC_NUMBER);	// 写入当前的魔数
			out.writeInt(Constants.MAIN_VERSION);	// 写入当前的主版本号
			out.writeInt(Constants.SUB_VERSION);	// 写入当前的次版本号
			out.writeInt(Constants.MODIFY_VERSION);	// 写入当前的修订版本号
			out.writeInt(message.getSessionId());     // 写入当前的SessionId

			out.writeByte(message.getMessageType().getType());	// 写入当前消息的类型

			out.writeShort(message.getAttachments().size());	// 写入当前消息的附加参数数量
			for (Map.Entry<String, String> entry : message.getAttachments().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				Charset charset = Charset.defaultCharset();

				// System.out.println("编码"+charset);
				//System.out.println("MessageEncoder消息：编码键长度"+key.length());
				// 写入键的长度
				try {
					out.writeInt(key.getBytes("UTF-8").length);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				out.writeCharSequence(key, charset);    // 写入键数据
				//System.out.println("MessageEncoder消息：编码值长度"+value.length());
				// 写入值的长度
				try {
					out.writeInt(value.getBytes("UTF-8").length);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//System.out.println("MessageEncoder消息：编码值"+value);
				out.writeCharSequence(value, charset);    // 写入值数据
			}

			if (null == message.getBody()) {
				out.writeInt(0);	// 如果消息体为空，则写入0，表示消息体长度为0
			} else {
				out.writeInt(message.getBody().getBytes("UTF-8").length);
				out.writeCharSequence(message.getBody(), Charset.defaultCharset());
			}
		}
	}

}
