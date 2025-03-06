
import interfaces.IHouse;
import interfaces.IOffice;
import interfaces.ISewagePlant;
import interfaces.ITanker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Tanker implements ITanker, Serializable {
    private int volume;
    private int currentWaste = 0;
    private int port;
    private final String host;
    private int Id = 0;
    private IOffice io;
    private ISewagePlant is;

    private JTextField tankerNumberField;
    private JTextField tankerVolumeField;
    private JTextField tankerFillLevelField;

    private JTextArea statusTextArea;


    public Tanker(int volume) throws IOException {
        this.volume = volume;
        this.port = port;
        host = InetAddress.getLocalHost().getHostAddress();

        UnicastRemoteObject.exportObject(this, 0);

        try {
            Registry r = LocateRegistry.getRegistry(2000);
            io = (IOffice) r.lookup("Office");
            is = (ISewagePlant) r.lookup("SewagePlant");
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }

        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Tanker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Текстовое поле для отображения текущего статуса цистерны
        statusTextArea = new JTextArea();
        statusTextArea.setBorder(BorderFactory.createTitledBorder("Current Status"));
        frame.add(new JScrollPane(statusTextArea), BorderLayout.CENTER);
        statusTextArea.setText("Disabled");

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(1, 5));

        // Текстовое поле для номера цистерны (правый верхний угол)
        tankerNumberField = new JTextField("Id: -");
        tankerNumberField.setEditable(false);
        tankerNumberField.setHorizontalAlignment(SwingConstants.RIGHT);
        infoPanel.add(tankerNumberField);

        // Текстовое поле для объема цистерны (левый угол)
        tankerVolumeField = new JTextField("Volume: "+volume+"L");
        tankerVolumeField.setEditable(false);
        tankerVolumeField.setHorizontalAlignment(SwingConstants.LEFT);
        infoPanel.add(tankerVolumeField);

        // Текстовое поле для текущей заполненности цистерны (центр)
        tankerFillLevelField = new JTextField("Current waste: "+currentWaste);
        tankerFillLevelField.setEditable(false);
        tankerFillLevelField.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(tankerFillLevelField);

        frame.add(infoPanel, BorderLayout.NORTH);


        // Панель для кнопок
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        // Кнопка для setReadyToServe
        JButton readyToServeButton = new JButton("Set Ready to Serve");
        readyToServeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setReadyToServe();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        buttonPanel.add(readyToServeButton);

        // Кнопка для register
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        buttonPanel.add(registerButton);

        // Кнопка для setPumpIn
        JButton pumpInButton = new JButton("Pump In");
        pumpInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setPumpIn();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        buttonPanel.add(pumpInButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void setReadyToServe() throws IOException {
        io.setReadyToServe(Id);
        statusTextArea.setText("Enabled");
    }

    private void register() {
        try {
            Id = io.register(this, "");
            tankerNumberField.setText("Id:"+Id);
            System.out.println("Tanker registered: " + Id + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void setJob(IHouse house) throws RemoteException {
        System.out.println("get order");

        currentWaste = house.getPumpOut(volume);
        tankerFillLevelField.setText("Current waste: "+currentWaste);


        statusTextArea.setText("Took waste from house");

    }
    void setPumpIn() throws IOException {
        is.setPumpIn(Id, currentWaste);
        currentWaste = 0;
        tankerFillLevelField.setText("Current waste: "+currentWaste);
        setReadyToServe();

    }

    public static void main(String[] args) throws IOException {
        try {
            Tanker tanker = new Tanker(800);
            new Tanker(500);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
