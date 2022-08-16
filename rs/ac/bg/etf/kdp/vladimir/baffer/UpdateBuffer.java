package rs.ac.bg.etf.kdp.nikola.baffer;

import java.util.List;

import rs.ac.bg.etf.kdp.nikola.data.DocumentData;
import rs.ac.bg.etf.kdp.nikola.gui.MainServerGUI;
import rs.ac.bg.etf.kdp.nikola.thread.UpdateThread;

import java.util.ArrayList;

public class UpdateBuffer {

	private List<Thread> threadGruop;
	private List<DocumentData> dataList;
	
	private Object lock;
	
	private boolean empty;
	
	private MainServerGUI mainServerGUI;
	
	public boolean isEmpty() {return empty;}
	public void setEmpty(boolean empty) {this.empty = empty;}

	//Konstruktor
	public UpdateBuffer(int threadN,int repeatSendingMessage, ConnectionBuffer connectionBuffer, MainServerGUI mainServerGUI) {
		this.empty = true;
		this.mainServerGUI = mainServerGUI;
		this.threadGruop = new ArrayList<Thread>();
		this.lock = new Object();
		for(int i = 0;i < threadN;i++) {
			threadGruop.add(new UpdateThread(this,repeatSendingMessage, connectionBuffer,mainServerGUI,lock));
			threadGruop.get(i).start();
		}
		this.dataList = new ArrayList<DocumentData>();
	}
	
	//Metod za ubacivanje podatka u bafer
	public void addData(DocumentData data) {
		synchronized(lock) {
			dataList.add(data);
			if(empty) {
				empty = false;
				lock.notify();
			}
		}
	}
	
	//metod za uklanjanje prvog podataka iz bafera
	public synchronized void removeData() {
		dataList.remove(0);
		if(dataList.isEmpty()) empty = true;
	}
	
	//metod za dohvatanje prvog podatka iz bafera
	public synchronized DocumentData getData() {
		return dataList.get(0);
	}
}
