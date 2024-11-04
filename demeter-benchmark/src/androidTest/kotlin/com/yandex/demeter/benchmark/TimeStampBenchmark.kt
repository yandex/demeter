import android.os.SystemClock
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.OrderWith
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Alphanumeric

@RunWith(AndroidJUnit4::class)
@OrderWith(Alphanumeric::class)
class TimeStampBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun currentTimeMillis() {
        var timeStamp = 0L
        benchmarkRule.measureRepeated {
            timeStamp = System.currentTimeMillis()
        }
        Assert.assertTrue(timeStamp != 0L)
    }

    @Test
    fun nanoTime() {
        var timeStamp = 0L
        benchmarkRule.measureRepeated {
            timeStamp = System.nanoTime()
        }
        Assert.assertTrue(timeStamp != 0L)
    }

    @Test
    fun uptimeMillis() {
        var timeStamp = 0L
        benchmarkRule.measureRepeated {
            timeStamp = SystemClock.uptimeMillis()
        }
        Assert.assertTrue(timeStamp != 0L)
    }

    @Test
    fun elapsedRealtime() {
        var timeStamp = 0L
        benchmarkRule.measureRepeated {
            timeStamp = SystemClock.elapsedRealtime()
        }
        Assert.assertTrue(timeStamp != 0L)
    }

    @Test
    fun elapsedRealtimeNanos() {
        var timeStamp = 0L
        benchmarkRule.measureRepeated {
            timeStamp = SystemClock.elapsedRealtimeNanos()
        }
        Assert.assertTrue(timeStamp != 0L)
    }
}