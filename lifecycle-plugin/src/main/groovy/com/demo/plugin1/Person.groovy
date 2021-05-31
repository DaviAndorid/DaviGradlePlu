package com.demo.plugin1

/**
 * （1）默认的类/方法等都默认是 public类型
 * */
class Person implements IAction, GroovyObject, Serializable {
    String name

    int age

    def incAge() {
        return name + age
    }

    @Override
    void eat() {
        println "-- eat --"
    }

    def invokeMethod(String methodName, Object param) {
        return "methodName is : ${methodName}, param is : ${param}"
    }

}


