package persistent;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

public class GUI extends Main{

	private JFrame frmCscAssignment;
	private JTextField url;

	public void runGUI() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmCscAssignment.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public GUI() {
		initialize();
	}
	
	private void initialize() {
		frmCscAssignment = new JFrame();
		frmCscAssignment.setBackground(Color.WHITE);
		frmCscAssignment.setForeground(Color.LIGHT_GRAY);
		frmCscAssignment.setTitle("CSC365 Assignment #2");
		frmCscAssignment.getContentPane().setBackground(Color.BLACK);
		frmCscAssignment.setBounds(100, 100, 450, 300);
		frmCscAssignment.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmCscAssignment.getContentPane().setLayout(null);
		
		url = new JTextField();
		url.setForeground(Color.CYAN);
		url.setBackground(Color.DARK_GRAY);
		url.setToolTipText("");
		url.setBounds(103, 11, 321, 20);
		frmCscAssignment.getContentPane().add(url);
		url.setColumns(10);
		
		JTextPane statusText = new JTextPane();
		statusText.setText("IDLE");
		statusText.setBackground(Color.DARK_GRAY);
		statusText.setEditable(false);
		statusText.setForeground(Color.CYAN);
		statusText.setBounds(81, 36, 136, 20);
		frmCscAssignment.getContentPane().add(statusText);
		
		JTextPane printScreen = new JTextPane();
		printScreen.setEditable(false);
		printScreen.setForeground(Color.CYAN);
		printScreen.setBackground(Color.DARK_GRAY);
		printScreen.setBounds(10, 61, 414, 189);
		frmCscAssignment.getContentPane().add(printScreen);
		
		JButton search = new JButton("Compare");
		search.setForeground(Color.CYAN);
		search.setBackground(Color.DARK_GRAY);
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String userURL = url.getText();
				statusText.setForeground(Color.RED);
				statusText.setText("Comparing to all webpages..");
				String output = Main.compareURL(userURL);
				printScreen.setText(output);
				statusText.setForeground(Color.GREEN);
				statusText.setText("Done comparing");
			}
		});
		search.setBounds(10, 10, 89, 23);
		frmCscAssignment.getContentPane().add(search);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 61, 414, 189);
		frmCscAssignment.getContentPane().add(scrollPane);
		
		JLabel lblStatus = new JLabel("Status:");
		lblStatus.setForeground(Color.CYAN);
		lblStatus.setBounds(25, 36, 46, 14);
		frmCscAssignment.getContentPane().add(lblStatus);
		
		JButton clearStatus = new JButton("Clear Status");
		clearStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				statusText.setForeground(Color.CYAN);
				statusText.setText("IDLE");
			}
		});
		clearStatus.setForeground(Color.CYAN);
		clearStatus.setBackground(Color.DARK_GRAY);
		clearStatus.setBounds(234, 37, 121, 19);
		frmCscAssignment.getContentPane().add(clearStatus);
	}
}
