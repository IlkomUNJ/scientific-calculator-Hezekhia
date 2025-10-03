import kotlin.math.*

sealed class CalculatorAction {
    data class Number(val number: Int): CalculatorAction()
    object Clear : CalculatorAction()
    object AllClear : CalculatorAction() // AC
    object ParenthesisOpen : CalculatorAction()
    object ParenthesisClose : CalculatorAction()
    object ToggleSign : CalculatorAction()
    object Percentage : CalculatorAction()
    object Decimal : CalculatorAction()
    object Calculate : CalculatorAction()
    data class Operation(val operator: Char) : CalculatorAction()

    object Pi : CalculatorAction()
    object E_Const : CalculatorAction()
    object SquareRoot : CalculatorAction()
    object CubeRoot : CalculatorAction()
    object Square : CalculatorAction()
    object Cube : CalculatorAction()
    object ExpE : CalculatorAction()
    object Exp10 : CalculatorAction()
    object Factorial : CalculatorAction()
    object Reciprocal : CalculatorAction()

    object PowerOf : CalculatorAction()
    object YRootX : CalculatorAction()

    object Log : CalculatorAction()
    object Ln : CalculatorAction()
    data class Sin(val inverse: Boolean): CalculatorAction()
    data class Cos(val inverse: Boolean): CalculatorAction()
    data class Tan(val inverse: Boolean): CalculatorAction()

    object ToggleMode : CalculatorAction()
    companion object {
        val Exponent: CalculatorAction
            get() {
                TODO()
            }
        val Inverse: CalculatorAction
            get() {
                TODO()
            }
    }
}
