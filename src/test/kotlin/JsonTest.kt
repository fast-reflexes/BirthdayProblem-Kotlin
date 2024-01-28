import com.bdayprob.BirthdayProblem
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class JsonTest {

    companion object {

        @JvmStatic
        private fun provideTestData(): Stream<Arguments> =
            Stream.of(
                Arguments.of("1 -p 1.0 -a", true, """{ "d": "1", "p": "100%", "results": { "trivial": { "result": "2" } } }"""),
                Arguments.of("1 -p 0.0 -a", true, """{ "d": "1", "p": "0%", "results": { "trivial": { "result": "1" } } }"""),
                Arguments.of("1 -p 0.5 -a", true, """{ "d": "1", "p": "50%", "results": { "trivial": { "result": "2" } } }"""),
                Arguments.of("1000000000 -p 0.0000001 -t", true, """{ "d": "1000000000 (=10^9)", "p": "0.00001% (=10^-7)", "results": { "taylor": { "result": "15" } } }"""),
                Arguments.of("1 -n 1 -a", true, """{ "d": "1", "n": "1", "results": { "trivial": { "result": "0%" } } }"""),
                Arguments.of("1 -n 0 -a", true, """{ "d": "1", "n": "0", "results": { "trivial": { "result": "0%" } } }"""),
                Arguments.of("1 -n 2 -a", true, """{ "d": "1", "n": "2", "results": { "trivial": { "result": "100%" } } }"""),
                Arguments.of("69 -p 0.5 -a", true, """{ "d": "69", "p": "50%", "results": { "exact": {"result": "11"}, "taylor": {"result": "10"} } }"""),
                Arguments.of("83 -p 0.5 -a", true, """{ "d": "83", "p": "50%", "results": { "exact": {"result": "12"}, "taylor": {"result": "11"} } }"""),
                Arguments.of("1000000000 -p 0.5 -a", true, """{ "d": "1000000000 (=10^9)", "p": "50%", "results": { "exact": {"result": "37234"}, "taylor": {"result": "37233"} } }"""),
                Arguments.of("366 -n 23 -a", true, """{ "d": "366", "n": "23", "results": { "exact": { "result": "≈50.6323011819%" }, "taylor": { "result": "≈51.4549326419%" }, "stirling": { "result": "≈50.6315474495%" } } }"""),
                Arguments.of("366 -p 0.5 -a", true, """{ "d": "366", "p": "50%", "results": { "taylor": { "result": "23" }, "exact": { "result": "23" } } }"""),
                Arguments.of(
                    "6274264876827642864872634872364782634 -n 2376287346287353638 -s -t",
                    true,
                    """{
                        "d": "6274264876827642864872634872364782634 (≈6*10^36)",
                        "n": "2376287346287353638 (≈2*10^18)",
                        "results": {
                            "taylor": { 
                                "result": "≈36.2366927782%" 
                            },
                            "stirling": { 
                                "result": "≈36.2366927782%" 
                            }
                        }
                    }"""
                ),
                Arguments.of("128 -n 0 -b -s -t", true, """{ "d": "2^128", "n": "2^0", "results": { "trivial": { "result": "0%" } } }"""),
                Arguments.of("128 -n 129 -b -s -t", true, """{ "d": "2^128", "n": "2^129", "results": { "trivial": { "result": "100%" } } }"""),
                Arguments.of("128 -n 64 -b -s -t", true, """{ "d": "2^128", "n": "2^64", "results": { "stirling": { "result": "≈39.3469340287%" }, "taylor": { "result": "≈39.3469340287%" } } }"""),
                Arguments.of("128 -p 0.5 -b -t", true, """{ "d": "2^128", "p": "50%", "results": { "taylor": { "result": "≈2^64.2356168135" } } }"""),
                Arguments.of("2000000 -n 1000000 -b -s -t", true, """{ "d": "2^2000000", "n": "2^1000000", "results": { "stirling": { "error": "needed precision for method exceeds maximum precision" }, "taylor": { "result": "≈39.3469340287%" } } }"""),
                Arguments.of("2000000 -p 0.5 -b -t", true, """{ "d": "2^2000000", "p": "50%", "results": { "taylor": { "result": "≈2^1000000.2356168135" } } }"""),
                Arguments.of("8 -n 3 -b -a", true, """{ "d": "2^8", "n": "2^3", "results": { "exact": { "result": "≈10.4576930892%" }, "stirling": { "result": "≈10.4567528314%" }, "taylor": { "result": "≈11.7503097415%" } } }"""),
                Arguments.of("256 -n 8 -a", true, """{ "d": "256", "n": "8", "results": { "exact": { "result": "≈10.4576930892%" }, "stirling": { "result": "≈10.4567528314%" }, "taylor": { "result": "≈11.7503097415%" } } }"""),
                Arguments.of("52 -p 0.1 -c -t", true, """{ "d": "≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67)", "p": "10%", "results": { "taylor": { "result": "4119363813276486714957808853108064 (≈4*10^33)" } } }"""),
                Arguments.of("52 -p 0.5 -c -t", true, """{ "d": "≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67)", "p": "50%", "results": { "taylor": { "result": "10565837726592754214318243269428637 (≈10^34)" } } }"""),
                Arguments.of(
                    "52 -n 10000000000000000000 -c -s -t",
                    true,
                    """{
                        "d": "≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67)",
                        "n": "10000000000000000000 (=10^19)",
                        "results": {
                            "stirling": { "result": "≈0% (≈6*10^-31)" },
                            "taylor": { "result": "≈0% (≈6*10^-31)" }
                        }
                    }"""
                ),
                Arguments.of(
                    "52 -n 10000000000000000000000000000000000 -c -s -t",
                    true,
                    """{
                        "d": "≈80529020383886612857810199580012764961409004334781435987268084328737 (≈8*10^67)",
                        "n": "10000000000000000000000000000000000 (=10^34)",
                        "results": {
                            "stirling": { "result": "≈46.2536366051%" },
                            "taylor": { "result": "≈46.2536366051%" }
                        }
                    }"""
                ),
                Arguments.of(
                    "4 -n 18 -b -c -a",
                    true,
                    """{ 
                        "d": "≈2^44.2426274105", 
                        "n": "2^18", 
                        "results": { 
                            "exact": { "result": "≈0.1649423866% (≈2*10^-3)" }, 
                            "stirling": { "result": "≈0.1649422224% (≈2*10^-3)" }, 
                            "taylor": { "result": "≈0.1649428504% (≈2*10^-3)" }
                        } 
                    }"""
                ),
                Arguments.of(
                    "16 -n 262144 -c -a",
                    true,
                    """{ 
                        "d": "≈20814114415223 (≈2*10^13)", 
                        "n": "262144 (≈3*10^5)", 
                        "results": { 
                            "exact": { "result": "≈0.1649423866% (≈2*10^-3)" }, 
                            "stirling": { "result": "≈0.1649422224% (≈2*10^-3)" }, 
                            "taylor": { "result": "≈0.1649428504% (≈2*10^-3)" } 
                        }
                    }"""
                ),
                Arguments.of(
                    "20922789888000 -n 262144 -a",
                    true,
                    """{
                        "d": "20922789888000 (≈2*10^13)",
                        "n": "262144 (≈3*10^5)",
                        "results": {
                            "exact": { "result": "≈0.1640861961% (≈2*10^-3)" },
                            "stirling": { "result": "≈0.1640861961% (≈2*10^-3)" },
                            "taylor": { "result": "≈0.1640868208% (≈2*10^-3)" }
                        }
                    }"""
                ),
                Arguments.of(
                    "128 -n 64 -b -c -s -t",
                    true,
                    """{
                        "d": "≈2^43065219282621326757565580404980237828911.4871409133",
                        "n": "2^64",
                        "results": {
                            "stirling": { "error": "d exceeds maximum size and is needed for method" },
                            "taylor": { "error": "overflow" }
                        }
                    }"""
                ),
                Arguments.of(
                    "1280 -n 640 -b -c -s -t",
                    true,
                    """{
                        "d": "≈2^26614275474014559821953787196100807012412948367028783328633986189111799719299525295290069853854877867120534538070982737886888824825850066183609939356930416666755910887266773840385877776851876084664629106697034459995685244418266399190317043076208186461319737435225525519543453247219560088300601118286958869004726993677805799134087110255288245085785541666888810491274634074724367056992419344.3330052449",
                        "n": "2^640",
                        "results": {
                            "stirling": { "error": "d exceeds maximum size and is needed for method" },
                            "taylor": { "error": "overflow" }
                        }
                    }"""
                ),
                Arguments.of(
                    "12800 -n 6400 -b -c -s -t",
                    false,
                    "dLog exceeds maximum size and is needed to initialize calculations"
                )
            )
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    fun `calculate the birthday problem and evaluate the Json result`(arg: String, shouldTestSucceed: Boolean, ans: String?) {
        val args = (arg.split(" ") + listOf("-j")).toTypedArray()
        if (shouldTestSucceed) {
            val res = BirthdayProblem.CLISolver.solve(args)
            assertNotNull(ans)
            val ansObj = jacksonObjectMapper().readValue<BirthdayProblem.BirthdayProblemResults>(ans!!)
            val resObj = jacksonObjectMapper().readValue<BirthdayProblem.BirthdayProblemResults>(res)
            assertEquals(ansObj, resObj)
        } else {
            val e = Assertions.assertThrows(BirthdayProblem.SolverException::class.java) {
                BirthdayProblem.CLISolver.solve(args)
            }
            ans?.let { assertEquals(e.message, it) }
        }
    }

}
