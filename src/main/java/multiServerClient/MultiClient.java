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



    private JTextArea updateCheckArea;
    private JTextArea syncArea;

    private JButton checkButton;
    private JButton syncButton;

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
        chatArea.setBounds(20, 100, 400, 240);
        chatArea.setEditable(false);
        frame.add(chatArea);

        updateCheckArea = new JTextArea();
        updateCheckArea.setBounds(20, 350, 200, 30);
        frame.add(updateCheckArea);

        checkButton = new JButton("파일 업데이트&삭제 탐지");
        checkButton.setBounds(240, 350, 240, 30);
        frame.add(checkButton);


        syncArea = new JTextArea();
        syncArea.setBounds(20, 390, 200, 30);
        frame.add(syncArea);



        syncButton = new JButton("파일 동기화");
        syncButton.setBounds(240, 390, 240, 30);
        frame.add(syncButton);


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
                FileInputStream fin=new FileInputStream(clientRepositoryPath+fileName);
                // Send the file name to the server
                byte[] buffer = new byte[1024];        //바이트단위로 임시저장하는 버퍼를 생성합니다.
                int len;                               //전송할 데이터의 길이를 측정하는 변수입니다.
                int data=0;                            //전송횟수, 용량을 측정하는 변수입니다.

                while((len = fin.read(buffer))>0){     //FileInputStream을 통해 파일에서 입력받은 데이터를 버퍼에 임시저장하고 그 길이를 측정합니다.
                    data++;                        //데이터의 양을 측정합니다.
                }

                int datas = data;                      //아래 for문을 통해 data가 0이되기때문에 임시저장한다.

                fin.close();
                fin = new FileInputStream(clientRepositoryPath+fileName);   //FileInputStream이 만료되었으니 새롭게 개통합니다.
                                  //데이터 전송횟수를 서버에 전송하고,

                len = 0;

                for(;data>0;data--){                   //데이터를 읽어올 횟수만큼 FileInputStream에서 파일의 내용을 읽어옵니다.
                    len = fin.read(buffer);        //FileInputStream을 통해 파일에서 입력받은 데이터를 버퍼에 임시저장하고 그 길이를 측정합니다.
                    dataOutputStream.write(buffer,0,0);       //서버에게 파일의 정보(1kbyte만큼보내고, 그 길이를 보냅니다.
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
            try {
                // Send the file name to the server for update check

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
                // Send the file name to the server for update check

                out.println( "sync"+fileName);
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
