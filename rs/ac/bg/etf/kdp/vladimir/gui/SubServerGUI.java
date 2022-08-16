package rs.ac.bg.etf.kdp.nikola.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import rs.ac.bg.etf.kdp.nikola.servers.SubServer;

public class SubServerGUI extends JFrame implements GUIGuideLines {

	// Komponente potrebne da se napravi LOG za podserver.
	private JPanel logPanel;
	private JLabel logLabel;
	private JTextArea logSubServer;
	private List<String> logList;

	// Komponente za unos informacije o pod serveru.
	private JPanel inPortPanel, inPostStartPanel;
	private JLabel inPortLabel;
	private JTextField inPortFild;
	private JButton startServer;

	// Komponente za unos informacija o glavnom serveru.
	private JPanel MainSPanel, MainSInPortPanel, MainSInHostPanel;
	private JLabel MainSInPortLabel, MainSInHostLabel;
	private JTextField MainSInPortFild, MainSInHostFild;
	private JPanel startPanel;

	// Portovi i ip adrese poservera i glavnog servera.
	private int mainPort, subPort;
	private String mainHost;
	
	private SubServer sServer;
	private Boolean notStarted;

	// Konstruktor za prozor podservera.
	public SubServerGUI() {
		super("Podserver");
		arrangeComponents();
		setButtens();
		logList = new ArrayList<String>();
		this.notStarted = true;
	}

	// Metod za ubacivanje komponenti prozora za podserver.
	@Override
	public void arrangeComponents() {

		logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		logLabel = new JLabel("  Log servera:");
		logPanel.add(logLabel, BorderLayout.NORTH);
		logSubServer = new JTextArea();
		logSubServer.setEditable(false);
		logPanel.add(logSubServer, BorderLayout.CENTER);
		logPanel.setBorder(new LineBorder(Color.BLACK, 3));
		this.add(logPanel);

		inPortPanel = new JPanel();
		inPortPanel.setLayout(new GridLayout(1, 2));
		inPortPanel.setBorder(new LineBorder(Color.WHITE, 10));
		inPortLabel = new JLabel("  Port za ulaz:");
		inPortPanel.add(inPortLabel);
		inPortFild = new JTextField("4001");
		inPortPanel.add(inPortFild);

		startServer = new JButton("Pokreni server");

		MainSInPortPanel = new JPanel();
		MainSInPortPanel.setLayout(new GridLayout(1, 2));
		MainSInPortPanel.setBorder(new LineBorder(Color.WHITE, 10));
		MainSInPortLabel = new JLabel("  Port glavnog servera:");
		MainSInPortPanel.add(MainSInPortLabel);
		MainSInPortFild = new JTextField("4002");
		MainSInPortPanel.add(MainSInPortFild);

		MainSInHostPanel = new JPanel();
		MainSInHostPanel.setLayout(new GridLayout(1, 2));
		MainSInHostPanel.setBorder(new LineBorder(Color.WHITE, 10));
		MainSInHostLabel = new JLabel("  Ip adresa glavnog servera:");
		MainSInHostPanel.add(MainSInHostLabel);
		MainSInHostFild = new JTextField("192.168.164.1");
		MainSInHostPanel.add(MainSInHostFild);

		MainSPanel = new JPanel();
		MainSPanel.setLayout(new GridLayout(2, 1));
		MainSPanel.add(MainSInPortPanel);
		MainSPanel.add(MainSInHostPanel);

		inPostStartPanel = new JPanel();
		inPostStartPanel.setLayout(new GridLayout(2, 1));
		inPostStartPanel.add(startServer);
		inPostStartPanel.add(inPortPanel);

		startPanel = new JPanel();
		startPanel.setLayout(new GridLayout(1, 2));
		startPanel.add(inPostStartPanel);
		startPanel.add(MainSPanel);

		this.add(startPanel);

		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(2, 1));
		this.setSize(700, 500);
		this.setResizable(false);
		this.setLocation(100, 100);
	}

	// Ovaj metod sluzi za upacivanje osluskivaca u za dugme koje sluzi za
	// pokretanje podservera.
	@Override
	public void setButtens() {
		startServer.addActionListener(e -> {
			if(notStarted) {
				mainPort = Integer.parseInt(MainSInPortFild.getText());
				subPort = Integer.parseInt(inPortFild.getText());
				mainHost = MainSInHostFild.getText();

				sServer = new SubServer(mainHost, mainPort, subPort, this);
				setLog("---Server je pokrenut na portu " + subPort + " ---");
				sServer.start();
				startServer.setText("Restartuj server");
				this.notStarted = false;
			}else {
				sServer.resetServer();
			}
		});
	}

	// Ovaj metod sluzi za ubacivanje tekstuelnih poruka u log podservera.
	@Override
	public void setLog(String logText) {
		logList.add(logText + "\n");
		if (logList.size() > 12)
			logList.remove(0);
		this.logSubServer.setText(logList.get(0));
		for (int i = 1; i < logList.size(); i++)
			this.logSubServer.setText(logSubServer.getText() + logList.get(i));
	}
	
	public static void main(String[] args) {
		new SubServerGUI();
	}
}
