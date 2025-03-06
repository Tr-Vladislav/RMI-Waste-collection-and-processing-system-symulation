

import interfaces.IOffice;
import interfaces.ISewagePlant;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SewagePlant implements ISewagePlant{
    private JTextArea statisticsTextArea;

    private int port;
    private final String host;
    private Map<Integer, Integer> tankerVolumes = new HashMap<>();

    public SewagePlant() throws IOException {
        host = InetAddress.getLocalHost().getHostAddress();
        System.out.print(host);
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Sewage Plant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Поле для отрисовки статистики
        statisticsTextArea = new JTextArea();
        statisticsTextArea.setEditable(false);
        statisticsTextArea.setBorder(BorderFactory.createTitledBorder("Statistics"));
        frame.add(new JScrollPane(statisticsTextArea), BorderLayout.CENTER);

        // Обновление статистики при старте
        //updateStatistics();

        frame.setVisible(true);
        System.out.println("Sewage Plant started.");
    }
    private void updateStatistics() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Генерация текста статистики в фоновом режиме
                StringBuilder statisticsText = new StringBuilder("Tanker Volumes:\n");
                for (Map.Entry<Integer, Integer> entry : tankerVolumes.entrySet()) {
                    statisticsText.append("Tanker ").append(entry.getKey())
                            .append(": ").append(entry.getValue())
                            .append(" units\n");
                }
                // Установка текста в текстовое поле должна выполняться в Event Dispatch Thread
                SwingUtilities.invokeLater(() -> statisticsTextArea.setText(statisticsText.toString()));
                return null;
            }

            @Override
            protected void done() {
                // В случае необходимости можно добавить дополнительную обработку после завершения задачи
                System.out.println("Statistics updated.");
            }
        };
        worker.execute();
    }


    public void setPumpIn(int number, int volume) {
        tankerVolumes.put(number, tankerVolumes.getOrDefault(number, 0) + volume);
        updateStatistics();
    }

    public int getStatus(int number){
        if(tankerVolumes.containsKey(number))return tankerVolumes.get(number);
        return 0;
    }

    public void setPayoff(int number){
        tankerVolumes.remove(number);
        updateStatistics();
    }
    public static void main(String[] args) throws IOException {
        try {
            SewagePlant sewagePlant = new SewagePlant();
            ISewagePlant is = (ISewagePlant) UnicastRemoteObject.exportObject(sewagePlant,0);
            Registry registry = LocateRegistry.getRegistry("localhost",2000);
            registry.rebind("SewagePlant", is);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
