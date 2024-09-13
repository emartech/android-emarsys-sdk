package com.emarsys.sample.inbox

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil.annotation.ExperimentalCoilApi
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.row.RowWithCenteredContent
import com.emarsys.sample.ui.component.screen.DetailScreen
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.style.rowWithMaxWidth

@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
class InboxScreen(
    override val context: Context
) : DetailScreen() {
    private val viewModel = InboxViewModel()
    private val messagePresenter = MessagePresenter(context)

    @Composable
    override fun Detail(paddingValues: PaddingValues) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = viewModel.refreshing.value,
            onRefresh = viewModel::onSwipeFetchMessages
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rowWithMaxWidth()
                .pullRefresh(pullRefreshState),
        )
        {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    RowWithCenteredContent() {
                        TitleText(titleText = stringResource(id = R.string.inbox_title))
                    }
                }
                item {
                    AnimatedVisibility(visible = viewModel.isFetchedMessagesEmpty()) {
                        RowWithCenteredContent {
                            Text(text = stringResource(id = R.string.pull_down))
                        }
                    }
                }
                items(
                    items = viewModel.fetchedMessages.toList(),
                    key = { message -> message.id }) { message ->
                    messagePresenter.MessageCard(message = message)
                }
            }

            PullRefreshIndicator(
                refreshing = viewModel.refreshing.value,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
