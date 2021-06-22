package PDFAnalyzerActors.View;

import PDFAnalyzerActors.Controller.Controller;
import PDFAnalyzerActors.Model.Occurrence;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class ShowGUI extends JFrame implements ActionListener {

    private final JLabel lblDirectoryPDF;
    private final JLabel lblFileToIgnore;
    private final JLabel lblOccurrences;
    private final JLabel lblTotalWords;
    private final JLabel lblOccurrencesRetrieve;
    private final JLabel lblErrorRequiredField;
    private final JButton btnStart;
    private final JButton btnStop;
    private final JButton btnDirectoryChooser;
    private final JButton btnToIgnoreFileChooser;
    private final JTextField wordsNumberTextField;
    private final JLabel lblShowOccurrences;
    private boolean directoryIsSet;
    private Controller controller;
    private JTextField counterWords;
    private JLabel lblChrono;
    private JCheckBox chckbxSplitter;

    private static final String TITLE = "PDF Analyzer";
    private static final String DIR_CHOOSER_LBL = "Directory PDF";
    private static final String TOIGNFILE_CHOOSER_LBL = "File con parole da ignorare";
    private static final String TOIGNFILE_CHOOSER = "Scegli file";
    private static final String DIR_CHOOSER = "Scegli directory";
    private static final String N_WORDS_LBL = "Numero di occorrenze massime";
    private static final String REQ_FIELD_ERR_MSG = "Attenzione, inserire almeno directory pdf e numero di occorrenze";
    private static final String OCCURRENCE = "Occorrenze:";
    private static final String AN_WORDS_LBL = "Totale parole analizzate";

    enum Choice {
        DIRPDF, TOIGNFILE
    }

    public ShowGUI(Controller controller) {

        this.controller = controller;

        setFont(new Font("Tahoma", Font.PLAIN, 16));
        setTitle(TITLE);
        setSize(1200, 600);
        getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 16));

        JPanel panel = new JPanel();
        panel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panel,
                GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panel,
                GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE));

        lblDirectoryPDF = new JLabel(DIR_CHOOSER_LBL);
        lblDirectoryPDF.setFont(new Font("Tahoma", Font.PLAIN, 16));

        lblOccurrences = new JLabel(N_WORDS_LBL);
        lblOccurrences.setFont(new Font("Tahoma", Font.PLAIN, 16));

        wordsNumberTextField = new JTextField();
        wordsNumberTextField.setFont(new Font("Tahoma", Font.PLAIN, 16));
        wordsNumberTextField.setColumns(10);

        lblFileToIgnore = new JLabel(TOIGNFILE_CHOOSER_LBL);
        lblFileToIgnore.setFont(new Font("Tahoma", Font.PLAIN, 16));

        btnStop = new JButton("Stop");
        btnStop.setFont(new Font("Tahoma", Font.PLAIN, 16));
        btnStop.setEnabled(false);

        btnStart = new JButton("Start");
        btnStart.setSelected(true);
        btnStart.setFont(new Font("Tahoma", Font.PLAIN, 16));

        btnDirectoryChooser = new JButton(DIR_CHOOSER);
        btnDirectoryChooser.setEnabled(true);
        btnDirectoryChooser.setFont(new Font("Tahoma", Font.PLAIN, 16));

        btnToIgnoreFileChooser = new JButton(TOIGNFILE_CHOOSER);
        btnToIgnoreFileChooser.setEnabled(true);
        btnToIgnoreFileChooser.setFont(new Font("Tahoma", Font.PLAIN, 16));

        lblTotalWords = new JLabel(AN_WORDS_LBL);
        lblTotalWords.setFont(new Font("Tahoma", Font.PLAIN, 16));

        lblOccurrencesRetrieve = new JLabel("Occorrenze:");
        lblOccurrencesRetrieve.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblOccurrencesRetrieve.setVisible(false);

        lblErrorRequiredField = new JLabel(REQ_FIELD_ERR_MSG);
        lblErrorRequiredField.setForeground(Color.RED);
        lblErrorRequiredField.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblErrorRequiredField.setVisible(false);

        counterWords = new JTextField();
        counterWords.setHorizontalAlignment(SwingConstants.RIGHT);
        counterWords.setFont(new Font("Tahoma", Font.PLAIN, 16));
        counterWords.setEditable(false);
        counterWords.setRequestFocusEnabled(false);
        counterWords.setText("0");
        counterWords.setColumns(10);

        lblShowOccurrences = new JLabel("");
        lblShowOccurrences.setFont(new Font("Tahoma", Font.PLAIN, 16));

        lblChrono = new JLabel("");
        lblChrono.setFont(new Font("Tahoma", Font.PLAIN, 16));
        
        chckbxSplitter = new JCheckBox("Split PDF");
        chckbxSplitter.setFont(new Font("Tahoma", Font.PLAIN, 16));

        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
        	gl_panel.createParallelGroup(Alignment.TRAILING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
        				.addGroup(gl_panel.createSequentialGroup()
        					.addContainerGap()
        					.addComponent(btnStart, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(btnStop, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel.createSequentialGroup()
        					.addGap(32)
        					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        						.addComponent(lblOccurrencesRetrieve, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
        						.addComponent(lblShowOccurrences, GroupLayout.DEFAULT_SIZE, 1054, Short.MAX_VALUE)
        						.addComponent(lblChrono, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(gl_panel.createSequentialGroup()
        					.addContainerGap()
        					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
        						.addGroup(gl_panel.createSequentialGroup()
        							.addComponent(lblErrorRequiredField, GroupLayout.PREFERRED_SIZE, 581, GroupLayout.PREFERRED_SIZE)
        							.addGap(386)
        							.addComponent(chckbxSplitter, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
        						.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
        							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
        								.addComponent(lblDirectoryPDF, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        								.addComponent(lblOccurrences, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 347, GroupLayout.PREFERRED_SIZE)
        								.addComponent(lblTotalWords, GroupLayout.PREFERRED_SIZE, 222, GroupLayout.PREFERRED_SIZE))
        							.addPreferredGap(ComponentPlacement.RELATED, 580, Short.MAX_VALUE)
        							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
        								.addComponent(btnDirectoryChooser)
        								.addComponent(wordsNumberTextField, 134, 134, 134)
        								.addGroup(gl_panel.createSequentialGroup()
        									.addComponent(counterWords, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        									.addPreferredGap(ComponentPlacement.RELATED))))
        						.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
        							.addComponent(lblFileToIgnore, GroupLayout.PREFERRED_SIZE, 492, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED, 475, Short.MAX_VALUE)
        							.addComponent(btnToIgnoreFileChooser)))
        					.addGap(10)))
        			.addContainerGap())
        );
        gl_panel.setVerticalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lblDirectoryPDF)
        				.addComponent(btnDirectoryChooser))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lblOccurrences)
        				.addComponent(wordsNumberTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lblFileToIgnore)
        				.addComponent(btnToIgnoreFileChooser))
        			.addGap(18)
        			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lblErrorRequiredField)
        				.addComponent(chckbxSplitter))
        			.addGap(34)
        			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lblTotalWords)
        				.addComponent(counterWords, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(35)
        			.addComponent(lblOccurrencesRetrieve)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(lblShowOccurrences, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(lblChrono)
        			.addPreferredGap(ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
        			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(btnStop)
        				.addComponent(btnStart))
        			.addContainerGap())
        );
        panel.setLayout(gl_panel);
        getContentPane().setLayout(groupLayout);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
            public void windowClosed(WindowEvent ev) {
                System.exit(0);
            }
        });

        //add listeners to the button Directory e Files
        btnStart.addActionListener(this);
        btnStop.addActionListener(this);
        btnDirectoryChooser.addActionListener(this);
        btnToIgnoreFileChooser.addActionListener(this);
    }


    public void actionPerformed(ActionEvent ev) {
        Object src = ev.getSource();
        if (src == btnStart) {
            if (checkRequiredFieldIsSet()) {
                controller.setNumberOfWords(Integer.parseInt(wordsNumberTextField.getText()));
                controller.notifyStarted();
                controller.setSplit(chckbxSplitter.isSelected());
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                lblErrorRequiredField.setVisible(false);
                lblChrono.setText("");
            } else {
                lblErrorRequiredField.setVisible(true);
            }
        } else if (src == btnStop) {
            controller.notifyStopped();
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            log("PREMUTO STOP");
        } else if (src == btnDirectoryChooser) {
            showPopup(Choice.DIRPDF);
        } else if (src == btnToIgnoreFileChooser) {
            showPopup(Choice.TOIGNFILE);
        }
    }

    public void showPopup(Enum<Choice> choice) {
        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            lblErrorRequiredField.setVisible(false);
            if (choice == Choice.DIRPDF) {
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    controller.setDirectoryPdf(selectedFile.getAbsolutePath());
                    lblDirectoryPDF.setText(selectedFile.getAbsolutePath());
                    this.directoryIsSet = true;
                }
            } else if (choice == Choice.TOIGNFILE) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
                fileChooser.setFileFilter(filter);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    controller.setToIgnoreFile(selectedFile.getAbsolutePath());
                    lblFileToIgnore.setText(selectedFile.getAbsolutePath());
                }
            }
        });
    }

    public boolean checkRequiredFieldIsSet() {
        return this.directoryIsSet && !wordsNumberTextField.getText().equals("");
    }

    public void updateCountValue(int value) {
        SwingUtilities.invokeLater(() -> {
            counterWords.setText("" + value);
        });
    }

    public void updateOccurrencesLabel(List<Occurrence> occurrences) {
        SwingUtilities.invokeLater(() -> {
            lblOccurrencesRetrieve.setVisible(true);
            lblShowOccurrences.setText("");
            occurrences.forEach(elem -> lblShowOccurrences.setText(lblShowOccurrences.getText() + "[" + elem.getWord() + ": " + elem.getCount() + "] "));
        });
    }

    public void updateComplete(double time) {
        SwingUtilities.invokeLater(() -> {
            lblChrono.setText("Completato in: " + time + " secondi");
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        });
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        });
    }

    private void log(String s) {
        System.out.println(s);
    }
}