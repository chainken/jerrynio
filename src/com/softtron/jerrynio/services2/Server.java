package com.softtron.jerrynio.services2;

import java.io.IOException;

public class Server {
	public static void main(String[] args) throws IOException {
		MainReactor mr = new MainReactor();//��reactor
		mr.init();
	}
}
