package com.ai;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

/**
 * @Author H
 * @Date 2018/10/26 10:03
 * @Desc
 **/
public  class SFTPUtils {

        private static ChannelSftp sftp;

        private static SFTPUtils instance = null;

        private SFTPUtils() {
        }

        public static SFTPUtils getInstance(String host,int port,String username,String password) {
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
        public ChannelSftp connect(String host, int port, String username, String password) {
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
         * 上传文件
         *
         * @param remoteDirectory  上传的目录
         * @param uploadFile 要上传的文件
         * @param localDirectory 本地文件路径
         */
        public boolean upload(String remoteDirectory, String uploadFile, String localDirectory) {
            try {
                sftp.cd(remoteDirectory);

                File file = new File(uploadFile);

                FileInputStream fileInputStream = new FileInputStream(file);
                sftp.put(fileInputStream, file.getName());
                fileInputStream.close();
                return true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }

        /**
         * 下载文件
         *
         * @param remoteDirectory    下载目录
         * @param saveFile 下载的文件
         * @param localDirectory     存在本地的路径
         */
        public File download(String localDirectory, String saveFile,String remoteDirectory) {
            try {
                sftp.cd(remoteDirectory);
                Vector<LsEntry> files = null;
                try {
                    //查看文件列表
                    files = listFiles(localDirectory);
                } catch (SftpException e) {
                    e.printStackTrace();
                }
                for (LsEntry file : files) {
                    System.out.println("###\t" + file.getFilename());
                }
                File file = new File(saveFile);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                sftp.get(localDirectory, fileOutputStream);
                fileOutputStream.close();
                return file;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        }

        /**
         * 下载文件
         *
         * @param downloadFilePath 下载的文件完整目录
         * @param saveFile     存在本地的路径
         */
        public File download(String downloadFilePath, String saveFile) {
            try {
                int i = downloadFilePath.lastIndexOf('/');
                if (i == -1) {
                    return null;
                }
                sftp.cd(downloadFilePath.substring(0, i));
                File file = new File(saveFile);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                sftp.get(downloadFilePath.substring(i + 1), fileOutputStream);
                fileOutputStream.close();
                return file;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        }

        /**
         * 删除文件
         *
         * @param directory  要删除文件所在目录
         * @param deleteFile 要删除的文件
         */
        public void delete(String directory, String deleteFile) {
            try {
                sftp.cd(directory);
                sftp.rm(deleteFile);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        public void disconnect() {
            try {
                sftp.getSession().disconnect();
            } catch (JSchException e) {
                System.out.println(e.getMessage());
            }
            sftp.quit();
            sftp.disconnect();
        }

        /**
         * 列出目录下的文件
         *
         * @param directory 要列出的目录
         * @throws SftpException
         */
        public Vector<LsEntry> listFiles(String directory) throws SftpException {
            return sftp.ls(directory);
        }

        public static void main(String[] args) throws IOException {
            String host = "172.17.76.52";
            int port= 22;
            String username= "sc_13000";
            String password = "iopsIOPS1234!@#$iops30";
            String localDirectory = args[0];
            String remotePath = args[1];
            SFTPUtils sf =SFTPUtils.getInstance(host,port,username,password);
//            com.ai.SFTPUtils sf =com.ai.SFTPUtils.getInstance("180.169.129.52",18022,"iopsc2280","iopsc2280!123");

            sf.download(remotePath,localDirectory);

/*
            Vector<LsEntry> files = null;
            try {
                //查看文件列表
                files = sf.listFiles(localDirectory);
            } catch (SftpException e) {
                e.printStackTrace();
            }
            for (LsEntry file : files) {
                System.out.println("###\t" + file.getFilename());
            }
            sf.disconnect();*/
        }
}

