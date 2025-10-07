package com.example.testbioprocessor.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testbioprocessor.ui.AppFonts
import com.example.testbioprocessor.ui.theme.Blue60
import com.example.testbioprocessor.ui.theme.Blue80
import com.example.testbioprocessor.ui.theme.White
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun AppBottomBar(model: BioViewModelNew) {
    val uiState by model.uiLoginState.collectAsStateWithLifecycle()
    val currentUser = uiState.login
    val vector = uiState.vectorSaved

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Blue80),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue80, Blue60)
                    )
                )
                .padding(horizontal = 32.dp), // Увеличил отступы по бокам
            horizontalArrangement = Arrangement.SpaceBetween, // Распределение между краями
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Блок пользователя
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Пользователь",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Пользователь",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = AppFonts.customFontFamily
                        ),
                        color = White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentUser,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Разделитель по центру
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(White.copy(alpha = 0.3f))
            )

            // Блок биовектора
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End // Выравнивание по правому краю
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Биовектор",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Биовектор",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = AppFonts.customFontFamily
                        ),
                        color = White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (vector.isSaved) vector.data else "Не зарегистрирован",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (vector.isSaved) White else White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}