package rs.ac.bg.etf.kdp.nikola.servers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import rs.ac.bg.etf.kdp.nikola.communication.ConvertBineryFile;
import rs.ac.bg.etf.kdp.nikola.gui.ClientGUI;
import rs.ac.bg.etf.kdp.nikola.gui.EditGUI;

public class Edit implements Runnable {

	public static int CNT = 0;
	
	public int id;
	
	private volatile Thread thread;
	private volatile boolean running;

	private File file;
	private String subServerIp;
	private int subServerPort;
	private ClientGUI clientGUI;
	private EditGUI editGUI;

	public Edit(EditGUI editGUI, File file, ClientGUI clientGUI, String subServerIp, int subServerPort) {
		this.file = file;
		this.subServerIp = subServerIp;
		this.subServerPort = subServerPort;
		this.clientGUI = clientGUI;
		this.editGUI = editGUI;
		editGUI.setEditing(true);
		id = CNT++;
	}
	
	@Override
	public void run() {
		while (editGUI.isEditing()) {

			try {
				thread.sleep(2000);
				try (Socket socket = new Socket(subServerIp, subServerPort);
						ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
					
					editGUI.getCaretPosition();
					
					editGUI.saveFile(file);

					out.writeObject("Change");
					
					if(!editGUI.isEdited()) {
						out.writeObject("Notedited");
						out.writeObject(file.getName());
					}else {
						out.writeObject("Edited");
						ConvertBineryFile.sendBineryFile(file, in, out);
						editGUI.setEdited(false);
					}
					
					String check =(String)in.readObject();

					if((check).equals("Valid")) {

						file = ConvertBineryFile.receiveBineryFile(in, out);

						editGUI.loadFile(file);
					
					} else {

						editGUI.subServerDisconnected(clientGUI);
						
					}
					
					editGUI.setCaretPosition();
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	// Metod za pokretanje niti
	public void start() {
		if (thread == null) {
			thread = new Thread(this, "Edit");
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
