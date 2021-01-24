package top.sh1yan.api;

import net.dongliu.requests.Requests;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.*;
import org.nmap4j.Nmap4j;
import org.nmap4j.core.nmap.NMapExecutionException;
import org.nmap4j.core.nmap.NMapInitializationException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NampApi {

    String nmap_run_result_temporary = null;
    HashMap<String, ArrayList<String>> nmap_xml = new HashMap<String,ArrayList<String>>();
    Document document;
    HashMap<String,ArrayList<String>> masscan_result_ip_port = new HashMap<String,ArrayList<String>>();
    public String nmap_path = "/";

    private void writeXml(){
        // 暂时该功能不使用
        String xml = nmap_run_result_temporary;
        File directory = new File("");
        File filePath = directory.getAbsoluteFile();

        try {
            writeOcrStrtoFile(xml, String.valueOf(filePath),"namp_result_test.xml");
        } catch (Exception e) {
            e.printStackTrace();
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
        this.nmap_path = pro.getProperty("path_nmap");
    }

    private void writeOcrStrtoFile(String result, String outPath, String outFileName)
            throws Exception {
        File dir = new File(outPath);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File txt = new File(outPath + "/SourceDocument/CacheFolder/" + outFileName);
        if (!txt.exists()) {
            txt.createNewFile();
        }
        byte bytes[] = new byte[512];
        bytes = result.getBytes();
        int b = bytes.length; // 是字节的长度，不是字符串的长度
        FileOutputStream fos = new FileOutputStream(txt);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }

    private void runNmapStart(String nmap_path,String nmap_ip,String nmap_port){
        // runNmapStart("D:\\My-software\\Nmap","localhost","445");
        String nmap_run_result = null;
        Nmap4j Ar1 = new Nmap4j(nmap_path);
//        Ar1.includeHosts("101.200.87.5");
        Ar1.includeHosts(nmap_ip);
        Ar1.addFlags("-T3 -oX - -sV -Pn -sS"+" -p"+nmap_port);
        try {
            Ar1.execute();
        } catch (NMapInitializationException e) {
            e.printStackTrace();
        } catch (NMapExecutionException e) {
            e.printStackTrace();
        }
        if(!Ar1.hasError()){
            String nmapRun = Ar1.getOutput();
            nmap_run_result = nmapRun;
        } else {
            System.out.println(Ar1.getExecutionResults().getErrors());
        }
        this.nmap_run_result_temporary = nmap_run_result;
    }

    private void setPortStateTest(String docNodes,String ip_port,int index,int data_index){
        List list_name1 = document.selectNodes(docNodes);
        for (int i=0;i < list_name1.size();i++){
            if(i == index){
                Attribute AAr2 = (Attribute)list_name1.get(i);
                String portid1 = AAr2.getValue();
                nmap_xml.get(ip_port).add(portid1);
            }
        }
        if (nmap_xml.get(ip_port).size()<data_index+1){
            nmap_xml.get(ip_port).add("null value");
        }
    }

    private void getNmapData(String ip){
        // {IP地址:扫描端口=[0:端口状态,1:端口号,2:协议,3:端口服务,4:端口服务版本,5:服务容器,6:操作系统类型，7:地理位置，8:运营商]}
        // getNmapData("localhost");
        String name11;
        String[] ip_place_78 = this.getPlace(ip);
        try {
            this.document = DocumentHelper.parseText(nmap_run_result_temporary);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List list_portid = document.selectNodes("//port/@portid");
        Node node11 = document.selectSingleNode("//address");
        try {
            name11 = node11.valueOf("@addr");
        }catch (NullPointerException e){
            return;
        }
        for (int i=0;i < list_portid.size();i++){
            Attribute AAr1 = (Attribute)list_portid.get(i);
            String portid = AAr1.getValue();
            ArrayList<String> xml_data = new ArrayList<String>();
            nmap_xml.put(name11+":"+portid,xml_data);
            setPortStateTest("//state/@state",name11+":"+portid,i,0);
            setPortStateTest("//port/@portid",name11+":"+portid,i,1);
            setPortStateTest("//port/@protocol",name11+":"+portid,i,2);
            setPortStateTest("//service/@name",name11+":"+portid,i,3);
            setPortStateTest("//service/@version",name11+":"+portid,i,4);
            setPortStateTest("//service/@product",name11+":"+portid,i,5);
            setPortStateTest("//service/@ostype",name11+":"+portid,i,6);
            nmap_xml.get(name11+":"+portid).add(ip_place_78[0]);
            nmap_xml.get(name11+":"+portid).add(ip_place_78[1]);
        }
    }

    private String[] getPlace(String ip){
        String resp = "";
        String url_api = "http://ip.siteloop.net/ip/";
        try {
            resp = Requests.get(url_api+ip).socksTimeout(180000).connectTimeout(180000).send().readToText();
        }catch (Exception e){
            String[] ip_place_result = new String[]{"null value", "null value"};
            return ip_place_result;
        }
        Pattern r = Pattern.compile("<title>(.*?)</title>");
        Matcher m = r.matcher(resp);
        if (m.find()) {
            String ip_title = m.group(0);
            String ip_title1 = ip_title.replace("<title>", "");
            String ip_title2 = ip_title1.replace("</title>", "");
            String[] ip_data = ip_title2.split(" ");
            if (ip_title2.length() == 0){
                String[] ip_place_result = new String[]{"null value", "null value"};
                return ip_place_result;
            }else if (ip_data.length == 2){
                String[] ip_place_result = new String[]{ip_data[1].replace("属于",""), "null value"};
                return ip_place_result;
            }else {
                String[] ip_place_result = new String[]{ip_data[1].replace("属于",""), ip_data[2]};
                return ip_place_result;
            }
        }
        String[] ip_place_result = new String[]{"null value", "null value"};
        return ip_place_result;
    }

    private  void createCell(String ip_port, String port_state, String portid, String protocol,String name ,String version, String product, String ostype, String place, String operator, XSSFSheet sheet) {
        XSSFRow dataRow = sheet.createRow(sheet.getLastRowNum());
        dataRow.createCell(0).setCellValue(ip_port);
        dataRow.createCell(1).setCellValue(port_state);
        dataRow.createCell(2).setCellValue(portid);
        dataRow.createCell(3).setCellValue(protocol);
        dataRow.createCell(4).setCellValue(name);
        dataRow.createCell(5).setCellValue(version);
        dataRow.createCell(6).setCellValue(product);
        dataRow.createCell(7).setCellValue(ostype);
        dataRow.createCell(8).setCellValue(place);
        dataRow.createCell(9).setCellValue(operator);
    }

    private void getFinalResult(){
        // {"101.200.87.5:10086":["open", "10086", "tcp", "ssh", "6.6.1", "OpenSSH", "null value", "北京市", "阿里云BGP数据中心"]}

        File directory = new File("");
        File filePath = directory.getAbsoluteFile(); // 获取当前项目路径
        XSSFWorkbook nmap_xml_excel = new XSSFWorkbook(); //新建一个工作簿
        try {
            FileOutputStream fout=new FileOutputStream(new File(filePath+"/OutcomeDocument/暴露面资产IP地址信息.xlsx")); // 生成xlsx文件
            XSSFSheet sheet1 = nmap_xml_excel.createSheet("Sheet1"); // 创建一个sheet页
            Row row1=sheet1.createRow(0); // 创建一个行 第一行
            XSSFCell cells[] = new XSSFCell[1]; // 创建一个高级表格单元类
            String[] titles = new String[]{"IP地址:扫描端口","端口状态","端口号","协议","端口服务","服务容器版本","服务容器","操作系统类型","地理位置","运营商"}; // 设置title名称（第一行第一个）
            for (int i=0;i<10;i++){
                cells[0]= (XSSFCell) row1.createCell(i);
                cells[0].setCellValue(titles[i]); // 填充标题名称
            }
            ArrayList<String> key_list = new ArrayList<String>();
            for(String key: this.nmap_xml.keySet()) {
                key_list.add(key);
            }
                for (int i = 0; i< this.nmap_xml.size(); i++){
                    String key = key_list.get(i);
                    XSSFRow row = sheet1.createRow(i+1); // 创建单元格
                    XSSFCell cell = row.createCell(0); // 标选第一列，进行数据存放
                    this.createCell(key,
                            this.nmap_xml.get(key).get(0),
                            this.nmap_xml.get(key).get(1),
                            this.nmap_xml.get(key).get(2),
                            this.nmap_xml.get(key).get(3),
                            this.nmap_xml.get(key).get(4),
                            this.nmap_xml.get(key).get(5),
                            this.nmap_xml.get(key).get(6),
                            this.nmap_xml.get(key).get(7),
                            this.nmap_xml.get(key).get(8),
                            sheet1);
                }
            nmap_xml_excel.write(fout);//Workbook提供了write的方法
            fout.close();//将输出流关闭
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readIpsPortsMap(){
        File directory = new File("");
        File filePath = directory.getAbsoluteFile();
        File excelFile = new File(filePath+"/SourceDocument/CacheFolder/"+"masscan_ports_result.xlsx");
        XSSFWorkbook wb = null;
        try {
            wb = new XSSFWorkbook(new FileInputStream(excelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet sheet = wb.getSheetAt(0);
        for (Row row : sheet) {
            for (Cell cell : row) {
                String tpra = String.valueOf(cell);
                if ("IP地址:Port号".equals(tpra)){
//                    System.out.println(tpra);
                    continue;
                }
                try {
                    String [] tpra_list = tpra.split(":");
                    // 存储样式 {x.x.x.x:xx=[x.x.x.x,xx]}
                    String tpra_key = tpra;
                    ArrayList<String> tpra_ip_port_list = new ArrayList<String>();
                    String tpra_ip = tpra_list[0];
                    String tpra_port = tpra_list[1];
                    tpra_ip_port_list.add(tpra_ip);
                    tpra_ip_port_list.add(tpra_port);
                    masscan_result_ip_port.put(tpra_key,tpra_ip_port_list);
//                    System.out.println(tpra_ip+":"+tpra_port);
                }catch (Exception e){
                    System.out.println("请检查 /SourceDocument/CacheFolder/ 目录中 masscan_ports_result.xlsx 文件是否存在异常！");
                }
            }
        }
//        System.out.println(masscan_result_ip_port.size());
    }

    public void getNmap(String is_start_config){
        this.readIpsPortsMap();
        if ("1".equals(is_start_config)){
            this.getConfigData();
        }
        System.out.println("[*] 本次共需扫描"+this.masscan_result_ip_port.size()+"个IP端口地址，请耐心等候~");
        for(String key: this.masscan_result_ip_port.keySet()) {
            String wait_scan_ip = this.masscan_result_ip_port.get(key).get(0);
            String wait_scan_port = this.masscan_result_ip_port.get(key).get(1);
            System.out.println("[*] 正在扫描IP端口地址："+ wait_scan_ip+":"+wait_scan_port);
            this.runNmapStart(this.nmap_path,wait_scan_ip,wait_scan_port);
            System.out.println("[*] 以上IP端口地址扫描结束~");
            this.getNmapData(wait_scan_ip);
        }
        this.getFinalResult();
        System.out.println("[*] 全部IP端口地址已扫描完成，详见扫描结果目录~");
    }

}
