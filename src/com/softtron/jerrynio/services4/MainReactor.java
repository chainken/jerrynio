package com.softtron.jerrynio.services4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainReactor {
	List<SonReacor> lists;
	byte processors;
	volatile Selector acceptSelector;
	public MainReactor() {
		// ��ȡ����
		processors = (byte) Runtime.getRuntime().availableProcessors();
		lists = new ArrayList<>();
		for (byte i = 0; i < processors; i++) {
			SonReacor sonReacor = new SonReacor();
			lists.add(sonReacor);
			sonReacor.start();
		}
		System.out.println(processors + "�������߳̾���");
		try {
			acceptSelector = Selector.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init() throws IOException {

		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		// 2�����ö˿�
		serverSocketChannel.socket().bind(new InetSocketAddress(9000));
		// 3������selector

		// 4���� ServerSocketChannelע�ᵽselector,����ע��accept�¼�
		serverSocketChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
		Handler handler = new Handler();
		int a = 0;
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
				SocketChannel socketChannel = serverSocketChannel.accept();
				socketChannel.configureBlocking(false);
				if (key.isAcceptable()) {
					// 6��accept��������ע��reader
					// ��ȡsocketchanel,�������÷�����
					SonReacor sonReacor = lists.get(a++ % processors);
					sonReacor.register(socketChannel);
					//sonReacor.wakeUP();	
				}
			}
		}
	}
}
