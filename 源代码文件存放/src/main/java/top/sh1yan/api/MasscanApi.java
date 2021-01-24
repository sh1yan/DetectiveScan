package top.sh1yan.api;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class MasscanApi {

    private ArrayList<String> ports; // 存放开放端口的空，数据样式如下：[x.x.x.x:xxx,x.x.x.x:xxx]
    private String mode; // 存放端口号的空间
    private JSONObject json_data; // 把读取的数据临时转为json格式
    public  String rate_value = "2000";

    public MasscanApi(String mode) {
        /**
         * 1. 有参构造函数
         * 2. 该使用该功能类必须输入需要扫描的端口号
         *
         */
        this.mode = mode;
        ports = new ArrayList<String>();
    }


    private void OptenPorts_exclusive(String line,String re,String lishi_line){
        if (line.length()>6){
            lishi_line = line.replaceFirst(re,"");
            this.json_data = new JSONObject(lishi_line);
            String ip = String.valueOf(this.json_data.get("ip"));
            JSONObject portsList = (JSONObject)this.json_data.getJSONArray("ports").get(0);
            String port = String.valueOf(portsList.get("port"));
            this.ports.add(ip+":"+port);
        }else {
            // System.out.println("去除掉 xx ：" + line);
        }
    }


    private void getConfigData(){
        Properties pro = new Properties();
        File directory = new File("");
        File filePath = directory.getAbsoluteFile();
        String filePath1 = filePath + "/SourceDocument/Config/detective_config.properties";
        InputStream inStr = null;
        try {
            inStr = new BufferedInputStream(new FileInputStream(filePath1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            pro.load(inStr);
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } // 上述设置的路径为相对路径，因为文件读取时，会从SRC目录下开始读取
        this.rate_value = pro.getProperty("rate_value");
    }


    private ArrayList<String> getOptenPorts(){
        /**
         * 1. 该函数通过读取本地masscan.json文件
         * 2. 返回相应的ip+端口号的数据
         * ArrayList<String>
         */
        ports = new ArrayList<String>();
        File directory = new File("");
        File filePath = directory.getAbsoluteFile();
        String resultPath = filePath+"/SourceDocument/CacheFolder/masscan.json";
        FileReader fr = null;
        BufferedReader br  = null;
        try {
            File file = new File(resultPath);  //把当前路径字符串转换为File类型
            fr = new FileReader(file);  // 以字符形式读取数据流中数据
            br = new BufferedReader(fr); //从字符输入流中读取文本并缓冲字符，以便有效地读取字符，数组和行
            String line = "";

            System.out.println();

            while((line = br.readLine()) != null){ // readLine() 包含行的内容的字符串，不包括任何行终止字符，如果已达到流的末尾，则为返回null
                String lishi_line = "";
                if(line.startsWith("[")){
                    // 去除掉 [
                    this.OptenPorts_exclusive(line,"\\[",lishi_line);
                }else if(line.startsWith(",")){
                    // 去除掉 ,
                    this.OptenPorts_exclusive(line,",",lishi_line);
                }else if (line.startsWith("]")){
                    // 去除掉 ]
                    this.OptenPorts_exclusive(line,"]",lishi_line);
                }else if(line.endsWith("{finished: 1}")){
                    // 去掉 {finished: 1}
                    System.out.println("已去除扫描结果中：{finished: 1} 关键词");

                }else {
                    lishi_line = line;
                    this.json_data = new JSONObject(lishi_line);
                    String ip = String.valueOf(this.json_data.get("ip"));
                    JSONObject portsList = (JSONObject)this.json_data.getJSONArray("ports").get(0);
                    String port = String.valueOf(portsList.get("port"));
                    this.ports.add(ip+":"+port);
                }
            }
        } catch (FileNotFoundException e) {
            // FileReader(File file) 可能会产生的异常
            e.printStackTrace();
        } catch (IOException e) {
            // BufferedReader.readLine() 可能会产生的异常
            e.printStackTrace();
        }finally{
            try {
                if(br != null) br.close(); //关闭字符形式读取的流数据
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ports;
    }

    private void runMasscanStart(String str){
        /**
         * 1. str 为端口号，传入端口号进行扫描。
         * 2. 该函数为通过执行本地,masscan工具快速扫描开放端口号情况
         *
         */
        ProcessBuilder processBuilder = new ProcessBuilder();
        File directory = new File("");
        File filePath = directory.getAbsoluteFile();
        processBuilder.command("masscan","-iL",filePath+"/SourceDocument/scan_ip.txt","-p",str,"-oJ",filePath+"/SourceDocument/CacheFolder/masscan.json","--rate",this.rate_value);
        //将标准输入流和错误输入流合并，通过标准输入流读取信息
        processBuilder.redirectErrorStream(true);
        try {
            //启动进程
            Process start = processBuilder.start();
            //获取输入流
            InputStream inputStream = start.getInputStream();
            //转成字符输入流
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8"); // 编码可根据实际cmd界面调整
            int len = -1;
            char[] c = new char[1024];
            StringBuffer outputString = new StringBuffer();
            //读取进程输入流中的内容
            while ((len = inputStreamReader.read(c)) != -1) {
                String s = new String(c, 0, len);
                outputString.append(s);
                System.out.print(s);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeXlsxData(){
        /**
         *  1. 该功能函数用于把masscan扫描的开放端口地址，存放到xlsx表中。
         *
         */
        ArrayList<String> ip_port_list = this.getOptenPorts(); // 获取扫描结果数据ArrayList格式数据
        File directory = new File("");
        File filePath = directory.getAbsoluteFile(); // 获取当前项目路径
        XSSFWorkbook wb = new XSSFWorkbook(); //新建一个工作簿
        try {
            FileOutputStream fout=new FileOutputStream(new File(filePath+"/SourceDocument/CacheFolder/masscan_ports_result.xlsx")); // 生成xlsx文件
            XSSFSheet sheet1 = wb.createSheet("Sheet1"); // 创建一个sheet页
            Row row1=sheet1.createRow(0); // 创建一个行 第一行
            XSSFCell cells[] = new XSSFCell[1]; // 创建一个高级表格单元类
            String[] titles = new String[]{"IP地址:Port号"}; // 设置title名称（第一行第一个）
            for (int i=0;i<1;i++){
                cells[0]= (XSSFCell) row1.createCell(i);
                cells[0].setCellValue(titles[i]); // 填充标题名称
            }
            for (int i = 0; i< ip_port_list.size(); i++){
                XSSFRow row = sheet1.createRow(i+1); // 创建单元格
                String ip_port = ip_port_list.get(i); // 获取ArrayList中存储的数据
                XSSFCell cell = row.createCell(0); // 标选第一列，进行数据存放
                cell.setCellValue(ip_port); // 扫描数据导入xlsx表中
            }
            wb.write(fout);//Workbook提供了write的方法
            fout.close();//将输出流关闭
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getMasscan(String is_start_config){
        System.out.println("[*] Masscan 已启动，正在扫描中~");
        if ("1".equals(is_start_config)){
            // 当参数为 1 时，则使用本地配置文件，若不是则使用默认的 2000
            this.getConfigData();
        }
        this.runMasscanStart(this.mode);
        this.writeXlsxData();
        System.out.println("[*] IP地址已扫描完毕，请等待IP端口地址扫描~");
    }
}
