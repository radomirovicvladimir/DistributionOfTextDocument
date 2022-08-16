package rs.ac.bg.etf.kdp.nikola.baffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import rs.ac.bg.etf.kdp.nikola.data.DocumentData;

public class ServerBuffer {

	private List<DocumentData> arrayBuffer;

	public ServerBuffer() {
		super();
		arrayBuffer = new ArrayList<DocumentData>();
	}
	
	//Metod koji dohvata trazeni fajl
	public synchronized File get(String fileName) {
		makeDirectory();
		for (int i = 0; i < arrayBuffer.size(); i++) {
			if (((DocumentData) (arrayBuffer.get(i))).getFile().equals(fileName)) {
				File f = new File("Server\\" + fileName);
				File f2 = new File(fileName);
				try {
					this.copyContent(f, f2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return f2;
			}
		}
		return null;
	}

	//Metod koji dohvata trazeni fajl i u DocumentData ubacuje ip i port
	public synchronized File get(String fileName , String ip, int port) {
		makeDirectory();
		for (int i = 0; i < arrayBuffer.size(); i++) {
			if (((DocumentData) (arrayBuffer.get(i))).getFile().equals(fileName)) {
				
				((DocumentData) (arrayBuffer.get(i))).addHostToList(ip, port);
				
				File f = new File("Server\\" + fileName);
				File f2 = new File(fileName);
				try {
					this.copyContent(f, f2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return f2;
			}
		}
		return null;
	}
	
	//Metod koji ubacuje fajl u server
	public synchronized void add(File file) {
		makeDirectory();
		if(!exists(file)) {
			arrayBuffer.add(new DocumentData(file));	
			File f = new File("Server\\" + file.getName());
			try {
				this.copyContent(file, f);
				f.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else change(file ,true);
	}
	
	//Metod koji ubacuje fajl u server i u DocumentData ubacuje ip i port
	public synchronized void add(File file, String ip, int port) {
		makeDirectory();
		if(!exists(file)) {
			arrayBuffer.add(new DocumentData(file, ip, port));
		
			File f = new File("Server\\" + file.getName());
		
			try {
			
				this.copyContent(file, f);
			
				f.createNewFile();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else change(file ,true);
	}
	
	//Metod koji menja postojeci fajl i postavlja mu informaciju da li je izmenjen ili nije
	public synchronized void change(File file, boolean changed) {
		makeDirectory();
		for (int i = 0; i < arrayBuffer.size(); i++) {
			if (((DocumentData) (arrayBuffer.get(i))).getFile().equals(file.getName())) {
				((DocumentData) (arrayBuffer.get(i))).setChanged(changed);
			}
		}
		
		File f = new File("Server\\" + file.getName());
		
		try {
			this.copyContent(file, f);
			f.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Metod koji izbacuje dati fajl iz bafera
	public synchronized void remove(File file) {
		makeDirectory();
		for (int i = 0; i < arrayBuffer.size(); i++) {
			if (((DocumentData) (arrayBuffer.get(i))).getFile().equals(file.getName())) {
				arrayBuffer.remove(i);
				new File("Server\\" + file.getName()).delete();
			}
		}
	}

	//Metod koji izbacuje sve fajlove iz bafera
	public synchronized void removeAll() {
		makeDirectory();
		for (int i = 0; i < arrayBuffer.size(); i++) {
			new File("Server\\" + ((DocumentData) (arrayBuffer.get(i))).getFile()).delete();
			arrayBuffer.remove(i);
		}
	}
	
	public int size() {
		return arrayBuffer.size();
	}
	
	public synchronized DocumentData getData(int i) {
		return (DocumentData) arrayBuffer.get(i);
	}
	
	//----------------------------------------------------------------------------------
	//Metod za proveru da li dati fajl vec postoji na serveru
	private boolean exists(File file) {
		for (int i = 0; i < arrayBuffer.size(); i++) {
			if (((DocumentData) (arrayBuffer.get(i))).getFile().equals(file.getName())) {
				return true;
			}
		}
		return false;
	}
	
	//Metod za kopiranje podatka iz jednog fajla u drugi fajl
	private void copyContent(File a, File b) throws Exception {
		FileInputStream in = new FileInputStream(a);
		FileOutputStream out = new FileOutputStream(b);

		try {

			int n;
			while ((n = in.read()) != -1) {
				out.write(n);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
	
	private void makeDirectory() {
		File theDir = new File("Server");
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
	}
	
}
