package sample;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public class Connection implements  Runnable {

    public Socket client;
    public OutputStream out;
    public InputStream in;
    public String addr;
    public String message;
    public boolean mutex;
    private int port;
    private  PrintWriter writer;
    private BufferedReader reader;
    public String command;
    public String argument;
    public TreeItem<File> fileToUpload;

    public Connection(String addr,String port)
    {
        this.addr=addr;
        this.port=Integer.valueOf(port);
    }
    @Override
    public void run() {


        try {
            if (command=="CONNECT")
            {
                connect();
            }
            if(command=="LIST")
            {
                System.out.println("LIST "+argument);
                message=list();
            }

            if(command=="CWD")
            {
                cwd();
            }
            if(command=="RMD")
            {
                rmd();
            }
            if(command=="MKD")
            {
                mkd();
            }
            if(command=="PASV")
            {
                pasv();
            }
            if(command=="MODE")
            {
                setTransmissionMode();
            }
//
        } catch (Exception e) {
            System.out.println("Brak połączenia");
        }
    }

//            while (keepAlive) {
//                if (in.available() > 0) {
//                    boolean binaryMode = false;
//                   // BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//
//                    //String serverMessage = reader.readLine();
//                    //  System.out.println("bytes received " + in.available());
//                    //  System.out.println("data received " + serverMessage);
//
//
//                    Arrays.fill(buff, (byte) 0);
//                    in.read(buff);
//
//                    if (buff[0] == (byte) -1) {
//                        System.out.println("Server aborted connection");
//                        keepAlive = false;
//                        continue;
//                    }
//
//                    if ((int)buff[0] == 0)
//                    {
//                        System.out.println("Saving stream");
//                        stream.write(buff);
//                        break;
//                    }
//
//                    System.out.println("Received data: " + new String(buff, StandardCharsets.UTF_8));
//
//
//                }
//                sleep(10);
//            }
//
//            stream.close();
//            client.close();
//
//        } catch (IOException e) {
//            System.out.println("Brak połączenia");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//
//        } catch (RuntimeException e) {
//            System.out.println(e.getMessage());
//        }


    public void connect () throws IOException {
        client = new Socket(addr, port);
        out = client.getOutputStream();
        in = client.getInputStream();
        writer = new PrintWriter(out, true);
        reader = new BufferedReader(new InputStreamReader(in));
    }

    public String list() throws IOException {
        String serverMessage="";
            String command = "LIST /"+argument;
            writer.println(command);
            serverMessage = reader.readLine();
        return serverMessage;

    }

    public void cwd() throws IOException {
        String serverMessage="";
        String command = "CWD "+argument;
        System.out.println(command);
        writer.println(command);
        serverMessage = reader.readLine();
        System.out.println(serverMessage);
    }

    public void rmd() throws  IOException{
        String command="RMD "+argument;
        writer.println(command);
        message = reader.readLine();
    }

    public void mkd() throws IOException{
        String command="MKD "+argument;
        writer.println(command);
        message = reader.readLine();
    }

    public void pasv() throws IOException{
        String command="PASV";
        writer.println(command);
        message = reader.readLine();
    }

    public void pwd() throws IOException{
        String command="PWD";
        writer.println(command);
        message = reader.readLine();
    }

    public void setTransmissionMode() throws IOException{
        String command="TYPE ";
        if(argument=="ASCII"){
            command+="A";
        }
        else
        {
            command+="I";
        }
        writer.println(command);
        message = reader.readLine();
    }
    public void stor(TreeItem<File> f) throws IOException{
        if(f.getValue().isDirectory()==true)
        {
            //stworz folder i wejdź w niego
            argument = getFileName(f);
            mkd();
            cwd();
            for(TreeItem file : fileToUpload.getChildren())
            {
                stor(file);
            }
            //po przetworzeniu wszystkich dzieci wejdź na serwerze poziom wyżej
            pwd();
            String pom[] = message.split("/");
            String dir="";
            for(int i=0;i<pom.length-1;i++)
            {
                dir+=pom[i];
            }
            argument = dir;
            cwd();
        }
        else
        {
            command = "STOR " + getFileName(f);
            writer.println(command);
            message = reader.readLine();
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            FileInputStream fis = new FileInputStream(f.getValue());
            byte[] buffer = new byte[4096];

            while (fis.read(buffer) > 0) {
                dos.write(buffer);
            }
            fis.close();
            dos.close();
        }

    }

    public String getFileName(TreeItem file)
    {
        String pom[]=file.getValue().toString().split("/");
        String name = pom[pom.length-1];
        return name;
    }

}


