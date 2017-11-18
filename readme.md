# 项目说明
这是一个精简轻量 android 下载框架。实现多任务多线程断点续传功能。并对下载任务的周期管理。
# 设计目的
1.不依赖其他第三方库
2.精简，轻量，方便阅读修改
3.使用简单零活方便

# 演示截图
![图1](https://github.com/4pp/downloader/blob/master/output/screenshot_1.png?raw=true)
![图2](https://github.com/4pp/downloader/blob/master/output/screenshot_2.png?raw=true)
![图3](https://github.com/4pp/downloader/blob/master/output/screenshot_3.png?raw=true)

# 演示demo
[点这里下载Demo的APK](https://raw.githubusercontent.com/4pp/downloader/master/output/demo-release.apk)
或扫码下载
![扫码](https://github.com/4pp/downloader/blob/master/output/qrcode.png?raw=true)
# 设计思路
对于断点的记录的技术点是数据的持久化，和保存数据格式的设计上。用 sqlite 或用其它的 Android 持久化方式。实现相应的增删改查功能。
HTTP1.1 协议（RFC2616）开始支持获取文件的部分内容，这为并行下载以及断点续传提供了技术支持。它通过在 Header 里两个参数实现的，客户端发请求时对应的是 Range ，服务器端响应时对应的是 Content-Range。
多任务和多线程，用 java 的线程池来做控制。用队列来管理不同状态下的任务。等待队列-停止队列-下载队列-任务完成队列。同时利用 java RandomAccessFile seek方法跳转到指定位置写入数据。

# 下载流程
1.添加任务
2.任务添加到下载队列执行，或进入等待队列等待执行。
3.进入下载队列的任务立即执行，首先进入网络链接中状态，触发 onTaskConnect 。获取下载数据长度。
4.根据下载数据长度，判断是单线程还是多线程执行任务。
5.成功获下载数据长度后，触发监听 onTaskStart。并开始下载。
6.下载过程中 触发监听 onTaskProcess。
7.在下载过程可以 stop 停止任务。进入停止状态。触发监听 onTaskStop。并唤醒其他的一个等待任务执行。
8.停止的任务可以 restart 重新开始，会从上诉2重新开始这个过程。
9.任务下载完成。触发监听 onTaskFinish。
![流程](https://github.com/4pp/downloader/blob/master/output/flow_chart.png?raw=true)

# 使用方式
首先处理好相关的权限配置。如网络，文件读写等必要权限。
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
*** 注：Android 6.0版本(Api 23) 动态权限的处理 ***
## 初始化
在程序启动时，一般是 Application onCreate 方法中 设置一个下载文件的保存目录。 初始化时也会把保存的任务列表异步的加载到内存中。
```java
 //设置文件的下载目录
String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
        File.separator + "mydownload" + File.separator;

Config cfg = new Config.Builder()
        .setSaveDir(path)
        .build();
DownLoader.init(this,cfg);
*** 注:如是一个多进程项目，避免 Application onCreate 中重复执行 init初始化方法。
```
## 注册监听
可以实现 DownLoadListener 接口的几个回调方法,来处理一个任务在不同状态下的处理方式。然后通过 registerListener(this); 注册这个监听。也可以注册多个不同的监听对象。
```java
@Override
public void onAddTask(Task task) {
     Log.d(TAG, "onAddTask: 添加任务 " + task.getFileName());
}

@Override
public void onCancelTask(Task task) {
    Log.d(TAG, type+"onCancelTask: 取消任务 " + task.getFileName());
}

@Override
public void onTaskConnect(Task task) {
   Log.d(TAG, "onTaskConnect: 网络连接中" + task.getFileName());
}

@Override
public void onTaskStart(Task task) {
    Log.d(TAG, "onTaskStart: 启动任务 " + task.getFileName());
}

@Override
public void onTaskStop(Task task) {
    Log.d(TAG,"onTaskStop: 停止任务 " + task.getFileName());
}

@Override
public void onTaskProcess(Task task) {
   Log.d(TAG, "onTaskProcess: 执行任务 " + task.getFileName() + " 进度:" + task.getFinishedLength() + "/" + task.getContentLength());
}

@Override
public void onTaskFinished(Task task) {
     Log.d(TAG, "onTaskFinished: 下载完成:" + task.getFileName());
}

@Override
public void onTaskError(Task task, String msg) {
    Log.d(TAG, "onTaskError: 下载失败:" + msg);
}

DownLoader.getInstance().registerListener(this);

```
## 取消监听
记得在合适的位置（如 onDestory 方法中）调用 unregisterListener() 方法解除这个监听。
```java
DownLoader.getInstance().registerListener(this);
```
所有暴露的方法均在 DownLoader 的单例对象中调用。
## 添加任务
add 方法相当于请求下载，会把请求任务添加到下载队列中，如没超出最大的并发任务数（maxTasks）则直接放入下载队列执行，反之放到等待队列，当有其他任务下载完成后，会自动唤醒等待的任务。按下载数据量的大小（可配置）划分单线程或多个线程执行。 add 方法返回一个 id，用于后边操作这个任务。此方法触发 onAddTask 监听方法。对于重复的下载链接，用源文件名加数字序号的方式重命名下载文件。
```java
long downloadId = DownLoader.getInstance().add("https://raw.githubusercontent.com/4pp/downloader/master/output/demo-release.apk");
```

## 取消/删除任务
cancel 方法相当于删除下载，对于下载中或已完成,会同时删除已下载保存的文件 ，此方法触发 onCancelTask 监听方法。
```
DownLoader.getInstance().cancel(downloadId);
```

## 停止/暂停任务
stop 方法相当于暂停下载，只有下载中的任务才会正确执行停止，当任务停止后，会唤醒后边的等待任务，此方法触发 onStopTask 监听方法。
```
DownLoader.getInstance().stop(downloadId);
```

## 重试/继续任务
restart 方法相当于恢复下载，当任务进入停止状态，如因为异常原因或stop方法后停止的任务，restart 在当前位置重新下载这个任务，回调周期方法如同新任务逻辑。
```
DownLoader.getInstance().restart(downloadId);
```

## 获取任务列表
在程序启动时，已把任务都加载到内存中，在下载任务的列表界面获取显示。分类为已完成和未完成的任务
```java
List<Task> list = DownLoader.getInstance().getRunningTasks();
adapter.setDataSource(list);

List<Task> list = DownLoader.getInstance().getFinishedTasks();
adapter.setDataSource(list);
```