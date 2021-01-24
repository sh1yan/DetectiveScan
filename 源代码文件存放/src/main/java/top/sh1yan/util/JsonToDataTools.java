package top.sh1yan.util;

import top.sh1yan.api.MasscanApi;
import top.sh1yan.api.NampApi;

public class JsonToDataTools {

    String threshold = null;
    String start_nmap_config = "0";

    public JsonToDataTools() {

    }

    public void jsonToXlsx(){
        System.out.println("[*] 正在运行json转xlsx功能~");
        try {
            MasscanApi Ar1 = new MasscanApi("jsonToXlsx");
            Ar1.writeXlsxData();
            System.out.println("[*] masscan.json 已成功转换成 xlsx 文件");
        }catch (Exception e){
            System.out.println("[*] Error！请检查 masscan.json 文件是否正常~");
        }
    }

    public void jsonToNmapResult(String is_start_nmap_config){
        System.out.println("[*] 正在对 masscan.json 进行扫描~");
        try{
            MasscanApi Ar2 = new MasscanApi("jsonToNmapResult");
            Ar2.writeXlsxData();
            System.out.println("[*] masscan.json 已扫描完毕，请等待 nmap 扫描~");
        }catch (Exception e){
            System.out.println("[*] Error！请检查 masscan.json 文件是否正常~");
        }
        try {
            NampApi Br1 = new NampApi();
            Br1.getNmap(is_start_nmap_config);
        }catch (Exception e){
            System.out.println("[*] Error！功能出现错误，请将错误信息截图发给作者本人~");
            System.out.println(e);
        }
    }

    public void jsonToDataTool(String seclect_port) {
        if ("jtx".equals(seclect_port)){
            this.threshold = "json_to_xlsx";
        }else if ("jtr".equals(seclect_port)){
            this.threshold = "json_to_nmap_result";
        }else {
            System.out.println("[*] 请输入 jtx 或者 jtr 这两个其中一个选项。");
            System.out.println("[*] jtx 为把 masscan.json 文件转为为 xlsx 文件。");
            System.out.println("[*] jtr 为通过 masscan.json 文件获得最终端口扫描结果。");
        }
    }

    public void runJsonToDataTools(String is_start_nmap_config){
        if ("json_to_xlsx".equals(threshold)){
            JsonToDataTools jtx = new JsonToDataTools();
            jtx.jsonToXlsx();
        }else if ("json_to_nmap_result".equals(threshold)){
            JsonToDataTools jtr = new JsonToDataTools();

            if ("-c".equals(is_start_nmap_config)){
                this.start_nmap_config = "1";
            }

            jtr.jsonToNmapResult(start_nmap_config);
        }else {
            return;
        }

    }



}
