# Submodule of AutoPet Project -- Android


## Description
AutoPet App 是一个简单的远程控制和检测的安卓应用。

## Recommend
安卓 9.0 + 

## Features

- [x] 与服务器建立TCP连接，并监听连接状态
- [x] 点击校正MCU时间，可校正MCU的RTC时间
- [x] 实时检测远端MCU与服务器的连接状态
- [x] 点击“投喂”按键，控制远端设备自动投喂
- [x] 点击“室内环境”按键，实时获取远端温湿度传感器的数据
- [ ] 视频环境传送（未实现，待续）


## Code Structure

![code_structure](https://github.com/Rick0514/AutoPet-Android/blob/main/img/code_structure.jpg)


## Logical process

* 点击app
    * 初始化工具栏菜单
    * 初始化网络环境监听
    * 初始化进度条通知
    * 初始化MCU状态监听器
    * 初始化按钮

* 点击TCP连接
    * 启动心跳线程
    * 启动读数据线程
    * 若TCP断开，重置Socket

* 点击投喂按钮
    * 发送投喂命令
    * 阻塞5s

* 点击室内环境按钮
    * 启动发送获取温湿度命令线程，每5s发送一次
    * 当读到温湿度数据，更新列表


## Screen

<center><img src="https://github.com/Rick0514/AutoPet-Android/blob/main/img/app1.png" alt="app1" width="200" height="400"/></center>


---
* 1 --> TCP连接
* 2 --> MCU状态
* 3 --> 投喂按钮
* 4 --> 室内环境按钮

---

<center><img src="https://github.com/Rick0514/AutoPet-Android/blob/main/img/app2.png" alt="app2" width="200" height="400"/></center>

---

