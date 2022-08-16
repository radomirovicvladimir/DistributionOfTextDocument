package rs.ac.bg.etf.kdp.nikola.thread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import rs.ac.bg.etf.kdp.nikola.baffer.ConnectionBuffer;
import rs.ac.bg.etf.kdp.nikola.baffer.UpdateBuffer;
import rs.ac.bg.etf.kdp.nikola.communication.ConvertBineryFile;
import rs.ac.bg.etf.kdp.nikola.data.DocumentData;
import rs.ac.bg.etf.kdp.nikola.gui.MainServerGUI;

public class UpdateThread extends Thread {

	private UpdateBuffer updateBuffer;
	private ConnectionBuffer connectionBuffer;

	private DocumentData processingData;
	private String subServerIp;
	private int subServerPort;
	private String fileName;

	private int repeatSendingMessage;

	private Object lock;

	private MainServerGUI mainServerGUI;

	//Konstruktor
	public UpdateThread(UpdateBuffer updateBuffer, int repeatSendingMessage, ConnectionBuffer connectionBuffer,
			MainServerGUI mainServerGUI, Object lock) {
		this.mainServerGUI = mainServerGUI;
		this.lock = lock;
		this.updateBuffer = updateBuffer;
		this.connectionBuffer = connectionBuffer;
		this.repeatSendingMessage = repeatSendingMessage;
	}

	//Metod sluzi za izvacenje DocumentData iz update bafera i u slucaju da je prazan uklanjanje
	//istog iz update bafera, svaka nit ulazi i zaustavlja se kod while petlje, tada se ceka 
	//signal za proveru da li ima neki podatak u update baferu, u slucaju da ima jedna nit prihvata 
	//podatak iz update bufera i signalizira sledecoj niti da moze da se odblokira
	private void getServerInfo() throws InterruptedException {
		synchronized (lock) {
			while (updateBuffer.isEmpty()) {	lock.wait();}
			processingData = updateBuffer.getData();
			subServerIp = processingData.getIpFromList(0);
			subServerPort = processingData.getPortFromList(0);
			processingData.removeIpFromList(subServerIp, subServerPort);
			fileName = processingData.getFile();
			if (processingData.listIsEmpty()) {	updateBuffer.removeData();}
			lock.notify();
		}
	}

	
	//Metod koji sluzi za slanje podataka podserverima koji treba da se azurira u slucaju da je 
	//podatak uspesno azuriran vratice se potvrdan odgovor
	private boolean sendUpdateFile() throws UnknownHostException, IOException, ClassNotFoundException {
		try (Socket socket = new Socket(subServerIp, subServerPort);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			out.writeObject("Update");
			ConvertBineryFile.sendBineryFile(new File("Server\\" + fileName), in, out);
			String ACK = (String) in.readObject();
			if ("ACK".equals(ACK))
				return true;
		}
		return false;
	}

	//Run metod niti UpdateThread prvo izvlazi informaciju koji podserver treba da se 
	//azurira u slucaju ne nema takvih podservera run metod se zablokira, u slucaju da ima
	//proverava se da li je taj podserver validan u sluvaju da jeste salje se podatak za izmenu
	//ukoliko je podatak neuspesno azuriran ponavlja se proces 3 puta, ako posle treceg puta 
	//podatak opet nije azuriran taj podatak se proglasava nevalidnim i povecava brojac 
	//nevalidnih podataka u ConnectionBaffer
	@Override
	public void run() {

		try {
			while (true) {
				getServerInfo();
				if (((ConnectionBuffer.ServerInfo) (connectionBuffer.getServer(subServerIp, subServerPort)))
						.isConnectionValid()) {
					int i = 0;
					for (; i < 3; i++) {
						try {
							if (sendUpdateFile()) {
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
							this.sleep(1000 * repeatSendingMessage);
						}
					}

					if (i == 3) {
						mainServerGUI.setLog("UPDATE: Server " + subServerIp + " nije uspesno promenio podatak!!!");
						((ConnectionBuffer.ServerInfo) (connectionBuffer.getServer(subServerIp, subServerPort)))
								.incrementUnvalidFiles();
						;
					} else
						mainServerGUI.setLog("UPDATE: Server " + subServerIp + " je uspesno promenio podatak.");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
