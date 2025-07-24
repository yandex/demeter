package com.yandex.demeter.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.OrderWith
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Alphanumeric

@RunWith(AndroidJUnit4::class)
@OrderWith(Alphanumeric::class)
class StringConcatenationBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var firstString: String
    private lateinit var secondString: String
    private lateinit var expectedResult: String

    private lateinit var threadLocalStringBuilders: ThreadLocal<StringBuilder>
    private lateinit var threadLocalStringBuffers: ThreadLocal<StringBuffer>

    @Before
    fun setup() {
        firstString = StringConcatenationBenchmark::class.qualifiedName.orEmpty()
        secondString = Thread.currentThread().name
        expectedResult = "$firstString$MIDDLE_CHAR_STRING$secondString"
        threadLocalStringBuilders = ThreadLocal.withInitial { StringBuilder() }
        threadLocalStringBuffers = ThreadLocal.withInitial { StringBuffer() }
    }

    @Test
    fun stringSetToLocalVariable() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = firstString
        }
        assertEquals(firstString, actualResult)
    }

    @Test
    fun stringBuilderInit() {
        var stringBuilder = StringBuilder()
        benchmarkRule.measureRepeated {
            stringBuilder = StringBuilder()
        }
        assertNotNull(stringBuilder)
    }

    @Test
    fun stringBufferInit() {
        var stringBuffer = StringBuffer()
        benchmarkRule.measureRepeated {
            stringBuffer = StringBuffer()
        }
        assertNotNull(stringBuffer)
    }

    @Test
    fun stringBuilderSimpleManualConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = StringBuilder()
                .append(firstString)
                .append(MIDDLE_CHAR_STRING)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferSimpleManualConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = StringBuffer()
                .append(firstString)
                .append(MIDDLE_CHAR_STRING)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBuilderAdvancedManualConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult =
                StringBuilder(firstString.length + MIDDLE_CHAR_STRING.length + secondString.length)
                    .append(firstString)
                    .append(MIDDLE_CHAR_STRING)
                    .append(secondString)
                    .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferAdvancedManualConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult =
                StringBuffer(firstString.length + MIDDLE_CHAR_STRING.length + secondString.length)
                    .append(firstString)
                    .append(MIDDLE_CHAR_STRING)
                    .append(secondString)
                    .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBuilderReusedManualConcatenation() {
        val stringBuilder = StringBuilder()
        var actualResult = ""
        benchmarkRule.measureRepeated {
            stringBuilder.setLength(0) // clear
            actualResult = stringBuilder
                .append(firstString)
                .append(MIDDLE_CHAR_STRING)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferReusedManualConcatenation() {
        val stringBuffer = StringBuffer()
        var actualResult = ""
        benchmarkRule.measureRepeated {
            stringBuffer.setLength(0) // clear
            actualResult = stringBuffer
                .append(firstString)
                .append(MIDDLE_CHAR_STRING)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBuilderReusedThreadLocalManualConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            val stringBuilder =
                threadLocalStringBuilders.get()!! // using withInitial method for create ThreadLocal object
            stringBuilder.setLength(0) // clear
            actualResult = stringBuilder
                .append(firstString)
                .append(MIDDLE_CHAR_STRING)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferReusedThreadLocalManualConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            val stringBuffer =
                threadLocalStringBuffers.get()!! // using withInitial method for create ThreadLocal object
            stringBuffer.setLength(0) // clear
            actualResult = stringBuffer
                .append(firstString)
                .append(MIDDLE_CHAR_STRING)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBuilderSimpleManualWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = StringBuilder()
                .append(firstString)
                .append(MIDDLE_CHAR)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferSimpleManualWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = StringBuffer()
                .append(firstString)
                .append(MIDDLE_CHAR)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBuilderAdvancedManualWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult =
                StringBuilder(firstString.length + secondString.length + 1 /* char */)
                    .append(firstString)
                    .append(MIDDLE_CHAR)
                    .append(secondString)
                    .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferAdvancedManualWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult =
                StringBuffer(firstString.length + secondString.length + 1 /* char */)
                    .append(firstString)
                    .append(MIDDLE_CHAR)
                    .append(secondString)
                    .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBuilderReusedManualWithCharConcatenation() {
        val stringBuilder = StringBuilder()
        var actualResult = ""
        benchmarkRule.measureRepeated {
            stringBuilder.setLength(0) // clear
            actualResult = stringBuilder
                .append(firstString)
                .append(MIDDLE_CHAR)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferReusedManualWithCharConcatenation() {
        val stringBuffer = StringBuffer()
        var actualResult = ""
        benchmarkRule.measureRepeated {
            stringBuffer.setLength(0) // clear
            actualResult = stringBuffer
                .append(firstString)
                .append(MIDDLE_CHAR)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBuilderReusedThreadLocalManualWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            val stringBuilder =
                threadLocalStringBuilders.get()!! // using withInitial method for create ThreadLocal object
            stringBuilder.setLength(0) // clear
            actualResult = stringBuilder
                .append(firstString)
                .append(MIDDLE_CHAR)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringBufferReusedThreadLocalManualWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            val stringBuffer =
                threadLocalStringBuffers.get()!! // using withInitial method for create ThreadLocal object
            stringBuffer.setLength(0) // clear
            actualResult = stringBuffer
                .append(firstString)
                .append(MIDDLE_CHAR)
                .append(secondString)
                .toString()
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringTemplateDollarConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = "$firstString#$secondString" // #: MIDDLE_CHAR for compiled string
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringPlusConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = firstString + MIDDLE_CHAR_STRING + secondString
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringPlusWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = firstString + MIDDLE_CHAR + secondString
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringJoinArrayConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = arrayOf(firstString, secondString).joinToString(MIDDLE_CHAR_STRING)
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringJoinListConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = listOf(firstString, secondString).joinToString(MIDDLE_CHAR_STRING)
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringFormatConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            actualResult = String.format(
                "%s#%s",
                firstString,
                secondString
            ) // #: MIDDLE_CHAR for compiled string
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringCharArrayConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            val resultCharArray =
                CharArray(firstString.length + MIDDLE_CHAR_STRING.length + secondString.length)
            System.arraycopy(
                firstString.toCharArray(),
                0,
                resultCharArray,
                0,
                firstString.length,
            )
            System.arraycopy(
                MIDDLE_CHAR_STRING.toCharArray(),
                0,
                resultCharArray,
                firstString.length,
                MIDDLE_CHAR_STRING.length,
            )
            System.arraycopy(
                secondString.toCharArray(),
                0,
                resultCharArray,
                firstString.length + MIDDLE_CHAR_STRING.length,
                secondString.length,
            )
            actualResult = String(resultCharArray)
        }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun stringCharArrayWithCharConcatenation() {
        var actualResult = ""
        benchmarkRule.measureRepeated {
            val resultCharArray =
                CharArray(firstString.length + secondString.length + 1 /* char */)
            System.arraycopy(
                firstString.toCharArray(),
                0,
                resultCharArray,
                0,
                firstString.length,
            )
            resultCharArray[firstString.length] = MIDDLE_CHAR
            System.arraycopy(
                secondString.toCharArray(),
                0,
                resultCharArray,
                firstString.length + 1, /* char */
                secondString.length,
            )
            actualResult = String(resultCharArray)
        }
        assertEquals(expectedResult, actualResult)
    }

    companion object {
        private const val MIDDLE_CHAR = '#'
        private const val MIDDLE_CHAR_STRING = MIDDLE_CHAR.toString()
    }
}
