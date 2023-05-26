package multiServerClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MultiClient {
    private JTextField nameField;
    private JButton loginButton;
    private JTextField fileField;
    private JTextField fileField1;
    private JButton sendButton;
    private JButton sendButton1;
    private JTextArea chatArea;

    private JTextArea updateCheckArea;

    private JButton checkButton;

    private Socket socket;
    private BufferedReader in;
    private PrintStream out;

    public static void main(String[] args) {
        MultiClient multiClient = new MultiClient();
        multiClient.start();
    }

    public void start() {
        try {
            socket = new Socket("localhost", 8000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());
            initializeUI();
            loginButton.addActionListener(new LoginButtonListener());
            sendButton.addActionListener(new SendButtonListener());
            sendButton1.addActionListener(new SendButton1Listener());
            checkButton.addActionListener(new CheckButtonListener());
            new ReceiveThread().start(); // Start a new thread to receive messages from the server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        JFrame frame = new JFrame("MultiClient");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(null);

        nameField = new JTextField();
        nameField.setBounds(20, 20, 200, 30);
        frame.add(nameField);

        loginButton = new JButton("로그인");
        loginButton.setBounds(240, 20, 100, 30);
        frame.add(loginButton);

        fileField1 = new JTextField();
        fileField1.setBounds(20, 60, 100, 30);
        frame.add(fileField1);

        sendButton1 = new JButton("파일 전송");
        sendButton1.setBounds(130, 60, 100, 30);
        frame.add(sendButton1);

        fileField = new JTextField();
        fileField.setBounds(250, 60, 100, 30);
        frame.add(fileField);

        sendButton = new JButton("파일 전송");
        sendButton.setBounds(360, 60, 100, 30);
        frame.add(sendButton);

        chatArea = new JTextArea();
        chatArea.setBounds(20, 100, 320, 240);
        chatArea.setEditable(false);
        frame.add(chatArea);

        updateCheckArea = new JTextArea();
        updateCheckArea.setBounds(20, 350, 200, 30);
        frame.add(updateCheckArea);

        checkButton = new JButton("파일 업데이트 탐지");
        checkButton.setBounds(240, 350, 150, 30);
        frame.add(checkButton);

        frame.setVisible(true);
    }

    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            try {
                // Send the name to the server
                out.println(name);
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = fileField.getText();
            try {
                // Send the file name to the server
                out.println(fileName);
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class SendButton1Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = fileField1.getText();
            try {
                // Send the file name to the server
                out.println(fileName);
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private class CheckButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = updateCheckArea.getText();
            try {
                // Send the file name to the server for update check
                out.println("CHECK" + fileName);
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    // Receive messages from the server and display them in the chat area
                    chatArea.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
