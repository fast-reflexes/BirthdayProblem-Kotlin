import com.bdayprob.BirthdayProblem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.stream.Stream

class LibraryTest {

    companion object {

        @JvmStatic
        fun provideTestData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    BirthdayProblemInputSpec("1", "1", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "1", false, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "1", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "0", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "0", false, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "0", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "2", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "1" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "2", false, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "1" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1", "2", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "1" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("366", "23", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0.506323011819" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("366", "23", false, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.506315474495" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("366", "23", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.514549326419" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("6274264876827642864872634872364782634", "2376287346287353638", false, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.362366927782" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("6274264876827642864872634872364782634", "2376287346287353638", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.362366927782" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "0", true, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "0", true, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "129", true, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "1" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "129", true, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "1" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "64", true, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.393469340287" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "64", true, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.393469340287" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("2000000", "1000000", true, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    false,
                    null,
                    "needed precision for method exceeds maximum precision"
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("2000000", "1000000", true, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.393469340287" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("8", "3", true, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0.104576930892" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("8", "3", true, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.104567528314" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("8", "3", true, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.117503097415" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("256", "8", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0.104576930892" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("256", "8", false, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.104567528314" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("256", "8", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.117503097415" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("52", "10000000000000000000", false, true, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("52", "10000000000000000000", false, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("52", "10000000000000000000000000000000000", false, true, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.462001746672" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("52", "10000000000000000000000000000000000", false, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.462001746672" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("4", "18", true, true, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0.001640861961" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("4", "18", true, true, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.001640861961" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("4", "18", true, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.001640868208" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("16", "262144", false, true, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0.001640861961" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("16", "262144", false, true, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.001640861961" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("16", "262144", false, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.001640868208" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("20922789888000", "262144", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "0.001640861961" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("20922789888000", "262144", false, false, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    true,
                    "0.001640861961" to BirthdayProblem.CalcPrecision.STIRLING_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("20922789888000", "262144", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "0.001640868208" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "64", true, true, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    false,
                    null,
                    "d exceeds maximum size and is needed for method"
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("128", "64", true, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    false,
                    null,
                    "Overflow"
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1280", "640", true, true, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    false,
                    null,
                    "d exceeds maximum size and is needed for method"
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("1280", "640", true, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    false,
                    null,
                    "Overflow"
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("12800", "6400", true, true, BirthdayProblem.CalcPrecision.STIRLING_APPROX),
                    false,
                    null,
                    "d exceeds maximum size and is needed for method"
                ),
                Arguments.of(
                    BirthdayProblemInputSpec("12800", "6400", true, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    false,
                    null,
                    "needed precision for method exceeds maximum precision"
                )
            )

        @JvmStatic
        fun provideInvertedTestData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1", "1", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "2" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1", "1", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "2" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1", "0", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "1" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1", "0", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "1" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1", "0.5", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "2" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1", "0.5", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "2" to BirthdayProblem.CalcPrecision.TRIVIAL,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1000000000", "0.0000001", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "15" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("69", "0.5", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "11" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("69", "0.5", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "10" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("83", "0.5", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "12" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("83", "0.5", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "11" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1000000000", "0.5", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "37234" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1000000000", "0.5", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "37233" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("366", "0.5", false, false, BirthdayProblem.CalcPrecision.EXACT),
                    true,
                    "23" to BirthdayProblem.CalcPrecision.EXACT,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("366", "0.5", false, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "23" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("128", "0.5", true, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "64.2356168135" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("2000000", "0.5", true, false, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "1000000.2356168135" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("52", "0.1", false, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "4122665867622533660736208120290868" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("52", "0.5", false, true, BirthdayProblem.CalcPrecision.TAYLOR_APPROX),
                    true,
                    "10574307231100289363611308602026252" to BirthdayProblem.CalcPrecision.TAYLOR_APPROX,
                    null
                ),
                Arguments.of(
                    InvertedBirthdayProblemInputSpec("1280", "0.5", true, true, BirthdayProblem.CalcPrecision.EXACT),
                    false,
                    null,
                    "d exceeds maximum size and is needed for method"
                )
            )
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    fun `calculate the birthday problem using the library methods and evaluate the result`(
        args: BirthdayProblemInputSpec,
        shouldTestSucceed: Boolean,
        ans: Pair<String, BirthdayProblem.CalcPrecision>?,
        exceptionMessage: String?
    ) {
        if (shouldTestSucceed) {
            val (res, method) = BirthdayProblem.Solver.solveForP(BigDecimal(args.dLogOrD), BigDecimal(args.nLogOrN), args.isBinary, args.isCombinations, args.method)
            assertNotNull(res)
            assertNotNull(method)
            assertEquals(res.setScale(12, RoundingMode.HALF_UP), ans?.first?.let { BigDecimal(it).setScale(12, RoundingMode.HALF_UP) })
            assertEquals(method, ans?.second)
        } else {
            try {
                BirthdayProblem.Solver.solveForP(BigDecimal(args.dLogOrD), BigDecimal(args.nLogOrN), args.isBinary, args.isCombinations, args.method)
            } catch(exception: Exception) {
                assertTrue(exception is BirthdayProblem.SolverException || exception is ArithmeticException)
                assertEquals(exception.message, exceptionMessage)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideInvertedTestData")
    fun `calculate the inverted birthday problem using the library methods and evaluate the result`(
        args: InvertedBirthdayProblemInputSpec,
        shouldTestSucceed: Boolean,
        ans: Pair<String, BirthdayProblem.CalcPrecision>?,
        exceptionMessage: String?
    ) {
        if (shouldTestSucceed) {
            val (res, method) = BirthdayProblem.Solver.solveForN(BigDecimal(args.dLogOrD), BigDecimal(args.p), args.isBinary, args.isCombinations, args.method)
            assertNotNull(res)
            assertNotNull(method)
            if(args.isBinary) {
                assertEquals(res.setScale(10, RoundingMode.HALF_UP), ans?.first?.let { BigDecimal(it) })
            } else {
                assertEquals(res.setScale(0, RoundingMode.CEILING), ans?.first?.let { BigDecimal(it) })
            }
            assertEquals(method, ans?.second)
        } else {
            try {
                BirthdayProblem.Solver.solveForN(BigDecimal(args.dLogOrD), BigDecimal(args.p), args.isBinary, args.isCombinations, args.method)
            } catch(exception: Exception) {
                assertTrue(exception is BirthdayProblem.SolverException || exception is ArithmeticException)
                assertEquals(exception.message, exceptionMessage)
            }

        }
    }

}

data class BirthdayProblemInputSpec(
    val dLogOrD: String,
    val nLogOrN: String,
    val isBinary: Boolean,
    val isCombinations: Boolean,
    val method: BirthdayProblem.CalcPrecision
)

data class InvertedBirthdayProblemInputSpec(
    val dLogOrD: String,
    val p: String,
    val isBinary: Boolean,
    val isCombinations: Boolean,
    val method: BirthdayProblem.CalcPrecision
)
