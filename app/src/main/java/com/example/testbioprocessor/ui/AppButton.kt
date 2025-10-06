package com.example.testbioprocessor.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Кастомная кнопка в едином стиле
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String,
    icon: ImageVector? = null,
    buttonType: AppButtonType = AppButtonType.PRIMARY
) {
    val colors = when (buttonType) {
        AppButtonType.PRIMARY -> ButtonColors(
            containerColor = Blue80,
            contentColor = White,
            disabledContainerColor = Blue40,
            disabledContentColor = White.copy(alpha = 0.5f)
        )
        AppButtonType.SECONDARY -> ButtonColors(
            containerColor = Color.Transparent,
            contentColor = Blue80,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Blue40
        )
        AppButtonType.TONAL -> ButtonColors(
            containerColor = Blue40,
            contentColor = Blue80,
            disabledContainerColor = Blue20,
            disabledContentColor = Blue40.copy(alpha = 0.5f)
        )
    }

    val elevation = when (buttonType) {
        AppButtonType.PRIMARY -> ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
        AppButtonType.SECONDARY -> null
        AppButtonType.TONAL -> ButtonDefaults.filledTonalButtonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        )
    }

    if (buttonType == AppButtonType.SECONDARY) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(50.dp),
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor,
                disabledContainerColor = colors.disabledContainerColor,
                disabledContentColor = colors.disabledContentColor
            ),
            border = BorderStroke(2.dp, Blue60)
        ) {
            ButtonContent(
                text = text,
                icon = icon,
                isLoading = isLoading,
                contentColor = colors.contentColor
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(56.dp),
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor,
                disabledContainerColor = colors.disabledContainerColor,
                disabledContentColor = colors.disabledContentColor
            ),
            elevation = elevation
        ) {
            ButtonContent(
                text = text,
                icon = icon,
                isLoading = isLoading,
                contentColor = colors.contentColor
            )
        }
    }
}

// Вспомогательный компонент для содержимого кнопки
@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    isLoading: Boolean,
    contentColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp
        )
    } else {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
        }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Text(
        text = if (isLoading) "Загрузка..." else text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = AppFonts.customFontFamily,
            lineHeight = 24.sp
        ),
        color = contentColor,
        fontWeight = FontWeight.Normal
    )
}

// Типы кнопок
enum class AppButtonType {
    PRIMARY,    // Основная кнопка - синяя
    SECONDARY,  // Вторичная кнопка - контурная
    TONAL       // Тональная кнопка - светлый фон
}

// Кастомные цвета для кнопок
data class ButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
)

// Цветовая палитра
val Blue80 = Color(0xFF1565C0)
val Blue60 = Color(0xFF42A5F5)
val Blue40 = Color(0xFF90CAF9)
val Blue20 = Color(0xFFE3F2FD)
val White = Color(0xFFFFFFFF)
val LightBlue = Color(0xFF03A9F4)