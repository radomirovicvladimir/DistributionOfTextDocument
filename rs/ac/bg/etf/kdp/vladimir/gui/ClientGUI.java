package rs.ac.bg.etf.kdp.nikola.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.LineBorder;

import rs.ac.bg.etf.kdp.nikola.servers.Edit;
import rs.ac.bg.etf.kdp.nikola.thread.ClientWorkingThread;

@SuppressWarnings("serial")
public class ClientGUI extends JFrame implements GUIGuideLines {

	// Komponente potrebne da se napravi LOG za klijenta.
	private JPanel logPanel;
	private JLabel logLabel;
	private JTextArea logClient;
	private List<String> logList;

	// Ostale neophodne komponente za unos podataka potebnih za slanje i dovlacenje
	// fajlova.
	private ButtonGroup protokolDokumenta;
	private JRadioButton dodajDokument, zatraziDokument;
	private JPanel radioButtonPanel;
	private JTextField portField, hostField, locationField;
	private JLabel portLabel, hostLabel, locationLabel;
	private JPanel southPanel, sP1, sP2, sP22;
	private JPanel textAreaPanel;
	private JButton sendRequest;

	// Onsnovne informacije potrebna za povezivanje sa serverom.
	private String protocol;
	private String host;
	private int port;
	private String fileName;
	
	//Konstruktor prozora
	public ClientGUI() {
		super("Klijent");
		arrangeComponents();
		setButtens();
		logList = new ArrayList<String>();
		setLog("Ceka se unos podataka...");
	}

	// Metod za ubacivanje komponenti glavnog prozora za klienta.
	public void arrangeComponents() {
		logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		logPanel.setSize(this.getWidth(), this.getHeight() - 20);
		logLabel = new JLabel("  LOG:");
		logPanel.add(logLabel, BorderLayout.NORTH);
		logClient = new JTextArea();
		logClient.setEditable(false);
		logPanel.add(logClient, BorderLayout.CENTER);
		logPanel.setBorder(new LineBorder(Color.BLACK, 3));
		this.add(logPanel, BorderLayout.CENTER);

		dodajDokument = new JRadioButton("Dodaj Dokument");
		zatraziDokument = new JRadioButton("Zatrazi Dokument");
		protokolDokumenta = new ButtonGroup();
		protokolDokumenta.add(dodajDokument);
		protokolDokumenta.add(zatraziDokument);
		radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new GridLayout(2, 1));
		radioButtonPanel.add(dodajDokument);
		radioButtonPanel.add(zatraziDokument);

		textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new GridLayout(1, 4));
		portLabel = new JLabel("  Port:");
		portField = new JTextField("4001");
		hostLabel = new JLabel("  Host:");
		hostField = new JTextField("192.168.164.1");
		textAreaPanel.add(portLabel);
		textAreaPanel.add(portField);
		textAreaPanel.add(hostLabel);
		textAreaPanel.add(hostField);

		sP1 = new JPanel();
		sP1.setLayout(new GridLayout(2, 1));
		sP1.add(radioButtonPanel);
		sP1.add(textAreaPanel);

		locationLabel = new JLabel("  Upisi lokaciju dokumenta\\ime zahtevanog dokumenta:");
		locationField = new JTextField("text1.txt");

		sP2 = new JPanel();
		sP2.setLayout(new GridLayout(1, 2));
		sP2.add(locationLabel);
		sP2.add(locationField);

		sendRequest = new JButton("Posalji zahtev");
		sP22 = new JPanel();
		sP22.setLayout(new GridLayout(2, 1));
		sP22.add(sP2);
		sP22.add(sendRequest);

		southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(2, 1));
		southPanel.add(sP1);
		southPanel.add(sP22);

		this.add(southPanel, BorderLayout.SOUTH);

		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(2, 1));
		this.setSize(700, 500);
		this.setResizable(false);
		this.setLocation(100, 100);
	}
	
	// Osluskivaci dugmeta i radio dugmeta glavnog prozora Unutar metoda se stvara
	// klasa client koja ce sluzi za povezivanjem sa serverom.
	public void setButtens() {
		sendRequest.addActionListener(e -> {
			sendRequest.setEnabled(false);
			port = Integer.parseInt(portField.getText());
			host = hostField.getText();
			fileName = locationField.getText();

			try {

				if (dodajDokument.isSelected())
					protocol = "addFile";
				else if (zatraziDokument.isSelected())
					protocol = "requestFile";

				ClientWorkingThread client = new ClientWorkingThread(host, port, fileName, protocol, this);
				client.start();
				
				sendRequest.setEnabled(true);

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
	}
	
	// Ovaj metod sluzi za ubacivanje tekstuelnih poruka u log.
	@Override
	public void setLog(String logText) {
		logList.add(logText + "\n");
		if (logList.size() > 12)
			logList.remove(0);
		this.logClient.setText(logList.get(0));
		for (int i = 1; i < logList.size(); i++)
			this.logClient.setText(logClient.getText() + logList.get(i));
	}

	public static void main(String[] args) {
		new ClientGUI();
	}

}
