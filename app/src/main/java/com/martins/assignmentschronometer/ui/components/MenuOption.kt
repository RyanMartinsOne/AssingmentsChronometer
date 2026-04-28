package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MenuOption(
    text: String,
    iconRes: Int,
    progress: Float, // O parâmetro deve estar aqui
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        contentColor = MaterialTheme.colorScheme.primary,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .graphicsLayer {
                alpha = progress
                clip = true
                shape = GenericShape { size, _ ->
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            center = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            radius = size.width * 2f * progress
                        )
                    )
                }
                // Movimento sutil de baixo/direita para cima/esquerda
                translationX = 40f * (1f - progress)
                translationY = 40f * (1f - progress)
            },
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(painterResource(iconRes), null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}