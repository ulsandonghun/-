package multiServerClient;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MultiServer {
    private List<PrintWriter> clientOutputs;
    private JTextArea chatArea;
    private String serverRepositoryPath;
    private String clientRepositoryPath;

    public static void main(String[] args) {
        MultiServer multiServer = new MultiServer();
        multiServer.start();
    }

    public void start() {
        clientOutputs = new ArrayList<>();
        chatArea = new JTextArea();
        serverRepositoryPath = "C:\\WinterSchool-spring\\분산시스템과제\\server\\";
        clientRepositoryPath = "C:\\WinterSchool-spring\\분산시스템과제\\client\\";

        JFrame frame = new JFrame("MultiServer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(null);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBounds(20, 20, 360, 320);
        frame.add(scrollPane);

        frame.setVisible(true);

        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(8000);
            while (true) {
                chatArea.append("[클라이언트 연결 대기중]\n");
                socket = serverSocket.accept();

                // client가 접속할 때마다 새로운 스레드 생성
                ReceiveThread receiveThread = new ReceiveThread(socket);
                receiveThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    chatArea.append("[서버 종료]\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    chatArea.append("[서버 소켓 통신 에러]\n");
                }
            }
        }
    }

    private class ReceiveThread extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private FileOutputStream fout;
        InputStream inputStream;
        DataInputStream dataInputStream=new DataInputStream(inputStream);

        private HashMap<String,Long> LogicalClock =new HashMap<>();

        public ReceiveThread(Socket socket) throws IOException {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                inputStream=socket.getInputStream();
                clientOutputs.add(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String name = "";
            List<String> sharedmember=new ArrayList<>();
            try {
               name = in.readLine();
                chatArea.append("[" + name + " 새 연결 생성]\n");
                sendAll("[" + name + "]님이 서버에 로그인하였습니다.");
                String clipath=clientRepositoryPath+"\\"+name+"\\";
                String servpath=serverRepositoryPath+"\\"+name+"\\";
                File folder=new File(clipath);
                File folder1=new File(servpath);
                if(!folder.exists()){
                    folder.mkdir();
                };if(!folder1.exists()){
                    folder1.mkdir();
                };

                while (in != null) {


                    String inputMsg = in.readLine();
                    //클라이언트가 보낸 String에서 Check 메세지가 오면 UPdate 프로토콜 시작

                    if(inputMsg.substring(0,4).equals("send")){
                        String format= inputMsg.substring(4,inputMsg.length());
                        int i =format.indexOf('_');
                        String client =format.substring(0,i);
                        sharedmember.add(client);
                        System.out.println("client = " + client);
                        format=format.substring(i+1,format.length());
                        System.out.println("format = " + format);
                        sendAll(name+"님이 "+client+" 님에게 " +format+"파일 을 공유합니다.\n");
                        FileInputStream fis = new FileInputStream(clipath+format);

                        byte[] byteBuff = new byte[20971520];
                        int nRLen = fis.read(byteBuff);
                        String strBuff = new String(byteBuff, 0, nRLen);

                        chatArea.append(String.format("읽은 바이트 수[%d byte]:\n읽은 내용:\n%s\n", nRLen, strBuff));

                        File file = new File(serverRepositoryPath+client+"\\"+format);


                        LogicalClock.put(file.getPath(),file.lastModified());
                        System.out.println("서버의 변경 시간을 LOGICALCLOCK MAP에 저장"+LogicalClock.get(file.getPath()));



                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(byteBuff, 0, nRLen);
                        fos.close();

                        continue;


                    }

                    if(inputMsg.substring(0,4).equals("sync")){
                        sendAll(name+"님의" +inputMsg.substring(4,inputMsg.length())+"파일 동기화를 수행합니다.\n");
                       String checkfile=inputMsg.substring(4,inputMsg.length());
                        String clientFilePath = clipath + checkfile;
                        File deleteFile = new File(clientFilePath);
                        if (!deleteFile.exists()) {
                            chatArea.append(checkfile + " 은 삭제되었습니다.\n");
                            sendAll(checkfile + " 파일은 클라이언트에서 삭제되었습니다.\n");

                            // 서버 저장소에서도 삭제
                            String serverFilePath = servpath+checkfile;
                            File serverFile = new File(serverFilePath);
                            if (serverFile.exists()) {
                                //해당 파일이 클라이언트에 존재하는 확인.
                                serverFile.delete();
                                chatArea.append(name+"_" +checkfile+" 파일을 서버 저장소에서 삭제하였습니다.\n");
                                sendAll(checkfile + " 파일을 서버 저장소에서 삭제하였습니다.\n");
                            }
                            continue;


                        } else {
                            boolean isUpdated = LogicalClockDetect(name, checkfile);

                            if (isUpdated) {
                                chatArea.append(checkfile + " 파일이 변경되었습니다.\n");

                                sendAll(name + "님의 " + checkfile + " 파일이 변경되었습니다.\n");


                                // 변경된 파일에 대한 추가 동작 수행(덮어쓰기)

                                FileInputStream fis = new FileInputStream(clipath+checkfile);

                                byte[] byteBuff = new byte[20971520];
                                int nRLen = fis.read(byteBuff);
                                String strBuff = new String(byteBuff, 0, nRLen);

                                chatArea.append(String.format("읽은 바이트 수[%d byte]:\n읽은 내용:\n%s\n", nRLen, strBuff));

                                File file = new File(servpath+ checkfile);

                                LogicalClock.put(file.getPath(),file.lastModified());
//                                System.out.println("서버의 변경 시간을 LOGICALCLOCK MAP에 저장"+LogicalClock.get(file.getPath()));

                                FileOutputStream fos = new FileOutputStream(file);
                                if(!sharedmember.isEmpty()){
                                    for(int i=0;i<sharedmember.size();i++){
                                    sendAll(name+"님이 공유한 "+sharedmember.get(i)+"님의 파일도 함께 동기화됩니다. ");

                                    File file1=new File(serverRepositoryPath+sharedmember.get(i)+"\\"+checkfile);
                                    FileOutputStream fos1=new FileOutputStream(file1);

                                    fos1.write(byteBuff,0,nRLen);
                                    fos1.close();
                                    }
                                }
                                fos.write(byteBuff, 0, nRLen);
                                fos.close();


                                sendAll(name +
                                        "님께서 " + checkfile + " 파일을 서버로 덮어쓰기 완료하였습니다.");
                                chatArea.append("서버 저장소에 저장 완료.\n");



                            } else {
                                chatArea.append(checkfile + " 파일은 변경되지 않았습니다.\n");
                                chatArea.append(checkfile + " 파일은 충돌하지 않습니다.\n");
                                sendAll(checkfile + " 파일은 변경되지 않았습니다.\n");
                                sendAll(name + "님의 " + checkfile + " 파일이 충돌되지 않습니다.\n");
                            }


                            continue;

                        }

                    }
                    if(inputMsg.substring(0,5).equals("CHECK")) {
                        String checkfile = inputMsg.substring(5, inputMsg.length());

                        chatArea.append(name + "님의 요청으로" + checkfile + "에 대한 파일 업데이트탐지를 진행합니다.\n");
                        sendAll(name + "님의 요청으로" + checkfile + "에 대한 파일 업데이트탐지를 진행합니다.\n");

                        // 파일 변경 감지 로직을 구현
                        String clientFilePath = clipath + checkfile;
                        String ServerFilePath= servpath+checkfile;
                        File deleteFile = new File(clientFilePath);
                        File serverExist=new File(ServerFilePath);
                        if(!serverExist.exists()){
                            chatArea.append(checkfile + " 은 새로 생성되었습니다.\n");
                            sendAll(checkfile + " 파일은 새로 생성되었습니다. \n");
                            continue;
                        }
                        if (!deleteFile.exists()) {
                            chatArea.append(checkfile + " 은 클라이언트 에서 삭제되었습니다.\n");
                            sendAll(checkfile + " 파일은 클라이언트에서 삭제되었습니다.\n");


                            continue;


                        } else {
                            boolean isUpdated = LogicalClockDetect(name, checkfile);

                            if (isUpdated) {
                                chatArea.append(checkfile + " 파일이 다른 클라이언트에 의해 변경되었습니다.\n");


                                sendAll(name + "님의 " + checkfile + " 파일이 누군가에 의해 변경되었습니다.\n");



                            } else {
                                chatArea.append(checkfile + " 파일은 변경되지 않았습니다.\n");
                                chatArea.append(checkfile + " 파일은 충돌되지 않습니다.\n");
                                sendAll(checkfile + " 파일은 변경되지 않았습니다.\n");
                                sendAll(checkfile + " 파일은 충돌되지 않습니다.\n");
                            }


                            continue;

                        }
                    }


                    String filename = inputMsg;

                    chatArea.append(name + "님이 " + filename + " 파일을 전송하였습니다.\n");
                    chatArea.append("전송받은 파일 내용:\n");

                    FileInputStream fis = new FileInputStream(clipath+filename);

                    byte[] byteBuff = new byte[20971520];
                    int nRLen = fis.read(byteBuff);
                    String strBuff = new String(byteBuff, 0, nRLen);

                    chatArea.append(String.format("읽은 바이트 수[%d byte]:\n읽은 내용:\n%s\n", nRLen, strBuff));

                    File file = new File(servpath  + filename);


                    LogicalClock.put(file.getPath(),file.lastModified());
                    System.out.println("서버의 변경 시간을 LOGICALCLOCK MAP에 저장"+LogicalClock.get(file.getPath()));



                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(byteBuff, 0, nRLen);
                    fos.close();

                    sendAll(name +
                            "님께서 " + inputMsg + " 파일을 서버로 전송 완료하였습니다.");
                    chatArea.append("서버 저장소에 저장 완료.\n");
                    if ("quit".equals(inputMsg)) break;
                }
            } catch (IOException e) {
                chatArea.append("[" + name + " 접속 끊김]\n");
            } finally {
                sendAll("[" + name + "]님이 서버에서 로그아웃 하셨습니다");
                clientOutputs.remove(out);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            chatArea.append("[" + name + " 연결 종료]\n");
        }

        private void sendAll(String s) {
            for (PrintWriter out : clientOutputs) {
                out.println(s);
                out.flush();
            }
        }

        private boolean LogicalClockDetect(String name, String checkfile) {
            String clientFilePath = clientRepositoryPath+name+"\\"+ checkfile;
            String serverFilePath = serverRepositoryPath + name + "\\" + checkfile;

            File clientFile = new File(clientFilePath);
            File serverFile = new File(serverFilePath);


            if (clientFile.exists() && serverFile.exists()) {
                long clientModifiedTime = clientFile.lastModified();
                long serverModifiedTime = serverFile.lastModified();

                //클라이언트영역의 파일이 변경된 시점이, 서버영역의 변경 시점보다 빠른지 계산.
                try {
                    long existingCLOCK = LogicalClock.get(serverFilePath);

                    if (existingCLOCK != serverModifiedTime) {

                        LogicalClock.put(serverFilePath, serverModifiedTime);
                    }
                    long LOGICALCLOCK_NEW = LogicalClock.get(serverFilePath);

//                    System.out.println("클라이언트의 변경시간 :" + clientModifiedTime);


//                    System.out.println("서버의 변경 시간" + LOGICALCLOCK_NEW);

                    return clientModifiedTime > LOGICALCLOCK_NEW;
                } catch(Exception e){
                     return clientModifiedTime>serverModifiedTime;
                }

            }
            return false;
        }
    }
}


