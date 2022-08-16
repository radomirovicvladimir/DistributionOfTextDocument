package rs.ac.bg.etf.kdp.nikola.servers;

import java.net.ServerSocket;
import java.net.Socket;

import rs.ac.bg.etf.kdp.nikola.baffer.ConnectionBuffer;
import rs.ac.bg.etf.kdp.nikola.baffer.ServerBuffer;
import rs.ac.bg.etf.kdp.nikola.baffer.UpdateBuffer;
import rs.ac.bg.etf.kdp.nikola.gui.MainServerGUI;
import rs.ac.bg.etf.kdp.nikola.thread.MainServerWorkingThread;
import rs.ac.bg.etf.kdp.nikola.thread.UpdateBufferWorkingThread;

public class MainServer implements Runnable {

	private volatile Thread thread;
	private volatile boolean running;

	private ServerBuffer buffer;
	private ConnectionBuffer connectionBuffer;
	private UpdateBuffer updateBuffer;
	private UpdateBufferWorkingThread updateBafferWorkingThread;

	private String protocol;
	private int port;
	
	private MainServerGUI mainServerGUI;

	//Konstruktor glavnog servera
	public MainServer(int port, int threadN, int repeatSendingMessage, int unvalideMessages, int notRespondTime,
			int updateCheckTime, MainServerGUI mainServerGUI) {
		this.port = port;
		this.mainServerGUI = mainServerGUI;
		this.buffer = new ServerBuffer();
		this.connectionBuffer = new ConnectionBuffer(unvalideMessages,notRespondTime, mainServerGUI);
		this.updateBuffer = new UpdateBuffer(threadN, repeatSendingMessage, connectionBuffer, mainServerGUI);
		this.updateBafferWorkingThread = new UpdateBufferWorkingThread(updateBuffer, buffer, updateCheckTime);
		this.updateBafferWorkingThread.start();
		this.thread = null;
		this.running = false;
	}

	// Run metod niti koja sluzi za primanje podataka i prosledjivanje takvih
	// podataka radnoj niti
	@Override
	public void run() {
		try (ServerSocket mainServerListener = new ServerSocket(port);) {
			while (true) {
				Socket subServer = mainServerListener.accept();
				new MainServerWorkingThread(subServer, buffer, connectionBuffer, mainServerGUI).start();
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
}
