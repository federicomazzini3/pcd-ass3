package PuzzleActors;

import akka.actor.typed.ActorSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.UnknownHostException;

public class InitialView extends JFrame implements ActionListener {
    private final JTextField txtFldRows;
    private final JTextField txtFldColumns;
    private final JTextField txtFldImageUrl;
    private final JTextField txtFldPublicAddress;
    private final JTextField txtFldPublicPort;
    private final JTextField txtFldLocalPort;
    private final JTextField txtFldFriendAddress;
    private final JLabel lblRows;
    private final JLabel lblColumns;
    private final JLabel lblImageUrl;
    private final JLabel lblPublicAddress;
    private final JLabel lblPublicPort;
    private final JLabel lblLocalPort;
    private final JLabel lblFriendAddress;
    private final JButton btnStartGame;
    private final JButton btnJoinGame;
    private final JButton btnStop;
    private ActorSystem<Void> system;

    public InitialView() {
    	setResizable(false);
        setTitle("PuzzleDecentralized");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(381, 420);
      
        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 377, 393);
        getContentPane().setLayout(null);

        txtFldRows = new JTextField();
        txtFldRows.setText("3");
        txtFldRows.setBounds(137, 30, 206, 23);
        txtFldRows.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldRows.setColumns(10);

        lblRows = new JLabel("Rows");
        lblRows.setBounds(20, 30, 107, 23);
        lblRows.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txtFldColumns = new JTextField();
        txtFldColumns.setText("5");
        txtFldColumns.setBounds(137, 64, 206, 23);
        txtFldColumns.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldColumns.setColumns(10);

        lblColumns = new JLabel("Columns");
        lblColumns.setBounds(20, 64, 107, 23);
        lblColumns.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txtFldImageUrl = new JTextField();
        txtFldImageUrl.setText("https://cdn.tuttosport.com/images/2015/07/15/120422051-727e4986-e8c0-48db-8e7f-2ee1162e92ac.jpg");
        txtFldImageUrl.setBounds(137, 98, 206, 23);
        txtFldImageUrl.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldImageUrl.setColumns(10);

        lblImageUrl = new JLabel("Image Url");
        lblImageUrl.setBounds(20, 98, 107, 23);
        lblImageUrl.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txtFldPublicAddress = new JTextField();
        txtFldPublicAddress.setText("127.0.0.1");
        txtFldPublicAddress.setBounds(137, 132, 206, 23);
        txtFldPublicAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldPublicAddress.setColumns(10);

        lblPublicAddress = new JLabel("Public Address");
        lblPublicAddress.setBounds(20, 132, 107, 20);
        lblPublicAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txtFldPublicPort = new JTextField();
        txtFldPublicPort.setText("25251");
        txtFldPublicPort.setBounds(137, 166, 206, 23);
        txtFldPublicPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldPublicPort.setColumns(10);

        lblPublicPort = new JLabel("Public port");
        lblPublicPort.setBounds(20, 164, 107, 23);
        lblPublicPort.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txtFldLocalPort = new JTextField();
        txtFldLocalPort.setText("25251");
        txtFldLocalPort.setBounds(137, 200, 206, 23);
        txtFldLocalPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldLocalPort.setColumns(10);

        lblLocalPort = new JLabel("Local port");
        lblLocalPort.setBounds(20, 200, 107, 23);
        lblLocalPort.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txtFldFriendAddress = new JTextField();
        txtFldFriendAddress.setText("127.0.0.1:25251");
        txtFldFriendAddress.setBounds(137, 234, 206, 23);
        txtFldFriendAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtFldFriendAddress.setColumns(10);

