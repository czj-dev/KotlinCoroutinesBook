import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext.Element
import kotlin.coroutines.CoroutineContext.Key

/**
 * 协程作用域
 * 我们之前用到了各式各样的协程构建方式,例如 [GlobalScope.launch] [runBlocking] 以及 [coroutineScope] 等等构建协程的方式
 * 其实它们都是 [CoroutineScope] 之上的扩展,通过 CoroutineScope 来管理协程的生命周期
 *
 * public interface CoroutineScope {
 *
 *  public val coroutineContext: CoroutineContext
 *
 * }
 * CoroutineScope  是一个接口协议,它的实现类需要提供一个 CoroutineContext 的实现,而 CoroutineContext 就是协程的实际管理者,协程运行在
 * CoroutineContext 上下文中.所有的协程构器起都接收一个可选的 CoroutineContext 参数,它提供默认的 EmptyCoroutineContext 实现.
 */


/**
 *  协程上下文包含一个携程调度器,它确定协程在哪个线程或哪些线程执行.调度器本身实现了 CoroutineContext,所以我们可以在协程构建器的 CoroutineContext 参数
 *  中显示的为启动的协程指定一个构建器
 */
fun coroutineDispatcher() = runBlocking {
    launch { // 运行在父协程的上下文中，即 runBlocking 主协程
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    /**
     * 当协程在 GlobalScope 中启动时，使用的是由 Dispatchers.Default 代表的默认调度器。
     * 默认调度器使用共享的后台线程池。 所以 launch(Dispatchers.Default) { …coroutineDispatcher… } 与 GlobalScope.launch { …… } 使用相同的调度器
     */
    launch(Dispatchers.Default) {
        println("Default Dispatcher Thread name :${Thread.currentThread().name}")
    }
    // Main 只能在 Android 设备环境使用
//    launch(Dispatchers.Main) {
//        println("Main Dispatcher Thread name :${Thread.currentThread().name}")
//    }
    launch(Dispatchers.IO) {
        println("IO Dispatcher Thread name :${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) {
        println("Unconfined Dispatcher Thread name :${Thread.currentThread().name}")
    }
    /**
     * newSingleThreadContext 为协程的运行启动了一个线程。 一个专用的线程是一种非常昂贵的资源。
     * 在真实的应用程序中两者都必须被释放，当不再需要的时候，使用 close 函数，或存储在一个顶层变量中使它在整个应用程序中被重用。
     */
    val newSingleThreadContext = newSingleThreadContext("singleThread")
    launch(newSingleThreadContext) {
        println("singleThread Dispatcher Thread name :${Thread.currentThread().name}")
        newSingleThreadContext.close()
    }
}

/**
 * 协程之间的关系
 * 但一个协会曾被其他协程在 CoroutineScope 中启动的时候,它将通过 CoroutineScope.coroutineContext
 * 来承袭上下文,并且这个新协程的 job 将会成为父协程作业的子作业.当一个父协程被取消的时候,所有它的子协程也会被递归的取消
 * 同时,父协程会等待所有的子协程结束,但这一等待过程并非显示的.
 * 下边用两个例子来分别说明
 */

/**
 * job1 通过　GlobalScope 启动了一个协程，所以它并没有父协程
 * job2 在 request 协程中创建启动,它依附于 request 协程,所以当 request 被取消的时候,job2 也被取消
 */
fun cancelParentCoroutine() = runBlocking {
    val request = launch {
        GlobalScope.launch {
            println("job1: I run in GlobalScope and execute independently!")
            delay(1000)
            println("job1:I am not affected by cancellation of the request")
        }
        launch {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancellled")
        }
    }

    delay(500)
    request.cancel()
    delay(1000)
    println("main: Who has survived request cancellation? ")
}

/**
 * 父协程会等待所有的子协程执行完毕,但这一过程并不是显示的,
 * 通过输出可以看到 request 的 println 并没有等待子协程执行完毕,
 * 而方法本身也启动了一个协程,通过 join 方法,等待 request 执行完毕后才输出 println
 */
fun coroutineExecutionSequence()= runBlocking {
    // 启动一个协程来处理某种传入请求（request）
    val request = launch {
        repeat(3) { i -> // 启动少量的子作业
            launch  {
                delay((i + 1) * 200L) // 延迟 200 毫秒、400 毫秒、600 毫秒的时间
                println("Coroutine $i is done")
            }
        }
        println("request: I'm done and I don't explicitly join my children that are still active")
    }
    request.join() // 等待请求的完成，包括其所有子协程
    println("Now processing of the request is complete")
}