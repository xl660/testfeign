package com.example.testfeign.client;

import com.example.testfeign.annotation.TestFeignClient;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;


public class TestFeignFactoryBean implements FactoryBean<Object> {

    private Class<?> type;

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public Object getObject() throws Exception {
        return newInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    public Object newInstance() {
        TestFeignClient annotation = this.type.getAnnotation(TestFeignClient.class);
        String value = annotation.value();
        if (value == null || value == ""){
            System.err.println("TestFeignClient is empty");
            return null;
        }
        TestFeignProxy testFeignProxy = new TestFeignProxy(value);
        Object proxyInstance = Proxy.newProxyInstance(this.type.getClassLoader(), new Class[]{this.type}, testFeignProxy);

        return proxyInstance;
    }
}
