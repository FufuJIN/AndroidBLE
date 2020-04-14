package Netty.coder;

import android.util.Log;

import Netty.coder.data.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.IOException;
public class Client {
	private  String host;
	private int port;
	private ChannelFuture future;
	private NioEventLoopGroup group;
	private boolean isConnect=false;
	public Client(String host,int port){
		this.host=host;
		this.port=port;

	}
	public void start() throws IOException {
	    group = new NioEventLoopGroup();
	    Bootstrap bootstrap = new Bootstrap();
	    try {
	      bootstrap.group(group)
	          .channel(NioSocketChannel.class)
	          .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
	          .handler(new ChannelInitializer<SocketChannel>() {
	            @Override
	            protected void initChannel(SocketChannel ch) throws Exception {
	              ChannelPipeline pipeline = ch.pipeline();
	              // 添加用于解决粘包和拆包问题的处理器
	              pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
	              pipeline.addLast(new LengthFieldPrepender(4));
	              // 添加用于进行心跳检测的处理器
	              pipeline.addLast(new IdleStateHandler(1, 2, 0));
	              // 添加用于根据自定义协议将消息与字节流进行相互转换的处理器
	              pipeline.addLast(new MessageEncoder());
	              pipeline.addLast(new MessageDecoder());
	              // 添加客户端消息处理器
	              pipeline.addLast(new ClientMessageHandler());
	            }
	          });
	      //已连接到服务器
	      future = bootstrap.connect(host, port).sync();
			if (future.isSuccess()) {
				isConnect = true;
			} else {
				isConnect = false;
			}
	      //从服务器断开
	      future.channel().closeFuture().sync();
	    } catch (InterruptedException e) {
	      e.printStackTrace();
			Log.d("Client", "connect fail ");
	    } finally {
	      group.shutdownGracefully();
	    }
	}
	public void disconnect() {
		if(isConnect==true){
			group.shutdownGracefully();
		}
	}
	public void sendMsg(Message cmsg){
		System.out.println("发送信息中");
		future.channel().writeAndFlush(cmsg);
	}

}
