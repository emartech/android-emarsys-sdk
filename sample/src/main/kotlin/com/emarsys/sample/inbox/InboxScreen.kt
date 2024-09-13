package com.emarsys.sample.inbox

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil.annotation.ExperimentalCoilApi
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.row.RowWithCenteredContent
import com.emarsys.sample.ui.component.screen.DetailScreen
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.style.columnWithMaxWidth
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay

class InboxScreen(
    override val context: Context
) : DetailScreen() {

    private val viewModel = InboxViewModel()
    private val messagePresenter = MessagePresenter(context)

    @ExperimentalCoilApi
    @ExperimentalComposeUiApi
    @Composable
    override fun Detail(paddingValues: PaddingValues) {
        SwipeRefreshCompose(messagePresenter = messagePresenter, paddingValues)
    }

    @OptIn(ExperimentalCoilApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
    @Composable
    private fun SwipeRefreshCompose(
        messagePresenter: MessagePresenter,
        innerPadding: PaddingValues
    ) {
        Scaffold(
            Modifier
                .columnWithMaxWidth(),
            topBar = {
                TitleText(titleText = stringResource(id = R.string.inbox_title))
            })
        {
            it.calculateBottomPadding()
            var refreshing by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = refreshing) {
                if (refreshing) {
                    delay(1000)
                    refreshing = false
                }
            }
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = refreshing),
                onRefresh = {
                    refreshing = true
                    viewModel.onSwipeFetchMessages()
                })
            {
                AnimatedVisibility(visible = viewModel.isFetchedMessagesEmpty()) {
                    RowWithCenteredContent {
                        Text(text = stringResource(id = R.string.pull_down))
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(1f)
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        items = viewModel.fetchedMessages.toList(),
                        key = { message -> message.id }) { message ->
                        messagePresenter.MessageCard(message = message)
                    }
                }
            }
        }
    }
}
