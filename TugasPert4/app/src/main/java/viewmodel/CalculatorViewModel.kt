import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.*

data class CalculatorState(
    val displayInput: String = "0",
    val number1: Double? = null,
    val operation: Char? = null,
    val isAwaitingOperand: Boolean = false,
    val isScientificMode: Boolean = false,
    val isInverse: Boolean = false,
    val fullExpression: String = "",
    val memoryValue: Double = 0.0
)

class CalculatorViewModel : ViewModel() {

    var state by mutableStateOf(CalculatorState())
        private set

    private fun formatResult(result: Double): String {
        return if (result.isFinite()) {
            val s = result.toString()
            if (s.endsWith(".0")) s.substring(0, s.length - 2) else s
        } else {
            "Error"
        }
    }

    private fun calculateFactorial(n: Double): Double {
        if (n < 0 || n != floor(n)) return Double.NaN
        if (n == 0.0) return 1.0
        var result = 1.0
        for (i in 1..n.toInt()) {
            result *= i
        }
        return result
    }

    private fun performCalculationInternal(): Double? {
        val num1 = state.number1 ?: return null
        val num2 = state.displayInput.toDoubleOrNull() ?: return num1
        val operator = state.operation ?: return num2

        return when (operator) {
            '+' -> num1 + num2
            '-' -> num1 - num2
            '*' -> num1 * num2
            '/' -> num1 / num2
            '^' -> num1.pow(num2)
            'R' -> num2.pow(1.0 / num1)
            else -> null
        }
    }

    private fun handleAllClear() {
        state = CalculatorState(isScientificMode = state.isScientificMode)
    }

    private fun handleClear() {
        state = state.copy(displayInput = "0")
    }

    private fun handleDecimal() {
        if (!state.displayInput.contains(".")) {
            state = state.copy(displayInput = state.displayInput + ".")
        }
    }

    private fun handleNumber(number: Int) {
        val currentDisplay = state.displayInput

        val isNewExpressionStart = state.number1 == null && state.operation == null && !state.isAwaitingOperand && currentDisplay == "0"

        if (state.isAwaitingOperand) {
            state = state.copy(
                displayInput = number.toString(),
                isAwaitingOperand = false,
                fullExpression = state.fullExpression + number.toString()
            )
        } else {
            val newDisplay = if (currentDisplay == "0" || isNewExpressionStart) number.toString() else currentDisplay + number.toString()

            val newFullExpression = if (state.fullExpression.endsWith('(')) {
                state.fullExpression + newDisplay
            } else if (state.operation == null) {
                newDisplay
            } else {
                val expressionBase = state.fullExpression.dropLastWhile { it.isDigit() || it == '.' }
                expressionBase + newDisplay
            }

            state = state.copy(
                displayInput = newDisplay,
                fullExpression = newFullExpression
            )
        }
    }

    private fun handleOperation(newOperator: Char) {
        val currentDisplayValue = state.displayInput.toDoubleOrNull() ?: return
        val expressionSymbol = when(newOperator) {
            '*' -> '×'
            '/' -> '÷'
            'R' -> "ʸ√x"
            '^' -> "xʸ"
            else -> newOperator
        }

        if (state.number1 == null) {
            state = state.copy(
                number1 = currentDisplayValue,
                operation = newOperator,
                isAwaitingOperand = true,
                fullExpression = state.displayInput + expressionSymbol
            )
        } else {
            val result = performCalculationInternal()

            if (result != null) {
                val safeNum1 = state.number1 ?: return

                val opSymbol = when(state.operation) {
                    '*' -> '×';
                    '/' -> '÷';
                    'R' -> "ʸ√x";
                    '^' -> "xʸ"
                    else -> state.operation
                }
                val num2String = state.displayInput
                val currentExpressionBase = formatResult(safeNum1) + opSymbol + num2String

                state = state.copy(
                    number1 = result,
                    operation = newOperator,
                    displayInput = formatResult(result),
                    isAwaitingOperand = true,
                    fullExpression = currentExpressionBase + expressionSymbol
                )
            }
        }
    }

    private fun handleConstant(constant: Double, symbol: String) {
        state = state.copy(
            displayInput = formatResult(constant),
            isAwaitingOperand = true,
            fullExpression = symbol
        )
    }

