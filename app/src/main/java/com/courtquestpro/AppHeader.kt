package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AppHeader(title: String, subtitle: String = "") {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF4B281D), Color(0xFF252127), Color(0xFF121315)
            )))
            .padding(start = 22.dp, end = 22.dp, top = 27.dp, bottom = 30.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFAD71), Color(0xFFFF783D)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SportsBasketball, null,
                    tint = Color(0xFF211713), modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("COURTQUEST", color = Color(0xFFF7F3EF), fontSize = 13.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 2.1.sp)
                Text("PERFORMANCE STUDIO", color = Color(0xFFFFA35C), fontSize = 9.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.clip(RoundedCornerShape(50))
                    .background(Color(0x553A2B29))
                    .border(1.dp, Color(0x66FFB080), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text("PLAYER LAB", color = Color(0xFFF7F3EF), fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.7.sp)
            }
        }
        Spacer(Modifier.height(30.dp))
        Box(Modifier.width(44.dp).height(3.dp).clip(CircleShape).background(Color(0xFFFFA35C)))
        Spacer(Modifier.height(13.dp))
        Text(title, color = Color(0xFFF7F3EF), fontSize = 34.sp,
            fontWeight = FontWeight.Black, lineHeight = 39.sp)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(11.dp))
            Text(subtitle, color = Color(0xFFB7ACA6), fontSize = 11.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 1.sp, lineHeight = 17.sp)
        }
    }
}

