package com.bdayprob

import ch.obermuhlner.math.big.BigDecimalMath
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.xenomachina.argparser.*
import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.system.exitProcess

class BirthdayProblem {

    class SolverException(
        val code: SolverErrorCode,
        val method: CalcPrecision? = null,
        message: String = "(no message available)"): RuntimeException(message) {

        override fun toString(): String {
            val methodText = if(method !== null) BirthdayProblemTextFormatter.methodToText(method) else "(method not available)"
            val messages = mapOf(
                SolverErrorCode.D_NOT_CALCULATED to "d exceeds maximum size and is needed to initialize calculations",
                SolverErrorCode.DLOG_NOT_CALCULATED to "dLog exceeds maximum size and is needed to initialize calculations",
                SolverErrorCode.D_NEEDED_FOR_METHOD to "d exceeds maximum size and is needed for method",
                SolverErrorCode.DLOG_NEEDED_FOR_METHOD to "dLog exceeds maximum size and is needed for method",
                SolverErrorCode.TOO_HIGH_PRECISION to "needed precision for method exceeds maximum precision",
                SolverErrorCode.BAD_INPUT to "bad input: "+ message
            )
            return messages.getOrDefault(code, "an unknown error was encountered for the " + methodText + " method with code " + code)
        }

    }

    enum class SolverErrorCode {
        UNKNOWN_ERROR,
        BAD_INPUT,
        DLOG_NOT_CALCULATED,
        D_NOT_CALCULATED,
        D_NEEDED_FOR_METHOD,
        DLOG_NEEDED_FOR_METHOD,
        TOO_HIGH_PRECISION
    }

    private class DecimalContext {

        companion object {

            const val MAX_PRECISION = 1000
            const val DECIMAL_PRECISION = 100
            var ctx = MathContext(MAX_PRECISION)

            fun reset() {
                ctx = MathContext(MAX_PRECISION)
            }

            fun isTooPrecise() =
                ctx.precision > MAX_PRECISION

            fun adjustPrecision(integerPartSz: Int) {
                ctx = MathContext((if(integerPartSz > 0) integerPartSz else 1) + DECIMAL_PRECISION)
            }
        }

    }

    private class DecimalFns {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	General calculation methods                 																										         									   #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        companion object {

            // basic constants
            val ZERO: BigDecimal = BigDecimal("0")
            val HALF: BigDecimal = BigDecimal("0.5")
            val ONE: BigDecimal = BigDecimal("1")
            val TWO: BigDecimal = BigDecimal("2")
            val TEN: BigDecimal = BigDecimal("10")
            val HUNDRED: BigDecimal = BigDecimal("100")

