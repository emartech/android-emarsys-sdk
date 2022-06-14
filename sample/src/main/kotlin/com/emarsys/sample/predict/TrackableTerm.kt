package com.emarsys.sample.predict

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.divider.DividerWithBackgroundColor
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.component.textfield.StyledTextField
import com.emarsys.sample.ui.style.columnWithMaxWidth
import com.emarsys.sample.ui.style.rowWithPointEightWidth

@ExperimentalComposeUiApi
@Composable
fun TrackableTerm(
    title: String,
    placeHolder: String,
    fieldToEdit: MutableState<String>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.columnWithMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleText(titleText = title)
        StyledTextField(
            fieldToEdit = fieldToEdit,
            label = placeHolder
        )
        DividerWithBackgroundColor()
    }
    Row(
        modifier = Modifier
            .rowWithPointEightWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.End
    ) {
        StyledTextButton(buttonText = stringResource(id = R.string.track)) {
            onClick()
        }
    }
}
