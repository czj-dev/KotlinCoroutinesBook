import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun testGlobalCoroutines() {
    GlobalScope.launch { // 在后台启动一个新的协程并继续
        delay(1000L)
        println("World!")
    }
    println("Hello,") // 主线程中的代码会立即执行
    runBlocking {     // 但是这个表达式阻塞了主线程
        delay(2000L)  // ……我们延迟 2 秒来保证 JVM 的存活
    }
}


suspend fun testJobJoin(){
    val job =GlobalScope.launch {
        delay(1000)
        println("World!")
    }
    println("Hello,")
    job.join()
}

fun testSuspendingFunction() = runBlocking {
    launch { doWorld() }
    println("Hello,")
}

// 挂起函数 使用 Suspend 修饰
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}