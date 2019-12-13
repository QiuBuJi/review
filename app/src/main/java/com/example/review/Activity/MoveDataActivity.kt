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
import android.view.View.OnClickListener
import android.widget.*
import com.example.review.R
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress

class MoveDataActivity : AppCompatActivity() {
    private lateinit var serviceId: EditText
    private lateinit var servicePort: EditText
    private lateinit var serviceBuild: Button
    private lateinit var clientId: EditText
    private lateinit var clientPort: EditText
    private lateinit var clientSend: Button
    private var port = 0
    private val TAG = "msg_mine"
    private var handler = Handler(Callback { message ->
        when (message.what) {
            HANDLER_SET_PORT_ENABLE -> setViewsEnabled(message.arg1 != 0)
            HANDLER_TOAST -> {
                val value = Toast.LENGTH_LONG
                if (message.obj != null)
                    Toast.makeText(this@MoveDataActivity, message.obj as String, value).show()
                else {
                    if (message.arg1 == 1) Toast.makeText(this@MoveDataActivity, "接收异常!", value).show()
                    else Toast.makeText(this@MoveDataActivity, "发送异常!", value).show()
                }
            }
        }
        false
    })
    private lateinit var tittle: TextView
    private lateinit var clientReceive: Button
    private lateinit var backButton: ImageView
    private var address: String? = null
    private val order = "give me some data"

    //设置一些特定的View的enable属性
    private fun setViewsEnabled(isEnable: Boolean = true) {
        servicePort.isEnabled = isEnable
        serviceBuild.isEnabled = isEnable
        clientId.isEnabled = isEnable
        clientPort.isEnabled = isEnable
        clientSend.isEnabled = isEnable
        clientReceive.isEnabled = isEnable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_data)

        initViews()

        clientSend.setOnClickListener(clientSend())
        serviceBuild.setOnClickListener(serviceBuild())
        clientReceive.setOnClickListener(clientReceive())
        backButton.setOnClickListener { finish() }

        val localIpAddress = localIpAddress
        serviceId.setText(localIpAddress)
        serviceId.isEnabled = false
        tittle.text = String.format("同步到局域网的另一端？\n\n>> %s\n>> %s", MainActivity.pathNexus.name, MainActivity.pathLibrary.name)
    }

    /**
     * 接收服务端的数据
     * */
    private fun clientReceive(): OnClickListener {
        return OnClickListener {
            val strAddress = clientId.text.toString()
            val port = clientPort.text.toString().toInt()
            val socket = Socket()
            val address: SocketAddress = InetSocketAddress(strAddress, port)

            Thread(Runnable {
                try {
                    socket.connect(address, 1000)
                    val input = socket.getInputStream()
                    val output = socket.getOutputStream()
                    val baos = ByteArrayOutputStream()

                    output.write(order.toByteArray())
                    socket.shutdownOutput()

                    val bytes = ByteArray(1024 * 1024)
                    var length: Int
                    while (input.read(bytes).also { length = it } != -1) baos.write(bytes, 0, length)

                    val buffer = baos.toByteArray()
                    val paths = ArrayList<String>()
                    dataProcess(paths, buffer)

                    val msg = Message()
                    msg.what = HANDLER_TOAST
                    msg.obj = "接收成功！"
                    handler.sendMessage(msg)

                    if (paths.isNotEmpty()) {
                        val data = Intent()
                        data.putStringArrayListExtra("paths", paths)
                        setResult(1, data)
                        finish()
                    }

                    input.close()
                    output.close()
                    socket.close()
                } catch (e: IOException) {
                    val msg = Message()
                    msg.what = HANDLER_TOAST
                    msg.obj = "出现异常！\n$e"
                    handler.sendMessage(msg)
                }
            }).start()
        }
    }

    private fun serviceBuild(): OnClickListener {
        return OnClickListener {
            //从界面读取数据
            port = servicePort.text.toString().toInt()

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
        val serverSocket = ServerSocket(port)//创建ServerSocket服务
        val socket = serverSocket.accept()//等待连接
        val input = socket.getInputStream()//连接成功，取输入流
        val baos = ByteArrayOutputStream()//读取全部数据
        val temp = ByteArray(1024 * 1024)
        var index: Int

        //分段读取数据
        while (input.read(temp).also { index = it } != -1) baos.write(temp, 0, index)
        var data = baos.toByteArray()

        if (data.size <= order.length) {
            val out = socket.getOutputStream()
            val pathNexus: File = MainActivity.pathNexus
            var fis = FileInputStream(pathNexus)
            var length = pathNexus.length()
            var buffer = ByteArray(length.toInt())

            fis.read(buffer)
            fis.close()
            baos.reset()

            val dos = DataOutputStream(baos)
            dataStream(dos, pathNexus.name.toByteArray())
            dataStream(dos, buffer)

            val pathLibrary: File = MainActivity.pathLibrary
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
            input.close()
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
            val path = File(MainActivity.pathApp, name) //todo
            paths.add(path.path)

            if (path.exists()) { //备份文件路径
                val pathBackup = File(MainActivity.pathApp, "$name.backup")
                if (pathBackup.exists()) pathBackup.delete()//备份文件存在，则删除它
                path.renameTo(pathBackup)//把旧文件备份
            } else {//文件不存在，则创建文件
                path.mkdirs()
                path.createNewFile()
            }

            val out = FileOutputStream(path)
            out.write(buffer)
            out.close()
        }
    }

    private fun clientSend(): OnClickListener {
        return OnClickListener { Thread(Runnable { client() }).start() }
    }

    private fun client() { //从界面读取数据
        port = Integer.valueOf(clientPort.text.toString())
        address = clientId.text.toString()
//        port    = Integer.valueOf(servicePort.getText().toString());
//        address = serviceId.getText().toString();
        val nameNex: String = MainActivity.pathNexus.name
        val nameLib: String = MainActivity.pathLibrary.name
        val bytesNex = ByteArray(MainActivity.pathNexus.length().toInt())
        val bytesLib = ByteArray(MainActivity.pathLibrary.length().toInt())
        var fis: FileInputStream
        try {
            fis = FileInputStream(MainActivity.pathNexus)
            fis.read(bytesNex)
            fis = FileInputStream(MainActivity.pathLibrary)
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

    private val localIpAddress: String
        get() {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var ipAddress = 0
            val wifiState = wifiManager.wifiState

            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                val connectionInfo = wifiManager.connectionInfo
                ipAddress = connectionInfo.ipAddress
            }
            return int2ip(ipAddress)
        }

    private fun int2ip(ipInt: Int): String {
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