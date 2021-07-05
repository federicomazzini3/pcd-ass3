package PuzzleActors;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Cursor;

public class View extends JFrame {
	private JTextField txtFldPublicAddress;
	private JTextField txtFldPublicPort;
	private JTextField txtFldLocalPort;
	private JTextField txtFldFriendAddress;
	private JLabel lblPublicAddress;
	private JLabel lblPublicPort;
	private JLabel lblLocalPort;
	private JLabel lblFriendAddress;


    public View() {
    	setResizable(false);
        setTitle("PuzzleCentralized");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(428, 396);
        
        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 411, 356);
        
        JButton btnJoinGame = new JButton("Join a game");
        btnJoinGame.setBounds(280, 320, 109, 25);
        btnJoinGame.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        JButton btnStartGame = new JButton("Start new game");
        btnStartGame.setBounds(137, 320, 133, 25);
        btnStartGame.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnStartGame.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        	}
        });
        getContentPane().setLayout(null);
        
        txtFldPublicAddress = new JTextField();
        txtFldPublicAddress.setBounds(137, 30, 206, 23);
        txtFldPublicAddress.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        txtFldPublicAddress.setToolTipText("");
        txtFldPublicAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldPublicAddress.setColumns(10);
        
        txtFldPublicPort = new JTextField();
        txtFldPublicPort.setBounds(137, 64, 206, 23);
        txtFldPublicPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldPublicPort.setColumns(10);
        
        txtFldLocalPort = new JTextField();
        txtFldLocalPort.setBounds(137, 98, 206, 23);
        txtFldLocalPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldLocalPort.setColumns(10);
        
        txtFldFriendAddress = new JTextField();
        txtFldFriendAddress.setBounds(137, 132, 206, 23);
        txtFldFriendAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldFriendAddress.setColumns(10);
        
        lblPublicAddress = new JLabel("Public Address");
        lblPublicAddress.setBounds(20, 30, 107, 20);
        lblPublicAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        lblPublicPort = new JLabel("Public port");
        lblPublicPort.setBounds(20, 66, 107, 17);
        lblPublicPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        lblLocalPort = new JLabel("Local port");
        lblLocalPort.setBounds(20, 100, 107, 17);
        lblLocalPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        lblFriendAddress = new JLabel("Friend Address");
        lblFriendAddress.setBounds(20, 134, 107, 17);
        lblFriendAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
        panel.setLayout(null);
        panel.add(btnStartGame);
        panel.add(btnJoinGame);
        panel.add(lblPublicAddress);
        panel.add(lblPublicPort);
        panel.add(lblLocalPort);
        panel.add(lblFriendAddress);
        panel.add(txtFldFriendAddress);
        panel.add(txtFldLocalPort);
        panel.add(txtFldPublicPort);
        panel.add(txtFldPublicAddress);
        getContentPane().add(panel);
        this.setVisible(true);

    }
}
