import kotlinx.coroutines.*
import java.lang.Exception

// 取消协程的执行
fun cancelCoroutines() = runBlocking {
    val job = launch {
        repeat(5) { i ->
            println("job: I invoke $i count...")
            delay(500L)
        }
    }

    delay(1600L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancel() // 取消该作业
    job.join() // 等待作业执行结束
    println("main: Now I can quit.")
}

// 从这个例子我们可以看出如果协程正在执行计算任务的并且没有在计算过程执行检查取消的话,那么它是不能被取消的
// job 协程主线程在 1300 毫秒后仍然执行了数次直到循环条件结束
fun cancelComputerCoroutines() = runBlocking {
    val startTime = System.currentTimeMillis();
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime;
        var i = 0
        while (i < 5) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I invoke ${i++} count...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    // 取消并且等待执行完毕
    job.cancel()
    println("main: Now I can quit.")
}

//如果我们想在计算过程中取消协程 有两种常用的方法

//第一种我们在结算的时候通过加入 isActive 来判断协程是否被取消,来决定是否继续执行计算过程
fun cancelComputerCoroutines1() = runBlocking {
    val startTime = System.currentTimeMillis();
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5 && isActive) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I invoke ${i++} count...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    // 取消并且等待执行完毕
    job.cancel()
    println("main: Now I can quit.")
}

// 我们还可以通过 yield 在每次循环执行之后将协程挂起判断当前任务是否被取消
fun cancelComputerCoroutines2() = runBlocking {
    val startTime = System.currentTimeMillis();
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I invoke ${i++} count...")
                nextPrintTime += 500L
            }
            yield()
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    // 取消并且等待执行完毕
    job.cancel()
    println("main: Now I can quit.")
}

// 当然 yield 的用法远不于此,它有以下特性
// 暂时打断一件长耗时的任务，并且让其他任务一个公平的机会去执行
// 检查当前任务是否是被取消，这个任务可能并没有在最后检查自己是否被取消
// 当你的入栈的任务数大于当前允许并行的数目时，允许子任务执行。这个对与任务依赖很重要
// 最后一个可能不是很好理解,我们可以用下边一个例子来说明,我们在一个上下文中执行两个协程,它们分别为 task1 和 task2 ,每个任务打印自己三次
// 如果执行执行的话,他们很明显会相继输出,如果我们在每次执行完毕后通过 yield 挂起,就可以达成 task1 和 task2 交互打印的协同效果
val singleThreadContext = newSingleThreadContext("task")

@ObsoleteCoroutinesApi
fun doubleJobRunning() = runBlocking {
    launch(singleThreadContext) {
        repeat(3) {
            println("job:task1 invoke")
            yield()
            delay(1000)
        }
    }
    launch(singleThreadContext) {
        repeat(3) {
            println("job:task2 invoke")
            yield()
            delay(1000)
        }
    }
}

// 当协程被取消的时候会抛出 CancellationException 我们可以在需要时候 try finally 然后执行它们的终结动作,例如回收保存之类的操作
fun listenerCancel() = runBlocking {
    val job = launch {
        try {
            repeat(100) { i ->
                println("job: task invoke $i count")
                delay(500)
            }

        } catch (e: Exception) {
            println(e)
        } finally {
            println("task invoke finally")
        }
    }
    delay(1300L)
    println("main:I`m tired of waiting")
    job.cancelAndJoin()
    println("main:Now I can quit")
}
