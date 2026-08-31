package org.khoyron.bilal.ui.qiblah

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun QiblahMojokertoPreview() {
    QiblahContent(
        uiState = QiblahUiState(
            locationName = "-7.527745, 112.379233",
            qiblahAngle = 295f, // -65 deg
            deviceBearing = 45f, // NE
            directionDescription = "Your device is currently facing North-West towards the Holy Kaaba.",
            rotationInstruction = "Rotate the phone 98° to the right"
        ),
        onRefresh = {}
    )
}
