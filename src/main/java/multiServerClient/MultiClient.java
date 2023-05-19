package multiServerClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class MultiClient {
    private JTextField nameField;
    private JButton loginButton;
    private JTextField fileField;
    private JButton sendButton;
    private JTextArea chatArea;
    private Socket socket;
    private PrintStream out;

    public static void main(String[] args) {
        MultiClient multiClient = new MultiClient();
        multiClient.start();
    }

    public void start() {
        try {
            socket = new Socket("localhost", 8000);
            out = new PrintStream(socket.getOutputStream());
            initializeUI();
            loginButton.addActionListener(new LoginButtonListener());
            sendButton.addActionListener(new SendButtonListener());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        JFrame frame = new JFrame("MultiClient");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(null);

        nameField = new JTextField();
        nameField.setBounds(20, 20, 200, 30);
        frame.add(nameField);

        loginButton = new JButton("로그인");
        loginButton.setBounds(240, 20, 100, 30);
        frame.add(loginButton);

        fileField = new JTextField();
        fileField.setBounds(20, 60, 200, 30);
        frame.add(fileField);

        sendButton = new JButton("파일 전송");
        sendButton.setBounds(240, 60, 100, 30);
        frame.add(sendButton);

        chatArea = new JTextArea();
        chatArea.setBounds(20, 100, 320, 240);
        chatArea.setEditable(false);
        frame.add(chatArea);

        frame.setVisible(true);
    }

    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            out.println(name);
            out.flush();
        }
    }

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = fileField.getText();
            out.println(fileName);
            out.flush();
        }
    }
}

