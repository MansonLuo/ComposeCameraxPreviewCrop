package com.example.composecameraxpreviewcrop.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.More
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberDropDownMenu(
    modifier: Modifier = Modifier,
    onNumberSelected: (Int) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .wrapContentSize(Alignment.Center)
    ) {

        IconButton(
            modifier = Modifier.fillMaxHeight(),
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                Icons.Default.More,
                contentDescription = "More",
                modifier = Modifier.size(100.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            repeat(7) { index ->
                DropdownMenuItem(
                    text = { Text(text = (7 - index).toString()) },
                    onClick = {
                        expanded = false
                        onNumberSelected(7 - index)
                    }
                )
            }
        }

    }
}