package rs.ac.bg.etf.kdp.nikola.baffer;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.kdp.nikola.gui.MainServerGUI;

public class ConnectionBuffer {
	
	//Unutrasnja klasa ServerInfo sluzi da bi se cuvala informazije o jednom serveru
	//koji je trenutno povezan na glavni server njegova Ip adresa, port, broji fajlova  
	//koji su u njemu nevalidne(unvalidFiles), preme poslednjeg odziva(notReplayTime) i 
	//da li je povezan na glavni server(connectionValid)
	public class ServerInfo {
		private boolean connectionValid;
		private int unvalidFiles;
		private int notReplayTimer;
		private String ip;
		private int port;

		public ServerInfo(String ip, int port) {
			this.connectionValid = true;
			this.unvalidFiles = 0;
			this.notReplayTimer = 0;
			this.ip = ip;
			this.port = port;
		}

		public int getNotReplayTimer() {return notReplayTimer;}
		public int getPort() {return port;}
		public int getUnvalidFiles() {return unvalidFiles;}
		public String getIp() {return ip;}
		public boolean isConnectionValid() {return connectionValid;}
		public void setConnectionValid(boolean connectionValid) {this.connectionValid = connectionValid;}
		
		public void incrementUnvalidFiles() {this.unvalidFiles++;}
		
		public void incrementNotReplayTimer() {this.notReplayTimer++;}
		public void resetNotReplayTimer() {this.notReplayTimer = 0;}

		public void resetConnection() {
			this.notReplayTimer = 0;
			this.unvalidFiles = 0;
			this.connectionValid = true;
		}
	}

	//Unutrasnja klasa Timer sluzi da bi otkucavala vreme poslednjeg odziva servera
	public class Timer extends Thread {
		@Override
		public void run() {
			try {
				while (true) {
					this.sleep(1000);
					incrementAllTimers();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private List<ConnectionBuffer.ServerInfo> connectedServers;
	private int unvalideMessages;
	private int notRespondTime;
	private Timer timer;
	private MainServerGUI mainServerGUI;

	public ConnectionBuffer(int unvalideMessages, int notRespondTime, MainServerGUI mainServerGUI) {
		this.connectedServers = new ArrayList<ConnectionBuffer.ServerInfo>();
		this.unvalideMessages = unvalideMessages;
		this.notRespondTime = notRespondTime;
		this.mainServerGUI = mainServerGUI;
		this.timer = new Timer();
		this.timer.start();
		
	}

	//Ovaj metod pravi novu klasu serverInfo i ubacuje je u listu svih servera
	//koji su povezani sa glavnim serverom
	public synchronized void addServer(String ip, int port) {
		for (int i = 0; i < connectedServers.size(); i++) {
			if ((((ServerInfo) (connectedServers.get(i))).getIp()).equals(ip)
					&& (((ServerInfo) (connectedServers.get(i))).getPort()) == port) {
				connectedServers.remove(i);
			}
		}
		connectedServers.add(new ServerInfo(ip, port));
	}

	//Ovaj metod vraca informacije o trazemo serveru
	public synchronized ServerInfo getServer(String ip, int port) throws Exception {
		for (int i = 0; i < connectedServers.size(); i++) {
			if ((((ServerInfo) (connectedServers.get(i))).getIp()).equals(ip)
					&& (((ServerInfo) (connectedServers.get(i))).getPort()) == port) {
				((ServerInfo) (connectedServers.get(i))).resetNotReplayTimer();
				return connectedServers.get(i);
			}
		}
		throw new Exception();
	}

	//Ovaj metod povecava broj nevalidnih informacije u naznacenom podserveru
	public synchronized void incUnvalidFile(String ip, int port) {
		for (int i = 0; i < connectedServers.size(); i++) {
			if ((((ServerInfo) (connectedServers.get(i))).getIp()).equals(ip)
					&& (((ServerInfo) (connectedServers.get(i))).getPort()) == port) {
				((ServerInfo) (connectedServers.get(i))).incrementUnvalidFiles();
				((ServerInfo) (connectedServers.get(i))).resetNotReplayTimer();
				if (((ServerInfo) (connectedServers.get(i))).getUnvalidFiles() == unvalideMessages) {
					((ServerInfo) (connectedServers.get(i))).setConnectionValid(false);
					mainServerGUI.setLog("FAILED CONNECTION: Server "+((ServerInfo) (connectedServers.get(i))).getIp()+" je proglasen za nevalidnim!!!");
				}
			}
		}
	}

	//Ovaj metod sluzi kao indikator da je neki podserver ponovno pokrenut pa glavni
	//server treba da resetuje sve informacije vezane za njega
	public synchronized void resetConnection(String ip, int port) {
		for (int i = 0; i < connectedServers.size(); i++) {
			if ((((ServerInfo) (connectedServers.get(i))).getIp()).equals(ip)
					&& (((ServerInfo) (connectedServers.get(i))).getPort()) == port) {
				((ServerInfo) (connectedServers.get(i))).resetConnection();
			}
		}
	}

	//Ovaj metod povecava svim serverima tajmer od kad su se poslednji put javili 
	//glavnom serveru, u slucaju da se neki server nije javio glavnom serveru duzi
	//vremenski period onda se taj server proglasava za nevalidnim 
	public synchronized void incrementAllTimers() {
		for (int i = 0; i < connectedServers.size(); i++) {
			if (((ServerInfo) (connectedServers.get(i))).isConnectionValid())
				((ServerInfo) (connectedServers.get(i))).incrementNotReplayTimer();
			if (((ServerInfo) (connectedServers.get(i))).getNotReplayTimer() >= notRespondTime
					&& ((ServerInfo) (connectedServers.get(i))).isConnectionValid()) {
				((ServerInfo) (connectedServers.get(i))).setConnectionValid(false);
				mainServerGUI.setLog("FAILED CONNECTION: Server "+((ServerInfo) (connectedServers.get(i))).getIp()+" je proglasen za nevalidnim!!!");
			}
		}
	}
}
