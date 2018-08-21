# JavaSvr--Dolphin
* java编写的后端服务器.


# 需要做以下抽象
* 泛化网络协议包（IoPacket）。请求-响应模式是我们最普遍的业务场景。
* 服务端I/O服务（ServerIoService）。负责网络收发工作，是对普通tcp/udp网络I/O的抽象。
* 业务层编解码服务（CodecService）。抽象了一致的流协议编解码服务。
* 任务管理与执行服务（WorkerService）。主要对任务的线程的模型进行抽象，提供基于线程池、协程池的实现，并尽量屏蔽两者间差异；负责简单的任务管理与调度。
* ServerIoService每接收到一个协议包需要调用业务代码处理逻辑，在此抽象一层子命令映射服务（ProcessorService），负责判断一个特定的网络协议包交由哪一个类进行具体业务处理；ProcessorService提供基于*.properties文件、jungle-cgi-spec.xml等实现

 
## 对于server间互调的场景，也进行了简单的抽象：
* 回调接口改造（IoCallBack）。历史版本的回调接口需要在同一个回调方法中处理正常业务逻辑与异常处理，在这里对这两者进行了分离
* 路由服务（RouterService）。
* 超时策略服务（TimeoutManager）。提供超时检测服务
* 回调执行。负责网络回包的回调执行；
* 客户端I/O服务（ClientIoService）。复用CodecService，整合以上服务，封装tcp/udp等传输层细节，提供异步（async）、同步（sync）方式的网络API。
