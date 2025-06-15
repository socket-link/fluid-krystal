package link.socket.krystal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KrystalStyle(
    val cornerRadius: Dp = 16.dp,
    val backgroundOpacity: Float = 0.05f,
    val borderOpacity: Float = 0.1f,
    val tintColor: Color = Color.Blue.copy(alpha = 0.1f)
)
