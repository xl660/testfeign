package com.example.testfeign.client;

import com.example.testfeign.annotation.OpenTestFeign;
import com.example.testfeign.annotation.TestFeignClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

public class TestFeignClientRegistrar implements ImportBeanDefinitionRegistrar , ResourceLoaderAware , EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        Map<String, Object> Attributes = importingClassMetadata.getAnnotationAttributes(OpenTestFeign.class.getName());
        String[] value =(String[]) Attributes.get("value");
        if (value == null){
            System.err.println("OpenTestFeign is empty");
            return;
        }
        ClassPathScanningCandidateComponentProvider scanner = this.getScanner();
        scanner.addIncludeFilter(new AnnotationTypeFilter(TestFeignClient.class));
        scanner.setResourceLoader(this.resourceLoader);

        Set<String> basePackages = new HashSet<String>();
        for (int i = 0; i < value.length; i++) {
            basePackages.add(value[i]);
        }
        Iterator var8 = basePackages.iterator();
        LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet();
        while(var8.hasNext()) {
            String basePackage = (String)var8.next();
            candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
        }

        Iterator var13 = candidateComponents.iterator();

        while(var13.hasNext()) {
            BeanDefinition candidateComponent = (BeanDefinition)var13.next();
            if (candidateComponent instanceof AnnotatedBeanDefinition) {
                AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition)candidateComponent;
                AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                String className = annotationMetadata.getClassName();
                Class type = ClassUtils.resolveClassName(className, (ClassLoader)null);

                this.registerClient(registry, annotationMetadata, type);
            }
        }

    }

    private void registerClient(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata,Class type) {

        TestFeignFactoryBean factoryBean = new TestFeignFactoryBean();
        factoryBean.setType(type);

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TestFeignFactoryBean.class);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.getPropertyValues().add("type",type);
        registry.registerBeanDefinition(type.getName(),beanDefinition);
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation()) {
                    isCandidate = true;
                }

                return isCandidate;
            }
        };
    }


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
