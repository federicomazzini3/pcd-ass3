package PuzzleActors;

import javax.swing.*;

public class View extends JFrame {


    public View() {
        setTitle("PuzzleCentralized");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 300);
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        this.getContentPane().add(button1);
        this.getContentPane().add(button2);
        this.setVisible(true);

    }
}
