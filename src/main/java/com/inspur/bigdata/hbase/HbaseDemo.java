package com.inspur.bigdata.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class HbaseDemo {

    public static Configuration configuration;
    public static Connection connection;
    public static Admin admin;

    public static void main(String[] args) throws IOException {
    	  System.out.println("begin...");
    	  System.out.println("ops is "+args[0]);
    	if(args[0].trim().equals("create"))
        {
    		createTable("t2",new String[]{"cf1","cf2"});
        }
    	else if (args[0].trim().equals("insert")) {
    		insterRow("t2", "rw1", "cf1", "q1", "val1");	
    	}else if(args[0].trim().equals("scan")){
    		scanData("t2", "rw1", "rw2");	
    	}else if(args[0].trim().equals("deleteRow")){
    		   deleteRow("t2","rw1","cf1","q1");	
    	}else if(args[0].trim().equals("delete")){
    		deleteTable("t2");
    	} 
    	 System.out.println("begin...");
    }

    //初始化链接
    public static void init(){
    	
    	Configuration configuration = new Configuration();
    	//configuration.addResource(new Path("E:\\osworkspace\\bigdata\\src\\main\\resources\\hbase-site.xml"));
    	//linux
    	configuration.addResource(new Path("/root/app/resources/hbase-site.xml"));
    	configuration = HBaseConfiguration.create(configuration);
//    	HBaseAdmin admin = new HBaseAdmin(conf);//HBaseAdmin负责跟表相关的操作如create,drop等
//    	HTable table = new HTable(conf, Bytes.toBytes("blog"));//HTabel负责跟记录相关的操作如增删改查
    	
//        configuration = HBaseConfiguration.create();
//        configuration.set("hbase.zookeeper.quorum","cluster2-agent-0.cluster2-agent.bigdata.svc.cluster.local,cluster2-agent-1.cluster2-agent.bigdata.svc.cluster.local,cluster2-agent-2.cluster2-agent.bigdata.svc.cluster.local");
//        configuration.set("hbase.zookeeper.property.clientPort","2181");
//        configuration.set("zookeeper.znode.parent","/hbase");

        try {
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //关闭连接
    public static  void close(){
        try {
            if(null != admin)
                admin.close();
            if(null != connection)
                connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //建表
    public static void createTable(String tableNmae,String[] cols) throws IOException {

        init();
        TableName tableName = TableName.valueOf(tableNmae);

        if(admin.tableExists(tableName)){
            System.out.println("talbe is exists!");
        }else {
            HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
            for(String col:cols){
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(col);
                hTableDescriptor.addFamily(hColumnDescriptor);
            }
            admin.createTable(hTableDescriptor);
        }
        close();
        System.out.println("Finished!");
    }

    //删表
    public static void deleteTable(String tableName) throws IOException {
        init();
        TableName tn = TableName.valueOf(tableName);
        if (admin.tableExists(tn)) {
            admin.disableTable(tn);
            admin.deleteTable(tn);
        }
        close();
        System.out.println("Finished!");
    }

    //查看已有表
    public static void listTables() throws IOException {
        init();
        HTableDescriptor hTableDescriptors[] = admin.listTables();
        for(HTableDescriptor hTableDescriptor :hTableDescriptors){
            System.out.println(hTableDescriptor.getNameAsString());
        }
        close();
        System.out.println("Finished!");
    }

    //插入数据
    public static void insterRow(String tableName,String rowkey,String colFamily,String col,String val) throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes(val));
        table.put(put);

        //批量插入
       /* List<Put> putList = new ArrayList<Put>();
        puts.add(put);
        table.put(putList);*/
        table.close();
        close();
        System.out.println("Finished!");
    }

    //删除数据
    public static void deleteRow(String tableName,String rowkey,String colFamily,String col) throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowkey));
        //删除指定列族
        //delete.addFamily(Bytes.toBytes(colFamily));
        //删除指定列
        //delete.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        table.delete(delete);
        //批量删除
       /* List<Delete> deleteList = new ArrayList<Delete>();
        deleteList.add(delete);
        table.delete(deleteList);*/
        table.close();
        close();
        System.out.println("Finished!");
    }

    //根据rowkey查找数据
    public static void getData(String tableName,String rowkey,String colFamily,String col)throws  IOException{
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Get get = new Get(Bytes.toBytes(rowkey));
        //获取指定列族数据
        //get.addFamily(Bytes.toBytes(colFamily));
        //获取指定列数据
        //get.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        Result result = table.get(get);

        showCell(result);
        table.close();
        close();
        System.out.println("Finished!");
    }

    //格式化输出
    public static void showCell(Result result){
        Cell[] cells = result.rawCells();
        for(Cell cell:cells){
            System.out.println("RowName:"+new String(CellUtil.cloneRow(cell))+" ");
            System.out.println("Timetamp:"+cell.getTimestamp()+" ");
            System.out.println("column Family:"+new String(CellUtil.cloneFamily(cell))+" ");
            System.out.println("row Name:"+new String(CellUtil.cloneQualifier(cell))+" ");
            System.out.println("value:"+new String(CellUtil.cloneValue(cell))+" ");
        }
    }

    //批量查找数据
    public static void scanData(String tableName,String startRow,String stopRow)throws IOException{
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        //scan.setStartRow(Bytes.toBytes(startRow));
        //scan.setStopRow(Bytes.toBytes(stopRow));
        ResultScanner resultScanner = table.getScanner(scan);
        for(Result result : resultScanner){
            showCell(result);
        }
        table.close();
        close();
        System.out.println("Finished!");
    }
}