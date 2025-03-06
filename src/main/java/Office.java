

import interfaces.IHouse;
import interfaces.IOffice;
import interfaces.ISewagePlant;
import interfaces.ITanker;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class Office implements IOffice {
    private static int id = 1;

    private JTextArea tankListTextArea;
    private JTextField setPayOffInput;
    private JTextField getStatusInput;
    private JTextField getStatusOutput;
    private JTextArea requestTextArea;

    private int port;
    private final String host;
    private Map<Integer, ITanker> tankers = new HashMap<>();
    private Map<Integer, Boolean> tankersIsEnable = new HashMap<>();

    private ISewagePlant is;

    public Office() throws IOException {
        host = InetAddress.getLocalHost().getHostAddress();
        System.out.print(host);
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Office");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350);
        frame.setLayout(new GridLayout(4, 1));

        // Текстовое поле для списка цистерн
        tankListTextArea = new JTextArea();
        tankListTextArea.setBorder(BorderFactory.createTitledBorder("Tank List"));
        frame.add(new JScrollPane(tankListTextArea));

        // Поле для setPayOff
        JPanel setPayOffPanel = new JPanel(new FlowLayout());
        setPayOffPanel.setBorder(BorderFactory.createTitledBorder("Set Pay Off"));
        setPayOffInput = new JTextField(10);
        JButton setPayOffButton = new JButton("Set");
        setPayOffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int number = Integer.parseInt(setPayOffInput.getText());
                try {
                    setPayoff(number);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        setPayOffPanel.add(new JLabel("Number:"));
        setPayOffPanel.add(setPayOffInput);
        setPayOffPanel.add(setPayOffButton);
        frame.add(setPayOffPanel);

        // Поле для getStatus
        JPanel getStatusPanel = new JPanel(new FlowLayout());
        getStatusPanel.setBorder(BorderFactory.createTitledBorder("Get Status"));
        getStatusInput = new JTextField(10);
        getStatusOutput = new JTextField(10);
        getStatusOutput.setEditable(false);
        JButton getStatusButton = new JButton("Get");
        getStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int number = Integer.parseInt(getStatusInput.getText());
                int status = 0;
                try {
                    status = getStatus(number);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                getStatusOutput.setText(String.valueOf(status));
            }
        });
        getStatusPanel.add(new JLabel("Number:"));
        getStatusPanel.add(getStatusInput);
        getStatusPanel.add(getStatusButton);
        getStatusPanel.add(new JLabel("Volume:"));
        getStatusPanel.add(getStatusOutput);
        frame.add(getStatusPanel);


        requestTextArea = new JTextArea();
        requestTextArea.setBorder(BorderFactory.createTitledBorder("Request"));
        frame.add(new JScrollPane(requestTextArea));

        frame.setVisible(true);
    }

    @Override
    public int register(ITanker tanker, String s) throws RemoteException {

        if(tankers.containsValue(tanker))return 0;

        int number = id++;
        tankers.put(number, tanker);
        tankersIsEnable.put(number, false);

        System.out.println("Tanker #" + number + " registered");
        updateTankList();
        return number;
    }
    @Override
    public int order(IHouse house, String s) throws RemoteException  {
        for (int i=1;i<=tankersIsEnable.size();i++) {
            if(tankersIsEnable.get(i)){
                tankers.get(i).setJob(house);
                tankersIsEnable.replace(i, false);
                updateTankList();
                return 1;
            }
        }
        return 0;
    }
    void updateTankList(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StringBuilder tankListText = new StringBuilder("Tank List:\n");
                for (int i=1;i<=tankersIsEnable.size();i++) {
                        String isEnabled = tankersIsEnable.get(i) ? "Enabled" : "Disabled";
                        tankListText.append("Id: ").append(i).append(" - ").append(isEnabled).append("\n");
                }
                tankListTextArea.setText(tankListText.toString());
            }
        });
    }

    public void setReadyToServe(int number){
        tankersIsEnable.replace(number, true);
        System.out.println("number: "+number);
        updateTankList();
    }
    public void setSewagePlant(ISewagePlant is){
        this.is = is;
    }
    void setPayoff(int number) throws IOException {
        is.setPayoff(number);
    }
    int getStatus(int number) throws IOException {
        return is.getStatus(number);
    }

    public static void main(String[] args) throws IOException {
        try {
            Office office = new Office();
            IOffice io = (IOffice) UnicastRemoteObject.exportObject(office,0);
            Registry registry = LocateRegistry.getRegistry("localhost",2000);
            registry.rebind("Office", io);
            office.setSewagePlant ((ISewagePlant) registry.lookup("SewagePlant"));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
