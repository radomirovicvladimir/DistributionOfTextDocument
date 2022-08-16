package rs.ac.bg.etf.kdp.nikola.thread;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import rs.ac.bg.etf.kdp.nikola.baffer.ConnectionBuffer;
import rs.ac.bg.etf.kdp.nikola.baffer.ServerBuffer;
import rs.ac.bg.etf.kdp.nikola.communication.ConvertBineryFile;
import rs.ac.bg.etf.kdp.nikola.gui.MainServerGUI;

public class MainServerWorkingThread extends Thread {

	private Socket subServer;

	private ServerBuffer buffer;
	private ConnectionBuffer connectionBuffer;

	private MainServerGUI mainServerGUI;

	//Konstruktor
	public MainServerWorkingThread(Socket subServer, ServerBuffer buffer, ConnectionBuffer connectionBuffer,
			MainServerGUI mainServerGUI) {
		super();
		this.subServer = subServer;
		this.buffer = buffer;
		this.connectionBuffer = connectionBuffer;
		this.mainServerGUI = mainServerGUI;
	}

	//Run metod prima podatke od podservera i proverava koja je vrsta podataka u pitanju
	//na osnovu cega poziva odgovarajuce metode
	@Override
	public void run() {
		try (Socket socket = this.subServer;
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			
			String dataType = (String) in.readObject();
			
			if (dataType.equals("Request")||dataType.equals("Change"))
				
				manageFileIncome(in, out);
			
			else if (dataType.equals("Connection"))
				
				manageConnection(in, out);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	//Metod koji proverava koja vrsta podataka je stigla do glavnog servera (fajl za cuvanje,
	//fajl za izmenu ili zahtev za fajlom) i na osnovu toga poziva odredjen metod
	private void manageFileIncome(ObjectInputStream in, ObjectOutputStream out)
			throws ClassNotFoundException, IOException {
		try {
			int port = (int) in.readInt();
			String serverIp = (String) in.readObject();
			if ((((ConnectionBuffer.ServerInfo) connectionBuffer.getServer(serverIp, port))).isConnectionValid()) {
				
				out.writeObject("Valid");
				
				String protokol = (String) in.readObject();
				
				if ("addFile".equalsIgnoreCase(protokol)) {

					sendFileToMainServer(in, out, serverIp, port, protokol);
					
				} else if ("requestFile".equalsIgnoreCase(protokol)) {

					receiveFileFromMainServer(in, out, serverIp, port, protokol);
					
				} else if("changeFile".equalsIgnoreCase(protokol)) {

					changeFileInMianServer(in, out);
				
				}
			} else {
				out.writeObject("Unvalid");
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Metod sluzi za cuvanjem fajla na glavnom serveru i slanjem povratne poruke 
	//da je uspesno sacuvan
	private void sendFileToMainServer(ObjectInputStream in, ObjectOutputStream out, String subServerIp, int subServerPort,
			String protokol) throws ClassNotFoundException, IOException {
		
		File file = ConvertBineryFile.receiveBineryFile(in, out);
		
		buffer.add(file,subServerIp,subServerPort);
		
		mainServerGUI.setLog(
				"SUCCESSFULL REQUEST:Fajl " + file.getName() + " je ubacen u glavni server od strane servera " + subServerIp + ".");
		out.writeObject("SAVED");		
	}

	//Metod sluzi za slanjem fajla podserveru u slucaju da se taj fajl nalazi na 
	//glavnom serveru
	private void receiveFileFromMainServer(ObjectInputStream in, ObjectOutputStream out,String subServerIp, int subServerPort,
			String protokol) throws ClassNotFoundException, IOException {
		String fileName = (String) in.readObject();
		mainServerGUI.setLog("REQUEST:Server " + subServerIp + " je zatrazio fajl " + fileName + " od glavnog servera...");

		File file = (File) buffer.get(fileName, subServerIp, subServerPort);

		if (file != null) {
			mainServerGUI.setLog("SUCCESSFULL REQUEST: Fajl je poslat.");
			ConvertBineryFile.sendBineryFile(file, in, out);
		} else {
			mainServerGUI.setLog("FAILED REQUEST: Fajl nije pronadjen!!!");
			out.writeObject("ERROR404");
		}
	}

	//Metod sluzi za izmenom postojeceg podatka ili slanjem podatka koji treba da se 
	//izmeni u slucaju da je primljena odgovarajuca poruka od podservera
	private void changeFileInMianServer(ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException {

		String edited = (String) in.readObject();
		
		String fileName = "";
		File file = null;
		
		if(edited.equals("Edited")) {
			file = ConvertBineryFile.receiveBineryFile(in, out);	

			buffer.change(file, true);
			
			fileName =file.getName();
			
			
		}else if(edited.equals("Notedited")) {
			fileName = (String)in.readObject();
		}
		
		file = buffer.get(fileName);
		
		ConvertBineryFile.sendBineryFile(file, in, out);
		
	}
	
	//Metod sluzi za primanjem zahteva od podservera za konekcijom na glavni server
	private void manageConnection(ObjectInputStream in, ObjectOutputStream out)
			throws ClassNotFoundException, IOException {
		
		int subServerPort = in.readInt();
		String subServerIp = (String) in.readObject();
		
		String request = (String) in.readObject();
		
		if(request.equals("Connect")) {
		
			connectionBuffer.addServer(subServerIp, subServerPort);
			mainServerGUI.setLog("CONNECTION: Podserver " + subServerIp + " se povezao sa glavnim serverom.");
			out.writeObject("ACK");
		
		} else if (request.equals("Reconnect")) {
			
			connectionBuffer.resetConnection(subServerIp, subServerPort);
			mainServerGUI.setLog("CONNECTION: Podserver " + subServerIp + " je ponovo pokrenut.");
			out.writeObject("ACK");
		
		}
	}
}
