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
		// 1������ServerSocketChannel,���ҷ�����
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		// 2�����ö˿�
		serverSocketChannel.socket().bind(new InetSocketAddress(9000));
		// 3������selector
		Selector acceptSelector = Selector.open();
		// 4���� ServerSocketChannelע�ᵽselector,����ע��accept�¼�
		serverSocketChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
		while (true) {
			// 5��select()->��ȡselectedKey���е���
			int z = acceptSelector.select();
			if (z == 0)
				continue;
			Set<SelectionKey> keys = acceptSelector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isAcceptable()) {
					// 6��accept��������ע��reader
					// ��ȡsocketchanel,�������÷�����
					SocketChannel socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);
					// ע����¼�
					socketChannel.register(acceptSelector, SelectionKey.OP_READ);
				}
				if (key.isReadable()) {
					// 7��reader,ע��writer
					// ��ȡsocketchannel
					SocketChannel sc = (SocketChannel) key.channel();
					ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
					byte[] bucket = new byte[1024];
					int length = 0;
					// ���ö�����httpЭ��
					List<String> lists = new ArrayList<>();
					while ((length = sc.read(byteBuffer)) != -1 && length != 0) {
						byteBuffer.clear();
						byteBuffer.get(bucket, 0, length);
						lists.add(new String(bucket));
						byteBuffer.clear();
					}
					// ��÷���·��
					String[] firstline = lists.get(0).split("\\s");
					String path = firstline[1];
					// ע��writer
					sc.register(acceptSelector, SelectionKey.OP_WRITE, path);
				}
				if (key.isWritable()) {
					SocketChannel sc = (SocketChannel) key.channel();
					String path = (String) key.attachment();
					if ("/".equals(path)) {
						path = "/index.html";// ����Ĭ�Ϸ���·��
					}
					path = path.substring(1);// ȥ��/
					File file = new File(path);
					String content = "";// ���ص�httpЭ��
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
					sc.write(byteBuffer);// д��Э��
					// д���ļ�
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
