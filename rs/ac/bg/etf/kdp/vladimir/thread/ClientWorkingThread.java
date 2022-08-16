package rs.ac.bg.etf.kdp.nikola.thread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

import rs.ac.bg.etf.kdp.nikola.communication.ConvertBineryFile;
import rs.ac.bg.etf.kdp.nikola.communication.Error404;
import rs.ac.bg.etf.kdp.nikola.gui.ClientGUI;
import rs.ac.bg.etf.kdp.nikola.gui.EditGUI;

public class ClientWorkingThread extends Thread {
	
	private String subServerIp;
	private int subServerPort;
	private File file;
	private String fileName;
	private String protocol;

	private ClientGUI clientGUI;
	
	public ClientWorkingThread(String subServerIp, int subServerPort, String fileName, String protocol, ClientGUI clientGUI) {
		super();
		this.subServerIp = subServerIp;
		this.subServerPort = subServerPort;
		this.fileName = fileName;
		this.file = new File(fileName);
		this.protocol = protocol;
		this.clientGUI = clientGUI;
	}

	// Geteri i seteri za fajl i protokol.
	public File getFile() {return file;}
	public void setFile(File docFile) {this.file = docFile;}
	public String getFileName() {return fileName;}
	public void setFileName(String fileName) {this.fileName = fileName;}
	public String getProtocol() {return protocol;}
	public void setProtocol(String s) {this.protocol = s;}

	@Override
	public void run() {
		try {
			sendRequest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Metod sa odabir zahteva klienta.
	public void sendRequest() throws IOException  {
		try ( Socket socket = new Socket(subServerIp, subServerPort);
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());){
			out.writeObject("Request");
			out.writeInt(subServerPort);
			out.flush();
			out.writeObject(socket.getLocalAddress().toString().replace("/", ""));
			out.writeObject(protocol);

			if (protocol.equals("requestFile")) {
				receiveFile(in, out);
			} else if (protocol.equals("addFile")) {
				sendFile(in, out);
			}

		} catch (Error404 e) {
			clientGUI.setLog("ERORR 404 !!!");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	// Ovaj metod predstavlja zahtev za dovlacenje fajla od glavnog servera
	// povezuje se sa podserverom pomocu socket-a i salje mu potrebne podatke
	// moguce je doci do greske u slucaju da se trazeni fajl ne nalazi na serveru.
	private void receiveFile(ObjectInputStream in, ObjectOutputStream out)
			throws IOException, ClassNotFoundException, Error404 {

		out.writeObject(fileName);

		file = ConvertBineryFile.receiveBineryFile(in, out);

		if (file != null) {
			new EditGUI().editFileGUI(file, clientGUI, this, subServerIp, subServerPort);
		} else 
			throw new Error404("ERORR 404 !!!");
	}

	// Ovej metod predstavlja zahtev za slanje fajla na poserver i glavni server,
	// isto kao i prethodni metod ovaj metod se povezuje sa podserverom i salje fajl
	// koji treba da sacuva na glavnom serveru i podserveru, moguce je doci do
	// greske u slucaju da se na sarveru nalazi fajl pod istim imenom ili ako nije
	// moguce sacuvati fajl.
	private void sendFile(ObjectInputStream in, ObjectOutputStream out)
			throws IOException, ClassNotFoundException, Error404 {

		ConvertBineryFile.sendBineryFile(file, in, out);

		clientGUI.setLog("REQUEST:Fajl " + fileName + " je poslat na server...");
		String s = (String) in.readObject();
		if (s.equals("SAVED"))
			clientGUI.setLog("SUCCESSFULL REQUEST:Fajl " + fileName + " je sacuvan na server.");
		else
			clientGUI.setLog("FAILED REQUEST:Fajl " + fileName + " nije sacuvan na server!!!");

	}
}
