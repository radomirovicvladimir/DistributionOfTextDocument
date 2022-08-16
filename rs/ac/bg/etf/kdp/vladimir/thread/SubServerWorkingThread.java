package rs.ac.bg.etf.kdp.nikola.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.management.remote.NotificationResult;

import rs.ac.bg.etf.kdp.nikola.baffer.ServerBuffer;
import rs.ac.bg.etf.kdp.nikola.communication.ConvertBineryFile;
import rs.ac.bg.etf.kdp.nikola.data.DocumentData;
import rs.ac.bg.etf.kdp.nikola.gui.SubServerGUI;

public class SubServerWorkingThread extends Thread {

	private Socket client;
	private ServerBuffer buffer;

	private String mainHost;
	private int mainPort, subPort;

	private String mainServerACK;

	private SubServerGUI subServerGUI;

	// Konstruktor radne niti podservera.
	public SubServerWorkingThread(Socket client, String mainHost, int mainPort, int subPort, ServerBuffer buffer,
			SubServerGUI subServerGUI) {
		super();
		this.client = client;
		this.mainHost = mainHost;
		this.mainPort = mainPort;
		this.subPort = subPort;
		this.buffer = buffer;
		this.subServerGUI = subServerGUI;
		this.mainServerACK = "";
	}

	// Metod za obradu pristiglih podataka preko osluskivaca.
	@Override
	public void run() {
		try (Socket socket = this.client;
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			
			manageFileIncome(in, out);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Metod koji sluzi za proveru pristiglih komandi od klienta ili glavnog servera
	// i poziva odgovarajucih metod za njihovu obradu.
	private void manageFileIncome(ObjectInputStream in, ObjectOutputStream out)
			throws ClassNotFoundException, IOException {

			String dataType = (String) in.readObject();
			
			if (dataType.equals("Request")) {
				int clientPort = (int) in.readInt();
				String clientIp = (String) in.readObject();
				String protokol = (String) in.readObject();

				if ("addFile".equalsIgnoreCase(protokol) ) {

					sendFileToSubServer(in, out, clientIp, clientPort, protokol);

				} else if ("requestFile".equalsIgnoreCase(protokol)) {

					receiveFileFromSubServer(in, out, clientIp, clientPort, protokol);

				}
			} else if (dataType.equals("Update")) {
				
				updateFile(in, out);
				
			} else if(dataType.equals("Change")) {
				
				changeFile(in, out);
			}
	}

	// Metod koji sluzi za dadavanje dokumenta u pod server kao i prosledjivanje
	// pristiglog fajla ka glavnom serveru, nakon cega proverava da li je fajl
	// uspesno ucitan od strane glavnog servera.
	private void sendFileToSubServer(ObjectInputStream in, ObjectOutputStream out, String clientIp, int clientPort,
			String protokol) throws ClassNotFoundException, IOException {
		File file = ConvertBineryFile.receiveBineryFile(in, out);
		if (file != null) {

			buffer.add(file);
			
			mainServerRequests(file, protokol);

			if (mainServerACK.equals("SAVED")) {
				subServerGUI
						.setLog("SUCCESSFULL REQUEST:Fajl " + file.getName() + " je ubacen u server od strane klijenta " + clientIp + ".");
				out.writeObject("SAVED");
			}  else out.writeObject("FAILED");
		} else
			subServerGUI.setLog("FAILED REQUEST:Nevalidno poslat podatak od klijenta " + clientIp + "!!!");
	}

	// Metod koji sluzi za prosledjivanje fajla klientu u slucaju da se fajl nalazi
	// kod klienta u suprotnom se vrsi upit kod glavnog servera.
	private void receiveFileFromSubServer(ObjectInputStream in, ObjectOutputStream out, String clientIp, int clientPort,
			String protokol) throws IOException, ClassNotFoundException {

		String fileName = (String) in.readObject();

		subServerGUI.setLog("REQUEST:Klijent " + clientIp + " je zatrazio fajl " + fileName + " od servera...");
		File file = buffer.get(fileName);

		if (file != null) {
			subServerGUI.setLog("SUCCESSFULL REQUEST:Fajl je poslat.");

			ConvertBineryFile.sendBineryFile(buffer.get(fileName), in, out);
		} else {

			subServerGUI.setLog("FAILED REQUEST:Fajl nije pronadjen na serveru!!!");
			subServerGUI.setLog("REQUEST:trazi se fajl od glavnog servera...");
			
			file = mainServerRequests(new File(fileName), protokol);

			if (file == null) {
				subServerGUI.setLog("FAILED REQUEST:Fajl nije pronadjen na glavnom serveru ili konekcija sa glavnim serverom je izgubljena!!!");
				out.writeObject("ERROR404");
			} else {
				subServerGUI
						.setLog("SUCCESSFULL REQUEST:Fajl je dovucen sa glavnog servera , cuva se kopija fajla i prosledjuje klijentu.");
				buffer.add(file);
				ConvertBineryFile.sendBineryFile(buffer.get(fileName), in, out);

			}
		}
	}

	// Metod koji sluzi za povezivanje sa glavnim serverom i prosledjivanjem
	// podataka pristiglih od strane klienta.
	private File mainServerRequests(File file, String protokol) {
		try (Socket c = new Socket(mainHost, mainPort);
				ObjectOutputStream out = new ObjectOutputStream(c.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(c.getInputStream());) {
			out.writeObject("Request");
			out.writeInt(subPort);
			out.flush();
			out.writeObject(c.getLocalAddress().toString().replace("/", ""));
			
			if(((String)in.readObject()).equals("Unvalid"))return null;
			
			out.writeObject(protokol);
			if (protokol.equals("requestFile")) {
				out.writeObject(file.getName());
				return ConvertBineryFile.receiveBineryFile(in, out);
			}
			if ("addFile".equalsIgnoreCase(protokol)) {
				ConvertBineryFile.sendBineryFile(file, in, out);
				mainServerACK = (String) in.readObject();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Metod koji sluzi za slanjem potvrde glavnom serveru o uspesnjo azuriranom
	// fajlu.
	private void updateFile(ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException {
		File file = ConvertBineryFile.receiveBineryFile(in, out);
		buffer.change(file, false);	
		out.writeObject("ACK");
	}
	
	//Metod koji prima fajl od klienta ili zahtev za fajlom i salje ga glavnom serveru
	//nakon toga ocekuje fajl od glavnog servera i taj fajl prosledjuje klijentu
	private void changeFile(ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException {
		
		String edited = (String) in.readObject();
		String fileName = "";
		File file = null;
		
		if(edited.equals("Edited")) {
			file = ConvertBineryFile.receiveBineryFile(in ,out);
			buffer.change(file, true);
		} else if(edited.equals("Notedited")) {
			fileName = (String)in.readObject();
		}
		
		try (Socket c = new Socket(mainHost, mainPort);
				ObjectInputStream mainIn = new ObjectInputStream(c.getInputStream());
				ObjectOutputStream mainOut = new ObjectOutputStream(c.getOutputStream());) {
			
			mainOut.writeObject("Change");
			
			mainOut.writeInt(subPort);
			mainOut.flush();
			mainOut.writeObject(c.getLocalAddress().toString().replace("/", ""));
			
			
			if(((String)mainIn.readObject()).equals("Valid")) {

				out.writeObject("Valid");

				mainOut.writeObject("changeFile");
				
				mainOut.writeObject(edited);
				
				if(edited.equals("Edited"))ConvertBineryFile.sendBineryFile(file, mainIn, mainOut);
				else if(edited.equals("Notedited")) mainOut.writeObject(fileName);
					
				file = ConvertBineryFile.receiveBineryFile(mainIn, mainOut);
				
				buffer.change(file, true);
				
				ConvertBineryFile.sendBineryFile(buffer.get(file.getName()), in, out);
			}else 
				out.writeObject("Unvalid");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}