package com.redhat.fuse.example.util;

import org.springframework.beans.factory.FactoryBean;

import java.util.List;

public class ClassArrayFactoryBean implements FactoryBean<Object> {

    private List<String> classNames;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {

        final Class<?>[] classes = new Class<?>[classNames.size()];
        for (int i = 0; i < classNames.size(); i++) {
            classes[i] = Class.forName(classNames.get(i));
        }
        return classes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class<?> getObjectType() {
        return Class[].class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * @return the classNames
     */
    public List<String> getClassNames() {
        return classNames;
    }

    /**
     * @param classNames the classNames to set
     */
    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

}
