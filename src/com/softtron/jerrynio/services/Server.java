package com.softtron.jerrynio.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Server {
	public static void main(String[] args) throws IOException {
		// 1、创建ServerSocketChannel,并且非阻塞
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		// 2、设置端口
		serverSocketChannel.socket().bind(new InetSocketAddress(9000));
		// 3、创建selector
		Selector acceptSelector = Selector.open();
		// 4、将 ServerSocketChannel注册到selector,并且注册accept事件
		serverSocketChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
		while (true) {
			// 5、select()->获取selectedKey进行迭代
			int z = acceptSelector.select();
			if (z == 0)
				continue;
			Set<SelectionKey> keys = acceptSelector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isAcceptable()) {
					// 6、accept处理，并且注册reader
					// 获取socketchanel,并且设置非阻塞
					SocketChannel socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);
					// 注册读事件
					socketChannel.register(acceptSelector, SelectionKey.OP_READ);
				}
				if (key.isReadable()) {
					// 7、reader,注册writer
					// 获取socketchannel
					SocketChannel sc = (SocketChannel) key.channel();
					ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
					byte[] bucket = new byte[1024];
					int length = 0;
					// 放置读到的http协议
					List<String> lists = new ArrayList<>();
					while ((length = sc.read(byteBuffer)) != -1 && length != 0) {
						byteBuffer.clear();
						byteBuffer.get(bucket, 0, length);
						lists.add(new String(bucket));
						byteBuffer.clear();
					}
					// 获得访问路径
					String[] firstline = lists.get(0).split("\\s");
					String path = firstline[1];
					// 注册writer
					sc.register(acceptSelector, SelectionKey.OP_WRITE, path);
				}
				if (key.isWritable()) {
					SocketChannel sc = (SocketChannel) key.channel();
					String path = (String) key.attachment();
					if ("/".equals(path)) {
						path = "/index.html";// 设置默认访问路径
					}
					path = path.substring(1);// 去掉/
					File file = new File(path);
					String content = "";// 返回的http协议
					if (!file.exists()) {
						content = "HTTP/1.0 404 \r\n Content-Type: text/html\r\n\r\n";
					} else if (path.lastIndexOf(".mp4") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: video/mp4\r\n\r\n";
					} else if (path.lastIndexOf(".png") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: image/png\r\n\r\n";
					} else if (path.lastIndexOf(".jpg") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: image/jpeg\r\n\r\n";
					} else if (path.lastIndexOf(".css") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: text/css \r\n\r\n";
					} else if (path.lastIndexOf(".js") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: application/x-javascript\r\n\r\n";
					} else if (path.lastIndexOf(".html") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
					}
					ByteBuffer byteBuffer = ByteBuffer.wrap(content.getBytes());
					sc.write(byteBuffer);// 写入协议
					// 写入文件
					if (file.exists()) {
						FileChannel fc = new FileInputStream(file).getChannel();
						fc.transferTo(0, file.length(), sc);
						fc.close();
					}
					sc.close();
				}

			}

		}

	}
}