    private fun handleUnaryOperation(action: CalculatorAction) {
        val value = state.displayInput.toDoubleOrNull() ?: return
        val result = when (action) {
            is CalculatorAction.Sin -> if (action.inverse) asin(value.degToRad()) else sin(value.degToRad())
            is CalculatorAction.Cos -> if (action.inverse) acos(value.degToRad()) else cos(value.degToRad())
            is CalculatorAction.Tan -> if (action.inverse) atan(value.degToRad()) else tan(value.degToRad())
            CalculatorAction.Log -> log10(value)
            CalculatorAction.Ln -> ln(value)
            CalculatorAction.SquareRoot -> sqrt(value)
            CalculatorAction.CubeRoot -> value.pow(1.0 / 3.0)
            CalculatorAction.Factorial -> calculateFactorial(value)
            CalculatorAction.Reciprocal -> 1.0 / value
            CalculatorAction.Square -> value * value
            CalculatorAction.Cube -> value.pow(3.0)
            CalculatorAction.ExpE -> E.pow(value)
            CalculatorAction.Exp10 -> 10.0.pow(value)
            CalculatorAction.ToggleSign -> -value
            CalculatorAction.Percentage -> value / 100.0
            else -> null
        }

        if (result != null) {
            val funcName = when (action) {
                is CalculatorAction.Sin -> if (action.inverse) "sin⁻¹" else "sin"
                is CalculatorAction.Cos -> if (action.inverse) "cos⁻¹" else "cos"
                is CalculatorAction.Tan -> if (action.inverse) "tan⁻¹" else "tan"
                else -> action::class.simpleName?.replace("Action", "")?.lowercase() ?: "func"
            }
            val newExpression = "$funcName(${formatResult(value)}) = ${formatResult(result)}"

            state = state.copy(
                displayInput = formatResult(result),
                isAwaitingOperand = true,
                fullExpression = newExpression
            )
        } else {
            state = state.copy(displayInput = "Error", fullExpression = "")
        }
    }

    private fun handleCalculate() {
        val num1 = state.number1 ?: return
        val operator = state.operation ?: return
        val num2 = state.displayInput.toDoubleOrNull() ?: return

        val result = performCalculationInternal()
        val num2String = formatResult(num2)

        if (result != null) {
            val opSymbol = when(operator) {
                '*' -> '×'
                '/' -> '÷'
                'R' -> "ʸ√x"
                '^' -> "xʸ"
                else -> operator
            }
            val currentExpression = formatResult(num1) + opSymbol + num2String

            state = state.copy(
                displayInput = formatResult(result),
                number1 = null,
                operation = null,
                isAwaitingOperand = true,
                fullExpression = currentExpression + "=" + formatResult(result)
            )
        } else {
            state = state.copy(displayInput = "Error", fullExpression = "")
        }
    }

    private fun handleParenthesisOpen() {
        state = state.copy(
            fullExpression = state.fullExpression + "(",
            isAwaitingOperand = false,
        )
    }

    private fun handleParenthesisClose() {
        state = state.copy(
            fullExpression = state.fullExpression + ")",
            isAwaitingOperand = true
        )
    }

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> handleNumber(action.number)
            is CalculatorAction.Operation -> handleOperation(action.operator)
            CalculatorAction.AllClear -> handleAllClear()
            CalculatorAction.Clear -> handleClear()
            CalculatorAction.Calculate -> handleCalculate()
            CalculatorAction.Decimal -> handleDecimal()
            CalculatorAction.ToggleMode -> state = state.copy(isScientificMode = !state.isScientificMode)

            CalculatorAction.ToggleSign, CalculatorAction.Percentage, CalculatorAction.Square,
            CalculatorAction.Cube, CalculatorAction.SquareRoot, CalculatorAction.CubeRoot,
            CalculatorAction.Log, CalculatorAction.Ln, CalculatorAction.Factorial,
            CalculatorAction.Reciprocal -> handleUnaryOperation(action)

            is CalculatorAction.Sin, is CalculatorAction.Cos, is CalculatorAction.Tan -> handleUnaryOperation(action)

            CalculatorAction.Pi -> handleConstant(PI, "π")
            CalculatorAction.E_Const -> handleConstant(E, "e")

            CalculatorAction.PowerOf -> handleOperation('^')
            CalculatorAction.YRootX -> handleOperation('R')

            CalculatorAction.ParenthesisOpen -> handleParenthesisOpen()
            CalculatorAction.ParenthesisClose -> handleParenthesisClose()

            else -> Unit
        }
    }
}

private fun Double.degToRad(): Double = this * PI / 180.0
