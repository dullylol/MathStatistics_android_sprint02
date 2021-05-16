package com.asustuf.mssprint02

import com.asustuf.mssprint02.model.DispersionAnalysis
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private val factorsValues = arrayOf(
        arrayOf(
            arrayOf(40.2, 40.8, 38.2, 39.6, 42.4, 44.5, 40.1, 38.8, 40.575, 40.575),
            arrayOf(42.5, 43.4, 44.5, 46.4, 40.1, 36.5, 40.3, 41.8, 38.0, 43.5),
            arrayOf(49.5, 50.2, 48.4, 50.0, 52.5, 38.4, 49.8, 50.4, 51.8, 49.0)
        ),
        arrayOf(
            arrayOf(33.4, 36.5, 34.4, 40.2, 42.0, 30.2, 31.8, 35.5, 34.0, 41.8),
            arrayOf(31.6, 33.4, 38.4, 35.0, 38.9, 29.5, 28.4, 30.6, 32.9, 43.0),
            arrayOf(29.3, 35.6, 36.0, 26.8, 38.0, 28.5, 30.6, 40.2, 33.3, 298.3 / 9)
        )
    )

    private val exampleFactorsValues = arrayOf(
        arrayOf(
            arrayOf(3.6, 3.9, 4.1),
            arrayOf(2.9, 3.1, 3.0),
            arrayOf(2.7, 2.5, 2.9)
        ),
        arrayOf(
            arrayOf(4.2, 4.0, 4.1),
            arrayOf(3.3, 2.9, 3.2),
            arrayOf(3.7, 3.5, 3.6)
        ),
        arrayOf(
            arrayOf(3.8, 3.5, 3.6),
            arrayOf(3.6, 3.7, 3.5),
            arrayOf(3.2, 3.0, 3.4)
        ),
        arrayOf(
            arrayOf(3.4, 3.2, 3.2),
            arrayOf(3.4, 3.6, 3.5),
            arrayOf(3.6, 3.8, 3.7)
        ),
    )

    @Test
    fun dispersionAnalysisVariantTest() {
        val dispersionAnalysis =
            DispersionAnalysis(factorsValues, DispersionAnalysis.SignificanceLevel.A005)

        dispersionAnalysis(dispersionAnalysis)
    }

    @Test
    fun dispersionAnalysisExampleTest() {
        val dispersionAnalysis =
            DispersionAnalysis(exampleFactorsValues, DispersionAnalysis.SignificanceLevel.A005)

        dispersionAnalysis(dispersionAnalysis)
    }

    fun dispersionAnalysis(dispersionAnalysis: DispersionAnalysis) {
        println("Rows averages: ${dispersionAnalysis.rowAverages.toList()}")
        println("Total average: ${dispersionAnalysis.totalAverage}")
        println("Cols averages: ${dispersionAnalysis.colsAverages.toList()}")

        println("Deviations squares sum: ${dispersionAnalysis.deviationsSquaresSum}")
        println("Free degrees: ${dispersionAnalysis.freeDegrees}")
        println("Corrected dispersions: ${dispersionAnalysis.correctedDispersions}")

        println("A influence: ${dispersionAnalysis.isFactorAInfluence()}")
        println("B influence: ${dispersionAnalysis.isFactorBInfluence()}")
        println("AB influence: ${dispersionAnalysis.isFactorsABInfluence()}")
    }
}