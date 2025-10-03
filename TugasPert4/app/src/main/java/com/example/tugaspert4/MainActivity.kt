import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview

val DisplayBlue = Color(0xFF4C7BAF)
val ScientificButtonColor = Color(0xFF6A9BCF)
val ButtonBlue = Color(0xFFC7D9ED)
val OperatorLight = Color(0xFFF0F0F0)
val TextDark = Color(0xFF333333)
val BackgroundColor = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<CalculatorViewModel>()
            CalculatorScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val state = viewModel.state
    val buttonSpacing = 4.dp

    val FINAL_BASIC_LAYOUT = listOf(
        listOf("7", "8", "9", "%", "AC"),
        listOf("4", "5", "6", "×", "÷"),
        listOf("1", "2", "3", "-", "+"),
        listOf("ModeToggle", "0", ".", "±", "=")
    )

    val ScientificLayout = listOf(
        listOf("sin", "cos", "tan", "(", ")"),

        listOf("sin⁻¹", "cos⁻¹", "tan⁻¹", "ln", "log"),

        listOf("x^y", "x³", "x²", "π", "e"),

        listOf("y√x", "3√x", "√x", "x!", "1/x"),

        FINAL_BASIC_LAYOUT[0],
        FINAL_BASIC_LAYOUT[1],
        FINAL_BASIC_LAYOUT[2],
        FINAL_BASIC_LAYOUT[3]
    )


    val layoutToDisplay = if (state.isScientificMode) ScientificLayout else FINAL_BASIC_LAYOUT

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(buttonSpacing)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .background(DisplayBlue, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = state.fullExpression,
                    fontSize = 32.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End
                )

                Text(
                    text = state.displayInput,
                    fontSize = 64.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(buttonSpacing))

        fun getButtonColors(symbol: String, isInverse: Boolean): Pair<Color, Color> {
            val scientificFunctions = listOf(
                "sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹", "π", "e",
                "x^y", "x³", "x²", "y√x", "3√x", "√x", "ln", "log", "x!", "1/x",
                "(", ")"
            )

            val operators = listOf("+", "-", "×", "÷", "=")
            val controlFunctions = listOf("%", "AC", "C", "±", "ModeToggle")
            val numbersAndDecimals = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")

            val isNumberOrDecimal = symbol in numbersAndDecimals
            val isScientificFunc = symbol in scientificFunctions
            val isOperator = symbol in operators
            val isControl = symbol in controlFunctions

            return when {
                isScientificFunc -> Pair(ScientificButtonColor, Color.White)

                isOperator || isControl -> Pair(ButtonBlue, TextDark)

                isNumberOrDecimal -> Pair(OperatorLight, TextDark)

                else -> Pair(OperatorLight, TextDark)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            layoutToDisplay.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    row.forEachIndexed { colIndex, symbol ->
                        val (bgColor, contentColor) = getButtonColors(symbol, state.isInverse)

                        val weight = 1f

                        val buttonModifier = Modifier
                            .weight(weight)
                            .aspectRatio(1.0f)

                        CalculatorButton(
                            symbol = symbol,
                            isInverse = state.isInverse,
                            isScientificMode = state.isScientificMode,
                            modifier = buttonModifier,
                            backgroundColor = bgColor,
                            contentColor = contentColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            handleAction(viewModel, symbol)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(buttonSpacing))
            }
        }
    }
}
fun handleAction(viewModel: CalculatorViewModel, symbol: String) {
    when (symbol) {
        "Sci", "Bas", "ModeToggle" -> viewModel.onAction(CalculatorAction.ToggleMode)

        "π" -> viewModel.onAction(CalculatorAction.Pi)
        "e" -> viewModel.onAction(CalculatorAction.E_Const)

        "x^y" -> viewModel.onAction(CalculatorAction.PowerOf)
        "y√x" -> viewModel.onAction(CalculatorAction.YRootX)

        "x³" -> viewModel.onAction(CalculatorAction.Cube)
        "x²" -> viewModel.onAction(CalculatorAction.Square)
        "eˣ" -> viewModel.onAction(CalculatorAction.ExpE)
        "10ˣ" -> viewModel.onAction(CalculatorAction.Exp10)
        "√x" -> viewModel.onAction(CalculatorAction.SquareRoot)
        "3√x" -> viewModel.onAction(CalculatorAction.CubeRoot)
        "ln" -> viewModel.onAction(CalculatorAction.Ln)
        "log" -> viewModel.onAction(CalculatorAction.Log)
        "1/x" -> viewModel.onAction(CalculatorAction.Reciprocal)
        "x!" -> viewModel.onAction(CalculatorAction.Factorial)
        "E" -> viewModel.onAction(CalculatorAction.Exponent)

        "F" -> println("Action F: Toggle secondary functions (Placeholder)")
        "MC" -> println("Action MC: Memory Clear (Placeholder)")
        "MR" -> println("Action MR: Memory Recall (Placeholder)")
        "M+" -> println("Action M+: Memory Add (Placeholder)")
        "M-" -> println("Action M-: Memory Subtract (Placeholder)")

        "Inv" -> viewModel.onAction(CalculatorAction.Inverse)
        "sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹" -> {
            val isInverse = symbol.endsWith("⁻¹") || viewModel.state.isInverse
            val baseSymbol = symbol.replace("⁻¹", "")
            when(baseSymbol) {
                "sin" -> viewModel.onAction(CalculatorAction.Sin(inverse = isInverse))
                "cos" -> viewModel.onAction(CalculatorAction.Cos(inverse = isInverse))
                "tan" -> viewModel.onAction(CalculatorAction.Tan(inverse = isInverse))
            }
        }

        "AC" -> viewModel.onAction(CalculatorAction.AllClear)
        "C" -> viewModel.onAction(CalculatorAction.Clear)
        "±" -> viewModel.onAction(CalculatorAction.ToggleSign)
        "." -> viewModel.onAction(CalculatorAction.Decimal)
        "=" -> viewModel.onAction(CalculatorAction.Calculate)
        "(" -> viewModel.onAction(CalculatorAction.ParenthesisOpen)
        ")$" -> viewModel.onAction(CalculatorAction.ParenthesisClose)
        "%" -> viewModel.onAction(CalculatorAction.Percentage)

        "+", "-", "×", "÷" -> {
            val op = when (symbol) {
                "×" -> '*'
                "÷" -> '/'
                else -> symbol.first()
            }
            viewModel.onAction(CalculatorAction.Operation(op))
        }
        else -> symbol.toIntOrNull()?.let { viewModel.onAction(CalculatorAction.Number(it)) }
    }
}
@Composable
fun CalculatorButton(
    symbol: String,
    isInverse: Boolean,
    isScientificMode: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    shape: RoundedCornerShape,
    isWide: Boolean = false,
    onClick: () -> Unit
) {
    val modeToggleText = if (symbol == "ModeToggle") {
        if (isScientificMode) "Bas" else "Sci"
    } else {
        symbol
    }

    val displayText = when (modeToggleText) {
        "x^y" -> "xʸ"
        "y√x" -> "ʸ√x"
        "x³" -> "x³"
        "x²" -> "x²"
        "eˣ" -> "eˣ"
        "10ˣ" -> "10ˣ"
        "3√x" -> "³√x"
        "√x" -> "√x"
        "1/x" -> "1/x"
        "x!" -> "x!"
        "±" -> "±"
        "E" -> "E"
        "sin" -> if (isInverse) "sin⁻¹" else "sin"
        "cos" -> if (isInverse) "cos⁻¹" else "cos"
        "tan" -> if (isInverse) "tan⁻¹" else "tan"
        else -> modeToggleText
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .border(0.5.dp, Color.LightGray, shape)
            .height(IntrinsicSize.Min),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            fontSize = 16.sp,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScientificCalculatorScreen() {
    CalculatorScreen(viewModel = viewModel<CalculatorViewModel>())
}
