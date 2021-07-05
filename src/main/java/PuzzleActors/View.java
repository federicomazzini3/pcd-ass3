package PuzzleActors;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Cursor;
import java.net.UnknownHostException;

public class View extends JFrame  implements ActionListener {
	private JTextField txtFldPublicAddress;
	private JTextField txtFldPublicPort;
	private JTextField txtFldLocalPort;
	private JTextField txtFldFriendAddress;
	private JLabel lblPublicAddress;
	private JLabel lblPublicPort;
	private JLabel lblLocalPort;
	private JLabel lblFriendAddress;
    private JButton btnStartGame;
    private JButton btnJoinGame;

    public View() {
    	setResizable(false);
        setTitle("PuzzleCentralized");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(362, 276);
        
        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 351, 240);
        
        btnJoinGame = new JButton("Join a game");
        btnJoinGame.setBounds(195, 186, 148, 43);
        btnJoinGame.setFont(new Font("Tahoma", Font.PLAIN, 16));
        
        btnStartGame = new JButton("Start new game");
        btnStartGame.setBounds(20, 186, 165, 43);
        btnStartGame.setFont(new Font("Tahoma", Font.PLAIN, 16));
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
        btnJoinGame.addActionListener(this);
        btnStartGame.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String publicAddress = txtFldPublicAddress.getText();
        int publicPort = Integer.parseInt(txtFldPublicPort.getText());
        int localPort = Integer.parseInt(txtFldLocalPort.getText());
        String friendAddress = txtFldFriendAddress.getText();
        String completeAddress = publicAddress + ":" + publicPort;

        Object src = ev.getSource();
        if (src == btnJoinGame) {
            if(!(txtFldPublicAddress.getText().isEmpty() && txtFldPublicPort.getText().isEmpty() && txtFldLocalPort.getText().isEmpty() && txtFldFriendAddress.getText().isEmpty())){
                try {
                    Application.startup("player", friendAddress, publicAddress, publicPort, localPort);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        } else if (src == btnStartGame) {
            if(!(txtFldPublicAddress.getText().isEmpty() && txtFldPublicPort.getText().isEmpty() && txtFldLocalPort.getText().isEmpty())){
                try {
                    Application.startup("firstPlayer", completeAddress, publicAddress, publicPort, localPort);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
