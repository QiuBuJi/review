package com.example.review.Activity

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import com.example.review.R
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress

class MoveDataActivity : AppCompatActivity() {
    internal var serviceId: EditText? = null
    internal var servicePort: EditText? = null
    internal var serviceBuild: Button? = null
    internal var clientId: EditText? = null
    internal var clientPort: EditText? = null
    internal var clientSend: Button? = null
    internal var port = 0
    private val TAG = "msg_mine"
    internal var handler = Handler(Callback { message ->
        when (message.what) {
            HANDLER_SET_PORT_ENABLE -> if (message.arg1 == 0) {
                servicePort!!.isEnabled = false
                serviceBuild!!.isEnabled = false
                clientId!!.isEnabled = false
                clientPort!!.isEnabled = false
                clientSend!!.isEnabled = false
                clientReceive!!.isEnabled = false
            } else {
                servicePort!!.isEnabled = true
                serviceBuild!!.isEnabled = true
                clientId!!.isEnabled = true
                clientPort!!.isEnabled = true
                clientSend!!.isEnabled = true
                clientReceive!!.isEnabled = true
            }
            HANDLER_TOAST -> if (message.obj != null) {
                Toast.makeText(this@MoveDataActivity, message.obj as String, Toast.LENGTH_SHORT).show()
            } else {
                if (message.arg1 == 1) Toast.makeText(this@MoveDataActivity, "接收异常!", Toast.LENGTH_SHORT).show() else Toast.makeText(this@MoveDataActivity, "发送异常!", Toast.LENGTH_SHORT).show()
            }
        }
        false
    })
    private var tittle: TextView? = null
    private var address: String? = null
    private var clientReceive: Button? = null
    private val order = "give me some data"
    private var backButton: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_data)
        initViews()
        clientSend!!.setOnClickListener(clientSend())
        serviceBuild!!.setOnClickListener(serviceBuild())
        clientReceive!!.setOnClickListener(clientReceive())
        val localIpAddress = localIpAddress
        serviceId!!.setText(localIpAddress)
        serviceId!!.isEnabled = false
        val name = String.format("同步到局域网的另一端？\n\n>> %s\n>> %s", MainActivity.Companion.pathNexus.getName(), MainActivity.Companion.pathLibrary.getName())
        tittle!!.text = name
        backButton!!.setOnClickListener { view: View? -> finish() }
    }

    private fun clientReceive(): OnClickListener {
        return OnClickListener { view: View? ->
            val strAddress = clientId!!.text.toString()
            val port = clientPort!!.text.toString().toInt()
            val socket = Socket()
            val address: SocketAddress = InetSocketAddress(strAddress, port)
            Thread(Runnable {
                try {
                    socket.connect(address, 1000)
                    val `in` = socket.getInputStream()
                    val out = socket.getOutputStream()
                    val baos = ByteArrayOutputStream()
                    out.write(order.toByteArray())
                    socket.shutdownOutput()
                    val bytes = ByteArray(1024 * 1024)
                    var length: Int
                    while (`in`.read(bytes).also { length = it } != -1) baos.write(bytes, 0, length)
                    val buffer = baos.toByteArray()
                    val paths = ArrayList<String>()
                    dataProcess(paths, buffer)
                    val msg = Message()
                    msg.what = HANDLER_TOAST
                    msg.obj = "success"
                    handler.sendMessage(msg)
                    `in`.close()
                    out.close()
                    socket.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }).start()
        }
    }

    private fun serviceBuild(): OnClickListener {
        return OnClickListener { view: View? ->
            //从界面读取数据
            port = servicePort!!.text.toString().toInt()
            Thread(Runnable {
                var msg: Message
                try { //发送Handler消息
                    msg = Message()
                    msg.what = HANDLER_SET_PORT_ENABLE
                    msg.arg1 = 0
                    handler.sendMessage(msg)
                    val paths = service()
                    if (paths.size > 0) {
                        val data = Intent()
                        data.putStringArrayListExtra("paths", paths)
                        setResult(1, data)
                        finish()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    msg = Message()
                    msg.what = HANDLER_TOAST
                    msg.arg1 = 1
                    handler.sendMessage(msg)
                } finally { //发送Handler消息
                    msg = Message()
                    msg.what = HANDLER_SET_PORT_ENABLE
                    msg.arg1 = 1
                    handler.sendMessage(msg)
                }
            }).start()
        }
    }

    @Throws(IOException::class)
    private fun service(): ArrayList<String> {
        val paths = ArrayList<String>()
        //创建ServerSocket服务
        val serverSocket = ServerSocket(port)
        //等待连接
        val socket = serverSocket.accept()
        //连接成功，取输入流
        val `in` = socket.getInputStream()
        //读取全部数据
        val baos = ByteArrayOutputStream()
        val temp = ByteArray(1024 * 1024)
        var index: Int
        //分段读取数据
        while (`in`.read(temp).also { index = it } != -1) baos.write(temp, 0, index)
        var data = baos.toByteArray()
        if (data.size <= order.length) {
            val out = socket.getOutputStream()
            val pathNexus: File = MainActivity.Companion.pathNexus
            var fis = FileInputStream(pathNexus)
            var length = pathNexus.length()
            var buffer = ByteArray(length.toInt())
            fis.read(buffer)
            fis.close()
            baos.reset()
            val dos = DataOutputStream(baos)
            dataStream(dos, pathNexus.name.toByteArray())
            dataStream(dos, buffer)
            val pathLibrary: File = MainActivity.Companion.pathLibrary
            fis = FileInputStream(pathLibrary)
            length = pathLibrary.length()
            buffer = ByteArray(length.toInt())
            fis.read(buffer)
            fis.close()
            dataStream(dos, pathLibrary.name.toByteArray())
            dataStream(dos, buffer)
            data = baos.toByteArray()
            dos.close()
            out.write(data)
            socket.shutdownOutput()
            //关闭连接、流
            out.close()
            `in`.close()
            socket.close()
            serverSocket.close()
        } else { //关闭连接、流
            socket.close()
            serverSocket.close()
            dataProcess(paths, data)
        }
        return paths
    }

    @Throws(IOException::class)
    private fun dataProcess(paths: ArrayList<String>, data: ByteArray) { //包装类
        val bais = ByteArrayInputStream(data)
        val dis = DataInputStream(bais)
        var buffer: ByteArray
        for (i in 0..1) {
            buffer = dataStream(dis) //读取文件名
            val name = String(buffer)
            buffer = dataStream(dis) //读取数据
            //保存文件的路径
            val path = File(MainActivity.Companion.pathApp, name) //todo
            paths.add(path.path)
            if (path.exists()) { //备份文件路径
                val pathBackup = File(MainActivity.Companion.pathApp, "$name.backup")
                //备份文件存在，则删除它
                if (pathBackup.exists()) pathBackup.delete()
                //把旧文件备份
                path.renameTo(pathBackup)
            }
            val out = FileOutputStream(path)
            out.write(buffer)
            out.close()
        }
    }

    private fun clientSend(): OnClickListener {
        return OnClickListener { view: View? -> Thread(Runnable { client() }).start() }
    }

    private fun client() { //从界面读取数据
        port = Integer.valueOf(clientPort!!.text.toString())
        address = clientId!!.text.toString()
        //        port    = Integer.valueOf(servicePort.getText().toString());
//        address = serviceId.getText().toString();
        val nameNex: String = MainActivity.Companion.pathNexus.getName()
        val nameLib: String = MainActivity.Companion.pathLibrary.getName()
        val bytesNex = ByteArray(MainActivity.Companion.pathNexus.length() as Int)
        val bytesLib = ByteArray(MainActivity.Companion.pathLibrary.length() as Int)
        var fis: FileInputStream
        try {
            fis = FileInputStream(MainActivity.Companion.pathNexus)
            fis.read(bytesNex)
            fis = FileInputStream(MainActivity.Companion.pathLibrary)
            fis.read(bytesLib)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try { //包装类
            val baos = ByteArrayOutputStream()
            val dos = DataOutputStream(baos)
            //数据通过dos汇集到baos中
            dataStream(dos, nameNex.toByteArray())
            dataStream(dos, bytesNex)
            dataStream(dos, nameLib.toByteArray())
            dataStream(dos, bytesLib)
            val buf = baos.toByteArray()
            val bais = ByteArrayInputStream(buf)
            val socket = Socket(address, port) //客户端socket
            val out = socket.getOutputStream() //取输出流
            //分段发送数据
            var index: Int
            val temp = ByteArray(1024 * 1024)
            while (bais.read(temp).also { index = it } != -1) out.write(temp, 0, index)
            socket.shutdownOutput()
            //关闭流
            dos.close()
            out.close()
            socket.close()
        } catch (e: IOException) {
            val msg = Message()
            msg.what = HANDLER_TOAST
            msg.arg1 = 2
            handler.sendMessage(msg)
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    internal fun dataStream(dos: DataOutputStream, bytes: ByteArray) {
        dos.writeInt(bytes.size)
        dos.write(bytes)
        Log.d(TAG, "dataStream out: size = " + bytes.size)
    }

    @Throws(IOException::class)
    internal fun dataStream(dis: DataInputStream): ByteArray {
        val len = dis.readInt()
        val bytes = ByteArray(len)
        dis.read(bytes)
        Log.d(TAG, "dataStream in: size = " + bytes.size)
        return bytes
    }

    private fun initViews() {
        serviceId = findViewById(R.id.moveData_et_ServiceIp)
        servicePort = findViewById(R.id.moveData_et_ServicePort)
        serviceBuild = findViewById(R.id.moveData_et_ServiceBuild)
        clientId = findViewById(R.id.moveData_et_ClientIp)
        clientPort = findViewById(R.id.moveData_et_clientPort)
        clientSend = findViewById(R.id.moveData_et_ClientSend)
        clientReceive = findViewById(R.id.moveData_et_ClientReceive)
        tittle = findViewById(R.id.moveData_tv_tittle)
        backButton = findViewById(R.id.moveData_imageView_back_button)
    }

    internal val localIpAddress: String
        internal get() {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var ipAddress = 0
            val wifiState = wifiManager.wifiState
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                val connectionInfo = wifiManager.connectionInfo
                ipAddress = connectionInfo.ipAddress
            }
            return int2ip(ipAddress)
        }

    internal fun int2ip(ipInt: Int): String {
        val sb = StringBuilder()
        sb.append(ipInt and 0xFF).append(".")
        sb.append(ipInt shr 8 and 0xFF).append(".")
        sb.append(ipInt shr 16 and 0xFF).append(".")
        sb.append(ipInt shr 24 and 0xFF)
        return sb.toString()
    }

    companion object {
        private const val HANDLER_SET_PORT_ENABLE = 306
        private const val HANDLER_TOAST = 307
    }
}