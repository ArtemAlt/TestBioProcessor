package com.example.testbioprocessor.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Карточка услуги
@Composable
fun ServiceCard(
    service: ServiceItem,
    onServiceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !service.isInDevelopment && service.destination.isNotEmpty(),
                onClick = onServiceClick,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blue20,
            contentColor = Blue80
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (!service.isInDevelopment) BorderStroke(1.dp, Blue40) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Иконка услуги
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (service.isInDevelopment) Blue40.copy(alpha = 0.5f) else Blue40,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.title,
                    tint = if (service.isInDevelopment) Blue60 else Blue80,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Текстовая информация
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AppFonts.customFontFamily,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue80
                )

                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall.copy( // Меньший шрифт
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic, // Курсив
                        lineHeight = 16.sp
                    ),
                    color = Blue80.copy(alpha = 0.5f) // Более светлый цвет
                )

                if (service.isInDevelopment) {
                    Text(
                        text = "В разработке",
                        style = MaterialTheme.typography.labelSmall.copy( // Самый маленький шрифт
                            fontFamily = AppFonts.customFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic // Курсив
                        ),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Стрелка или иконка статуса
            if (service.isInDevelopment) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "В разработке",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Перейти",
                    tint = Blue80,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}