        lblFriendAddress = new JLabel("Friend Address");
        lblFriendAddress.setBounds(20, 234, 107, 23);
        lblFriendAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));

        btnJoinGame = new JButton("Join a game");
        btnJoinGame.setBounds(185, 277, 165, 41);
        btnJoinGame.setFont(new Font("Tahoma", Font.PLAIN, 14));

        btnStartGame = new JButton("Start new game");
        btnStartGame.setBounds(15, 277, 165, 41);
        btnStartGame.setFont(new Font("Tahoma", Font.PLAIN, 14));

        btnStop = new JButton("Stop game");
        btnStop.setBounds(15, 329, 335, 41);
        btnStop.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnStop.setEnabled(false);

        panel.setLayout(null);
        panel.add(btnStartGame);
        panel.add(btnJoinGame);
        panel.add(btnStop);
        panel.add(lblPublicAddress);
        panel.add(lblRows);
        panel.add(lblColumns);
        panel.add(lblImageUrl);
        panel.add(lblPublicPort);
        panel.add(lblLocalPort);
        panel.add(lblFriendAddress);
        panel.add(txtFldRows);
        panel.add(txtFldColumns);
        panel.add(txtFldImageUrl);
        panel.add(txtFldFriendAddress);
        panel.add(txtFldLocalPort);
        panel.add(txtFldPublicPort);
        panel.add(txtFldPublicAddress);
        getContentPane().add(panel);
        this.setVisible(true);
        btnJoinGame.addActionListener(this);
        btnStartGame.addActionListener(this);
        btnStop.addActionListener(this);
    }

    public void display(boolean flag){
        SwingUtilities.invokeLater(() -> this.setVisible(flag));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String publicAddress = txtFldPublicAddress.getText();
        String publicPort = txtFldPublicPort.getText();
        String localPort = txtFldLocalPort.getText();
        String friendAddress = txtFldFriendAddress.getText();
        String completeAddress = publicAddress + ":" + publicPort;
        String rows = txtFldRows.getText();
        String cols = txtFldColumns.getText();
        String imageUrl = txtFldImageUrl.getText();

        Object src = ev.getSource();
        if (src == btnJoinGame) {
            if (requiredFieldJoinGame()) {
                try {
                    system = Application.startup("player", 0, 0, "", friendAddress, publicAddress, Integer.parseInt(publicPort), Integer.parseInt(localPort));
                    SwingUtilities.invokeLater(() -> {
                        this.btnStop.setEnabled(true);
                        this.btnStartGame.setEnabled(false);
                        this.btnJoinGame.setEnabled(false);
                    });
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            } else System.out.println("Other field required");
        } else if (src == btnStartGame) {
            if (requiredFieldStartGame()) {
                try {
                    system = Application.startup("firstPlayer", Integer.parseInt(rows), Integer.parseInt(cols), imageUrl, completeAddress, publicAddress, Integer.parseInt(publicPort), Integer.parseInt(localPort));
                    SwingUtilities.invokeLater(() -> {
                        this.btnStop.setEnabled(true);
                        this.btnStartGame.setEnabled(false);
                        this.btnJoinGame.setEnabled(false);
                    });
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            } else System.out.println("Other field required");
        } else if (src == btnStop) {
            system.terminate();
            SwingUtilities.invokeLater(() -> {
                this.btnStop.setEnabled(false);
                this.btnStartGame.setEnabled(true);
                this.btnJoinGame.setEnabled(true);
            });
        }
    }

    private boolean requiredFieldStartGame() {
        return !txtFldPublicAddress.getText().isEmpty() &&
                !txtFldPublicPort.getText().isEmpty() &&
                !txtFldLocalPort.getText().isEmpty() &&
                !txtFldRows.getText().isEmpty() &&
                !txtFldColumns.getText().isEmpty() &&
                !txtFldImageUrl.getText().isEmpty();
    }

    private boolean requiredFieldJoinGame() {
        return !txtFldPublicAddress.getText().isEmpty() &&
                !txtFldPublicPort.getText().isEmpty() &&
                !txtFldLocalPort.getText().isEmpty() &&
                !txtFldFriendAddress.getText().isEmpty();
    }
}
