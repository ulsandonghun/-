package multiServerClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class MultiClient {
    private JTextField nameField;
    private JButton loginButton;
    private JTextField fileField;
    private JTextField fileField1;
    private JButton sendButton;
    private JButton sendButton1;
    private JTextArea chatArea;
private String name;


    private JTextArea updateCheckArea;
    private JTextArea syncArea;

    private JButton checkButton;
    private JButton syncButton;

    private JTextArea sendClientArea;
    private JButton sendClientButton;

    private Socket socket;
    private BufferedReader in;
    private PrintStream out;

    private DataOutputStream dataOutputStream;

    private String clientRepositoryPath = "C:\\WinterSchool-spring\\분산시스템과제\\client\\";

    public static void main(String[] args) {
        MultiClient multiClient = new MultiClient();
        multiClient.start();
    }

    public void start() {
        try {
            socket = new Socket("localhost", 8000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());
            dataOutputStream=new DataOutputStream(socket.getOutputStream());
            initializeUI();
            loginButton.addActionListener(new LoginButtonListener());
            sendButton.addActionListener(new SendButtonListener());
            sendButton1.addActionListener(new SendButton1Listener());
            checkButton.addActionListener(new CheckButtonListener());
            syncButton.addActionListener(new syncbuttonListener());
            sendClientButton.addActionListener(new sendClientListener());
            new ReceiveThread().start();
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
        chatArea.setBounds(20, 100, 400, 240);
        chatArea.setEditable(false);
        frame.add(chatArea);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBounds(20, 100, 400, 240);
        frame.add(scrollPane);

        updateCheckArea = new JTextArea();
        updateCheckArea.setBounds(20, 350, 200, 30);
        frame.add(updateCheckArea);

        checkButton = new JButton("파일 변화 및 삭제 탐지");
        checkButton.setBounds(240, 350, 240, 30);
        frame.add(checkButton);


        syncArea = new JTextArea();
        syncArea.setBounds(20, 390, 200, 30);
        frame.add(syncArea);



        syncButton = new JButton("파일 동기화");
        syncButton.setBounds(240, 390, 240, 30);
        frame.add(syncButton);


        sendClientArea = new JTextArea();
        sendClientArea.setBounds(20, 430, 200, 30);
        frame.add(sendClientArea);



        sendClientButton = new JButton("클라이언트에게 파일 전송");
        sendClientButton.setBounds(240, 430, 240, 30);
        frame.add(sendClientButton);




        frame.setVisible(true);
    }

    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            name = nameField.getText();
            try {

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
                FileInputStream fin=new FileInputStream(clientRepositoryPath+name+"\\"+fileName);

                byte[] buffer = new byte[1024];
                int len;
                int data=0;

                while((len = fin.read(buffer))>0){
                    data++;
                }

                int datas = data;

                fin.close();
                fin = new FileInputStream(clientRepositoryPath+name+"\\"+fileName);


                len = 0;

                for(;data>0;data--){
                    len = fin.read(buffer);
                    dataOutputStream.write(buffer,0,0);
                }


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
           try{

                out.println("CHECK" + fileName);
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class syncbuttonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = syncArea.getText();
            try {


                out.println( "sync"+fileName);
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class sendClientListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = sendClientArea.getText();
            try {


                out.println( "send"+fileName);
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

                    chatArea.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
