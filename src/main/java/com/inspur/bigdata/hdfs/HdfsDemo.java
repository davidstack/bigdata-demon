package com.inspur.bigdata.hdfs;


import java.io.IOException;
import java.security.PrivilegedAction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;


public class HdfsDemo {
    
    //定义配置项和文件句柄
    static Configuration configuration = new Configuration();
    static FileSystem hdfsFileSystem;
   
    //加载配置文件到configuration中
    static {
//        configuration.addResource(new Path("E:\\osworkspace\\bigdata\\src\\main\\resources\\core-site.xml"));
//        configuration.addResource(new Path("E:\\osworkspace\\bigdata\\src\\main\\resources\\hdfs-site.xml"));
//        configuration.addResource(new Path("E:\\osworkspace\\bigdata\\src\\main\\resources\\mapred-site.xml"));
//        configuration.addResource(new Path("E:\\osworkspace\\bigdata\\src\\main\\resources\\hbase-site.xml"));
  //linux      
        configuration.addResource(new Path("/root/app/resources/core-site.xml"));
        configuration.addResource(new Path("/root/app/resources/hdfs-site.xml"));
        configuration.addResource(new Path("/root/app/resources/mapred-site.xml"));
        configuration.addResource(new Path("/root/app/resources/hbase-site.xml"));
    }
    
    //加载HDFS的用户
    static UserGroupInformation ugi = UserGroupInformation.createRemoteUser("hdfs");
    
    /**
     * 创建目录，如果目录存在，则返回已经创建过该目录
     * @param dir 目录在HDFS上的路径
     */
    public void createDir(final String dir) {
        final Path path = new Path(dir);
        System.out.println("create dir"); 
        //创建目录，通过使用已经定义的HDFS的用户来进行相关操作
        ugi.doAs(new PrivilegedAction<String>() {
            
            public String run() {
                // TODO Auto-generated method stub
                try {
                    //加载配置项到FileSystem中，用于访问集群
                    hdfsFileSystem = FileSystem.get(configuration);
                    
                    //查看要创建的目录是否已经存在，如果存在则返回并报错
                    if (hdfsFileSystem.exists(path)) {
                        System.out.println("already created \t" + configuration.get("fs.default.name") + dir); 
                    } else { //如果不存在该目录则创建目录
                        hdfsFileSystem.mkdirs(path);
                        System.out.println("new dir \t" + configuration.get("fs.default.name") + dir);
                    }
                    
                } catch (Exception e) {
                	System.out.println("create dir exception"+e.getMessage());
                    e.printStackTrace();
                }
                finally{
                	try {
						hdfsFileSystem.close();
					} catch (Exception e) {
						System.out.println("close hdfsFileSystem exception"+e.getMessage());
	                    e.printStackTrace();
					}	
                }
                return null;
            }
        });
                System.out.println("finished");
    }
    
    /**
     * 删除对应目录
     * @param dir 目录在hdfs上的路径
     */
    public void deleteDir(final String dir) {
        final Path path = new Path(dir);
        
        //使用hdfs这个用户删除目录
        ugi.doAs(new PrivilegedAction<String>() {
           
            public String run() {
                // TODO Auto-generated method stub
                try {
                    
                    //加载配置项到FileSystem中，用于访问集群
                    hdfsFileSystem = FileSystem.get(configuration);
                    
                    //查看要创建的目录是否已经存在
                    //如果存在则进行删除操作，如果不存在则说明不存在并退出
                    boolean isExits = hdfsFileSystem.exists(path);
                    if (isExits) {
                        //删除文件目录
                        boolean isDel = hdfsFileSystem.delete(path, true);
                        System.out.println(configuration.get("fs.default.name") + path + "    是否被删除? \t " + isDel);
                    } else {
                        System.out.println(configuration.get("fs.default.name") + path + "    是否存在? \t " + isExits);
                    }
                    
                } catch (Exception e) {
                	System.out.println("deleteDir exception"+e.getMessage());
                    e.printStackTrace();
                }finally{
                	try {
						hdfsFileSystem.close();
					} catch (Exception e) {
						System.out.println("close hdfsFileSystem exception"+e.getMessage());
	                    e.printStackTrace();
					}	
                }
                return null;
            }
        });
        System.out.println("finished");
       
    }
    
    public void copyFile(String localSrc,String hdfsDst) throws IOException{
    	final Path src = new Path(localSrc);
    	final Path dst = new Path(hdfsDst);
        ugi.doAs(new PrivilegedAction<String>() {
           
            public String run() {
                // TODO Auto-generated method stub
                try {
                    hdfsFileSystem = FileSystem.get(configuration);
                    hdfsFileSystem.copyFromLocalFile(src, dst);
                    
                    FileStatus files [] = hdfsFileSystem.listStatus(dst);
                    System.out.println("Upload to \t" + configuration.get("fs.default.name") + dst);
                    System.out.println("copy file：");
                    for (FileStatus fileStatus : files) {
                        System.out.println(fileStatus.getPath());
                    }
                } catch (Exception e) {
                	System.out.println("close hdfsFileSystem exception"+e.getMessage());
                    e.printStackTrace();
                }finally{
                	try {
						hdfsFileSystem.close();
					} catch (Exception e) {
						System.out.println("close hdfsFileSystem exception"+e.getMessage());
	                    e.printStackTrace();
					}	
                }
                return null;
            }
        });
        System.out.println("finished");
    }
    
    
    public void createFile(final String localSrc,final String fileContent) throws IOException{
    	final Path src = new Path(localSrc);
        ugi.doAs(new PrivilegedAction<String>() {
         
            public String run() {
                // TODO Auto-generated method stub
                try {
                    hdfsFileSystem = FileSystem.get(configuration);
                    byte[] bytes = fileContent.getBytes();
                    
                    FSDataOutputStream output = hdfsFileSystem.create(src);
                    output.write(bytes);
                    System.out.println("new file \t" + configuration.get("fs.default.name") + localSrc);
                    output.flush();
                    output.close();
                } catch (Exception e) {
                	System.out.println("createFile exception"+e.getMessage());
                    e.printStackTrace();
                }finally{
                	try {
						hdfsFileSystem.close();
					} catch (Exception e) {
						System.out.println("close hdfsFileSystem exception"+e.getMessage());
	                    e.printStackTrace();
					}	
                }
                return null;
            }
        });
        System.out.println("finished");
    }
    
    
    public void closeFileSystem() throws IOException {
        hdfsFileSystem.close(); 
    }
    
    public static void main(String[] args) throws IOException {
    	 System.out.println("begin");
    	HdfsDemo hdfsDemo = new HdfsDemo();
    	System.out.println("ops is "+args[0]);
        if(args[0].trim().equals("createDir"))
        {
        	System.out.println("in create dir");
            //在集群的HDFS上面创建/data/upload目录
        	hdfsDemo.createDir("/data/test");
        }
    	else if (args[0].trim().equals("createFile")) {
    	    //在集群的/data/upload目录下面添加wordCount.txt文件并在文件中写入Hello, hdfs
            hdfsDemo.createFile("/data/test/zhejiang.txt", "Hello, Zhejiang");	
    	}else if (args[0].trim().equals("uploadFile")){
    		//上传文件
    		hdfsDemo.copyFile("/root/test", "/data/upload");
    	}
    	else if(args[0].trim().equals("deleteDir")){
    	       //将集群中的/data/testtt目录删除        
            hdfsDemo.deleteDir("/data"); 
    	}       
        System.out.println("end");
        //关闭FileSystem
       // hdfsDemo.closeFileSystem();
    }
   
    
}
