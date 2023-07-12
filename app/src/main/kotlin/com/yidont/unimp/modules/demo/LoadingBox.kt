package com.yidont.unimp.modules.demo

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.tan

@Composable
fun LoadingBox(hasBg: Boolean = true, msg: String? = null) {
    Box(
        Modifier
            .fillMaxSize()
            .background(if (hasBg) MaterialTheme.colorScheme.background else Color.Transparent)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(128.dp),
            shape = RoundedCornerShape(16.dp),
            color = Black1
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingBar()
                Text(
                    text = msg ?: "加载中",
                    Modifier.padding(top = 16.dp),
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

val Black1 = Color(0xFF4C4C4C)

@Composable
private fun LoadingBar() {
    val infinite = rememberInfiniteTransition()
    val angle by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(800, easing = LinearEasing))
    )
    Spacer(
        modifier = Modifier
            .size(42.dp)
            .rotate(angle)
            .drawBehind {
                val brush =
                    Brush.sweepGradient(listOf(Color.Transparent, Color.White))
                val height = size.width / 2.0
                val width = 2.dp.toPx()
                val tan = tan(width / height)
                val offset = Math
                    .toDegrees(tan)
                    .toFloat()
                drawArc(
                    brush, offset, 360 - offset * 2, false,
                    style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
    )
}