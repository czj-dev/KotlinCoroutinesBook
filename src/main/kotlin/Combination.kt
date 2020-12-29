import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // 假设我们在这里做了一些有用的事
    println("start invoke doSomethingUsefulOne")
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // 假设我们在这里也做了一些有用的事
    println("start invoke doSomethingUsefulTwo")
    return 29
}

suspend fun doSomethingUsefulThree(): Int {
    delay(1000L) // 假设我们在这里也做了一些有用的事
    println("start invoke doSomethingUsefulThree")
    return 2
}

// 在协程中执行不同的任务,他们的执行顺序和常规代码一样都是顺序执行的
fun combinationInvoke() = runBlocking {
    val time = measureTimeMillis {
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        println("the answer is ${one + two}")
    }
    println("Completed in $time ms")
}

// 如果两个任务之间没有关联,并不要求执行的顺序,我们可以使用 async 让它们并发执行
// async 类型 launch .它启动了一个轻量级的协程,可以去其他所有的协程一起执行并发的工作
// 不同之处在于, launch 返回一个 job 并不带任何结果值,而 async 返回一个轻量级的非阻塞的 future --- Deferred.
// 你可以通过执行它的 await 方法延期得到它的执行结果,而且 deferred 也是一个 job ,我们同样可以取消它.
fun asyncCombinationInvoke() = runBlocking {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        println("the answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

fun lazyAsyncCombinationInvoke() = runBlocking {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        val three = async { doSomethingUsefulThree() }
        one.start()
        two.start()
        println("the answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

// 在并发中,同一个作用域的任意函数内部发生了错误,并且它抛出了一个异常,
// 那么所在作用域的中启动的所有协程都会被取消

suspend fun asyncExceptionCancel() = coroutineScope {
    //模拟一个需要长时间执行的任务
    val one = async<Int> {
        try {
            delay(Long.MAX_VALUE)
            42
        } catch (e: Exception) {
            println("task execute failed ")
            0
        }
    }
    val two = async<Int> {
        throw  ArithmeticException()
    }
    one.await() + two.await()
}

fun invokeAsyncExceptionMethod() = runBlocking {
    try {
        asyncExceptionCancel()
    } catch (e: Exception) {
        println("method execute failed")
    }
}