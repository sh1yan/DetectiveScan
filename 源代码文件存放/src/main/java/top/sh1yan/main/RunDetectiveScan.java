package top.sh1yan.main;

import top.sh1yan.util.ExposureIpDataScan;
import top.sh1yan.util.JsonToDataTools;

public class RunDetectiveScan {

    public static ExposureIpDataScan start_scan;
    public static JsonToDataTools start_tool;

    public static void main(String[] args) {
        /**
         *2020年8月25日编写，当外接无参数时，则默认提示使用方法！
         *
         */
        String prompt = "请在命令行中填写相应参数！\r\n"
                + "工具使用说明：\r\n"
                + "[root@shiyan20200825 DetectiveScan]#java -jar DetectiveScan.jar [参数] [可选/-c]  \r\n"
                + "具体工具使用说明：\r\n"
                + "0. 本工具在 Linux 和 Windows下均可运行，但需注意几点：\r\n"
                + "0.1  使用本工具前，请保证系统环境已安装 masscan 和 nmap 这两个软件\r\n"
                + "0.2  Linux 环境：默认通过系统软件包进行安装即可，但需注意一定设置好软连接\r\n"
                + "0.3  Windows 环境：masscan 必须配置环境变量，nmap 必须在配置文件中设置 nmap 安装目录地址\r\n"
                + "1. 工具目前有 4 个固定参数和 1 个可选参数，具体介绍如下：\r\n"
                + "1.1  固定参数：web、all、custom、jtx、jtr；可选参数：-c\r\n"
                + "1.2  web 为只扫描常见的 web 服务开放的端口\r\n"
                + "1.3  all 为只扫描 ip 地址的全量端口，即 65535 个端口\r\n"
                + "1.4  custom 为扫描 config 中设置的端口地址 \r\n"
                + "1.5  jtx 为把 masscan.json 文件转为为 xlsx 文件\r\n"
                + "1.6  jtr 为通过 masscan.json 文件获得最终端口扫描结果\r\n"
                + "1.7  -c  为使用本地配置文件，目前仅支持 masscan 探测速度配置和 nmap 安装路径配置\r\n"
                + "2. 需要扫描的IP地址，请在 /SourceDocument/ 目录中的 scan_ip.txt 中填写 \r\n"
                + "3. 工具本身所有默认文件夹、文件名都请勿更改，毕竟都写si了\r\n"
                + "4. 工具为第3版，依旧很多潜在bug，若出现问题请把报错截图和详细描述以邮件形式发送至我邮箱\r\n"
                + "5. 邮箱地址：506130869@qq.com\r\n"
                + "\r\n";
        isUserInport(args,prompt);

    }

    public static void isUserInport(String[] inports, String prompt){

        String[] static_input = new String[]{"web","all","jtx","jtr","custom"};

        if (inports.length == 0) {
            System.out.println(prompt);
        } else if (inports.length == 1) {
            String input = String.valueOf(inports[0]);
            runDS(input,"not load config",prompt);
        } else if (inports.length == 2) {
            String input_1 = String.valueOf(inports[0]);
            String input_2 = String.valueOf(inports[1]);
            String input_static = "not";
            String input_optional = "0";
            for (String input_key:static_input){
                if (input_key.equals(input_1)){
                    input_static = input_1;
                }else if (input_key.equals(input_2)){
                    input_static = input_2;
                }
            }
            if ("-c".equals(input_1)){
                input_optional = input_1;
            }else if ("-c".equals(input_2)){
                input_optional = input_2;
            }
            runDS(input_static,input_optional,prompt);
        } else {
            System.out.println(prompt);
        }
    }

    public static void runDS(String input, String is_config, String prompt){
        switch (input){
            case "web":
            case "all":
            case "custom":
                start_scan = new ExposureIpDataScan(input);
                start_scan.runMasscanNmap(is_config);
                break;
            case "jtx":
            case "jtr":
                start_tool = new JsonToDataTools();
                start_tool.jsonToDataTool(input);
                start_tool.runJsonToDataTools(is_config);
                break;
            default:
                System.out.println(prompt);
        }
    }



}
