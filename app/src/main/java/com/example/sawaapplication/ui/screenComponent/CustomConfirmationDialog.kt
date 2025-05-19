package com.example.sawaapplication.ui.screenComponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.sawaapplication.R

@Composable
fun CustomConfirmationDialog(
    message: String,
    confirmText: String = stringResource(R.string.yesImSure),
    cancelText: String = stringResource(R.string.no),
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(integerResource(R.integer.mediumSpace).dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = integerResource(R.integer.smallerSpace).dp
        ) {
            Column(modifier = Modifier.padding(integerResource(R.integer.padding).dp)) {
                Text(message, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(integerResource(R.integer.mediumSpace).dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(cancelText)
                    }
                    TextButton(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
