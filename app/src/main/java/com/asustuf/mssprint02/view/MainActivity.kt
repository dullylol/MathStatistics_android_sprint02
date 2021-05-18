package com.asustuf.mssprint02.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.addTextChangedListener
import com.asustuf.mssprint02.R
import com.asustuf.mssprint02.databinding.ActivityMainBinding
import com.asustuf.mssprint02.model.DispersionAnalysis
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var factorsBlocks: Array<Array<String>>
    private lateinit var factorsValues: Array<Array<Array<Double>>>
    private var factorASize: Int = 0
    private var factorBSize: Int = 0
    private var factorsBlocksSize: Int = 0


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.defaultValuesBtn.setOnClickListener {
            binding.factorASize.setText("3")
            binding.factorBSize.setText("2")
            binding.factorsBlockSize.setText("10")
            binding.a005.isChecked = true
            binding.createTableBtn.callOnClick()

            (((binding.inputFactorsTable
                .getChildAt(1) as TableRow)
                .getChildAt(1) as FrameLayout)
                .getChildAt(0) as EditText)
                .setText("40.2, 40.8, 38.2, 39.6, 42.4, 44.5, 40.1, 38.8, 40.575, 40.575")
            (((binding.inputFactorsTable
                .getChildAt(1) as TableRow)
                .getChildAt(2) as FrameLayout)
                .getChildAt(0) as EditText)
                .setText("42.5, 43.4, 44.5, 46.4, 40.1, 36.5, 40.3, 41.8, 38.0, 43.5")
            (((binding.inputFactorsTable
                .getChildAt(1) as TableRow)
                .getChildAt(3) as FrameLayout)
                .getChildAt(0) as EditText)
                .setText("49.5, 50.2, 48.4, 50.0, 52.5, 38.4, 49.8, 50.4, 51.8, 49.0")

            (((binding.inputFactorsTable
                .getChildAt(2) as TableRow)
                .getChildAt(1) as FrameLayout)
                .getChildAt(0) as EditText)
                .setText("33.4, 36.5, 34.4, 40.2, 42.0, 30.2, 31.8, 35.5, 34.0, 41.8")
            (((binding.inputFactorsTable
                .getChildAt(2) as TableRow)
                .getChildAt(2) as FrameLayout)
                .getChildAt(0) as EditText)
                .setText("31.6, 33.4, 38.4, 35.0, 38.9, 29.5, 28.4, 30.6, 32.9, 43.0")
            (((binding.inputFactorsTable
                .getChildAt(2) as TableRow)
                .getChildAt(3) as FrameLayout)
                .getChildAt(0) as EditText)
                .setText("29.3, 35.6, 36.0, 26.8, 38.0, 28.5, 30.6, 40.2, 33.3, ${298.3 / 9}")
        }

        binding.createTableBtn.setOnClickListener {
            try {
                factorASize = binding.factorASize.text.toString().toInt()
                factorBSize = binding.factorBSize.text.toString().toInt()
                factorsBlocksSize = binding.factorsBlockSize.text.toString().toInt()
            } catch (e: Exception) {
                Toast.makeText(this, "Empty data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            factorsBlocks = Array(factorBSize) {
                Array(factorASize) {""}
            }
            factorsValues = Array(factorBSize) {
                Array(factorASize) {
                    Array(factorsBlocksSize)
                    { 0.0 }
                }
            }
            createTable(binding.inputFactorsTable, factorBSize, factorASize)
            binding.doDispersionAnalysisBtn.visibility = View.VISIBLE
        }

        binding.doDispersionAnalysisBtn.setOnClickListener {
            try {
                parseData()
                doDispersionAnalysis(if (binding.a001.isChecked) {
                    DispersionAnalysis.SignificanceLevel.A001
                }
                else {
                    DispersionAnalysis.SignificanceLevel.A005
                })
                binding.resultTable.visibility = View.VISIBLE
                binding.factorAResultCard.visibility = View.VISIBLE
                binding.factorBResultCard.visibility = View.VISIBLE
                binding.factorsAAndBResultCard.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                binding.resultTable.visibility = View.GONE
                binding.factorAResultCard.visibility = View.GONE
                binding.factorBResultCard.visibility = View.GONE
                binding.factorsAAndBResultCard.visibility = View.GONE
            }
        }

    }

    @Throws(Exception::class)
    private fun parseData() {
        val factorsValues = Array(factorBSize) {Array(factorASize) {Array(factorsBlocksSize) {0.0}}}
        for (i in 0 until factorBSize) {
            for (j in 0 until factorASize) {
                val factorsBlocks = this.factorsBlocks[i][j]
                    .replace("\\s+".toRegex(), "").split(",")

                if (factorsBlocks.size != factorsBlocksSize) {
                    throw Exception("Incorrect size in block [${i+1}; ${j+1}]")
                }

                for (k in factorsBlocks.indices) {
                    try {
                        factorsValues[i][j][k] = factorsBlocks[k].toDouble()
                    } catch (e: NumberFormatException) {
                        throw Exception("Incorrect input in block [${i+1}; ${j+1}], word: ${k+1}")
                    }
                }
            }
        }
        this.factorsValues = factorsValues
    }

    private fun doDispersionAnalysis(significanceLevel: DispersionAnalysis.SignificanceLevel) {
        val dispersionAnalysis = DispersionAnalysis(factorsValues, significanceLevel)

        // deviation squares sum
        binding.deviationSquaresSum1.text =
            dispersionAnalysis.deviationsSquaresSum["factor_A"].toString()
        binding.deviationSquaresSum2.text =
            dispersionAnalysis.deviationsSquaresSum["factor_B"].toString()
        binding.deviationSquaresSum3.text =
            dispersionAnalysis.deviationsSquaresSum["factors_A_and_B"].toString()
        binding.deviationSquaresSum4.text =
            dispersionAnalysis.deviationsSquaresSum["factors_random_influence"].toString()
        binding.deviationSquaresSumTotal.text =
            dispersionAnalysis.deviationsSquaresSum["total_dispersion"].toString()

        // free degrees
        binding.freeDegrees1.text =
            dispersionAnalysis.freeDegrees["factor_A"]?.toInt().toString()
        binding.freeDegrees2.text =
            dispersionAnalysis.freeDegrees["factor_B"]?.toInt().toString()
        binding.freeDegrees3.text =
            dispersionAnalysis.freeDegrees["factors_A_and_B"]?.toInt().toString()
        binding.freeDegrees4.text =
            dispersionAnalysis.freeDegrees["factors_random_influence"]?.toInt().toString()
        binding.freeDegreesTotal.text =
            dispersionAnalysis.freeDegrees["total_dispersion"]?.toInt().toString()

        // corrected dispersions
        binding.correctedDispersion1.text =
            dispersionAnalysis.correctedDispersions["factor_A"].toString()
        binding.correctedDispersion2.text =
            dispersionAnalysis.correctedDispersions["factor_B"].toString()
        binding.correctedDispersion3.text =
            dispersionAnalysis.correctedDispersions["factors_A_and_B"].toString()
        binding.correctedDispersion4.text =
            dispersionAnalysis.correctedDispersions["factors_random_influence"].toString()
        binding.correctedDispersionTotal.text =
            dispersionAnalysis.correctedDispersions["total_dispersion"].toString()

        //critical points
        binding.criticalPointFactorA.text =
                getString(
                    R.string.critical_point,
                    dispersionAnalysis.criticalPointA.toString()
                )
        binding.criticalPointFactorB.text =
            getString(
                R.string.critical_point,
                dispersionAnalysis.criticalPointB.toString()
            )
        binding.criticalPointFactorsAB.text =
            getString(
                R.string.critical_point,
                dispersionAnalysis.criticalPointAB.toString()
            )

        // observed criterion values
        binding.observedCriterionValueA.text =
                getString(
                    R.string.observed_criterion_value,
                    dispersionAnalysis.observedCriterionValueA.toString()
                )
        binding.observedCriterionValueB.text =
            getString(
                R.string.observed_criterion_value,
                dispersionAnalysis.observedCriterionValueB.toString()
            )
        binding.observedCriterionValueAB.text =
            getString(
                R.string.observed_criterion_value,
                dispersionAnalysis.observedCriterionValueAB.toString()
            )

        // hypothesis
        binding.hypothesisResultFactorA.text =
            getString(
                R.string.factor_a_influence,
                dispersionAnalysis.isFactorAInfluence().toString()
            )

        binding.hypothesisResultFactorB.text =
            getString(
                R.string.factor_b_influence,
                dispersionAnalysis.isFactorBInfluence().toString()
            )

        binding.hypothesisResultFactorsAB.text =
            getString(
                R.string.factors_a_and_b_influence,
                dispersionAnalysis.isFactorsABInfluence().toString()
            )
    }

    @SuppressLint("SetTextI18n")
    private fun createTable(factorsTable: TableLayout, rows: Int, cols: Int) {
        factorsTable.removeAllViewsInLayout()
        for (i in 0..rows) {
            val factorsRow = TableRow(this)

            for (j in 0..cols) {
                val frameLayout = FrameLayout(ContextThemeWrapper(this, R.style.Border))

                val factorsBlock = if (i == 0 && j == 0) {
                    View(this).also {
                        frameLayout.layoutParams = TableRow.LayoutParams(100, 100)
                    }
                }
                else if (i == 0) {
                    TextView(this).apply {
                        text = "A${j}"
                        gravity = Gravity.CENTER
                    }.also {
                        frameLayout.layoutParams = TableRow.LayoutParams(300, 100)
                    }
                } else if (j == 0) {
                    TextView(this).apply {
                        text = "B${i}"
                        gravity = Gravity.CENTER
                    }.also {
                        frameLayout.layoutParams = TableRow.LayoutParams(100, 300)
                    }
                } else {
                    EditText(this).apply {
                        isSingleLine = false
                        imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                        keyListener = DigitsKeyListener.getInstance("0123456789., ")
                        gravity = Gravity.CENTER
                        setEms(5)

                        addTextChangedListener {
                            factorsBlocks[i - 1][j - 1] = it.toString()
                        }
                    }.also {
                        frameLayout.layoutParams = TableRow.LayoutParams(300, 300)
                    }
                }

                frameLayout.addView(factorsBlock)
                factorsRow.addView(frameLayout)
            }

            factorsTable.addView(factorsRow)
        }

    }
}