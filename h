[1mdiff --git a/client/src/main/java/io/github/springstudent/ada/client/core/RemoteControlled.java b/client/src/main/java/io/github/springstudent/ada/client/core/RemoteControlled.java[m
[1mindex 7ae406e..24c5f01 100644[m
[1m--- a/client/src/main/java/io/github/springstudent/ada/client/core/RemoteControlled.java[m
[1m+++ b/client/src/main/java/io/github/springstudent/ada/client/core/RemoteControlled.java[m
[36m@@ -43,11 +43,13 @@[m [mpublic class RemoteControlled extends RemoteControll implements RemoteScreenRobo[m
     @Override[m
     public void stop() {[m
         super.stop();[m
[32m+[m[32m        remoteGrabber.stop();[m
     }[m
 [m
     @Override[m
     public void start() {[m
         super.start();[m
[32m+[m[32m        remoteGrabber.start();[m
     }[m
 [m
     public void closeSession(String deviceCode) {[m
[36m@@ -60,11 +62,9 @@[m [mpublic class RemoteControlled extends RemoteControll implements RemoteScreenRobo[m
             CmdResCapture cmdResCapture = (CmdResCapture) cmd;[m
             if (cmdResCapture.getCode() == CmdResCapture.START_) {[m
                 RemoteClient.getRemoteClient().setControlledAndCloseSessionLabelVisible(true);[m
[31m-                remoteGrabber.start();[m
                 start();[m
             } else if (cmdResCapture.getCode() == CmdResCapture.STOP_) {[m
                 RemoteClient.getRemoteClient().setControlledAndCloseSessionLabelVisible(false);[m
[31m-                remoteGrabber.stop();[m
                 stop();[m
             }[m
         } else if (cmd.getType().equals(CmdType.KeyControl)) {[m
[1mdiff --git a/client/src/main/java/io/github/springstudent/ada/client/core/RemoteController.java b/client/src/main/java/io/github/springstudent/ada/client/core/RemoteController.java[m
[1mindex 028e769..86efb5c 100644[m
[1m--- a/client/src/main/java/io/github/springstudent/ada/client/core/RemoteController.java[m
[1m+++ b/client/src/main/java/io/github/springstudent/ada/client/core/RemoteController.java[m
[36m@@ -72,8 +72,11 @@[m [mpublic class RemoteController extends RemoteControll implements RemoteScreenList[m
     }[m
 [m
     @Override[m
[31m-    public void stop() {[m
[32m+[m[32m    public synchronized void stop() {[m
         super.stop();[m
[32m+[m[32m        if (remoteSubscribe != null) {[m
[32m+[m[32m            remoteSubscribe.close();[m
[32m+[m[32m        }[m
     }[m
 [m
     @Override[m
[36m@@ -127,9 +130,7 @@[m [mpublic class RemoteController extends RemoteControll implements RemoteScreenList[m
             }[m
         } else if (cmd.getType().equals(CmdType.ResStream)) {[m
             CompletableFuture.runAsync(() -> {[m
[31m-                if (remoteSubscribe != null) {[m
[31m-                    remoteSubscribe.close();[m
[31m-                }[m
[32m+[m[32m                stop();[m
                 try {[m
                     remoteSubscribe = new RemoteSubscribe(((CmdResStream) cmd).getPlayUrl());[m
                 } catch (Exception e) {[m
