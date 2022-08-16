package rs.ac.bg.etf.kdp.nikola.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.border.LineBorder;

import rs.ac.bg.etf.kdp.nikola.servers.Edit;
import rs.ac.bg.etf.kdp.nikola.thread.ClientWorkingThread;

public class EditGUI{
	private static final int EXIT_ON_CLOSE = 3;
	
	// Komponente za potrebne za stvaranje edit prozora.
	private JFrame fileEditor;
	private JPanel textPanel, buttonPanel, exitPanel;
	private JButton exit;
	private JTextArea textArea;
	
	private boolean editing;
	
	private int caretPosition; 
	
	private boolean edited;
	
	private Edit edit;

	//Getters and setters
	public  boolean isEditing() {return editing;}
	public void setEditing(boolean e) {editing = e;}
	public boolean isEdited() {return edited;}
	public void setEdited(boolean edited) {this.edited = edited;}
	public void getCaretPosition() { 
		caretPosition = textArea.getCaretPosition(); 
	}
	public void setCaretPosition() { 
		int len = textArea.getDocument().getLength();
		if( len >= caretPosition ) textArea.setCaretPosition(caretPosition);
		else { 
			len = textArea.getDocument().getLength();
			textArea.setCaretPosition(len);
		}
	}
	
	// Metod za ubacivanje komponenti vezanih za prozor za editovanje
	public void editFileGUI(File file, ClientGUI clientGUI, ClientWorkingThread client, String subServerIp, int subServerPort) {

		clientGUI.setVisible(false);
		fileEditor = new JFrame();
		fileEditor.setTitle("Izmeni fajl");
		textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1, 1));
		textArea = new JTextArea();
		textArea.setAutoscrolls(true);
		textPanel.add(textArea);
		fileEditor.add(textPanel, BorderLayout.CENTER);

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 1));
		exitPanel = new JPanel();
		exitPanel.setBorder(new LineBorder(Color.GRAY, 80));
		exitPanel.setLayout(new GridLayout(1, 1));
		exit = new JButton("exit");
		exitPanel.add(exit);
		buttonPanel.add(exitPanel);

		fileEditor.add(buttonPanel);

		loadFile(file);

		addEditButtons(file, clientGUI);
		addKeyListener();
		
		edit = new Edit(this, file, clientGUI, subServerIp, subServerPort);
		edit.start();
		
		fileEditor.setVisible(true);
		fileEditor.setDefaultCloseOperation(EXIT_ON_CLOSE);
		fileEditor.setLayout(new GridLayout(1, 1));
		fileEditor.setSize(1000, 500);
		fileEditor.setResizable(false);
		fileEditor.setLocation(100, 100);
	}

	//Metod za ubacivanje fajla u polje za izmenu
	public void loadFile(File file) {
		try {
			
			String data = "";
			
			Scanner scan = new Scanner(file);
			
			FileReader fr = new FileReader(file); 
				  
			int i; 
			try {
				while ((i=fr.read()) != -1) data += (char)i;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
				  
			
			textArea.setText(data);
			
			scan.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//Metod koji sluzi za cuvanje fajla iz polja za izmenu
	public void saveFile(File file) throws IOException {
		FileWriter writer = new FileWriter(file.getName());
		writer.write(textArea.getText());
		writer.close();
	}
	
	//Metod koji rukuje sa edit prozorom u slucaju da nije ostvarena konekcija sa glavnim prozorom
	public void subServerDisconnected(ClientGUI clientGUI) throws IOException {
		setEditing(false);				
		
		clientGUI.setVisible(true);
		fileEditor.setVisible(false);
			
		clientGUI.setLog("FAILED CHANGING: Prekinuta je veza izmedju podservera i glavnog servera.");
	}
	
	// Ubacivanje dugmadi za edit prozor, ubaciju se dva dugmeta, prvo sluzi za
	// potvrdu izmene fajla a drugo sluzi da bi se odustalo od izmene fajla.
	private void addEditButtons(File file, ClientGUI clientGUI) {
		exit.addActionListener(e -> {
			setEditing(false);				
			
			clientGUI.setVisible(true);
			fileEditor.setVisible(false);
				
			clientGUI.setLog("CHANGING: Zatvoren je prozor za izmenu.");
		});
	}
	
	//Osluskivac da li je pritisnuto dugme
	private void addKeyListener() {
		textArea.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				setEdited(true);
			}
		});
	}
}
