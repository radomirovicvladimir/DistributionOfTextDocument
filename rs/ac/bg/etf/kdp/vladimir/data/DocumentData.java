package rs.ac.bg.etf.kdp.nikola.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DocumentData implements DocumentInterface<String>{

	//Unutrasnja klasa koja cuva informacije o serveru
	public class HostAndPort {
		private int port;
		private String host;

		public HostAndPort(int port, String host) {
			super();
			this.port = port;
			this.host = host;
		}
		public int getPort() {return port;}
		public void setPort(int port) {this.port = port;}
		public String getHost() {return host;}
		public void setHost(String host) {this.host = host;}
	}
	
	//Cuvaju se informacije da li je fajl bio promenjen i ime cajla koji se cuva 
	private boolean changed;
	private String file;
	private List<HostAndPort> ipAdress;

	//Konstruktor
	public DocumentData() {
		this.file = null;
		this.ipAdress = new ArrayList<HostAndPort>();
		this.changed = false;
	}
	public DocumentData(File file) {
		this.file = file.getName();
		this.ipAdress = new ArrayList<HostAndPort>();
		this.changed = false;
	}
	public DocumentData(File file, String ip, int port) {
		this.file = file.getName();
		this.ipAdress = new ArrayList<HostAndPort>();
		this.ipAdress.add(new HostAndPort(port, ip));
		this.changed = false;
	}

	//Getters and setters
	public boolean isChanged() {return changed;}
	public void setChanged(boolean changed) {this.changed = changed;}
	public void setFile(String file) {this.file = file;}
	public String getFile() {return file;}

	//additional getters and setters
	public String getIpFromList(int i) {return ipAdress.get(i).getHost();}
	public int getPortFromList(int i) {return ipAdress.get(i).getPort();}
	public HostAndPort getHostToList(int i) {return ipAdress.get(i);}
	public void addHostToList(String ip, int port) {this.ipAdress.add(new HostAndPort(port, ip));}
	public int sizeList(){return ipAdress.size();}
	public boolean listIsEmpty() {return ipAdress.isEmpty();}
	
	public void removeIpFromList(String ip, int port) {
		for (int i = 0; i < ipAdress.size(); i++)
			if (ip.equals(ipAdress.get(i).getHost()) && port == ipAdress.get(i).getPort())
				ipAdress.remove(i);
	}

	//Metod Clone koji kopira informacije iz jedne DocumentData kalse u drugu
	public DocumentData clone() {
		DocumentData newDocData = new DocumentData();
		newDocData.setFile(this.getFile());
		for (int i = 0; i < this.sizeList(); i++) {
			newDocData.addHostToList(this.getIpFromList(i), this.getPortFromList(i));
		}
		return newDocData;
	}
}