import com.bdayprob.BirthdayProblem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class TextTest {

    companion object {

        @JvmStatic
        private fun provideTestData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "1 -p 1.0 -a",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of 1 items, needed to have at least a 100% chance of a non-unique sample is:",
                        "          2 (Trivial solution)"
                    )
                ),
                Arguments.of(
                    "1 -p 0.0 -a",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of 1 items, needed to have at least a 0% chance of a non-unique sample is:",
                        "          1 (Trivial solution)"
                    )
                ),
                Arguments.of(
                    "1 -p 0.5 -a",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of 1 items, needed to have at least a 50% chance of a non-unique sample is:",
                        "          2 (Taylor series approximation used in main calculation)"
                    )
                ),
                Arguments.of(
                    "1000000000 -p 0.0000001",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of 1000000000 (=10^9) items, needed to have at least a 0.00001% (=10^-7) chance of a non-unique sample is:",
                        "          15 (Taylor series approximation used in main calculation)"
                    )
                ),
                Arguments.of(
                    "1 -n 1 -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 1 samples, sampled uniformly at random from a set of 1 items, is:",
                        "          0% (Trivial solution)"
                    )
                ),
                Arguments.of(
                    "1 -n 0 -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 0 samples, sampled uniformly at random from a set of 1 items, is:",
                        "          0% (Trivial solution)"
                    )
                ),
                Arguments.of(
                    "1 -n 2 -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2 samples, sampled uniformly at random from a set of 1 items, is:",
                        "          100% (Trivial solution)"
                    )
                ),
                Arguments.of(
                    "366 -n 23 -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 23 samples, sampled uniformly at random from a set of 366 items, is:",
                        "          ≈50.6323011819% (Exact method)",
                        "          ≈50.6315474495% (Stirling's approximation used in factorial calculation)",
                        "          ≈51.4549326419% (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "366 -p 0.5",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of 366 items, needed to have at least a 50% chance of a non-unique sample is:",
                        "          23 (Taylor series approximation used in main calculation)"
                    )
                ),
                Arguments.of(
                    "6274264876827642864872634872364782634 -n 2376287346287353638 -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2376287346287353638 (≈2*10^18) samples, sampled uniformly at random from a set of 6274264876827642864872634872364782634 (≈6*10^36) items, is:",
                        "          ≈36.2366927782% (Stirling's approximation used in factorial calculation)",
                        "          ≈36.2366927782% (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "128 -n 0 -b -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^0 samples, sampled uniformly at random from a set of 2^128 items, is:",
                        "          0% (Trivial solution)"
                    )
                ),
                Arguments.of(
                    "128 -n 129 -b -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^129 samples, sampled uniformly at random from a set of 2^128 items, is:",
                        "          100% (Trivial solution)"
                    )
                ),
                Arguments.of(
                    "128 -n 64 -b -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^64 samples, sampled uniformly at random from a set of 2^128 items, is:",
                        "          ≈39.3469340287% (Stirling's approximation used in factorial calculation)",
                        "          ≈39.3469340287% (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "128 -p 0.5 -b",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of 2^128 items, needed to have at least a 50% chance of a non-unique sample is:",
                        "          ≈2^64.2356168135 (Taylor series approximation used in main calculation)"
                    )
                ),
                Arguments.of(
                    "2000000 -n 1000000 -b -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^1000000 samples, sampled uniformly at random from a set of 2^2000000 items, is:",
                        "          N/A             (Calculation failed: needed precision for method exceeds maximum precision (Exact method with Stirling's approximation))",
                        "          ≈39.3469340287% (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "2000000 -p 0.5 -b",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of 2^2000000 items, needed to have at least a 50% chance of a non-unique sample is:",
                        "          ≈2^1000000.2356168135 (Taylor series approximation used in main calculation)"
                    )
                ),
                Arguments.of(
                    "8 -n 3 -b -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^3 samples, sampled uniformly at random from a set of 2^8 items, is:",
                        "          ≈10.4576930892% (Exact method)",
                        "          ≈10.4567528314% (Stirling's approximation used in factorial calculation)",
                        "          ≈11.7503097415% (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "256 -n 8 -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 8 samples, sampled uniformly at random from a set of 256 items, is:",
                        "          ≈10.4576930892% (Exact method)",
                        "          ≈10.4567528314% (Stirling's approximation used in factorial calculation)",
                        "          ≈11.7503097415% (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "52 -p 0.1 -c",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of ≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67) items, needed to have at least a 10% chance of a non-unique sample is:",
                        "          4119363813276486714957808853108064 (≈4*10^33) (Taylor series approximation used in main calculation)"
                    )
                ),
                Arguments.of(
                    "52 -p 0.5 -c",
                    true,
                    listOf(
                        "The number of samples, sampled uniformly at random from a set of ≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67) items, needed to have at least a 50% chance of a non-unique sample is:",
                        "          10565837726592754214318243269428637 (≈10^34) (Taylor series approximation used in main calculation)"
                    )
                ),
                Arguments.of(
                    "52 -n 10000000000000000000 -c -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 10000000000000000000 (=10^19) samples, sampled uniformly at random from a set of ≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67) items, is:",
                        "          ≈0% (≈6*10^-31) (Stirling's approximation used in factorial calculation)",
                        "          ≈0% (≈6*10^-31) (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "52 -n 10000000000000000000000000000000000 -c -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 10000000000000000000000000000000000 (=10^34) samples, sampled uniformly at random from a set of ≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67) items, is:",
                        "          ≈46.2536366051% (Stirling's approximation used in factorial calculation)",
                        "          ≈46.2536366051% (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "4 -n 18 -b -c -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^18 samples, sampled uniformly at random from a set of ≈2^44.2426274105 items, is:",
                        "          ≈0.1649423866% (≈2*10^-3) (Exact method)",
                        "          ≈0.1649422224% (≈2*10^-3) (Stirling's approximation used in factorial calculation)",
                        "          ≈0.1649428504% (≈2*10^-3) (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "16 -n 262144 -c -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 262144 (≈3*10^5) samples, sampled uniformly at random from a set of ≈20814114415223 (≈2*10^13) items, is:",
                        "          ≈0.1649423866% (≈2*10^-3) (Exact method)",
                        "          ≈0.1649422224% (≈2*10^-3) (Stirling's approximation used in factorial calculation)",
                        "          ≈0.1649428504% (≈2*10^-3) (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "20922789888000 -n 262144 -a",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 262144 (≈3*10^5) samples, sampled uniformly at random from a set of 20922789888000 (≈2*10^13) items, is:",
                        "          ≈0.1640861961% (≈2*10^-3) (Exact method)",
                        "          ≈0.1640861961% (≈2*10^-3) (Stirling's approximation used in factorial calculation)",
                        "          ≈0.1640868208% (≈2*10^-3) (Taylor series approximation used in main calculation (removes need for factorial calculation))"
                    )
                ),
                Arguments.of(
                    "128 -n 64 -b -c -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^64 samples, sampled uniformly at random from a set of ≈2^43065219282621326757565580404980237828911.4871409133 items, is:",
                        "          N/A (Calculation failed: d exceeds maximum size and is needed for method (Exact method with Stirling's approximation))",
                        "          N/A (Calculation failed: overflow (Taylor approximation))"
                    )
                ),
                Arguments.of(
                    "1280 -n 640 -b -c -s -t",
                    true,
                    listOf(
                        "The probability of finding at least one non-unique sample among 2^640 samples, sampled uniformly at random from a set of ≈2^26614275474014559821953787196100807012412948367028783328633986189111799719299525295290069853854877867120534538070982737886888824825850066183609939356930416666755910887266773840385877776851876084664629106697034459995685244418266399190317043076208186461319737435225525519543453247219560088300601118286958869004726993677805799134087110255288245085785541666888810491274634074724367056992419344.3330052449 items, is:",
                        "          N/A (Calculation failed: d exceeds maximum size and is needed for method (Exact method with Stirling's approximation))",
                        "          N/A (Calculation failed: overflow (Taylor approximation))"
                    )
                ),
                Arguments.of(
                    "12800 -n 6400 -b -c -s -t",
                    false,
                    listOf("dLog exceeds maximum size and is needed to initialize calculations")
                )
            )
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    fun `calculate the birthday problem and evaluate the text result`(arg: String, shouldTestSucceed: Boolean, ans: List<String>?) {
        val args = arg.split(" ").toTypedArray()
        if(shouldTestSucceed) {
            val res = BirthdayProblem.CLISolver.solve(args)
            assertNotNull(ans)
            assertEquals(res, ans!!.joinToString("\n"))
        }
        else {
            val e = assertThrows(BirthdayProblem.SolverException::class.java) {
                BirthdayProblem.CLISolver.solve(args)
            }
            ans?.let { assertEquals(e.message, it.first()) }
        }
    }

}