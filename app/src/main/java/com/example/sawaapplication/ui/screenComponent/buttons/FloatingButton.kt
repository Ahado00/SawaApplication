package com.example.sawaapplication.ui.screenComponent.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import com.example.sawaapplication.R
import com.example.sawaapplication.ui.theme.firstOrange
import com.example.sawaapplication.ui.theme.white

@Composable
fun FloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = { onClick() },
        containerColor = firstOrange,
        contentColor = white,
        modifier = modifier,
        shape = CircleShape,
    ) {
        Icon(
            Icons.Filled.Add,
            "floating action button",
            Modifier.size(integerResource(id = R.integer.floatingButton).dp)
        )
    }
}