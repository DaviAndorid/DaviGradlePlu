package com.demo.plugin1

/**
 * （1）流操作完毕之后，可以不用自己close
 * （2）
 * */
class FileTool {

    /**
     * 文件的拷贝
     * */
    static def copy(String sourcePath, String desc) {
        try {
            def descFile = new File(desc)
            if (!descFile.exists()) {
                descFile.createNewFile()
            }
            //开始拷贝
            new File(sourcePath).withReader { render ->
                def lines = render.readLines()

                descFile.withWriter { writer ->
                    //一行行写入
                    lines.each { line ->
                        writer.append(line + "\r\n")
                    }
                }
            }

            //ok
            return true
        } catch (Exception e) {
            e.printStackTrace()
        }

        //default
        return false
    }

    /**
     * 对象存储到文件
     * */
    static def saveObj(Object o, String path) {
        try {
            def descFile = new File(path)
            if (!descFile.exists()) {
                descFile.createNewFile()
            }
            descFile.withObjectOutputStream { out ->
                out.writeObject(o)
            }
            //ok
            return true
        } catch (Exception e) {
            e.printStackTrace()
        }

        //default
        return false
    }

    /**
     * 从文件中读取对象
     * */
    static def readObj(String path) {
        def obj = null

        try {
            def descFile = new File(path)
            if (descFile == null || !descFile.exists()) {
                return null
            }
            descFile.withObjectInputStream { inputStream ->
                obj = inputStream.readObject()
            }
            return obj
        } catch (Exception e) {
            e.printStackTrace()
        }

        //default
        return null
    }

}






