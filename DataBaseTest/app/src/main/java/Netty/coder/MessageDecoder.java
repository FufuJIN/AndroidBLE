package Netty.coder;

import Netty.coder.data.Message;
import Netty.coder.data.MessageTypeEnum;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageDecoder  extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
		Message message = new Message();
		message.setMagicNumber(byteBuf.readInt());  // 读取魔数
		message.setMainVersion(byteBuf.readInt()); // 读取主版本号
		message.setSubVersion(byteBuf.readInt()); // 读取次版本号
		message.setModifyVersion(byteBuf.readInt());	// 读取修订版本号
		message.setSessionId(byteBuf.readInt());       // 读取sessionId

		message.setMessageType(MessageTypeEnum.get(byteBuf.readByte()));	// 读取当前的消息类型
		short attachmentSize = byteBuf.readShort();	// 读取附件长度
		System.out.println("MessageDecoder消息：附件长度"+attachmentSize);
		for (short i = 0; i < attachmentSize; i++) {
			int keyLength = byteBuf.readInt();	// 读取键长度和数据
			//System.out.println("MessageDecoder消息：键长度"+keyLength);
			CharSequence key = byteBuf.readCharSequence(keyLength, Charset.forName("UTF-8"));
			int valueLength = byteBuf.readInt();	// 读取值长度和数据
			//System.out.println("MessageDecoder消息：键数据"+key)；
			//System.out.println("MessageDecoder消息：值长度"+valueLength);
			CharSequence value = byteBuf.readCharSequence(valueLength, Charset.forName("UTF-8"));
			//System.out.println("MessageDecoder消息：解码值"+value);
			message.addAttachment(key.toString(), value.toString());
		}
		int bodyLength = byteBuf.readInt();	// 读取消息体长度和数据
		CharSequence body = byteBuf.readCharSequence(bodyLength, Charset.defaultCharset());
		message.setBody(body.toString());
		out.add(message);
	}
}
