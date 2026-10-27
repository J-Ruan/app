package com.warmcycle.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable fun WarmTheme(mode: String, content: @Composable () -> Unit) {
    val dark = mode == "dark" || (mode == "system" && isSystemInDarkTheme())
    val colors = if (dark) darkColorScheme(primary = Color(0xFFF2AAC5), onPrimary = Color(0xFF472338),
        primaryContainer = Color(0xFF563144), onPrimaryContainer = Color(0xFFFFD9E7),
        background = Color(0xFF1D191C), surface = Color(0xFF292228), surfaceVariant = Color(0xFF44363F),
        secondaryContainer = Color(0xFF393D32), onSecondaryContainer = Color(0xFFDDE8CF))
    else lightColorScheme(primary = Color(0xFFA94F72), onPrimary = Color.White,
        primaryContainer = Color(0xFFF6DCE5), onPrimaryContainer = Color(0xFF663349),
        background = Color(0xFFFFF8FA), surface = Color.White, surfaceVariant = Color(0xFFF6EDF1),
        secondaryContainer = Color(0xFFEDF1E5), onSecondaryContainer = Color(0xFF4C5D46))
    MaterialTheme(colorScheme = colors, shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp)), content = content)
}
