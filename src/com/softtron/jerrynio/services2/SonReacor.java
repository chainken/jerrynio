package com.softtron.jerrynio.services2;
/**
 * ��������д�¼�����
 * @author apple
 *
 */

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SonReacor extends Thread {
	ExecutorService es = Executors.newFixedThreadPool(50);
	public Selector selector;// һ��sonreactor��Ӧһ��Selector
	// �̳߳�

	public SonReacor() {
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void register(SocketChannel socketChannel) {
		try {
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Handler handler = new Handler();
		while (true) {
			try {
				int z = selector.selectNow();
				// ע��
				if (z == 0)
					continue;
				Set<SelectionKey> keys = selector.selectedKeys();
				if (keys.size() == 0)
					continue;

				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
				
					if (key.isReadable()) {
						handler.read(key, selector);						
					}
					if(key.isWritable()) {	
						handler.write(key);
								
					}
				}

			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ����selector
	public void wakeUP() {
		selector.wakeup();
	}
}
