package com.demo.plugin1

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginImpl implements Plugin<Project> {

    void apply(Project project) {
        //println "---apply start---"
        //file()
        //println "---apply end---"
    }


    /**
     * groovy的高级用法
     * （1）json操作
     * （2）xml文件操作
     * （3）文件操作
     * */
    void file() {
        /**
         * 对象转json字符串
         * */
        def list = [
                new Person(name: 'davi1', age: 18),
                new Person(name: 'davi2', age: 19)
        ]
        println "对象转json字符串：" + JsonOutput.prettyPrint(JsonOutput.toJson(list))

        /**
         * json 转对象
         * */
        def json = '''
         {
            "name": "davi",
            "age": "18"
        }
        '''
        def j = new JsonSlurper()
        println "json 转对象 ： name is " + j.parseText(json).name

        /**
         * xml解析 todo 不行？
         * */
        def xml = '''
        <note>
            <to>George</to>
            <from>John</from>
            <heading>Reminder</heading>
            <body>Don't forget the meeting!</body>
        </note>
        '''
        def xmlP = new XmlSlurper()
        def xmlObj = xmlP.parseText(xml)
        println "xml to is " + xmlObj.note

        /**
         * xml文件生成
         * <langs type = 'cur'>
         *     <language flavor = 'static'>kotlin</language>
         * </langs>
         * */
        def sw = new StringWriter()
        def xmlBuilder = new MarkupBuilder(sw)
        //静态方式
        xmlBuilder.langs(type: 'cur') {
            language(flavor: 'static', 'kotlin')
        }
        println "静态方式 my xml is : " + sw
        //动态方式，自己定义对象，自己赋值模式..

        /**
         * 文件操作
         * 1）java中的类，处理文件的，groovy中都可以使用
         * 2）groovy拓展了新的API，一般用这些..
         * */
        String sourcePath = "/Users/yabber/Desktop/lifecycle-plugin.iml"
        def file = new File(sourcePath)

        //一行行读取
        file.eachLine { line ->
            println "eachLine模式读取每一行：" + line
        }

        //读取文件部分内容
        def reader = file.withReader { render ->
            char[] buffer = new char[100]
            render.read(buffer)
            return buffer
        }
        println "读取文件部分内容 : " + reader

        //文件拷贝
        String desPath = "/Users/yabber/Desktop/lifecycle-plugin222.iml"
        FileTool.copy(sourcePath, desPath)

        //对象存储到文件
        def p = new Person(name: 'Davi', age: 18)
        def pathObj = "/Users/yabber/Desktop/p.bin"
        FileTool.saveObj(p, pathObj)

        //对象读取出来
        def pp = (Person) FileTool.readObj(pathObj)
        println "对象读取出来 : " + pp.toString()
    }


    /***
     * 面向对象
     * */
    void obj() {
        //类和接口的定义和使用，和java类似
        def p = new Person(name: "davi", age: 18)
        println "name is : ${p.name}"
        println "from obj，name is : ${p.getName()}"
        println "age is : ${p.age}"

        /***
         * todo 【元编程】？？？在安卓环境中测试不了视频的效果？？？？？
         *
         * 在java中，如果方法没有那边会直接编译报错
         * 但是在groovy中，没有方法编译是不会报错的，只有在运行时候才会去执行一个机制，
         * 具体机制如下：
         * 类中方法是否存在？
         *  存在，直接调用
         *  不存在，MetaClass中是否有此方法
         *      存在，直接调用
         *      不存在，是否重写类methodMissing方法
         *          有重写，直接调用
         *          没有重写，再看下是否重写了invokeMethod方法
         *              有重写，直接调用
         *              没有重写，throw MissingMethodException异常
         * */

        /**
         * 调用一个类中没有的方法，同时没有重写任何
         * 报错信息：
         * ERROR: No signature of method: com.demo.plugin1.Person.cry() is applicable for argument types: () values: []
         * Possible solutions: any(), any(groovy.lang.Closure), eat(), every(), grep(), macro(groovy.lang.Closure)
         * */

        /***
         * 调用一个类中没有的方法，同时重写了：invokeMethod
         *
         * */
        // printlin p.cry()


        //todo 类，动态添加属性;    ？？？在安卓环境中测试不了视频的效果？？？？？
        Person.metaClass.sex = 'boy'
        def p1 = new Person(name: "davi", age: 18)
        printlin "动态添加的属性sex为：" + p1.sex

        //todo 类，动态添加方法;    ？？？在安卓环境中测试不了视频的效果？？？？？
        /*
        Person.metaClass.sexPrint = { -> printlin "sexPrint 里面的sex = " + sex }
        def p2 = new Person(name: "davi", age: 18)
        p2.sexPrint()
        */

        /**
         * 运行时候添加属性/方法等的好处
         *
         * 正常来说，我们需要在第三方库的一个类里面添加属性或者方法，一般会选择
         * 继承，然后添加方法
         * 但是，如果类是一个final类，那么继承的方式就不行类
         * 采用groovy的运行时注入属性/方法等可以实现
         * */

    }

    /**
     * 数据结构 : 范围
     * list 的辅助
     * */
    void studyStructure2() {
        //定义
        def rang = 1..10
        println "范围的第一个元素：" + rang[0]
        println "范围的开始元素：" + rang.from
        println "范围的最后一个元素：" + rang.to

        //列表相对比较重，range会比较轻一点

        //遍历
        rang.each {
            println "循环：" + it
        }

        //成绩
        println "成绩：" + rangResult(65)
    }

    String rangResult(Number number) {
        def result = "默认值"
        switch (number) {
            case 0..<60:
                result = "不及格"
                break
            case 60..<70:
                result = "及格"
                break
        }
        return result
    }


    /**
     * ******************
     * 数据结构，常用的3个
     * ******************
     * 列表
     * map
     * ...
     * */
    void studyStructure() {
        //列表定义
        def list = [1, 3, -1, -2]
        printlin list.class

        //列表排序
        printlin "默认排序规则，排序后：" + list.sort().toListString()
        printlin "指定排序规则，排序后：" + list.sort {
            a, b ->
                a == b ? 0 :
                        Math.abs(a) < Math.abs(b) ? 1 : -1
        }.toListString()

        //列表的查找..一堆API
        //打印偶数
        printlin list.find { return it % 2 == 0 }.toListString()
        //打印基数
        printlin list.find { return it % 2 != 0 }.toListString()
        //打印all
        printlin list.toListString()
        //任意偶数
        printlin list.any { return it % 2 == 0 }.toListString()
        //所有基数
        printlin list.every() { return it % 2 != 0 }.toListString()

        //数组定义
        def arr = [1, 2, 3, 4] as int[]
        int[] arr2 = [1, 2, 3, 4]

        /***************
         * map 定义&使用
         * *************
         * */
        def map = [
                red : '0xffff',
                blue: '0xooo'
        ]
        //索引
        printlin map['red']
        printlin map.red
        //添加元素
        map.add = '0xccc'
        printlin map.toMapString()
        //默认类型
        printlin map.getClass()
        //map 遍历
        def map2 = [
                1: [age: '16', name: 'davi'],
                2: [age: '15', name: 'xiaoming'],
                3: [age: '7', name: 'xiaohong']
        ]
        map2.each { obj ->
            printlin "each的遍历：key is ${obj.key}, value is ${obj.value}"
        }
        //查找
        printlin "年龄为16的：" + map2.find { def o -> return o.value.age == 16 }.toMapString()
        printlin "年龄大于0的：" + map2.findAll { def o -> return o.value.age > 0 }.toMapString()
        printlin "年龄大于0的数量有：" + map2.count { def o -> return o.value.age > 0 }.toMapString()
        printlin "年龄大于7的同学名称有：" + map2.findAll {
            def o -> return o.value.age > 7
        }.collect {
            return it.value.name
        }.toMapString()
    }

    //todo ****************>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>...
    //todo ****************>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>...
    //todo ****************>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>...
    //todo ****************>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>...

    /**
     * *************
     * 闭包的委托策略
     * *************
     * */
    class Student {
        String name
        def p = { "my name is ${name}" }

        String print() {
            p.call()
        }
    }

    class Teacher {
        String name
    }

    class Teacher2 {
        String name2
    }

    void testEntrust() {
        def stu = new Student(name: 'Student A')
        def te = new Teacher(name: 'Teacher B')
        def te2 = new Teacher2(name: 'Teacher 2')
        println "委托前：" + stu.print()

        //进入委托模式
        stu.p.resolveStrategy = Closure.DELEGATE_FIRST

        //先会在委托对象里面找属性或者方法，如果找不到那么会回去找
        stu.p.delegate = te
        println "委托成功后：" + stu.print()

        //注意变量名称/方法名需要一样，才能算匹配成功！！
        stu.p.delegate = te2
        println "委托失败后：" + stu.print()
    }


    /**
     * ***********************************
     * 闭包的使用 ??????? 编译不通过  ？？？
     * ***********************************
     * */

    void closureDo() {
        //求num的阶成
        println fab(5)

        //string类型结合闭包
        strFab()
    }


    /**
     * ******************
     * 闭包的3个重要的变量
     * ******************
     * */
    void demo() {
        //3个重要的变量
        def closure = {
            //闭包定义处的类，比如：PluginImpl.groovy
            println "closure this :" + this

            //闭包定义处的类/对象
            println "closure owner :" + owner

            //任意对象，默认和'owner'一致
            println "closure delegate :" + delegate

            //大部分情况下，三个关键字一致
            //如果闭包中嵌套了闭包，那么'this'指向的是最外层的，'owner' 'delegate' 指向的是最近那一层的

            def inner = {
                println "inner this :" + this
                println "inner owner :" + owner
                println "inner delegate :" + delegate
            }
        }
        closure.call()
    }


    /**
     *  string类型结合闭包
     * */
    void strFab() {
        def str = '2 and 3 is 5'
        str.each {
            String s -> println a
        }
    }

    /**
     * 基本数据类型结合闭包
     * 1）闭包和方法的结合，代码实现简洁
     * 2）不用自己写for循环
     * */
    int fab(int num) {
        int result = 1
        //1看是循环到num
        1.upto(num, { n -> result *= n })
        return result
    }


    /**
     * ***********************
     * 闭包，基础：定义/调用/参数
     * 闭包有点像是一个方法的定义，然后可以以参数形式传递，最后函数里面调用这样
     * ****************************************************************
     * */
    void closure() {
        //定义和调用
        def closure1 = {
            println 'hello closure!'
        }
        closure1.call()
        //closure1()

        //自定义的带参数
        def closure2 = {
            String name, int age -> println "hello ${name}"
        }
        closure2('closure???', 9)

        //it  默认的参数（不需要声明参数..）
        def closure3 = {
            println "hello ${it}"
        }
        closure3.call("it 默认的参数...")

        //返回值
        def closure4 = {
            return '闭包返回值'
        }
        println closure4.call()
    }


    /**
     * ******************
     * string 相关API学习
     * ******************
     * */
    void strfun() {
        def str = "string1"
        def str2 = "string2"
        //字符串的扩充
        //1）中心填充
        println str.center(5, 'xx')
        //2）左填充
        println str.padLeft(5, 'aa')

        //比较
        println str.compareTo(str2)
        println str > str2

        //索引
        println str[0]
        println str[0..1]

        //剪切方法
        println str.minus(str2)

        //首字母大写
        println str.capitalize()

        println str.isNumber()

        println str.toInteger()

    }

    /***
     * *******************
     * string 3种常用的定义
     * ********************
     * */
    void defStr() {
        //方式1：1个单引号定义字符串
        //没有格式，如果需要格式（如：换行等）需要+号进行拼接
        def name = '单引号定义字符串'
        println "【单引号定义字符串】 类型为：" + name.class


        //方式3：2个单引号定义（*最常用*）
        //可拓展，可包含变量/表达式等..
        def nam2 = "2个单引号定义字符串"
        def helloExt = "hello : ${nam2}"
        def sum = "the sum is : ${3 + 2}"
        println "【2个单引号定义】 类型为：" + nam2.class
        println "【2个单引号定义】 内容可拓展(变量)：" + helloExt
        println "【2个单引号定义】 内容可拓展后（变量）的类型：" + helloExt.class
        println "【2个单引号定义】 内容可拓展（表达式）sum is ：" + sum

        //方式2：3个单引号定义
        //可以指定格式（换行/json等格式等）
        def name1 = '''
        3个单引号定义字符串
        第一行
        第二行
        第三行
        {
            name：Davi
            age：boy
        }
        '''
        println "【3个单引号定义】 内容为：" + name1
        println "【3个单引号定义】 类型为：" + name1.class

    }

    /**
     * *******
     * 变量定义
     * ********
     * */
    void defStudy() {
        //groovy 最终都是非基本型类型举例
        //基本型类型都会被装包成引用型类型
        //强类型 定义
        int x = 10
        println "x 类型为：" + x.class //x 类型为：class java.lang.Integer

        //def
        //弱类型 定义
        def y = 10
        def str = 'string'
        println "y 类型为：" + y.class
        println "str 类型为：" + str.class

        //def定义方式是可以改变类型的
        def a = 1
        println "开始：a 类型为：" + a.class
        a = 'str'
        println "动态改变之后：a 类型为：" + a.class

    }
}




