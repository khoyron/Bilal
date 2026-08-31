package org.khoyron.bilal.util

import androidx.compose.runtime.Composable

@Composable
expect fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
)
