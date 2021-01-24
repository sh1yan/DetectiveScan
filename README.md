**工具名**：DetectiveScan

**功能说明**：

0. 本工具在 Linux 和 Windows下均可运行，但需注意几点：
0.1  使用本工具前，请保证系统环境已安装 masscan 和 nmap 这两个软件
0.2  Linux 环境：默认通过系统软件包进行安装即可，但需注意一定设置好软连接
0.3  Windows 环境：masscan 必须配置环境变量，nmap 必须在配置文件中设置 nmap 安装目录地址
1. 工具目前有 4 个固定参数和 1 个可选参数，具体介绍如下：
1.1  固定参数：web、all、custom、jtx、jtr；可选参数：-c
1.2  web 为只扫描常见的 web 服务开放的端口
1.3  all 为只扫描 ip 地址的全量端口，即 65535 个端口
1.4  custom 为扫描 config 中设置的端口地址
1.5  jtx 为把 masscan.json 文件转为为 xlsx 文件
1.6  jtr 为通过 masscan.json 文件获得最终端口扫描结果
1.7  -c  为使用本地配置文件，目前仅支持 masscan 探测速度配置和 nmap 安装路径配置
2. 需要扫描的IP地址，请在 /SourceDocument/ 目录中的 scan_ip.txt 中填写
3. 工具本身所有默认文件夹、文件名都请勿更改，毕竟都写si了
4. 工具为第3版，依旧很多潜在bug，若出现问题请把报错截图和详细描述以邮件形式发送至我邮箱
5. 邮箱地址：506130869@qq.com