import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FunctionTest {

    @Test
    fun testMySuspendingFunction() = runBlocking<Unit> {
        GlobalScope.launch { // 在后台启动一个新的协程并继续
            delay(1000L)
            println("World!")
        }
        println("Hello,") // 主协程在这里会立即执行
        delay(2000L)
    }

    suspend fun doWorld() {
        delay(1000L)
        println("World!")
    }

    @Test
    fun testSuspendingFunction() {

    }

}