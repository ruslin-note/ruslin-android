package org.dianqk.ruslin.ui.component

import androidx.compose.animation.core.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R

@Composable
fun SuspendConfirmAlertDialog(
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable (() -> Unit)? = null,
    inProgressIcon: @Composable ((Modifier) -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    onConfirm: suspend () -> Unit,
    onConfirmFinished: () -> Unit,
) {
    var inProgress by remember { mutableStateOf(false) }
    val inProgressAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing)
        )
    )
    AlertDialog(
        onDismissRequest = {
            if (!inProgress) {
                onDismissRequest()
            }
        },
        confirmButton = {
            TextButton(enabled = !inProgress, onClick = {
                inProgress = true
                scope.launch {
                    onConfirm()
                    inProgress = false
                    onConfirmFinished()
                }
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(enabled = !inProgress, onClick = {
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        icon = {
            if (inProgress) {
                if (inProgressIcon != null) {
                    inProgressIcon(Modifier.alpha(inProgressAnimation))
                }
            } else {
                if (icon != null) {
                    icon()
                }
            }
        },
        title = title,
        text = text
    )
}