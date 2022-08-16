package rs.ac.bg.etf.kdp.nikola.servers;

import java.net.ServerSocket;
import java.net.Socket;

import rs.ac.bg.etf.kdp.nikola.baffer.ServerBuffer;
import rs.ac.bg.etf.kdp.nikola.communication.SetConnection;
import rs.ac.bg.etf.kdp.nikola.gui.SubServerGUI;
import rs.ac.bg.etf.kdp.nikola.thread.SubServerWorkingThread;

public class SubServer implements Runnable {

	private volatile Thread thread;
	private volatile boolean running;

	private ServerBuffer buffer;

	private String protocol;
	private String mainHost;
	private int mainPort, subPort;

	private SubServerGUI subServerGUI;

	// Konstruktor podservera
	public SubServer(String mainHost, int mainPort, int subPort, SubServerGUI subServerGUI) {
		this.mainHost = mainHost;
		this.mainPort = mainPort;
		this.subPort = subPort;
		this.buffer = new ServerBuffer();
		this.subServerGUI = subServerGUI;
		this.thread = null;
		this.running = false;
	}

	// Run metod niti koja sluzi za primanje podataka i prosledjivanje takvih
	// podataka radnoj niti
	@Override
	public void run() {
		
		try (ServerSocket subServerListener = new ServerSocket(subPort)) {
			SetConnection.setConnectionWithMainServer(mainHost, mainPort, subPort, subServerGUI);
			while (true) {
				Socket client = subServerListener.accept();
				new SubServerWorkingThread(client, mainHost, mainPort, subPort, buffer, subServerGUI).start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Metod za pokretanje niti
	public void start() {
		if (thread == null) {
			thread = new Thread(this, "Server");
			running = true;
			thread.start();
		}
	}

	// Metod za zaustavljanje niti
	public void stop() {
		running = false;
		thread.interrupt();
	}

	// Metod za proveru da li je nit pokrenuta
	public boolean isRunning() {
		return running;
	}
	
	public void resetServer() {
		try {
			buffer.removeAll();
			SetConnection.reconnectServer(mainHost, mainPort, subPort, subServerGUI);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}