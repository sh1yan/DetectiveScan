package top.sh1yan.util;

import top.sh1yan.api.MasscanApi;
import top.sh1yan.api.NampApi;

import java.io.*;
import java.util.Properties;

public class ExposureIpDataScan {

//    int [] web_ports_lists = {80, 81, 82, 88, 91, 443, 2018, 2019, 7000, 7001, 7002, 8000, 8001, 8008, 8014, 8042, 8069, 8080, 8081, 8082, 8087, 8088, 8090, 8091, 8100, 8118, 8123, 8172, 8222, 8243, 8251, 8280, 8281, 8333, 8443, 8500, 8834, 8879, 8880, 8888, 8983, 9000, 9043, 9060, 9080, 9090, 9091, 9200, 9443, 9800, 9981, 10000, 10001, 10002, 10003, 12443, 15672, 16080, 18080, 18082, 18091, 18092, 20720, 28017};
    String web_port_lists = "80,81,82,88,91,443,2018,2019,7000,7001,7002,8000,8001,8008,8014,8042,8069,8080,8081,8082,8087,8088,8090,8091,8100,8118,8123,8172,8222,8243,8251,8280,8281,8333,8443,8500,8834,8879,8880,8888,8983,9000,9043,9060,9080,9090,9091,9200,9443,9800,9981,10000,10001,10002,10003,12443,15672,16080,18080,18082,18091,18092,20720,28017";
    String all_ports = "1-65535";
    public String scan_port = "22";
    String threshold = null;
    MasscanApi masscan_run;
    String start_config = "0";

    public ExposureIpDataScan() {
        System.out.println("[*] 请输入 web 或者 all 这两个其中一个选项。");
        System.out.println("[*] web 为只扫描常见的 web 服务开放的端口。");
        System.out.println("[*] all 为只扫描 ip 地址的全量端口，即 65535 个端口。");
    }

    public ExposureIpDataScan(String seclect_port) {
        if ("web".equals(seclect_port)){
            this.threshold = "web_port_lists";
        }else if ("all".equals(seclect_port)){
            this.threshold = "all_ports";
        }else if ("custom".equals(seclect_port)){
            this.threshold = "custom";
        }else {
            System.out.println("[*] 请输入 web 、 all 、 custom 这三个其中一个选项。");
            System.out.println("[*] web 为只扫描常见的 web 服务开放的端口。");
            System.out.println("[*] all 为只扫描 ip 地址的全量端口，即 65535 个端口。");
            System.out.println("[*] custom 为只扫描用户自定义的端口。");
        }
    }

    public void runMasscanNmap(String is_start_config){

        if ("web_port_lists".equals(threshold)){
            this.masscan_run = new MasscanApi(web_port_lists);
        }else if ("all_ports".equals(threshold)){
            this.masscan_run = new MasscanApi(all_ports);
        }else if ("custom".equals(threshold)){
            this.start_config = "1";
            this.getConfigData();
            this.masscan_run = new MasscanApi(this.scan_port);
        }else {
            return;
        }

        if ("-c".equals(is_start_config)){
            this.start_config = "1";
        }

        this.masscan_run.getMasscan(start_config);
        NampApi start_nmap = new NampApi();
        start_nmap.getNmap(start_config);
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
        this.scan_port = pro.getProperty("scan_port");
    }

}
