package com.example.review;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.Activity.MainActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class MoveDataActivity extends AppCompatActivity {

    EditText serviceId;
    EditText servicePort;
    Button   serviceBuild;
    EditText clientId;
    EditText clientPort;
    Button   clientSend;

    int port;
    private String TAG = "msg_mine";

    private static final int HANDLER_SET_PORT_ENABLE = 306;
    private static final int HANDLER_TOAST           = 307;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case HANDLER_SET_PORT_ENABLE:
                    if (message.arg1 == 0) {
                        servicePort.setEnabled(false);
                        serviceBuild.setEnabled(false);

                        clientId.setEnabled(false);
                        clientPort.setEnabled(false);
                        clientSend.setEnabled(false);
                        clientReceive.setEnabled(false);
                    } else {
                        servicePort.setEnabled(true);
                        serviceBuild.setEnabled(true);

                        clientId.setEnabled(true);
                        clientPort.setEnabled(true);
                        clientSend.setEnabled(true);
                        clientReceive.setEnabled(true);
                    }
                    break;
                case HANDLER_TOAST:
                    if (message.obj != null) {
                        Toast.makeText(MoveDataActivity.this, ((String) message.obj), Toast.LENGTH_SHORT).show();
                    } else {
                        if (message.arg1 == 1)
                            Toast.makeText(MoveDataActivity.this, "接收异常!", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(MoveDataActivity.this, "发送异常!", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return false;
        }
    });
    private TextView  tittle;
    private String    address;
    private Button    clientReceive;
    private String    order = "give me some data";
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_data);

        initViews();

        clientSend.setOnClickListener(clientSend());
        serviceBuild.setOnClickListener(serviceBuild());
        clientReceive.setOnClickListener(clientReceive());
        String localIpAddress = getLocalIpAddress();
        serviceId.setText(localIpAddress);
        serviceId.setEnabled(false);

        String name = String.format("同步到局域网的另一端？\n\n>> %s\n>> %s", MainActivity.pathNexus.getName(), MainActivity.pathLibrary.getName());
        tittle.setText(name);
        backButton.setOnClickListener(view -> finish());
    }

    private View.OnClickListener clientReceive() {
        return view -> {
            String        strAddress = clientId.getText().toString();
            int           port       = Integer.parseInt(clientPort.getText().toString());
            Socket        socket     = new Socket();
            SocketAddress address    = new InetSocketAddress(strAddress, port);

            new Thread(() -> {
                try {
                    socket.connect(address, 1000);
                    InputStream           in   = socket.getInputStream();
                    OutputStream          out  = socket.getOutputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    out.write(order.getBytes());
                    socket.shutdownOutput();

                    byte[] bytes = new byte[1024 * 1024];
                    int    length;

                    while ((length = in.read(bytes)) != -1) baos.write(bytes, 0, length);
                    byte[] buffer = baos.toByteArray();


                    ArrayList<String> paths = new ArrayList<>();
                    dataProcess(paths, buffer);

                    Message msg = new Message();
                    msg.what = HANDLER_TOAST;
                    msg.obj  = "success";
                    handler.sendMessage(msg);

                    in.close();
                    out.close();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        };
    }

    private View.OnClickListener serviceBuild() {
        return view -> {
            //从界面读取数据
            port = Integer.parseInt(servicePort.getText().toString());

            new Thread(() -> {
                Message msg;
                try {
                    //发送Handler消息
                    msg      = new Message();
                    msg.what = HANDLER_SET_PORT_ENABLE;
                    msg.arg1 = 0;
                    handler.sendMessage(msg);

                    ArrayList<String> paths = service();
                    if (paths.size() > 0) {
                        Intent data = new Intent();
                        data.putStringArrayListExtra("paths", paths);
                        setResult(1, data);
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    msg      = new Message();
                    msg.what = HANDLER_TOAST;
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                } finally {
                    //发送Handler消息
                    msg      = new Message();
                    msg.what = HANDLER_SET_PORT_ENABLE;
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                }

            }).start();
        };
    }

    private ArrayList<String> service() throws IOException {
        ArrayList<String> paths = new ArrayList<>();

        //创建ServerSocket服务
        ServerSocket serverSocket = new ServerSocket(port);
        //等待连接
        Socket socket = serverSocket.accept();
        //连接成功，取输入流
        InputStream in = socket.getInputStream();


        //读取全部数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[]                temp = new byte[1024 * 1024];
        int                   index;

        //分段读取数据
        while ((index = in.read(temp)) != -1) baos.write(temp, 0, index);
        byte[] data = baos.toByteArray();


        if (data.length <= order.length()) {
            OutputStream out = socket.getOutputStream();

            File            pathNexus = MainActivity.pathNexus;
            FileInputStream fis       = new FileInputStream(pathNexus);
            long            length    = pathNexus.length();
            byte[]          buffer    = new byte[(int) length];
            fis.read(buffer);
            fis.close();
            baos.reset();
            DataOutputStream dos = new DataOutputStream(baos);

            dataStream(dos, pathNexus.getName().getBytes());
            dataStream(dos, buffer);

            File pathLibrary = MainActivity.pathLibrary;
            fis    = new FileInputStream(pathLibrary);
            length = pathLibrary.length();
            buffer = new byte[(int) length];
            fis.read(buffer);
            fis.close();

            dataStream(dos, pathLibrary.getName().getBytes());
            dataStream(dos, buffer);
            data = baos.toByteArray();
            dos.close();

            out.write(data);
            socket.shutdownOutput();

            //关闭连接、流
            out.close();
            in.close();
            socket.close();
            serverSocket.close();

        } else {
            //关闭连接、流
            socket.close();
            serverSocket.close();

            dataProcess(paths, data);
        }
        return paths;
    }

    private void dataProcess(ArrayList<String> paths, byte[] data) throws IOException {
        //包装类
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream      dis  = new DataInputStream(bais);
        byte[]               buffer;

        for (int i = 0; i < 2; i++) {
            buffer = dataStream(dis);//读取文件名
            String name = new String(buffer);
            buffer = dataStream(dis);//读取数据


            //保存文件的路径
            File path = new File(MainActivity.pathApp, name);//todo
            paths.add(path.getPath());

            if (path.exists()) {
                //备份文件路径
                File pathBackup = new File(MainActivity.pathApp, name.concat(".backup"));
                //备份文件存在，则删除它
                if (pathBackup.exists()) pathBackup.delete();
                //把旧文件备份
                path.renameTo(pathBackup);
            }

            FileOutputStream out = new FileOutputStream(path);
            out.write(buffer);
            out.close();
        }
    }


    private View.OnClickListener clientSend() {
        return view -> new Thread(() -> client()).start();
    }

    private void client() {
        //从界面读取数据
        port    = Integer.valueOf(clientPort.getText().toString());
        address = clientId.getText().toString();

//        port    = Integer.valueOf(servicePort.getText().toString());
//        address = serviceId.getText().toString();

        String nameNex = MainActivity.pathNexus.getName();
        String nameLib = MainActivity.pathLibrary.getName();

        byte[] bytesNex = new byte[(int) MainActivity.pathNexus.length()];
        byte[] bytesLib = new byte[(int) MainActivity.pathLibrary.length()];

        FileInputStream fis;
        try {
            fis = new FileInputStream(MainActivity.pathNexus);
            fis.read(bytesNex);
            fis = new FileInputStream(MainActivity.pathLibrary);
            fis.read(bytesLib);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //包装类
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream      dos  = new DataOutputStream(baos);


            //数据通过dos汇集到baos中
            dataStream(dos, nameNex.getBytes());
            dataStream(dos, bytesNex);
            dataStream(dos, nameLib.getBytes());
            dataStream(dos, bytesLib);

            byte[]               buf  = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);

            Socket       socket = new Socket(address, port);//客户端socket
            OutputStream out    = socket.getOutputStream();//取输出流


            //分段发送数据
            int    index;
            byte[] temp = new byte[1024 * 1024];
            while ((index = bais.read(temp)) != -1) out.write(temp, 0, index);
            socket.shutdownOutput();

            //关闭流
            dos.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            Message msg = new Message();
            msg.what = HANDLER_TOAST;
            msg.arg1 = 2;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    void dataStream(DataOutputStream dos, byte[] bytes) throws IOException {
        dos.writeInt(bytes.length);
        dos.write(bytes);
        Log.d(TAG, "dataStream out: size = " + bytes.length);
    }

    byte[] dataStream(DataInputStream dis) throws IOException {
        int    len   = dis.readInt();
        byte[] bytes = new byte[len];
        dis.read(bytes);
        Log.d(TAG, "dataStream in: size = " + bytes.length);
        return bytes;
    }

    private void initViews() {
        serviceId    = findViewById(R.id.moveData_et_ServiceIp);
        servicePort  = findViewById(R.id.moveData_et_ServicePort);
        serviceBuild = findViewById(R.id.moveData_et_ServiceBuild);

        clientId      = findViewById(R.id.moveData_et_ClientIp);
        clientPort    = findViewById(R.id.moveData_et_clientPort);
        clientSend    = findViewById(R.id.moveData_et_ClientSend);
        clientReceive = findViewById(R.id.moveData_et_ClientReceive);
        tittle        = findViewById(R.id.moveData_tv_tittle);
        backButton    = findViewById(R.id.moveData_imageView_back_button);
    }

    String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int         ipAddress   = 0;

        int wifiState = wifiManager.getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            ipAddress = connectionInfo.getIpAddress();
        }
        return int2ip(ipAddress);
    }

    String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }
}
