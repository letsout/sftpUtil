package com.ai;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * @Author H
 * @Date 2018/10/26 10:03
 * @Desc
 **/
public class SFTPUtils {

    private static ChannelSftp sftp;

    private static SFTPUtils instance = null;

    private static String host = "172.17.70.222";
    private static int port = 22;
    private static String username = "iopsc2280";
    private static String password = "iopsc2280_PomP_2018#Euop";

    private SFTPUtils() {
    }

    private static SFTPUtils getInstance() {
        if (instance == null) {
            if (instance == null) {
                instance = new SFTPUtils();
                //获取连接
                sftp = instance.connect(host, port, username, password);
            }
        }
        return instance;
    }

    /**
     * 连接sftp服务器
     *
     * @param host     主机
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return
     */
    private ChannelSftp connect(String host, int port, String username, String password) {
        ChannelSftp sftp = null;
        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            Session sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            System.out.println("SFTP Session connected.");

            Channel channel = sshSession.openChannel("sftp");
            channel.connect();

            sftp = (ChannelSftp) channel;
            System.out.println("Connected to " + host);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return sftp;
    }


    /**
     * @param remoteFile 远程文件全路径
     * @param localFile  本地文件全路径
     * @return
     */
    private void upload(String remoteFile, String localFile) {
        FileInputStream localFileStream = null;
        try {
            //远程文件全路径,路径不能以'/'结尾
            if (remoteFile.endsWith("/")) {
                remoteFile = remoteFile.substring(0, remoteFile.length() - 1);
            }
            String remoteDir = remoteFile.substring(0, remoteFile.lastIndexOf("/"));
            System.out.println("making dirs");
            //创建目录
            mkDir(remoteDir);
            localFileStream = new FileInputStream(new File(localFile));
            System.out.println("uploading....");
            sftp.put(localFileStream, remoteFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (localFileStream != null)
                    localFileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("upload complete!");
            disconnect();
        }
    }

    /**
     * @param remotePath 远程路径
     * @param localPath  本地路径
     * @return
     */
    private void download(String remotePath, String localPath) {
        try {
            //本地路径,必须以'/'结尾
            if (!localPath.endsWith("/")) {
                localPath += "/";
            }

            Vector<ChannelSftp.LsEntry> files = sftp.ls(remotePath);
            if (files.size() <= 2) {
                System.out.println("该目录下没有文件");
                return;
            }

            File f=new File(localPath);
            if (!f.exists()){
                System.out.println(localPath+" is not exists! creating...");
                System.out.println(f.mkdirs());
            }

            sftp.cd(remotePath);
            for (ChannelSftp.LsEntry file : files) {
                String fileName = file.getFilename();
                if (fileName.equals(".") || fileName.equals("..")) {
                    continue;
                }
                System.out.println(fileName + "is downloading.");
                OutputStream os = new FileOutputStream(new File(localPath + fileName));
                sftp.get(fileName, os);
                os.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("download complete!");
            disconnect();
        }
    }

    private void disconnect() {
        try {
            sftp.getSession().disconnect();
            sftp.quit();
            sftp.disconnect();
        } catch (JSchException e) {
            System.out.println(e.getMessage());
        }
    }

    private void mkDir(String pathname) throws Exception {
        //分割路径,从根目录开始遍历
        String[] strings = pathname.split("/");
        StringBuffer dirPath = new StringBuffer("/");
        for (String str : strings) {
            if (str.length() == 0) {
                continue;
            }
            dirPath.append(str + "/");
            String dir = dirPath.toString();
            //目录是否存在
            if (!isDirExit(dir)) {
                sftp.mkdir(dir);
            }
            sftp.cd(dir);
        }
    }

    private boolean isDirExit(String dir) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(dir);
            isDirExistFlag = sftpATTRS.isDir();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isDirExistFlag;
    }

    /**
     * @param args 0:d(下载)/u(上传) 1:remotePath 2:localPath
     */
    public static void main(String[] args){
        if (args.length < 3)
            System.out.println("please input 3 params");

        String remotePath = args[1];
        String localPath = args[2];

        SFTPUtils sftp = SFTPUtils.getInstance();

        if ("d".equals(args[0])) {
            sftp.download(remotePath, localPath);
        } else if ("u".equals(args[0])) {
            sftp.upload(remotePath, localPath);
        } else {
            System.out.println("please choose the right operation!");
        }
    }
}

