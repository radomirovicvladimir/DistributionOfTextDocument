package rs.ac.bg.etf.kdp.nikola.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;

import rs.ac.bg.etf.kdp.nikola.gui.SubServerGUI;

public class SetConnection {
	
	//Staticki metod koji sluzi za slanje podataka za konekciju na glavni server
	public static void setConnectionWithMainServer(String mainHost, int mainPort, int subPort, SubServerGUI subServerGUI) throws ClassNotFoundException {
		try (Socket c = new Socket(mainHost, mainPort);
				ObjectOutputStream out = new ObjectOutputStream(c.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(c.getInputStream());) {
			out.writeObject("Connection");
			out.writeInt(subPort);
			out.flush();
			out.writeObject(c.getLocalAddress().toString().replace("/", ""));
			
			out.writeObject("Connect");
			
			String ACK = (String) in.readObject();
			if("ACK".equals(ACK))subServerGUI.setLog("CONNECTION: Podserver se povezao sa glavnim serverom.");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	//Staticki metod koji sluzi za slanje podataka za ponovnu konekciju na server
	public static void reconnectServer(String mainHost, int mainPort, int subPort, SubServerGUI subServerGUI) throws ClassNotFoundException {
		try (Socket c = new Socket(mainHost, mainPort);
				ObjectOutputStream out = new ObjectOutputStream(c.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(c.getInputStream());) {
			out.writeObject("Connection");
			out.writeInt(subPort);
			out.flush();
			out.writeObject(c.getLocalAddress().toString().replace("/", ""));
			
			out.writeObject("Reconnect");
			
			String ACK = (String) in.readObject();
			if("ACK".equals(ACK))subServerGUI.setLog("CONNECTION: Podserver se povezao sa glavnim serverom.");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
