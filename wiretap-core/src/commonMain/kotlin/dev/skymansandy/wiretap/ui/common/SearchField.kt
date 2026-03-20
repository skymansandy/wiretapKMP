package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.search_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchField(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = query,
        onValueChange = onQueryChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = LocalContentColor.current.copy(alpha = 0.6f),
                )

                Spacer(
                    modifier = Modifier.width(8.dp),
                )

                Box {
                    if (query.isEmpty()) {
                        Text(
                            stringResource(Res.string.search_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalContentColor.current.copy(alpha = 0.4f),
                        )
                    }

                    innerTextField()
                }
            }
        },
    )
}

@Preview
@Composable
private fun SearchFieldEmptyPreview() {
    MaterialTheme {
        SearchField(
            query = "",
            onQueryChange = {},
            focusRequester = FocusRequester(),
        )
    }
}

@Preview
@Composable
private fun SearchFieldWithQueryPreview() {
    MaterialTheme {
        SearchField(
            query = "api/users",
            onQueryChange = {},
            focusRequester = FocusRequester(),
        )
    }
}
