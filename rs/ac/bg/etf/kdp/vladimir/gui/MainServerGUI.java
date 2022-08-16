package rs.ac.bg.etf.kdp.nikola.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import rs.ac.bg.etf.kdp.nikola.servers.MainServer;

public class MainServerGUI extends JFrame implements GUIGuideLines {

	// LOG
	private JTextArea logMainServer;
	private JPanel logPanel;
	private JLabel logLabel;
	private List<String> logList;

	// MainServer port za unos
	private JPanel inPortPanel, inPostStartPanel;
	private JLabel inPortLabel;
	private JTextField inPortFild;

	private JPanel threadPanel, repeatPanel, failedMPanel, respondTPanel, updatePanel;
	private JLabel threadLanel, repeatLanel, failedMLanel,respondTLanel, updateLanel;
	private JTextField threadFild, repeatFild, failedMFild, respondTFild , updateFild;

	private JPanel startPanel;
	private JButton startServer;

	private JPanel botPanel, timerPanel;

	private int port;
	private int threadN,repeatSendingMessage,failedMessages, notRespondTime, updateCheckTime;

	public MainServerGUI() {
		super("Glavni server");
		arrangeComponents();
		setButtens();
		logList = new ArrayList<String>();
	}

	// Metod za ubacivanje komponenti prozora za glavni server.
	public void arrangeComponents() {

		logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		try {
			logLabel = new JLabel("Log glavnog servera " + Inet4Address.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logPanel.add(logLabel, BorderLayout.NORTH);
		logMainServer = new JTextArea();
		logMainServer.setEditable(false);
		logPanel.add(logMainServer, BorderLayout.CENTER);
		logPanel.setBorder(new LineBorder(Color.BLACK, 3));
		this.add(logPanel);

		inPortPanel = new JPanel();
		inPortPanel.setLayout(new GridLayout(1, 2));
		inPortPanel.setBorder(new LineBorder(Color.WHITE, 10));
		inPortLabel = new JLabel("  Port za ulaz:");
		inPortPanel.add(inPortLabel);
		inPortFild = new JTextField("4002");
		inPortPanel.add(inPortFild);

		startPanel = new JPanel();
		startPanel.setLayout(new GridLayout(1, 2));
		startServer = new JButton("Pokreni server");
		startPanel.setBorder(new LineBorder(Color.WHITE, 10));
		startPanel.add(startServer);
		startPanel.add(inPortPanel);

		botPanel = new JPanel();
		botPanel.setLayout(new GridLayout(2, 1));
		botPanel.add(startPanel);

		threadPanel = new JPanel();
		threadPanel.setLayout(new GridLayout(1, 2));
		threadLanel = new JLabel("  Br. niti:");
		threadFild = new JTextField("4");
		threadPanel.add(threadLanel);
		threadPanel.add(threadFild);
		
		repeatPanel = new JPanel();
		repeatPanel.setLayout(new GridLayout(1, 2));
		repeatLanel = new JLabel("Ponovi upit:");
		repeatFild = new JTextField("10");
		repeatPanel.add(repeatLanel);
		repeatPanel.add(repeatFild);
		
		failedMPanel = new JPanel();
		failedMPanel.setLayout(new GridLayout(1, 2));
		failedMLanel = new JLabel(" Br. greski:");
		failedMFild = new JTextField("3");
		failedMPanel.add(failedMLanel);
		failedMPanel.add(failedMFild);
		
		respondTPanel = new JPanel();
		respondTPanel.setLayout(new GridLayout(1, 2));
		respondTLanel = new JLabel("Gasi Server:");
		respondTFild = new JTextField("200");
		respondTPanel.add(respondTLanel);
		respondTPanel.add(respondTFild);
		
		updatePanel = new JPanel();
		updatePanel.setLayout(new GridLayout(1, 2));
		updateLanel = new JLabel("menjaj sadrzaj:");
		updateFild = new JTextField("10");
		updatePanel.add(updateLanel);
		updatePanel.add(updateFild);
		
		timerPanel = new JPanel();
		timerPanel.setLayout(new GridLayout(1, 5));
		timerPanel.setBorder(new LineBorder(Color.WHITE, 10));
		timerPanel.add(threadPanel);
		timerPanel.add(repeatPanel);
		timerPanel.add(failedMPanel);
		timerPanel.add(respondTPanel);
		timerPanel.add(updatePanel);
		
		botPanel.add(timerPanel);

		this.add(botPanel);

		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(2, 1));
		this.setSize(900, 500);
		this.setResizable(false);
		this.setLocation(100, 100);
	}

	// Ovaj metod sluzi za upacivanje osluskivaca i za dugme koje sluzi za
	// pokretanje podservera.
	@Override
	public void setButtens() {
		startServer.addActionListener(e -> {
			startServer.setEnabled(false);
			port = Integer.parseInt(inPortFild.getText());
			threadN = Integer.parseInt(threadFild.getText());
			repeatSendingMessage = Integer.parseInt(repeatFild.getText());
			failedMessages = Integer.parseInt(failedMFild.getText());
			notRespondTime = Integer.parseInt(respondTFild.getText());
			updateCheckTime  = Integer.parseInt(updateFild.getText());
			MainServer mServer = new MainServer(port, threadN, repeatSendingMessage, failedMessages, notRespondTime, updateCheckTime, this);
			setLog("---Glavni server je pokrenut na portu " + port + "---");
			mServer.start();
		});
	}

	// Ovaj metod sluzi za ubacivanje tekstuelnih poruka u log glavnog servera.
	@Override
	public void setLog(String logText) {
		logList.add(logText + "\n");
		if (logList.size() > 12)
			logList.remove(0);
		this.logMainServer.setText(logList.get(0));
		for (int i = 1; i < logList.size(); i++)
			this.logMainServer.setText(logMainServer.getText() + logList.get(i));
	}

	public static void main(String[] args) {
		new MainServerGUI();
	}

}