            // hard-coded constants with 100 / 1000 decimal precision
            val PI_100 = BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679",
                DecimalContext.ctx
            )
            val PI_1000 = BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679821480865132823066470938446095505822317253594081284811174502841027019385211055596446229489549303819644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412737245870066063155881748815209209628292540917153643678925903600113305305488204665213841469519415116094330572703657595919530921861173819326117931051185480744623799627495673518857527248912279381830119491298336733624406566430860213949463952247371907021798609437027705392171762931767523846748184676694051320005681271452635608277857713427577896091736371787214684409012249534301465495853710507922796892589235420199561121290219608640344181598136297747713099605187072113499999983729780499510597317328160963185950244594553469083026425223082533446850352619311881710100031378387528865875332083814206171776691473035982534904287554687311595628638823537875937519577818577805321712268066130019278766111959092164201989",
                DecimalContext.ctx
            )
            val E_100 =  BigDecimal("2.7182818284590452353602874713526624977572470936999595749669676277240766303535475945713821785251664274",
                DecimalContext.ctx
            )
            val E_1000 =  BigDecimal("2.7182818284590452353602874713526624977572470936999595749669676277240766303535475945713821785251664274274663919320030599218174135966290435729003342952605956307381323286279434907632338298807531952510190115738341879307021540891499348841675092447614606680822648001684774118537423454424371075390777449920695517027618386062613313845830007520449338265602976067371132007093287091274437470472306969772093101416928368190255151086574637721112523897844250569536967707854499699679468644549059879316368892300987931277361782154249992295763514822082698951936680331825288693984964651058209392398294887933203625094431173012381970684161403970198376793206832823764648042953118023287825098194558153017567173613320698112509961818815930416903515988885193458072738667385894228792284998920868058257492796104841984443634632449684875602336248270419786232090021609902353043699418491463140934317381436405462531520961836908887070167683964243781405927145635490613031072085103837505101157477041718986106873969655212671546889570350354",
                DecimalContext.ctx
            )
            val PI = PI_1000
            val E = E_1000

            // e base and 2 base logarithms of 2 and PI for repeatedly used values and logarithm base conversions
            //val LOG_E_2 = BigDecimal("0.6931471805599453094172321214581765680755001343602552541206800094933936219696947156058633269964186875420014810205706857336855202357581305570326707516350759619307275708283714351903070386238916734711233501153644979552391204751726815749320651555247341395258829504530070953263666426541042391578149520437404303855008019441706416715186447128399681717845469570262716310645461502572074024816377733896385506952606683411372738737229289564935470257626520988596932019650585547647033067936544325476327449512504060694381471046899465062201677204245245296126879465461931651746813926725041038025462596568691441928716082938031727143677826548775664850856740776484514644399404614226031930967354025744460703080960850474866385231381816767514386674766478908814371419854942315199735488037516586127535291661000710535582498794147295092931138971559982056543928717000721808576102523688921324497138932037843935308877482597017155910708823683627589842589185353024363421436706118923678919237231467232172053401649256872747782344535347648114941864238677677440606956", DecimalContext.ctx)
            //val LOG_E_PI = BigDecimal("1.1447298858494001741434273513530587116472948129153115715136230714721377698848260797836232702754897077020098122286979891590482055279234565872790810788102868252763939142663459029024847733588699377892031196308247567940119160282172273798881265631780498236973133106950036000644054872638802232700964335049595118150662372524683433912698965797514047770385779953998258425660228485014813621791592525056707638686028076345688975051233436078143991414426429596712897781136526452345041059007160818570824981188183186897672845928110257656875172422338337189273043288217348651042761532375161028392221340143696717585616442473718780506046692056283377310133621627451589875201512996545465739691528252391695852453793594601400379956519666036538000112659858500129765699060744667455472671045084950668558743390774251341592412652317771784917799588095767880510296444750901508911403278080768337337938949488075152890091875363766086707435833345108139232535574067684327431198049633999761803046221286361595859836404758009861799938264629277646275948484896414107483132", DecimalContext.ctx)
            val LOG_E_2: BigDecimal = BigDecimalMath.log(TWO, DecimalContext.ctx)
            val LOG_E_PI: BigDecimal = BigDecimalMath.log(PI, DecimalContext.ctx)
            val LOG_2_E: BigDecimal = ONE.divide(LOG_E_2, DecimalContext.ctx)
            val LOG_2_PI: BigDecimal = LOG_E_PI.divide(LOG_E_2, DecimalContext.ctx)

            fun isLessThan(a: BigDecimal, b: BigDecimal): Boolean =
                a.compareTo(b) == -1

            fun isGreaterThan(a: BigDecimal, b: BigDecimal): Boolean =
                a.compareTo(b) == 1

            fun isGreaterThan(a: BigInteger, b: BigInteger): Boolean =
                a.compareTo(b) == 1

            fun areEqual(a: BigDecimal, b: BigDecimal): Boolean =
                a.compareTo(b) == 0

            fun areNotEqual(a: BigDecimal, b: BigDecimal): Boolean =
                !areEqual(a, b)

            fun isZero(a: BigDecimal): Boolean =
                areEqual(a, ZERO)

            fun isOne(a: BigDecimal): Boolean =
                areEqual(a, ONE)

            fun isNotOne(a: BigDecimal): Boolean =
                areNotEqual(a, ONE)

            fun isGreaterThanOne(a: BigDecimal): Boolean =
                isGreaterThan(a, ONE)

            fun isLessThanOne(a: BigDecimal): Boolean =
                isLessThan(a, ONE)

            fun isGreaterThanZero(a: BigDecimal): Boolean =
                isGreaterThan(a, ZERO)

            fun isLessThanZero(a: BigDecimal): Boolean =
                isLessThan(a, ZERO)

            fun isInteger(a: BigDecimal): Boolean =
                try {
                    a.toBigIntegerExact()
                    true
                }
                catch(e: ArithmeticException) {
                    false
                }

            fun toPercent(p: BigDecimal): BigDecimal =
                p.multiply(HUNDRED, DecimalContext.ctx)

            /**
             * facultyNTakeM calculates (n)_m = n! / (n - m)!. This can be done naively by calculating (n)_m directly or by first calculating n! and then dividing it by (n - m)!.
             * In log space, that's equal to log(n! / (n - m)!) = log(n!) - log((n - m)!)
             */

            // input in natural numbers, output in natural logarithms. Not suitable for large m! n! can be calculated naively by using n = m
            fun facultyNTakeMNaive(n: BigDecimal, m: BigDecimal): BigDecimal {
                var nTakeMFacLogE = ZERO
                var i = n.toBigInteger()
                val stop = i.subtract(m.toBigInteger())
                while (isGreaterThan(i, stop)) {
                    nTakeMFacLogE =
                        nTakeMFacLogE.add(BigDecimalMath.log(i.toBigDecimal(0, DecimalContext.ctx), DecimalContext.ctx),
                            DecimalContext.ctx
                        )
                    i = i.subtract(BigInteger.ONE)
                }
                return nTakeMFacLogE
            }

            // in log space base 2
            fun facultyNTakeMLog2(n: BigDecimal, nLog2: BigDecimal, m: BigDecimal): BigDecimal {
                val nFacLog2 = facultyLog(n, nLog2, true)
                val nSubM = n.subtract(m, DecimalContext.ctx)
                val nSubMLogE = BigDecimalMath.log(nSubM, DecimalContext.ctx)
                val nSubMFacLogE = facultyLog(nSubM, nSubMLogE, false) // in log space with base e
                val nSubMFacLog2 = nSubMFacLogE.divide(
                    LOG_E_2,
                    DecimalContext.ctx
                ) // convert to log space with base 2 by dividing with ln(2), that is log_e_a = log_2_a / log_e_2
                val nTakeMFacLog2 = nFacLog2.subtract(nSubMFacLog2, DecimalContext.ctx)
                return nTakeMFacLog2
            }

            // in e-log space
            fun facultyNTakeMLogE(n: BigDecimal, nLogE: BigDecimal, m: BigDecimal): BigDecimal {
                val nFacLogE = facultyLog(n, nLogE, false)
                val nSubM = n.subtract(m, DecimalContext.ctx)
                val nSubMLogE = BigDecimalMath.log(nSubM, DecimalContext.ctx)
                val nSubMFacLogE = facultyLog(nSubM, nSubMLogE, false)
                val nTakeMFacLogE = nFacLogE.subtract(nSubMFacLogE, DecimalContext.ctx)
                return nTakeMFacLogE
            }

            // faculty method wrapper for both natural and base-2 logarithms
            fun facultyLog(n: BigDecimal, nLog: BigDecimal, isLog2: Boolean): BigDecimal {
                if (isZero(n)) // n == 0
                    return ONE
                else {
                    if (isLog2)
                        return facultyStirlingLog2(n, nLog)
                    else
                        return facultyStirlingLogE(n, nLog)
                }
            }

            /**
             *  Stirling's formula is an approximation for n!:
             *
             *      n! 		~ 	(n/e)^n * sqrt(2 * pi * n)
             *
             *  In log space with base e this is:
             *
             *      ln(n!) 	~ 	ln((n/e)^n * sqrt(2 * pi * n))
             *              =	ln((n/e)^n) + ln(sqrt(2 * pi * n))
             *              =	n(ln(n/e)) + ln((2 * pi * n)^(1/2))
             *              =	n(ln(n) - ln(e)) + 0.5(ln(2) + ln(pi) + ln(n))
             *              = 	n(nLogE - 1) + 0.5(LOG_E_2 + LOG_E_PI + nLogE
            */
            // in e-log space
            fun facultyStirlingLogE(n: BigDecimal, nLogE: BigDecimal): BigDecimal {
                val t1InnerSubtrNFacLogE = nLogE.subtract(ONE, DecimalContext.ctx)
                val t1NFacLogE = n.multiply(t1InnerSubtrNFacLogE, DecimalContext.ctx)
                val t2NFacLogE =
                    HALF.multiply(
                        LOG_E_2.add(LOG_E_PI, DecimalContext.ctx).add(nLogE,
                            DecimalContext.ctx
                        ), DecimalContext.ctx
                    )
                val nFacLogE = t1NFacLogE.add(t2NFacLogE, DecimalContext.ctx)
                return nFacLogE
            }

            /**
             * Stirling's formula in log base 2 space
             *
             *      lg(n!) 	~ 	lg((n/e)^n * sqrt(2 * pi * n))
             *              =	lg((n/e)^n) + lg(sqrt(2 * pi * n))
             *              =	n(lg(n/e)) + lg((2 * pi * n)^(1/2))
             *              =	n(lg(n) - lg(e)) + 0.5(lg(2) + lg(pi) + lg(n))
             *              = 	n(nLog2 - LOG_2_E) + 0.5(1 + LOG_2_PI + nLog2
             */
            // in 2-log
            fun facultyStirlingLog2(n: BigDecimal, nLog2: BigDecimal): BigDecimal {
                val t1InnerSubtrNFacLog2 = nLog2.subtract(LOG_2_E, DecimalContext.ctx)
                val t1NFacLog2 = n.multiply(t1InnerSubtrNFacLog2, DecimalContext.ctx)
                val t2NFacLog2 = HALF
                    .multiply(ONE.add(LOG_2_PI, DecimalContext.ctx).add(nLog2,
                        DecimalContext.ctx
                    ), DecimalContext.ctx
                    )
                val nFacLog2 = t1NFacLog2.add(t2NFacLog2, DecimalContext.ctx)
                return nFacLog2
            }

        }
    }

    // ways to calculate the birthday problem
    enum class CalcPrecision {
        EXACT, // exact calculation (do not use for large numbers)
        STIRLING_APPROX, // exact calculation but uses Stirling's formula for faculties which indirectly makes it an approximation
        TAYLOR_APPROX, // uses an approximation of the main formula
        TRIVIAL // trivial solution where no calculation is needed
    }

    private class BirthdayProblemSolver {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	Wrapper class for solving the birthday problem by calling this program from another class in Kotlin (BirthdayProblemCLISolver assumes output to console or to JSON with formatting included       #
        #	where as this class only outputs the results)         																																			   #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        companion object {

            /**
             * Returns the probability p of at least one non-unique sample when sampling nOrNLog times from a set of size dOrDLog. If isBinary is true, dOrDLog, nOrNLog and the result are in base-2 logarithmic
             * form. If isCombinations is true, then dOrDLog is the number of members, s, of a set from which we should generate the sample set d = s!. If both isBinary and isCombinations are true, then dOrDLogis
             * the base-2 logarithm of the number of members, s, of a set from which we should generate the sample set d = (2^s)!. nOrNLog is not affected by the isCombinations flag.
             */
            fun solveForP(dOrDLog: BigDecimal, nOrNLog: BigDecimal, isBinary: Boolean, isCombinations: Boolean, method: CalcPrecision): Pair<BigDecimal, CalcPrecision> {
                DecimalContext.reset() // reset to initial context precision
                BirthdayProblemInputHandler.sanitize(
                    dOrDLog,
                    nOrNLog,
                    null,
                    isBinary,
                    isCombinations,
                    method === CalcPrecision.STIRLING_APPROX,
                    method === CalcPrecision.TAYLOR_APPROX,
                    method == CalcPrecision.EXACT,
                    false
                )
                val (dPair, nPair, _) = BirthdayProblemInputHandler.setup(
                    dOrDLog,
                    nOrNLog,
                    null,
                    isBinary,
                    isCombinations
                )
                val (d, dLog) = dPair
                val (n, nLog) = nPair
                if(dLog === null)
                    throw SolverException(SolverErrorCode.DLOG_NOT_CALCULATED)
                return BirthdayProblemSolverChecked.birthdayProblem(d, dLog, n, nLog!!, method, isBinary)
            }

            /**
             * Returns the number of samples n required to have a probability p of at least one non-unique sample when sampling from a set of size dOrDLog. If isBinary is true, dOrDLog and the result are
             * in base-2 logarithmic form. If isCombinations is true, then dOrDLog is the number of members, s, of a set from which we should generate the sample set d (s!). If both isBinary and
             * isCombinations are true, then dOrDLog is the base-2 logarithm of the number of members, s, of a set from which we should generate the sample set d ((2^s)!).
             */
            fun solveForN(dOrDLog: BigDecimal, pIn: BigDecimal, isBinary: Boolean, isCombinations: Boolean): Pair<BigDecimal, CalcPrecision> {
                DecimalContext.reset() // reset to initial context precision
                BirthdayProblemInputHandler.sanitize(
                    dOrDLog,
                    null,
                    pIn,
                    isBinary,
                    isCombinations,
                    false,
                    false,
                    false,
                    false
                )
                val (dPair, _, p) = BirthdayProblemInputHandler.setup(dOrDLog, null, pIn, isBinary, isCombinations)
                val (d, dLog) = dPair
                if(dLog === null)
                    throw SolverException(SolverErrorCode.DLOG_NOT_CALCULATED)
                return BirthdayProblemSolverChecked.birthdayProblemInv(d, dLog, p!!, isBinary)
            }

        }
    }

    private class BirthdayProblemSolverChecked {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	Main drivers  that solves the birthday problem. Requires that input is checked and in correct form. Do not use directly, instead use BirthdayProblem.CLISolver or BirthdayProblem.Solver.          #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        companion object {

            fun birthdayProblem(
                maybeD: BigDecimal?,
                dLog: BigDecimal,
                maybeN: BigDecimal?,
                nLog: BigDecimal,
                calcPrecision: CalcPrecision,
                dIsLog2: Boolean
            ): Pair<BigDecimal, CalcPrecision> {
                if ((dIsLog2 && DecimalFns.isLessThanOne(nLog)) || (!dIsLog2 && DecimalFns.isLessThan(
                        nLog,
                        DecimalFns.LOG_E_2
                    )))
                    // trivially, if you sample less than 2 times, the chance of a non-unique sample is 0%
                    return Pair(DecimalFns.ZERO, CalcPrecision.TRIVIAL)
                else if (DecimalFns.isGreaterThan(nLog, dLog))
                    // trivially, if you sample more times than the number of items in the set to sample from, the chance of a non-unique item is 100%
                    return Pair(DecimalFns.ONE, CalcPrecision.TRIVIAL)
                else {

                    if (listOf(
                            CalcPrecision.EXACT,
                            CalcPrecision.STIRLING_APPROX
                        ).contains(calcPrecision) && (maybeD === null || maybeN === null)
                    )
                        // d and n are needed for these methods
                        throw SolverException(SolverErrorCode.D_NEEDED_FOR_METHOD, calcPrecision)

                    // carry out the calculations
                    DecimalContext.adjustPrecision((maybeD ?: dLog).run { precision() - scale() })
                    if (calcPrecision == CalcPrecision.EXACT) {
                        if (DecimalContext.isTooPrecise())
                            // with a too high precision, even the simplest calculation takes too long
                            throw SolverException(SolverErrorCode.TOO_HIGH_PRECISION, calcPrecision)
                        if (dIsLog2)
                            return Pair(
                                birthdayProblemExact(maybeD!!, dLog.divide(DecimalFns.LOG_2_E, DecimalContext.ctx), maybeN!!),
                                CalcPrecision.EXACT
                            )
                        else
                            return Pair(birthdayProblemExact(maybeD!!, dLog, maybeN!!), CalcPrecision.EXACT)
                    } else if (calcPrecision == CalcPrecision.TAYLOR_APPROX) {
                        if (DecimalContext.isTooPrecise()) {
                            DecimalContext.adjustPrecision(dLog.run { precision() - scale() })
                            if (DecimalContext.isTooPrecise())
                                // with a too high precision, even the simplest calculation takes too long
                                throw SolverException(SolverErrorCode.TOO_HIGH_PRECISION, calcPrecision)
                        }
                        if (dIsLog2)
                            return Pair(birthdayProblemTaylorApproxLog2(dLog, nLog), CalcPrecision.TAYLOR_APPROX)
                        else
                            return Pair(birthdayProblemTaylorApproxLogE(dLog, nLog), CalcPrecision.TAYLOR_APPROX)
                    } else {
                        if (DecimalContext.isTooPrecise())
                            // with a too high precision, even the simplest calculation takes too long
                            throw SolverException(SolverErrorCode.TOO_HIGH_PRECISION, calcPrecision)
                        if (dIsLog2)
                            return Pair(
                                birthdayProblemStirlingApproxLog2(maybeD!!, dLog, maybeN!!),
                                CalcPrecision.STIRLING_APPROX
                            )
                        else
                            return Pair(
                                birthdayProblemStirlingApproxLogE(maybeD!!, dLog, maybeN!!),
                                CalcPrecision.STIRLING_APPROX
                            )
                    }
                }
            }

            fun birthdayProblemInv(
                maybeD: BigDecimal?,
                dLog: BigDecimal,
                p: BigDecimal,
                dIsLog2: Boolean
            ): Pair<BigDecimal, CalcPrecision> {
                if (DecimalFns.isZero(p))
                    // trivially, to have a 0% chance of picking a duplicate, just pick one sample (or 0)
                    return Pair(
                        if (dIsLog2) DecimalFns.ZERO else DecimalFns.ONE,
                        CalcPrecision.TRIVIAL
                    )
                else if (DecimalFns.isOne(p) || DecimalFns.isGreaterThanOne(p)) {
                    // also trivially, to have a 100% (or more) chance of picking a duplicate, pick one more than the number of items in the input
                    if (dIsLog2)
                        // if d is too large to calculate adding 1 to it is negligible
                        return Pair(
                            maybeD
                                ?.let {
                                    BigDecimalMath.log(it.add(DecimalFns.ONE, DecimalContext.ctx),
                                        DecimalContext.ctx
                                    ).divide(DecimalFns.LOG_E_2, DecimalContext.ctx)
                                }
                                ?: dLog,
                            CalcPrecision.TRIVIAL)
                    else
                        // if d is too large to calculate adding 1 to it is negligible
                        return Pair(maybeD?.add(DecimalFns.ONE, DecimalContext.ctx) ?: dLog,
                            CalcPrecision.TRIVIAL
                        )
                } else {
                    // carry out the calculations
                    DecimalContext.adjustPrecision((maybeD ?: dLog).run { precision() - scale() })
                    if (DecimalContext.isTooPrecise()) {
                        DecimalContext.adjustPrecision(dLog.run { precision() - scale() })
                        if (DecimalContext.isTooPrecise())
                            // with a too high precision, even the simplest calculation takes too long
                            throw SolverException(SolverErrorCode.TOO_HIGH_PRECISION, CalcPrecision.TAYLOR_APPROX)
                    }
                    if (dIsLog2)
                        return Pair(birthdayProblemInvTaylorApproxLog2(dLog, p), CalcPrecision.TAYLOR_APPROX)
                    else
                        return Pair(
                            BigDecimalMath.exp(birthdayProblemInvTaylorApproxLogE(dLog, p), DecimalContext.ctx),
                            CalcPrecision.TAYLOR_APPROX
                        )
                }
            }

            /*######################################################################################################################################################################################################
            ########################################################################################################################################################################################################
            #																																																	   #
            #	Internal drivers                 																																								   #
            #																																																	   #
            ########################################################################################################################################################################################################
            ######################################################################################################################################################################################################*/

            /**
             * 	A frequent formula in the context of the birthday problem (or paradox) calculates that chance of no two items being equal (all items unique) when drawing d (picked) items from a population of
             * 	n(possibilities) items. Since we can choose unique items from n in (n)_d ways, and you can pick d items (any) from n in n^d, the formula for this is:
             *
             * 	    ^P(n, d) 	= (n)_d / n^d
             *
             *  In log space, this is:
             *
             *      lg(^P(n, d))= lg((n)_d / n^d)
             *                  = ln((n)_d) - lg(n^d)
             *                  = ln((n)_d) - d * lg(n)
             *
             * This result calculates the chance of all items unique, but most often, we are interested in the chance of there being at least one (trivially two) non-unique item(s) among dm P(n, d), which is
             * why we take the complement of ^P(n, d) as the final result of these functions.
             *
             *      P(n, d) 	= 1 - ^P(n, d)
             */

            fun birthdayProblemExact(d: BigDecimal, dLogE: BigDecimal, n: BigDecimal): BigDecimal {
                val favourableLogE = DecimalFns.facultyNTakeMNaive(d, n)
                val possibleLogE = dLogE.multiply(n, DecimalContext.ctx)
                val negProbLogE = favourableLogE.subtract(possibleLogE, DecimalContext.ctx)
                val negProb = BigDecimalMath.exp(negProbLogE, DecimalContext.ctx)
                val prob = DecimalFns.ONE.subtract(negProb, DecimalContext.ctx) // complement
                return prob
            }

            // calculates result in base 2 logarithmic space with base 2. Outputs probability in [0, 1]. Assumes non-trivial solution.
            fun birthdayProblemStirlingApproxLog2(
                d: BigDecimal, dLog2: BigDecimal, n: BigDecimal): BigDecimal {
                val favourableLog2 = DecimalFns.facultyNTakeMLog2(d, dLog2, n) // numerator
                val possibleLog2 = dLog2.multiply(n, DecimalContext.ctx) // denominator
                val negProbLog2 =
                    favourableLog2.subtract(possibleLog2, DecimalContext.ctx) // division in log space is subtraction
                val negProb =
                    BigDecimalMath.pow(DecimalFns.TWO, negProbLog2, DecimalContext.ctx) // go back to non-logarithmic space
                val prob = DecimalFns.ONE.subtract(negProb, DecimalContext.ctx) // complement
                return prob.coerceAtLeast(DecimalFns.ZERO) // fix precision errors leading to negative result
            }

            // calculates the result in natural base logarithms.
            fun birthdayProblemStirlingApproxLogE(d: BigDecimal, dLogE: BigDecimal, n: BigDecimal): BigDecimal {
                val favourableLogE = DecimalFns.facultyNTakeMLogE(d, dLogE, n) // numerator
                val possibleLogE = dLogE.multiply(n, DecimalContext.ctx)
                val negProbLogE =
                    favourableLogE.subtract(possibleLogE, DecimalContext.ctx) // division in log space is subtraction
                val negProb = BigDecimalMath.exp(negProbLogE, DecimalContext.ctx) // back to non-logarithmic space
                val prob = DecimalFns.ONE.subtract(negProb, DecimalContext.ctx)
                return prob.coerceAtLeast(DecimalFns.ZERO) // fix precision errors leading to negative result
            }

            /**
             *  In the previous versions we used an exact version of the main formula, even though Stirling's approximation was used for faculties. In the next version, we use an approximation for the main formula
             *  which is based on Taylor series. This approximation is the best one available (it mght be improved by adding other terms but the method is still the same).
             *
             *  The formula is based on the observation that ln(n!) = ln(n) + ln(n - 1):
             *
             *      P(n, d) 			~ 1 - e^(-(n^2/2d))
             *
             *  This implies that
             *
             *      ^P(n, d)			~ e^(-(n^2/2d))
             *
             *  In natural log space this is:
             *
             *      ln(^P(n, d))		~ ln(e^(-(n^2/2d)))
             *                          = -(n^2/2d)
             *
             *  This implies
             *
             *      -ln(^P(n, d))		~ (n^2/2d)
             *
             *  Now, for any logarithmic base (including natural), we now get by taking logarithms again
             *
             *      lg(-ln(^P(n, d)))	~ lg(n^2/2d)
             *
             * lg(-ln(^P(n, d))) is actually defined in the real domain here since ^P(n, d) <= 1.0, which gives that ln(^P(n ,d)) <= 0), which gives -ln(^P(n, d)) >= 0
             *
             *                          = lg(n^2) - lg(2d)
             *                          = 2 * lg(n) - (lg(2) + lg(d))
             *
             *  For base-2 logarithms, this yields:
             *
             *      lg(-ln(^P(n, d)))	~ 2 * lg(n) - (lg(2) + lg(d))
             *                          = 2 * nLog2 - (1 + dLog2)
             */

            // calculates result in base-2 logarithms (second level of logs)
            fun birthdayProblemTaylorApproxLog2(dLog2: BigDecimal, nLog2: BigDecimal): BigDecimal {
                val t1NegProbMinusLogELog2 = nLog2.multiply(DecimalFns.TWO, DecimalContext.ctx)
                val t2NegProbMinusLogELog2 = dLog2.add(DecimalFns.ONE, DecimalContext.ctx)
                val negProbMinusLogELog2 =
                    t1NegProbMinusLogELog2.subtract(
                        t2NegProbMinusLogELog2,
                        DecimalContext.ctx
                    )
                val negProbMinusLogE =
                    BigDecimalMath.pow(
                        DecimalFns.TWO,
                        negProbMinusLogELog2,
                        DecimalContext.ctx
                    ) // go back to non-logarithmic space
                val negProbLogE = negProbMinusLogE.negate()
                val negProb = BigDecimalMath.exp(negProbLogE, DecimalContext.ctx)
                val prob = DecimalFns.ONE.subtract(negProb, DecimalContext.ctx) // complement
                return prob
            }

            /**
             *  For base-e logarithms, the last part of the previous section yields:
             *
             *      ln(-ln(^P(n, d)))	~ 2 * ln(n) - (ln(2) + ln(d))
             *                          = 2 * nLogE - (LOG_E_2 + dLogE)
             */

            // calculates result in natural logarithmic space
            fun birthdayProblemTaylorApproxLogE(dLogE: BigDecimal, nLogE: BigDecimal): BigDecimal {
                val t1NegProbMinusLogELogE = nLogE.multiply(DecimalFns.TWO, DecimalContext.ctx)
                val t2NegProbMinusLogELogE = dLogE.add(DecimalFns.LOG_E_2, DecimalContext.ctx)
                val negProbMinusLogELogE =
                    t1NegProbMinusLogELogE.subtract(
                        t2NegProbMinusLogELogE,
                        DecimalContext.ctx
                    )
                val negProbMinusLogE = BigDecimalMath.exp(negProbMinusLogELogE, DecimalContext.ctx) // go back to non-logarithmic space
                val negProbLogE = negProbMinusLogE.negate()
                val negProb = BigDecimalMath.exp(negProbLogE, DecimalContext.ctx)
                val prob = DecimalFns.ONE.subtract(negProb, DecimalContext.ctx) // complement
                return prob
            }

            /**
             *  The formula for calculating the inverted birthday problem, namely how many times to sample from a set to reach a probability p of some non-unique samples, also uses the above Taylor
             *  approximation.
             *
             *  Starting, from the previous section, with:
             *
             *      P(n, d) 			~ 1 - e^(-(n^2/2d))
             *
             *  We have:
             *
             *      ^P(n, d) 			~ e^(-(n^2/2d))
             *
             *  Trying to solve for n, we get:
             *
             *      ln(^P(n, d))		~ (-(n^2/2d))
             *      -ln(^P(n, d))		~ (n^2/2d)
             *      -ln(^P(n, d))) * 2d ~ n^2
             *      n 					~ sqrt(-ln(^P(n, d))) * 2d)
             *      n(P, d)				~ sqrt(-ln(^P) * 2d)
             *
             *  The above works for the same reason as stated earlier. -ln(^P) >= 0 since ^P <= 1.0 and so the value in sqrt() is positive.
             *
             *  Using logarithms to solve this, we have:
             *
             *      lg(n(P, d))			~ lg(sqrt(-ln(^P) * 2d))
             *                          = lg((-ln(^P) * 2d)^(1/2))
             *                          = 0.5 * lg(-ln(^P) * 2d)
             *                          = 0.5 * ( lg(-ln(^P)) + lg(2d) )
             *                          = 0.5 * ( lg(-ln(^P)) + (lg(2) + lg(d)))
             *                          = 0.5 * ( lg(-ln(^P)) + lg(2) + lg(d) )
             *
             * Working with P, instead of ^P, we have:
             *
             *      lg(n(P, d))			~ 0.5 * ( lg(-ln(1 - P)) + lg(2) + lg(d) )
             *
             *  For natural logarithms we arrive at:
             *
             *      lg(n(P, d))			~ 0.5 * ( ln(-ln(1 - P)) + ln(2) + ln(d))
             *                          = 0.5 * ( ln(-ln(1 - p)) + LOG_E_2 + dLogE )
             *
             */

            // with base e logarithms
            fun birthdayProblemInvTaylorApproxLogE(dLogE: BigDecimal, p: BigDecimal): BigDecimal {
                val t1SamplesLogE2 = BigDecimalMath.log(DecimalFns.ONE.subtract(p,
                    DecimalContext.ctx
                ), DecimalContext.ctx
                )
                val t1SamplesLogE = BigDecimalMath.log(t1SamplesLogE2.negate(), DecimalContext.ctx)
                val samplesLogE =
                    DecimalFns.HALF
                        .multiply(t1SamplesLogE.add(
                            DecimalFns.LOG_E_2.add(dLogE, DecimalContext.ctx),
                            DecimalContext.ctx
                        ), DecimalContext.ctx
                        )
                return samplesLogE
            }

            /**
             *  For base 2 logarithms we arrive at:
             *
             *      lg(n(P, d))			~ 0.5 * ( lg(-ln(1 - P)) + lg(2) + lg(d) )
             */

            // with base 2 logarithms
            fun birthdayProblemInvTaylorApproxLog2(dLog2: BigDecimal, p: BigDecimal): BigDecimal {
                val dLogE = dLog2.divide(DecimalFns.LOG_2_E, DecimalContext.ctx) // go to natural logarithms
                val samplesLogE = birthdayProblemInvTaylorApproxLogE(dLogE, p)
                val samplesLog2 = samplesLogE.divide(DecimalFns.LOG_E_2, DecimalContext.ctx) // back to base 2 logarithms
                return samplesLog2
            }
        }
    }

    private class BirthdayProblemNumberFormatter {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	Output formatting functions to format the resulting numbers only. Used by BirthdayProblem.CLISolver to output numbers in a nice form.										  			 		   #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        companion object {

            // threshold outside which we also print values in log10 space
            val LOG10_LOWER_THRESHOLD = BigDecimal("1e-2", DecimalContext.ctx)
            val LOG10_UPPER_THRESHOLD = BigDecimal("1e+5", DecimalContext.ctx)

            // default decimal precision of output
            const val OUTPUT_PRECISION = 10 // not really precision in BigDecimal sense but number of decimals

            // error constant for floating point rounding
            val ERR = BigDecimal("1e-12", DecimalContext.ctx)

            /* compare number dBase^dExp10 to originalD and see whether they differ, relatively, by more than ERR or not. This implies filtering out a common power of 10 and then comparing the results. The reason is
            to determine whether to use approximation or equality sign. */
            fun isExpReprEqualToStandardRepr(dBase: BigDecimal, dExp10: Int, originalD: BigDecimal): Boolean {
                val originalDExp = originalD.run { (precision() - scale()) - 1 } // powers of 10 to filter out from the original input
                val scaledOriginalD = originalD.scaleByPowerOfTen(-originalDExp)
                val base10PowersDiff = dExp10 - originalDExp // scale dBase so that they have the same number of powers of 10 filtered out
                val leveledD = dBase.scaleByPowerOfTen(base10PowersDiff)
                val equal = DecimalFns.isLessThan(scaledOriginalD.subtract(leveledD, DecimalContext.ctx).abs(), ERR)
                return equal
            }

            // returns log10 representation of a number or the empty string if the input is not outside the defined log10 representation thresholds.
            fun toLog10ReprOrNone(argD: BigDecimal): String {
                var d = argD
                val inputD = argD
                var exp = 0 // powers of 10 filtered out of d
                if((DecimalFns.isGreaterThan(LOG10_LOWER_THRESHOLD, d) || DecimalFns.isLessThan(
                        LOG10_UPPER_THRESHOLD,
                        d
                    )) && DecimalFns.isGreaterThanZero(d)
                ) {
                    // d is smaller than the lower log 10 repr threshold or larger than the upper log 10 repr threshold, and not 0, so a complementary log 10 representation is called for
                    while(true) {
                        // loop here due to floating point arithmetic
                        // example: d can, after filtering out powers of 10, be 9.9999 which rounds to 10 in which case we can filter out another power of 10 before proceeding
                        val roundExp = d.run { (precision() - scale()) - 1 } // current powers of 10 filtered out
                        d = d.scaleByPowerOfTen(-roundExp)
                        exp += roundExp
                        d = d.add(ERR, DecimalContext.ctx) // add error constant to get around rounding errors due to floating point arithmetic (for example, 2.5 being stored as 2.49999999)
                        d = d.setScale(0, RoundingMode.HALF_UP)
                        if(DecimalFns.isLessThan(d, DecimalFns.TEN)) {
                            // d is less than 10, we have a nice base 10 representation
                            val equalOrApprox = if(isExpReprEqualToStandardRepr(d, exp, inputD)) "=" else "≈"
                            return equalOrApprox + (if(DecimalFns.isNotOne(d)) (d.toPlainString() + "*") else "") + "10^" + exp
                        }
                    }
                }
                else
                    return ""
            }

            // results in a number with prec decimals (if prec is positive which it should be)
            fun toOutputNumberFormatted(d: BigDecimal, prec: Int? = null): String =
                toOutputNumber(d.setScale(prec ?: OUTPUT_PRECISION, RoundingMode.HALF_UP).toPlainString())

            fun toFloatRoundedAndApproximateParts(f: BigDecimal, prec: Int? = null): Pair<String, String> {
                val roundedF = toOutputNumberFormatted(f, prec)
                val prefix = if(DecimalFns.areNotEqual(f, BigDecimal(roundedF, DecimalContext.ctx))) "≈" else ""
                return prefix to roundedF
            }

            fun toFloatRoundedAndApproximate(f: BigDecimal, prec: Int? = null): String =
                toFloatRoundedAndApproximateParts(f, prec).toList().joinToString("")

            fun toIntegralRounded(d: BigDecimal, rounding: RoundingMode = RoundingMode.HALF_UP): Pair<String, String> {
                val roundedD = d.setScale(0, rounding).toBigInteger() // round correctly before going to BigInteger
                return "" to roundedD.toString()
            }

            fun toIntegralRoundedAndApproximateParts(d: BigDecimal): Pair<String, String> {
                val roundedD = d.setScale(0, RoundingMode.HALF_UP).toBigInteger() // round correctly before going to BigInteger
                val prefix = if(DecimalFns.areNotEqual(d, BigDecimal(roundedD, DecimalContext.ctx))) "≈" else ""
                return prefix to roundedD.toString()
            }

            fun toIntegralRoundedAndApproximate(d: BigDecimal): String =
                toIntegralRoundedAndApproximateParts(d).toList().joinToString("")

            fun toOutputNumber(inputOut: String): String {
                var out = inputOut
                if(out.contains('.')) {
                    out = out.trimEnd('0')
                    out = out.trimEnd('.')
                }
                return out
            }
        }
    }

    private class BirthdayProblemTextFormatter {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	Text output formatting functions to format the text only. Used by BirthdayProblem.CLISolver.solve() to output text in a nice way.													               #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        companion object {

            const val INDENT_SZ = 10

            fun parenthesize(text: String) =
                if (text.isNotBlank()) " ($text)" else text

            fun methodToShortDescription(method: CalcPrecision) =
                when (method) {
                    CalcPrecision.EXACT -> "Exact method"
                    CalcPrecision.TAYLOR_APPROX -> "Taylor approximation"
                    CalcPrecision.STIRLING_APPROX -> "Exact method with Stirling's approximation"
                    else -> "Unknown method"
                }

            fun methodToText(method: CalcPrecision) =
                when (method) {
                    CalcPrecision.EXACT -> "Exact"
                    CalcPrecision.TAYLOR_APPROX -> "Taylor"
                    CalcPrecision.STIRLING_APPROX -> "Stirling"
                    CalcPrecision.TRIVIAL -> "Trivial"
                    else -> "Unknown"
                }

            fun methodToDescription(method: CalcPrecision, isInv: Boolean) =
                when (method) {
                    CalcPrecision.EXACT -> "Exact method"
                    CalcPrecision.TAYLOR_APPROX -> "Taylor series approximation used in main calculation${if (isInv) "" else " (removes need for factorial calculation)"}"
                    CalcPrecision.STIRLING_APPROX -> "Stirling's approximation used in factorial calculation"
                    CalcPrecision.TRIVIAL -> "Trivial solution"
                    else -> "Unknown method"
                }

            fun headerTextBirthdayProblemInvNumbers(
                dLogOrNot: BigDecimal,
                p: BigDecimal,
                pPercent: BigDecimal,
                isLog2: Boolean,
                prec: Int? = null
            ): Pair<String, String> {
                val (dLog2Text, dLog10Text, dTextPair) = isLog2.let {
                    if (it)
                        Triple(
                            "2^",
                            "",
                            BirthdayProblemNumberFormatter.toFloatRoundedAndApproximateParts(dLogOrNot, prec)
                        )
                    else
                        Triple(
                            "",
                            parenthesize(BirthdayProblemNumberFormatter.toLog10ReprOrNone(dLogOrNot)),
                            BirthdayProblemNumberFormatter.toIntegralRoundedAndApproximateParts(dLogOrNot)
                        )
                }
                val (prefix, dText) = dTextPair
                val pLog10Text = parenthesize(BirthdayProblemNumberFormatter.toLog10ReprOrNone(p))
                return (prefix + dLog2Text + dText + dLog10Text) to (BirthdayProblemNumberFormatter.toFloatRoundedAndApproximate(
                    pPercent
                ) + "%" + pLog10Text)
            }

            fun headerTextBirthdayProblemInv(
                dLogOrNot: BigDecimal,
                p: BigDecimal,
                pPercent: BigDecimal,
                isLog2: Boolean,
                prec: Int? = null
            ): String {
                val (dText, pText) = headerTextBirthdayProblemInvNumbers(dLogOrNot, p, pPercent, isLog2, prec)
                return "The number of samples, sampled uniformly at random from a set of $dText items, needed to have at least a $pText chance of a non-unique sample is:"
            }

            fun resultTextBirthdayProblemInvNumbers(n: BigDecimal, isLog2: Boolean, prec: Int? = null): String {
                val (nLog2Text, nLog10Text, nTextPair) = isLog2.let {
                    if (it)
                        Triple(
                            "2^",
                            "",
                            BirthdayProblemNumberFormatter.toFloatRoundedAndApproximateParts(n, prec)
                        )
                    else
                        Triple(
                            "",
                            parenthesize(BirthdayProblemNumberFormatter.toLog10ReprOrNone(n)),
                            BirthdayProblemNumberFormatter.toIntegralRounded(n, RoundingMode.CEILING)
                        )
                }
                val (prefix, nText) = nTextPair
                return prefix + nLog2Text + nText + nLog10Text
            }

            fun resultTextBirthdayProblemInv(
                n: BigDecimal,
                isLog2: Boolean,
                method: CalcPrecision,
                prec: Int? = null
            ): String {
                val nText = resultTextBirthdayProblemInvNumbers(n, isLog2, prec)
                return nText + parenthesize(methodToDescription(method, true))
            }

            fun headerTextBirthdayProblemNumbers(
                dLogOrNot: BigDecimal,
                nLogOrNot: BigDecimal,
                isLog2: Boolean,
                prec: Int? = null
            ): Pair<String, String> {
                val (log2Text, log10TextPair, dTextPair) = isLog2.let {
                    if (it)
                        Triple(
                            "2^",
                            Pair("", ""),
                            BirthdayProblemNumberFormatter.toFloatRoundedAndApproximateParts(dLogOrNot, prec)
                        )
                    else
                        Triple(
                            "",
                            Pair(
                                parenthesize(BirthdayProblemNumberFormatter.toLog10ReprOrNone(dLogOrNot)),
                                parenthesize(BirthdayProblemNumberFormatter.toLog10ReprOrNone(nLogOrNot))
                            ),
                            BirthdayProblemNumberFormatter.toIntegralRoundedAndApproximateParts(dLogOrNot)
                        )
                }
                val (dLog10Text, nLog10Text) = log10TextPair
                val (prefix, dText) = dTextPair
                return (prefix + log2Text + dText + dLog10Text) to (log2Text + nLogOrNot.toPlainString() + nLog10Text)
            }

            fun headerTextBirthdayProblem(dLogOrNot: BigDecimal, nLogOrNot: BigDecimal, isLog2: Boolean, prec: Int? = null): String {
                val (dText, nText) = headerTextBirthdayProblemNumbers(dLogOrNot, nLogOrNot, isLog2, prec)
                return "The probability of finding at least one non-unique sample among $nText samples, sampled uniformly at random from a set of $dText items, is:"
            }

            fun resultTextBirthdayProblemNumbers(p: BigDecimal, pPercent: BigDecimal, prec: Int? = null): Pair<String, String> {
                val pLog10Text = parenthesize(BirthdayProblemNumberFormatter.toLog10ReprOrNone(p))
                return BirthdayProblemNumberFormatter.toFloatRoundedAndApproximate(pPercent, prec) + "%" to pLog10Text
            }

            fun resultTextBirthdayProblem(p: BigDecimal, pPercent: BigDecimal, method: CalcPrecision, prec: Int? = null): Triple<String, String, String> {
                val (pText, pLog10Text) = resultTextBirthdayProblemNumbers(p, pPercent, prec)
                return Triple(pText, pLog10Text, parenthesize(methodToDescription(method, false)))
            }

            fun indented(text: String) =
                "".padStart(INDENT_SZ, ' ') + text

        }
    }

    private class BirthdayProblemInputHandler {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	Input sanitizer		            																																								   #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        companion object {

            fun illegalInputString(varName: String? = null) =
                if(varName === null) "Illegal input" else "Illegal input for '$varName'"

            fun checkDecimal(variable: Any, varName: String): BigDecimal {
                if(variable !is BigDecimal)
                    throw Exception("${illegalInputString(varName)}: must be of type 'BigDecimal'")
                return variable
            }

            fun checkBoolean(variable: Any, varName: String): Boolean {
                if(variable !is Boolean)
                    throw Exception("${illegalInputString(varName)}: must be of type 'Boolean'")
                return variable
            }

            // method that takes the input and checks the arguments and their semantic joint meaning and throws an error if it is not accepted
            fun sanitize(
                dOrDLog: Any?,
                nOrNLog: Any?,
                p: Any?,
                isBinary: Any,
                isCombinations: Any,
                isStirling: Any,
                isTaylor: Any,
                isExact: Any,
                isAll: Any,
                varMap: Map<String, String> = emptyMap()
            ) {
                if(dOrDLog === null)
                    throw Exception(illegalInputString(varMap.getOrDefault("dOrDLog", "dOrDLog")) + ": please provide a size for the set to sample from.")
                checkDecimal(dOrDLog, varMap.getOrDefault("dOrDLog", "dOrDLog"))
                dOrDLog as BigDecimal

                listOf(
                    Pair(isBinary, varMap.getOrDefault("isBinary", "isBinary")),
                    Pair(isCombinations, varMap.getOrDefault("isCombinations", "isCombinations")),
                    Pair(isStirling, varMap.getOrDefault("isStirling", "isStirling")),
                    Pair(isTaylor, varMap.getOrDefault("isTaylor", "isTaylor")),
                    Pair(isExact, varMap.getOrDefault("isExact", "isExact")),
                    Pair(isAll, varMap.getOrDefault("isAll", "isAll"))
                ).forEach {
                        (variable, name) ->
                    checkBoolean(variable, name)
                }
                isBinary as Boolean
                isCombinations as Boolean
                isStirling as Boolean
                isTaylor as Boolean
                isExact as Boolean
                isAll as Boolean

                if(!DecimalFns.isInteger(dOrDLog))
                    throw Exception(illegalInputString(varMap.getOrDefault("dOrDLog", "dOrDLog")) + ": please provide an integer")
                else if(DecimalFns.isLessThanZero(dOrDLog))
                    throw Exception(illegalInputString(varMap.getOrDefault("dOrDLog", "dOrDLog")) + ": please provide a non-negative integer")
                else if(DecimalFns.isZero(dOrDLog) && !isBinary && !isCombinations)
                    throw Exception("${illegalInputString(varMap.getOrDefault("dOrDLog", "dOrDLog"))}: please provide a value for '${varMap.getOrDefault("dOrDLog", "dOrDLog")}' that results in a non-empty set of unique items from which samples are taken.")

                if((p === null && nOrNLog === null) || (p !== null && nOrNLog !== null))
                    throw Exception("${illegalInputString()}: please provide a non-None value for either '${varMap.getOrDefault("nOrDLog", "nOrDLog")}' or '${varMap.getOrDefault("p", "p")}' (not both)")

                if(nOrNLog !== null) {
                    checkDecimal(nOrNLog, varMap.getOrDefault("nOrDLog", "nOrDLog"))
                    nOrNLog as BigDecimal
                    if(!isStirling && !isExact && !isTaylor && !isAll)
                        throw Exception("${illegalInputString()}: must set at least one of '${varMap.getOrDefault("isStirling", "isStirling")}', '${varMap.getOrDefault("isTaylor", "isTaylor")}', '${varMap.getOrDefault("isExact", "isExact")}' or '${varMap.getOrDefault("isAll", "isAll")}' when '${varMap.getOrDefault("nOrNLog", "nOrNLog")}' is not None.")
                    else if((isStirling || isExact || isTaylor) && isAll)
                        throw Exception("${illegalInputString()}: flag '${varMap.getOrDefault("isAll", "isAll")}' was true and implicitly includes '${varMap.getOrDefault("isStirling", "isStirling")}', '${varMap.getOrDefault("isTaylor", "isTaylor")}' and '${varMap.getOrDefault("isExact", "isExact")}' set to True which should then not be set to True.")
                    else if(!DecimalFns.isInteger(nOrNLog))
                        throw Exception("${illegalInputString(varMap.getOrDefault("nOrDLog", "nOrDLog"))}: please provide an integer")
                    else if(DecimalFns.isLessThanZero(nOrNLog))
                        throw Exception("${illegalInputString(varMap.getOrDefault("nOrDLog", "nOrDLog"))}: please provide a non-negative integer")
                }
                else {
                    checkDecimal(p!!, varMap.getOrDefault("p", "p"))
                    p as BigDecimal
                    if(isStirling || isExact || isTaylor)
                         throw Exception("${illegalInputString()}: '${varMap.getOrDefault("isStirling", "isStirling")}', '${varMap.getOrDefault("isTaylor", "isTaylor")}' and '${varMap.getOrDefault("isExact", "isExact")}' or '${varMap.getOrDefault("isAll", "isAll")}' should only be non-false when '${varMap.getOrDefault("nOrDLog", "nOrDLog")}' is not null (with '${varMap.getOrDefault("p", "p")}' != null), Taylor approximation is always used).")
                    else if(DecimalFns.isGreaterThanOne(p) || DecimalFns.isLessThanZero(p))
                        throw Exception("${illegalInputString(varMap.getOrDefault("p", "p"))}: please provide a non-negative decimal number in the range [0.0, 1.0]")
                }
            }


            // further processes correct input and extract all arguments that is needed for calculations to start (based on isBinary and isCombinations)
            fun setup(
                dOrDLog: BigDecimal,
                nOrNLog: BigDecimal?,
                p: BigDecimal?,
                isBinary: Boolean,
                isCombinations: Boolean
            ): Triple<Pair<BigDecimal?, BigDecimal?>, Pair<BigDecimal?, BigDecimal?>, BigDecimal?> {
                var d: BigDecimal? = dOrDLog
                var dLog: BigDecimal? = null
                var n: BigDecimal? = nOrNLog
                var nLog: BigDecimal? = null

                // prepare by taking isBinary and sCombinations flags into account to established the actual sizes of d and dLogD
                try {
                    if (isCombinations) {
                        // d is the size of a set of items, calculate the number of permutations that is possible with it
                        if (isBinary) {
                            dLog = DecimalFns.facultyLog(
                                BigDecimalMath.pow(DecimalFns.TWO, dOrDLog, DecimalContext.ctx),
                                dOrDLog,
                                true
                            )
                            d = BigDecimalMath.pow(DecimalFns.TWO, dLog, DecimalContext.ctx)
                        } else {
                            dLog =
                                DecimalFns.facultyLog(dOrDLog, BigDecimalMath.log(dOrDLog, DecimalContext.ctx), false)
                            d = BigDecimalMath.exp(dLog, DecimalContext.ctx)
                        }
                    } else {
                        // d is already the size of the set of combinations
                        if (isBinary) {
                            dLog = dOrDLog
                            d = BigDecimalMath.pow(DecimalFns.TWO, dOrDLog, DecimalContext.ctx)
                        }
                        else
                            dLog = BigDecimalMath.log(dOrDLog, DecimalContext.ctx)
                    }
                } catch (e: Exception) {
                    // either calculation of dLog threw and then dLog remains null and d is larger and definitely not calculated = null, or just calc of d threw which means d should be null
                    d = null
                }

                if(p === null) {
                    // calculate probability p based on d and n
                    try {
                        if (isBinary) {
                            nLog = nOrNLog
                            n = BigDecimalMath.pow(DecimalFns.TWO, nLog, DecimalContext.ctx)
                        } else
                            // for all purposes, sampling 0 times is the same as samping 1 times
                            nLog = if(DecimalFns.isGreaterThanZero(nOrNLog!!)) BigDecimalMath.log(nOrNLog,
                                DecimalContext.ctx
                            ) else DecimalFns.ZERO
                    } catch (e: Exception) {
                        n = null // calc of n threw which means n should be None
                    }
                }

                return Triple(Pair(d, dLog), Pair(n, nLog), p)

            }
        }
    }

    private class BirthdayProblemInputParser {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	Input parser used by Birthday.CLISolver to parse input arguments 																								      							   #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        private class BirthdayProblemInputParserArgs(parser: ArgParser) {

            val d by parser.positional(
                "D",
                help = "Input number D, the total number of unique items, or a number from which the total number of unique items can be derived, in the set we are sampling from."
            )

            val samples by parser.storing(
                "-n",
                "--samples",
                help = "Input number N, the number of samples, or a number from which the number of samples can be derived from, taken from the full set of D items. When present the probability P of at least one non-unique item among the samples will be calculated. Requires one of flags -e, -s, -t or -a to determine the desired precision(s) of the calculation."
            ).default<String?>(null)

            val probability by parser.storing(
                "-p",
                "--probability",
                help = "Input number P in [0.0, 1.0], the the probability of at least one non-unique item among the samples. When present the needed number of samples N will be approximated with Taylor series."
            ).default<String?>(null)

            val binary by parser.flagging(
                "-b",
                "--binary",
                help = "Inputs D and N are seen as exponents with base 2"
            )
            val combinations by parser.flagging(
                "-c",
                "--combinations",
                help = "Input D is seen as a number of unique items in a set from which we can yield N! (factorial) different members for the resulting set of unique items from which we sample. The calculation of D! uses Stirling's approximation which might introduce a small error responsible for the difference in results with the same input with and without -c flag."
            )

            val taylor by parser.flagging(
                "-t",
                "--taylor",
                help = "Use Taylor approximation to calculate the birthday problem (only with flag -n) (best suited for extremely large numbers)"
            )
            val stirling by parser.flagging(
                "-s",
                "--stirling",
                help = "Use exact method but approximate faculty calculations with Stirling\\'s formula (only with flag -n) (best suited up to extremely large numbers)"
            )
            val exact by parser.flagging(
                "-e",
                "--exact",
                help = "Use exact method (only with flag -n) (WARNING! This method becomes too slow very quickly as calculations grow with complexity O(N!) where N is the size of the sampled set) (best suited for smaller numbers)"
            )
            val all by parser.flagging(
                "-a",
                "--all",
                help = "Use all methods for the calculation (same as using flags -e, -s, -t when used with -n, otherwise it has no effect)"
            )
            val json by parser.flagging(
                "-j",
                "--json",
                help = "Output results as a Json object"
            )
            val prec by parser.storing(
                "--prec",
                help = "The number of digits (at most) to the right of the decimal point, where applicable, in the answer (a number between 0 and 10, default is 10)"
            ) {
                try { toInt() }
                catch(nfe: NumberFormatException) { throw SystemExitException("Illegal input for prec: please provide an integer number in the range [0, 10]", 1) }
            }.default(10)

        }

        companion object {

            const val description = "Treats the generalized birthday problem for arbitrary values." +
                    "\n" +
                    "\n" +
                    "Calculates the generalized birthday problem, the probability P that, when sampling uniformly at random N times (with replacement) from a set of D unique items, there is a non-unique " +
                    "item among the N samples. In the original birthday problem formulation, N is 23 and D is 366 (or 365) for a risk of P ≈ 0.5 = 50% of at least two people having the same birthday." +
                    "\n" +
                    "\n" +
                    "Supports calculating both the probability P from N and D (using exact method, exact method with Stirling's approximation in the calculation of faculties and Taylor approximation) and " +
                    "N from D and P (Taylor approximation only). Both approximations get asymptotically close to the exact result as D grows towards infinity. The exact method should not be used for larger " +
                    "numbers. For extremely small probabilities P, the exact method with Stirling's approximation used for faculties may become unstable as it involves many more different operations than " +
                    "the Taylor approximation which, each, results in small round-offs. Another source of error in this case arises from the use of Stirling's formula for two calculations of faculties (D! " +
                    "and (D - N)!). Since one of these ((D - N)!) diverges slightly more from the exact result than the other (D!), the difference between these (used for calculations in log space) might " +
                    "introduce small errors when P is extremely small. A good check to see whether the approximation in question is suffering or not is to compare it to the Taylor approximation and see " +
                    "whether they match well." +
                    "\n" +
                    "\n" +
                    "Inputs D and N can be seen as literal input numbers or as exponents of base 2 (with -b flag). Furthermore, input D can be seen as a set of items from which we should produce the D! " +
                    "permutations before proceeding with further calculations (with flag -c)." +
                    "\n" +
                    "\n" +
                    "Example usage:" +
                    "\n" +
                    "\n" +
                    "    Example 1:" +
                    "\n\n" +
                    "\tCalculate the probability P of at least one non-unique birthday among N = 23 persons with all available methods:" +
                    "\n\n" +
                    "\t\t> python BirthdayProblem.py 366 -n 23 -a" +
                    "\n" +
                    "\n" +
                    "    Example 2:" +
                    "\n\n" +
                    "\tCalculate the number of times N a deck of cards has to be shuffled to have a P = 50% probability of seeing a repeated shuffle:" +
                    "\n\n" +
                    "\t\t> python BirthdayProblem.py 52 -p 0.5 -c" +
                    "\n" +
                    "\n" +
                    "    Example 3:" +
                    "\n\n" +
                    "\tCalculate the probability P of a collision in a 128-bit hash when hashing N = 2^32 = 4294967296 items with approximative methods and output answer as a Json object:" +
                    "\n\n" +
                    "\t\t> python BirthdayProblem.py 128 -n 32 -b -s -t"

            fun parse(inputArgs: Array<String>): BirthdayProblemInputParameters {
                lateinit var args: BirthdayProblemInputParserArgs
                mainBody {
                    args = ArgParser(inputArgs, helpFormatter = DefaultHelpFormatter(prologue = description)).parseInto(
                        BirthdayProblemInputParser::BirthdayProblemInputParserArgs
                    )

                    if(args.probability === null && args.samples === null)
                        throw SystemExitException("Please provide one of flags -n or -p with corresponding argument.", 1)
                    else if(args.probability !== null && args.samples !== null)
                        throw SystemExitException("Please provide EITHER a flag -n or -p, not both.", 1)
                    else if(args.samples !== null && !args.stirling && !args.exact && !args.taylor && !args.all)
                        throw SystemExitException("Must set at least one of flags -s, -t, -e or -a together with -n.", 1)
                    else if((args.stirling || args.exact || args.taylor) && args.samples === null)
                        throw SystemExitException("Flags -s, -t and -e should only be used with flag -n (with flag -p, Taylor approximation is always used).", 1)
                    else if((args.stirling || args.exact || args.taylor) && args.all)
                        throw SystemExitException("Flag -a was set and implicitly includes -s, -t and -e which should then not be used.", 1)
                    else if(!"""[\d]+""".toRegex().matches(args.d))
                        throw SystemExitException("Illegal input for D: please provide a non-negative integer with digits only", 1)
                    else if(args.samples !== null && !"""[\d]+""".toRegex().matches(args.samples!!))
                        throw SystemExitException("Illegal input for N: please provide a non-negative integer with digits only", 1)
                    else if(args.probability !== null && !"""(1\.[0]+|0\.[\d]+)""".toRegex().matches(args.probability!!))
                        throw SystemExitException("Illegal input for P: please provide a non-negative decimal number in the range [0.0, 1.0]", 1)
                    else if(args.prec < 0 || args.prec > 10)
                        throw SystemExitException("Illegal input for prec: please provide an integer number in the range [0, 10]", 1)
                }
                return BirthdayProblemInputParameters(args.d, args.samples, args.probability, args.binary, args.combinations, args.stirling, args.taylor, args.exact, args.all, args.json, args.prec)
            }
        }

    }

    private class BirthdayProblemCLISolver {

        /*######################################################################################################################################################################################################
        ########################################################################################################################################################################################################
        #																																																	   #
        #	Solver for standalone use from the command line. Output to the commandline in program form or in Json form for other UIs.																		   #
        #																																																	   #
        ########################################################################################################################################################################################################
        ######################################################################################################################################################################################################*/

        companion object {

            fun setup(args: Array<String>, varMap: Map<String, String> = emptyMap()): BirthdayProblemParameters {
                DecimalContext.reset()
                val inputParams = BirthdayProblemInputParser.parse(args)
                val initialDOrDLog = BigDecimal(inputParams.dOrDLog)
                val initialNOrNLog = inputParams.nOrNLog?.let { BigDecimal(it) }
                val initialP = inputParams.p?.let { BigDecimal(it) }

                BirthdayProblemInputHandler.sanitize(
                    initialDOrDLog,
                    initialNOrNLog,
                    initialP,
                    inputParams.isBinary,
                    inputParams.isCombinations,
                    inputParams.isStirling,
                    inputParams.isTaylor,
                    inputParams.isExact,
                    inputParams.isAll,
                    varMap
                )
                val (dPair, nPair, p) =
                    BirthdayProblemInputHandler.setup(
                        initialDOrDLog,
                        initialNOrNLog,
                        initialP,
                        inputParams.isBinary,
                        inputParams.isCombinations
                    )
                val (d, dLog) = dPair
                val (n, nLog) = nPair

                return BirthdayProblemParameters(
                    d,
                    dLog,
                    n,
                    nLog,
                    p,
                    p?.let { DecimalFns.toPercent(it) },
                    inputParams.isBinary,
                    inputParams.isStirling,
                    inputParams.isTaylor,
                    inputParams.isExact,
                    inputParams.isAll,
                    inputParams.isJson,
                    inputParams.prec
                )
            }

            fun solve(args: Array<String>, isCLI: Boolean): String {
                try {
                    val params = setup(args)

                    if(params.dLog === null || params.dLog.scale() < 0) // implies the precision was not enough to store the size of this number, a scale had to be used
                        throw Exception("couldn't setup calculations because input numbers were too large: the log of the resulting input set size D must not exceed 1000 digits.")

                    if(params.isJson)
                        return solveJson(params, isCLI)
                    else
                        return solveText(params, isCLI)
                }
                catch(e: Exception) {
                    if(isCLI) {
                        println("Failed due to: ${e}")
                        println("program terminated abnormally with exit code 1")
                        exitProcess(1)
                    }
                    else
                        throw e
                }
            }

            fun solveText(params: BirthdayProblemParameters, isCLI: Boolean): String {
                val res = mutableListOf<String>()
                val outputter: (s: String) -> Unit = { s -> if(isCLI) println(s); res.add(s); }
                var pPercent: BigDecimal

                // do the calculations based on mode
                if(params.p !== null) {
                    outputter(
                        BirthdayProblemTextFormatter.headerTextBirthdayProblemInv(
                            if (params.isBinary) params.dLog!! else params.d!!,
                            params.p,
                            params.pPercent!!,
                            params.isBinary,
                            params.prec
                        )
                    )
                    try {
                        if(params.dLog === null)
                            throw Exception("dLog was not successfully calculated and is needed for Taylor method.")
                        val (n, methodUsed) = BirthdayProblemSolverChecked.birthdayProblemInv(
                            params.d,
                            params.dLog,
                            params.p,
                            params.isBinary
                        )
                        outputter(
                            BirthdayProblemTextFormatter.indented(
                                BirthdayProblemTextFormatter.resultTextBirthdayProblemInv(
                                    n,
                                    params.isBinary,
                                    methodUsed,
                                    params.prec
                                )
                            )
                        )
                    }
                    catch(e: Exception) {
                        outputter(BirthdayProblemTextFormatter.indented("N/A (Calculation failed)"))
                    }
                }
                else {
                    outputter(
                        BirthdayProblemTextFormatter.headerTextBirthdayProblem(
                            if (params.isBinary) params.dLog!! else params.d!!,
                            if (params.isBinary) params.nLog!! else params.n!!,
                            params.isBinary,
                            params.prec
                        )
                    )
                    var lastMethodUsed: CalcPrecision? = null
                    val results = mutableListOf<Triple<String, String, String>>()
                    listOf(
                        Pair(CalcPrecision.EXACT, params.isExact),
                        Pair(CalcPrecision.STIRLING_APPROX, params.isStirling),
                        Pair(CalcPrecision.TAYLOR_APPROX, params.isTaylor)
                    ).forEach {
                        (method, included) ->
                            if((included || params.isAll) && lastMethodUsed !== CalcPrecision.TRIVIAL) {
                                try {
                                    if(params.nLog === null || params.dLog === null)
                                        throw Exception("dLog or nLog was not successfully calculated and are both needed for ${
                                            BirthdayProblemTextFormatter.methodToText(
                                                method
                                            )
                                        } method.")
                                    val (p, methodUsed) =
                                        BirthdayProblemSolverChecked.birthdayProblem(
                                            params.d,
                                            params.dLog,
                                            params.n,
                                            params.nLog,
                                            method,
                                            params.isBinary
                                        )
                                    lastMethodUsed = methodUsed
                                    pPercent = DecimalFns.toPercent(p)
                                    results.add(
                                        BirthdayProblemTextFormatter.resultTextBirthdayProblem(
                                            p,
                                            pPercent,
                                            methodUsed,
                                            params.prec
                                        )
                                    )
                                }
                                catch(e: Exception) {
                                    results.add(Triple("N/A", "",  " (Calculation failed with this method" + BirthdayProblemTextFormatter.parenthesize(
                                        BirthdayProblemTextFormatter.methodToShortDescription(method)
                                    ) + ")"))
                                }
                            }
                    }
                    val (maxLenRes, maxLenLog10Repr) =
                        results
                            .map{ (res, log10ReprRes, _) -> Pair(res.length, log10ReprRes.length) }
                            .unzip()
                            .toList()
                            .map{ it.maxOrNull() }
                    results.forEach {
                        (resText, log10Repr, methodText) ->
                            outputter(
                                BirthdayProblemTextFormatter.indented(
                                    resText.padEnd(
                                        maxLenRes!!,
                                        ' '
                                    ) + log10Repr.padEnd(maxLenLog10Repr!!, ' ') + methodText
                                )
                            )
                    }
                }
                return res.joinToString("\n")
            }

            fun solveJson(params: BirthdayProblemParameters, isCLI: Boolean): String {
                val result = BirthdayProblemResult()
                var pPercent: BigDecimal

                // do the calculations based on mode
                if(params.p !== null) {
                    val (dText, pText) = BirthdayProblemTextFormatter.headerTextBirthdayProblemInvNumbers(
                        if (params.isBinary) params.dLog!! else params.d!!,
                        params.p,
                        params.pPercent!!,
                        params.isBinary,
                        params.prec
                    )
                    result.d = dText
                    result.p = pText
                    try {
                        if(params.dLog === null)
                            throw Exception("dLog was not successfully calculated and is needed for Taylor method.")
                        val (n, methodUsed) = BirthdayProblemSolverChecked.birthdayProblemInv(
                            params.d,
                            params.dLog,
                            params.p,
                            params.isBinary
                        )
                        val nText = BirthdayProblemTextFormatter.resultTextBirthdayProblemInvNumbers(n, params.isBinary, params.prec)
                        result.results[BirthdayProblemTextFormatter.methodToText(methodUsed).toLowerCase()] = nText
                    }
                    catch(e: Exception) {
                        result.results[BirthdayProblemTextFormatter.methodToText(CalcPrecision.TAYLOR_APPROX).toLowerCase()] = "N/A (Calculation failed)"
                    }
                }
                else {
                    val (dText, nText) = BirthdayProblemTextFormatter.headerTextBirthdayProblemNumbers(
                        if (params.isBinary) params.dLog!! else params.d!!,
                        if (params.isBinary) params.nLog!! else params.n!!,
                        params.isBinary,
                        params.prec
                    )
                    result.d = dText
                    result.n = nText
                    var lastMethodUsed: CalcPrecision? = null
                    listOf(
                        Pair(CalcPrecision.EXACT, params.isExact),
                        Pair(CalcPrecision.STIRLING_APPROX, params.isStirling),
                        Pair(CalcPrecision.TAYLOR_APPROX, params.isTaylor)
                    ).forEach {
                            (method, included) ->
                        if((included || params.isAll) && lastMethodUsed !== CalcPrecision.TRIVIAL) {
                            try {
                                if(params.nLog === null || params.dLog === null)
                                    throw Exception("dLog or nLog was not successfully calculated and are both needed for ${
                                        BirthdayProblemTextFormatter.methodToText(
                                            method
                                        )
                                    } method.")
                                val (p, methodUsed) =
                                    BirthdayProblemSolverChecked.birthdayProblem(
                                        params.d,
                                        params.dLog,
                                        params.n,
                                        params.nLog,
                                        method,
                                        params.isBinary
                                    )
                                lastMethodUsed = methodUsed
                                pPercent = DecimalFns.toPercent(p)
                                val pText = BirthdayProblemTextFormatter.resultTextBirthdayProblemNumbers(p, pPercent, params.prec).toList().joinToString("")
                                result.results[BirthdayProblemTextFormatter.methodToText(methodUsed).toLowerCase()] = pText
                            }
                            catch(e: Exception) {
                                result.results[BirthdayProblemTextFormatter.methodToText(method).toLowerCase()] = "N/A"
                            }
                        }
                    }
                }
                val res = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(result)
                if(isCLI)
                    println(res)
                return res
            }
        }

    }

    class Solver {

        companion object {

            fun solveForP(dOrDLog: BigDecimal, nOrNLog: BigDecimal, isBinary: Boolean, isCombinations: Boolean, method: CalcPrecision) =
                BirthdayProblemSolver.solveForP(dOrDLog, nOrNLog, isBinary, isCombinations, method)

            fun solveForN(dOrDLog: BigDecimal, p: BigDecimal, isBinary: Boolean, isCombinations: Boolean) =
                BirthdayProblemSolver.solveForN(dOrDLog, p, isBinary, isCombinations)

        }

    }
    class CLISolver {

        companion object {

            @JvmStatic
            fun main(args: Array<String>) {
                solve(args, true)
            }

            fun solve(args: Array<String>) =
                solve(args, false)

            private fun solve(args: Array<String>, isCLI: Boolean) =
                BirthdayProblemCLISolver.solve(args, isCLI)

        }

    }

    data class BirthdayProblemResult(
        var d: String? = null,
        var n: String? = null,
        var p: String? = null,
        val results: MutableMap<String, String> = mutableMapOf()
    )

    private data class BirthdayProblemInputParameters(
        val dOrDLog: String,
        val nOrNLog: String?,
        val p: String?,
        val isBinary: Boolean,
        val isCombinations: Boolean,
        val isStirling: Boolean,
        val isTaylor: Boolean,
        val isExact: Boolean,
        val isAll: Boolean,
        val isJson: Boolean,
        val prec: Int
    )

    private data class BirthdayProblemParameters(
        val d: BigDecimal?,
        val dLog: BigDecimal?,
        val n: BigDecimal?,
        val nLog: BigDecimal?,
        val p: BigDecimal?,
        val pPercent: BigDecimal?,
        val isBinary: Boolean,
        val isStirling: Boolean,
        val isTaylor: Boolean,
        val isExact: Boolean,
        val isAll: Boolean,
        val isJson: Boolean,
        val prec: Int
    )

}