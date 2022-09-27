package com.example.testfeign.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class TestFeignProxy implements InvocationHandler {

    private String requestPort;

    public TestFeignProxy(String requestPort) {
        this.requestPort = requestPort;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        String requestMethod = "GET";
        String requestUrl = null;

        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        if (annotation != null){
            if (annotation.method() != null){
                requestMethod = String.valueOf(annotation.method()[0]);
            }
            requestUrl = annotation.value()[0];
        }
        GetMapping getannotation = method.getAnnotation(GetMapping.class);
        if (getannotation != null){
            requestUrl = getannotation.value()[0];
        }
        PostMapping postannotation = method.getAnnotation(PostMapping.class);
        if (postannotation != null){
            requestMethod = "POST" ;
            requestUrl = postannotation.value()[0];
        }
        String urlParam = requestPort+"/"+requestUrl ;
        String rs = sendRequest(urlParam, requestMethod, args);
        Object o = JSONObject.parseObject(rs, returnType);
        return o;
    }

    public String sendRequest(String urlParam,String requestType, Object[] args) {

        HttpURLConnection con = null;
        BufferedReader buffer = null;
        StringBuffer resultBuffer = null;

        try {
            URL url = new URL(urlParam);
            //得到连接对象
            con = (HttpURLConnection) url.openConnection();
            //设置请求类型
            con.setRequestMethod(requestType);

            //设置请求需要返回的数据类型和字符集类型
            con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            //允许写出
            con.setDoOutput(true);
            //允许读入
            con.setDoInput(true);
            //不使用缓存
            con.setUseCaches(false);

            OutputStream outputStream = con.getOutputStream();
            outputStream.write(args.toString().getBytes(StandardCharsets.UTF_8));
            //得到响应码
            int responseCode = con.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                //得到响应流
                InputStream inputStream = con.getInputStream();
                //将响应流转换成字符串
                resultBuffer = new StringBuffer();
                String line;
                buffer = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                while ((line = buffer.readLine()) != null) {
                    resultBuffer.append(line);
                }
                return resultBuffer.toString();
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


//    public Object socketSend(String urlParam,String requestType) {
//
//
//        return null;
//    }
//
//    public void nettyConnect() {
//        try {
//            Channel localhost = new Bootstrap()
//                    // 添加group
//                    .group(new NioEventLoopGroup())
//                    // 添加通道
//                    .channel(NioSocketChannel.class)
//                    // 添加处理器
//                    .handler(new ChannelInitializer<NioSocketChannel>() {
//                        @Override
//                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
//                            nioSocketChannel.pipeline().addLast(new StringDecoder());
//                            nioSocketChannel.pipeline().addLast(new StringEncoder());
//
//                        }
//                    })
//                    // 建立连接
//                    .connect("localhost", 8081)
//                    .sync()
//                    .channel();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
