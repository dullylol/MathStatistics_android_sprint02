package com.asustuf.mssprint02.model

import kotlin.math.pow

class DispersionAnalysis(
    private val factorsValues: Array<Array<Array<Double>>>,
    private val significanceLevel: SignificanceLevel
) {

    init {
        if (factorsValues.isEmpty() || factorsValues[0].isEmpty()) {
            throw DispersionAnalysisException("Empty array of factors!")
        }
        var aSize = factorsValues[0][0].size

        for (b in factorsValues) {
            for (a in b) {
                if (aSize != a.size) {
                    throw DispersionAnalysisException("Factors counts in blocks are not equals!")
                }
            }
        }
    }

    val blockAverages by lazy {
        val blockAverages = Array(factorsValues.size) { Array(factorsValues[0].size) { 0.0 } }

        for (i in blockAverages.indices) {
            for (j in blockAverages[0].indices) {
                blockAverages[i][j] = factorsValues[i][j].average()
            }
        }

        blockAverages
    }

    val rowAverages by lazy {
        val rowAverages = Array(blockAverages.size) { 0.0 }

        for (i in rowAverages.indices) {
            rowAverages[i] = blockAverages[i].average()
        }

        rowAverages
    }

    val totalAverage by lazy {
        rowAverages.average()
    }

    val colsAverages by lazy {
        val colsAverages = Array(blockAverages[0].size) { 0.0 }

        for (i in colsAverages.indices) {
            for (j in blockAverages.indices) {
                colsAverages[i] += blockAverages[j][i]
            }
            colsAverages[i] /= blockAverages.size.toDouble()
        }

        colsAverages
    }

    val factorASize by lazy { factorsValues[0].size }

    val factorBSize by lazy { factorsValues.size }

    val totalValuesCount by lazy { factorASize * factorBSize * blockSize }

    val blockSize by lazy { factorsValues[0][0].size }

    val values by lazy {
        mapOf(
            Pair(
                "deviations_squares_sum", mutableMapOf(
                    Pair("factor_A", 0.0),
                    Pair("factor_B", 0.0),
                    Pair("factors_A_and_B", 0.0),
                    Pair("factors_A_and_B_influence", 0.0),
                    Pair("total_dispersion", 0.0)
                )
            ),
            Pair(
                "free_degrees", mutableMapOf(
                    Pair("factor_A", 0.0),
                    Pair("factor_B", 0.0),
                    Pair("factors_A_and_B", 0.0),
                    Pair("factors_A_and_B_influence", 0.0),
                    Pair("total_dispersion", 0.0)
                )
            ),
            Pair(
                "corrected_dispersions", mutableMapOf(
                    Pair("factor_A", 0.0),
                    Pair("factor_B", 0.0),
                    Pair("factors_A_and_B", 0.0),
                    Pair("factors_A_and_B_influence", 0.0),
                    Pair("total_dispersion", 0.0)
                )
            )
        )
    }

    val deviationsSquaresSum by lazy {
        var sumA = 0.0
        for (i in 0 until factorASize) {
            sumA += (colsAverages[i] - totalAverage).pow(2)
        }
        values["deviations_squares_sum"]!!["factor_A"] = factorBSize * blockSize * sumA


        var sumB = 0.0
        for (i in 0 until factorBSize) {
            sumB += (rowAverages[i] - totalAverage).pow(2)
        }
        values["deviations_squares_sum"]!!["factor_B"] = factorASize * blockSize * sumB


        var sumAB = 0.0
        for (i in 0 until factorBSize) {
            for (j in 0 until factorASize) {
                sumAB += (blockAverages[i][j] - colsAverages[j] - rowAverages[i] + totalAverage)
                    .pow(2)
            }
        }
        values["deviations_squares_sum"]!!["factors_A_and_B"] = blockSize * sumAB


        var sumABBlocks = 0.0
        var sumABBlockTotal = 0.0
        for (i in 0 until factorBSize) {
            for (j in 0 until factorASize) {
                for (k in 0 until blockSize) {
                    sumABBlocks += (factorsValues[i][j][k] - blockAverages[i][j]).pow(2)
                    sumABBlockTotal += (factorsValues[i][j][k] - totalAverage).pow(2)
                }
            }
        }
        values["deviations_squares_sum"]!!["factors_A_and_B_influence"] = sumABBlocks
        values["deviations_squares_sum"]!!["total_dispersion"] = sumABBlockTotal

        values["deviations_squares_sum"]!!
    }

    val freeDegrees by lazy {
        values["free_degrees"]!!["factor_A"] = factorASize - 1.0
        values["free_degrees"]!!["factor_B"] = factorBSize - 1.0
        values["free_degrees"]!!["factors_A_and_B"] =
            (factorASize - 1.0) * (factorBSize - 1.0)
        values["free_degrees"]!!["factors_A_and_B_influence"] =
            totalValuesCount.toDouble() - factorASize * factorBSize
        values["free_degrees"]!!["total_dispersion"] = totalValuesCount - 1.0

        values["free_degrees"]!!
    }

    val correctedDispersions by lazy {
        values["corrected_dispersions"]!!["factor_A"] =
            deviationsSquaresSum["factor_A"]!! / freeDegrees["factor_A"]!!

        values["corrected_dispersions"]!!["factor_B"] =
            deviationsSquaresSum["factor_B"]!! / freeDegrees["factor_B"]!!

        values["corrected_dispersions"]!!["factors_A_and_B"] =
            deviationsSquaresSum["factors_A_and_B"]!! / freeDegrees["factors_A_and_B"]!!

        values["corrected_dispersions"]!!["factors_A_and_B_influence"] =
            deviationsSquaresSum["factors_A_and_B_influence"]!! /
                    freeDegrees["factors_A_and_B_influence"]!!

        values["corrected_dispersions"]!!["total_dispersion"] =
            deviationsSquaresSum["total_dispersion"]!! / freeDegrees["total_dispersion"]!!

        values["corrected_dispersions"]!!
    }


    fun isFactorAInfluence(): Boolean {
        val fA = correctedDispersions["factor_A"]!! /
                correctedDispersions["factors_A_and_B_influence"]!!
        val criticalPoint = significanceLevel.criticalPoint(
            freeDegrees["factors_A_and_B_influence"]!!.toInt() - 1,
            freeDegrees["factor_A"]!!.toInt() - 1
        )
        return fA > criticalPoint
    }

    fun isFactorBInfluence(): Boolean {
        val fB = correctedDispersions["factor_B"]!! /
                correctedDispersions["factors_A_and_B_influence"]!!
        val criticalPoint = significanceLevel.criticalPoint(
            freeDegrees["factors_A_and_B_influence"]!!.toInt() - 1,
            freeDegrees["factor_B"]!!.toInt() - 1
        )
        return fB > criticalPoint
    }

    fun isFactorsABInfluence(): Boolean {
        val fAB = correctedDispersions["factors_A_and_B"]!! /
                correctedDispersions["factors_A_and_B_influence"]!!
        val criticalPoint = significanceLevel.criticalPoint(
            freeDegrees["factors_A_and_B_influence"]!!.toInt() - 1,
            freeDegrees["factors_A_and_B"]!!.toInt() - 1
        )
        return fAB > criticalPoint
    }


    class DispersionAnalysisException(message: String) : Exception(message)

    enum class SignificanceLevel {
        A001, A005;

        private val phisherCriticalPoints001 by lazy {
            arrayOf(
                //               1       2     3       4     5    6   7     8     9     10    11   12   24    ...
                /*1*/	arrayOf(4052.0, 4999.0, 5403.0, 5625.0, 5764.0, 5889.0, 5928.0, 5981.0, 6022.0, 6056.0, 6082.0, 6106.0, 6234.0, 6366.0),
                /*2*/	arrayOf(98.49, 99.01, 90.17, 99.25, 99.33, 99.30, 99.34, 99.36, 99.36, 99.40, 99.41, 99.42, 99.5, 99.5),
                /*3*/	arrayOf(34.12, 30.81, 29.46, 28.71, 28.24, 27.91, 27.67, 27.49, 27.34, 27.23, 27.13, 27.05, 26.6, 26.1),
                /*4*/	arrayOf(21.20, 18.00, 16.69, 15.98, 15.52, 15.21, 14.98, 14.80, 14.66, 14.54, 14.45, 14.37, 13.9, 13.5),
                /*5*/	arrayOf(16.26, 13.27, 12.06, 11.39, 10.97, 10.67, 10.45, 10.27, 10.15, 10.05, 9.96, 9.89, 9.5, 9.0),
                /*6*/   arrayOf(13.74, 10.92, 9.78, 9.15, 8.75, 8.47, 8.26, 8.10, 7.98, 7.87, 7.79, 7.72, 7.3, 6.9),
                /*7*/   arrayOf(12.25, 9.55, 8.45, 7.85, 7.46, 7.19, 7.00, 6.84, 6.71, 6.62, 6.54, 6.47, 6.1, 5.7),
                /*8*/	arrayOf(11.26, 8.65, 7.59, 7.01, 6.63, .37, 6.19, 6.03, 5.91, 5.82, 5.74, 5.67, 5.3, 4.9),
                /*9*/	arrayOf(10.56, 8.02, 6.99, 6.42, 6.06, 5.8, 5.62, 5.47, 5.35, 5.26, 5.18, 5.11, 4.7, 4.3),
                /*10*/	arrayOf(10.04, 7.56, 6.55, 5.99, 5.64, 5.39, 5.21, 5.06, 4.95, 4.85, 4.78, 4.71, 4.3, 3.9),
                /*11*/	arrayOf(9.86, 7.2, 6.22, 5.67, 5.32, 5.07, 4.88, 4.74, 4.63, 4.54, 4.46, 4.4, 4.0, 3.6),
                /*12*/	arrayOf(9.33, 6.93, 5.95, 5.41, 5.06, 4.82, 4.65, 4.50, 4.39, 4.30, 4.22, 4.16, 3.8, 3.4),
                /*13*/	arrayOf(9.07, 6.7, 5.74, 5.20, 4.86, 4.62, 4.44, 4.30, 4.19, 4.10, 4.02, 3.96, 3.6, 3.2),
                /*14*/	arrayOf(8.86, 6.51, 5.56, 5.03, 4.69, 4.46, 4.28, 4.14, 4.03, 3.94, 3.86, 3.80, 3.4, 3.0),
                /*15*/	arrayOf(8.68, 6.36, 5.42, 4.89, 4.56, 4.32, 4.14, 4.00, 3.89, 3.80, 3.73, 3.67, 3.3, 2.9),
                /*16*/	arrayOf(8.53, 6.23, 5.29, 4.77, 4.44, 4.2, 4.03, 3.89, 3.78, 3.69, 3.61, 3.55, 3.2, 2.8),
                /*17*/	arrayOf(8.4, 6.11, 5.18, 4.67, 4.34, 4.1, 3.93, 3.79, 3.68, 3.59, 3.52, 3.45, 3.1, 2.7),
                /*18*/	arrayOf(8.3, 6.0, 5.1, 4.6, 4.3, 4.0, 3.7, 3.4, 3.1, 2.6),
                /*19*/	arrayOf(8.2, 5.9, 5.0, 4.5, 4.2, 3.9, 3.6, 3.3, 2.9, 2.4),
                /*20*/	arrayOf(8.1, 5.9, 4.9, 4.4, 4.1, 3.9, 3.6, 3.2, 2.9, 2.4),
                /*22*/	arrayOf(7.9, 5.7, 4.8, 4.3, 4.0, 3.8, 3.5, 3.1, 2.8, 2.3),
                /*24*/	arrayOf(7.8, 5.6, 4.7, 4.2, 3.9, 3.7, 3.3, 3.0, 2.7, 2.2),
                /*26*/	arrayOf(7.7, 5.5, 4.6, 4.1, 3.8, 3.6, 3.3, 3.0, 2.6, 2.1),
                /*28*/	arrayOf(7.6, 5.5, 4.6, 4.1, 3.8, 3.5, 3.2, 2.9, 2.5, 2.1),
                /*30*/	arrayOf(7.6, 5.4, 4.5, 4.0, 3.7, 3.5, 3.2, 2.8, 2.5, 2.0),
                /*40*/	arrayOf(7.3, 5.2, 4.3, 3.8, 3.5, 3.3, 3.0, 2.7, 2.3, 1.8),
                /*60*/	arrayOf(7.1, 5.0, 4.1, 3.7, 3.3, 3.1, 2.8, 2.5, 2.1, 1.6),
                /*120*/	arrayOf(6.9, 4.8, 4.0, 3.5, 3.2, 3.0, 2.7, 2.3, 2.0, 1.4),
                /*...*/	arrayOf(6.6, 4.6, 3.8, 3.3, 3.0, 2.8, 2.5, 2.2, 1.8, 1.0),
            )
        }

        private val phisherCriticalPoints005 by lazy {
            arrayOf(
                //               1      2    3     4     5     6     8    12    24    ...
                /*1 */ arrayOf(161.45, 199.50, 215.72, 224.57, 230.17, 233.97, 238.89, 243.91, 249.04, 234.52),
                /*2 */ arrayOf(18.51, 19.00, 19.16, 19.25, 19.30, 19.33, 19.37, 19.41, 19.45, 19.50),
                /*3 */ arrayOf(10.13, 9.55, 9.28, 9.12, 9.01, 8.94, 8.84, 8.74, 8.64, 8.53),
                /*4 */ arrayOf(7.71, 6.94, 6.59, 6.39, 6.26, 6.16, 6.04, 5.91, 5.77, 5.63),
                /*5 */ arrayOf(6.61, 5.79, 5.41, 5.19, 5.05, 4.95, 4.82, 4.68, 4.53, 4.36),
                /*6 */ arrayOf(5.99, 5.14, 4.76, 4.53, 4.39, 4.28, 4.15, 4.00, 3.84, 3.67),
                /*7 */ arrayOf(5.59, 4.74, 4.35, 4.12, 3.97, 3.87, 3.73, 3.57, 3.41, 3.23),
                /*8 */ arrayOf(5.32, 4.46, 4.07, 3.84, 3.69, 3.58, 3.44, 3.28, 3.12, 2.93),
                /*9 */ arrayOf(5.12, 4.26, 3.86, 3.63, 3.48, 3.37, 3.23, 3.07, 2.90, 2.71),
                /*10*/ arrayOf(4.96, 4.10, 3.71, 3.48, 3.33, 3.22, 3.07, 2.91, 2.74, 2.54),
                /*11*/ arrayOf(4.84, 3.98, 3.59, 3.36, 3.20, 3.09, 2.95, 2.79, 2.61, 2.40),
                /*12*/ arrayOf(4.75, 3.88, 3.49, 3.26, 3.11, 3.00, 2.85, 2.69, 2.50, 2.30),
                /*13*/ arrayOf(4.67, 3.80, 3.41, 3.18, 3.02, 2.92, 2.77, 2.60, 2.42, 2.21),
                /*14*/ arrayOf(4.60, 3.74, 3.34, 3.11, 2.96, 2.85, 2.70, 2.53, 2.35, 2.13),
                /*15*/ arrayOf(4.54, 3.68, 3.29, 3.06, 2.90, 2.79, 2.64, 2.48, 2.29, 2.07),
                /*16*/ arrayOf(4.49, 3.63, 3.24, 3.01, 2.85, 2.74, 2.59, 2.42, 2.24, 2.01),
                /*17*/ arrayOf(4.45, 3.59, 3.20, 2.96, 2.81, 2.70, 2.55, 2.38, 2.19, 1.96),
                /*18*/ arrayOf(4.41, 3.55, 3.16, 2.93, 2.77, 2.66, 2.51, 2.34, 2.15, 1.92),
                /*19*/ arrayOf(4.38, 3.52, 3.13, 2.90, 2.74, 2.63, 2.48, 2.31, 2.11, 1.88),
                /*20*/ arrayOf(4.35, 3.49, 3.10, 2.87, 2.71, 2.60, 2.45, 2.28, 2.08, 1.84),
                /*21*/ arrayOf(4.32, 3.47, 3.07, 2.84, 2.68, 2.57, 2.42, 2.25, 2.05, 1.81),
                /*22*/ arrayOf(4.30, 3.44, 3.05, 2.82, 2.66, 2.55, 2.40, 2.23, 2.03, 1.78),
                /*23*/ arrayOf(4.28, 3.42, 3.03, 2.80, 2.64, 2.53, 2.38, 2.20, 2.00, 1.76),
                /*24*/ arrayOf(4.26, 3.40, 3.01, 2.78, 2.62, 2.51, 2.36, 2.18, 1.98, 1.73),
                /*25*/ arrayOf(4.24, 3.38, 2.99, 2.76, 2.60, 2.49, 2.34, 2.16, 1.96, 1.71),
                /*26*/ arrayOf(4.22, 3.37, 2.98, 2.74, 2.59, 2.47, 2.32, 2.15, 1.95, 1.69),
                /*27*/ arrayOf(4.21, 3.35, 2.96, 2.73, 2.57, 2.46, 2.30, 2.13, 1.93, 1.67),
                /*28*/ arrayOf(4.20, 3.34, 2.95, 2.71, 2.56, 2.44, 2.29, 2.12, 1.91, 1.65),
                /*29*/ arrayOf(4.18, 3.33, 2.93, 2.70, 2.54, 2.43, 2.28, 2.10, 1.90, 1.64),
                /*30*/ arrayOf(4.17, 3.32, 2.92, 2.69, 2.53, 2.42, 2.27, 2.09, 1.89, 1.62),
                /*35*/ arrayOf(4.12, 3.26, 2.87, 2.64, 2.48, 2.37, 2.22, 2.04, 1.83, 1.57),
                /*40*/ arrayOf(4.08, 3.23, 2.84, 2.61, 2.45, 2.34, 2.18, 2.00, 1.79, 1.51),
                /*45*/ arrayOf(4.06, 3.21, 2.81, 2.58, 2.42, 2.31, 2.15, 1.97, 1.76, 1.48),
                /*50*/ arrayOf(4.03, 3.18, 2.79, 2.56, 2.40, 2.29, 2.13, 1.95, 1.74, 1.44),
                /*60*/ arrayOf(4.00, 3.15, 2.76, 2.52, 2.37, 2.25, 2.10, 1.92, 1.70, 1.39),
                /*70*/ arrayOf(3.98, 3.13, 2.74, 2.50, 2.35, 2.23, 2.07, 1.89, 1.67, 1.35),
                /*80*/ arrayOf(3.96, 3.11, 2.72, 2.49, 2.33, 2.21, 2.06, 1.88, 1.65, 1.31),
                /*90*/ arrayOf(3.95, 3.10, 2.71, 2.47, 2.32, 2.20, 2.04, 1.86, 1.65, 1.31),
                /*100*/ arrayOf(3.94, 3.09, 2.70, 2.46, 2.30, 2.19, 2.03, 1.85, 1.63, 1.26),
                /*125*/ arrayOf(3.92, 3.07, 2.68, 2.44, 2.29, 2.17, 2.01, 1.83, 1.60, 1.21),
                /*150*/ arrayOf(3.90, 3.06, 2.66, 2.43, 2.27, 2.16, 2.00, 1.82, 1.59, 1.18),
                /*200*/ arrayOf(3.89, 3.04, 2.65, 2.42, 2.26, 2.14, 1.98, 1.80, 1.57, 1.14),
                /*300*/ arrayOf(3.87, 3.03, 2.64, 2.41, 2.25, 2.13, 1.97, 1.79, 1.55, 1.10),
                /*400*/ arrayOf(3.86, 3.02, 2.63, 2.40, 2.24, 2.12, 1.96, 1.78, 1.54, 1.07),
                /*500*/ arrayOf(3.86, 3.01, 2.62, 2.39, 2.23, 2.11, 1.96, 1.77, 1.54, 1.06),
                /*1000*/ arrayOf(3.85, 3.00, 2.61, 2.38, 2.22, 2.10, 1.95, 1.76, 1.53, 1.03),
                /*...*/ arrayOf(3.84, 2.99, 2.60, 2.37, 2.21, 2.09, 1.94, 1.75, 1.52, 1.00)
            )
        }

        fun criticalPoint(_k1: Int, _k2: Int): Double {
            return when (this) {
                A001 -> {
                    val k1 = when {
                        (_k1 < 0) -> 1
                        (_k1 in 20..21) -> 20
                        (_k1 in 22..23) -> 21
                        (_k1 in 24..25) -> 22
                        (_k1 in 26..27) -> 23
                        (_k1 in 28..29) -> 24
                        (_k1 in 30..35) -> 25
                        (_k1 in 36..50) -> 26
                        (_k1 in 51..90) -> 27
                        (_k1 in 91..150) -> 28
                        (_k1 > 150) -> 29
                        else -> _k1
                    }

                    val k2 = when {
                        (_k2 < 0) -> 1
                        (_k2 in 13..18) -> 12
                        (_k2 in 19..30) -> 13
                        (_k2 > 30) -> 14
                        else -> _k2
                    }

                    phisherCriticalPoints001[k1 - 1][k2 - 1]
                }
                A005 -> {
                    val k1 = when {
                        (_k1 < 0) -> 1
                        (_k1 in 31..33) -> 30
                        (_k1 in 34..38) -> 31
                        (_k1 in 39..43) -> 32
                        (_k1 in 44..48) -> 33
                        (_k1 in 49..55) -> 34
                        (_k1 in 56..65) -> 35
                        (_k1 in 66..75) -> 36
                        (_k1 in 76..85) -> 37
                        (_k1 in 86..95) -> 38
                        (_k1 in 96..113) -> 39
                        (_k1 in 114..138) -> 40
                        (_k1 in 139..175) -> 41
                        (_k1 in 176..250) -> 42
                        (_k1 in 251..350) -> 43
                        (_k1 in 351..450) -> 44
                        (_k1 in 451..750) -> 45
                        (_k1 in 751..1500) -> 46
                        (_k1 > 1500) -> 47
                        else -> _k1
                    }

                    val k2 = when {
                        (_k2 < 0) -> 1
                        (_k2 == 7) -> 6
                        (_k2 in 8..10) -> 7
                        (_k2 in 11..18) -> 8
                        (_k2 in 19..30) -> 9
                        (_k2 > 30) -> 10
                        else -> _k2
                    }

                    phisherCriticalPoints005[k1 - 1][k2 - 1]
                }
            }
        }

        fun value() = when (this) {
            A001 -> 0.01
            A005 -> 0.05
        }
    }
}