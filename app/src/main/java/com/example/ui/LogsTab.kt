package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.R
import com.example.data.entity.BlockedLog
import com.example.ui.components.BlockedLogCard
import java.time.ZoneId

@Composable
internal fun rememberCurrentZoneId(): ZoneId {
    val context = LocalContext.current
    var timezoneChangeCount by remember { mutableIntStateOf(0) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                timezoneChangeCount++
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_TIMEZONE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    return remember(timezoneChangeCount) { ZoneId.systemDefault() }
}

fun LazyListScope.logsTab(
    blockedLogs: LazyPagingItems<BlockedLog>,
    totalCount: Int,
    zoneId: ZoneId,
    locale: java.util.Locale,
    onClearAll: () -> Unit,
    onDeleteLog: (Int) -> Unit,
) {
    item(key = "logs_header") {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.filtered_operations_log),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (totalCount > 0) {
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("purge_logs_button"),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.clear_log))
                }
            }
        }
    }

    when {
        blockedLogs.loadState.refresh is LoadState.Loading -> item(key = "logs_loading") {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("logs_loading"))
            }
        }
        blockedLogs.loadState.refresh is LoadState.Error -> item(key = "logs_error") {
            val error = (blockedLogs.loadState.refresh as LoadState.Error).error
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.logs_load_error), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = blockedLogs::retry, modifier = Modifier.testTag("logs_retry")) {
                    Text(stringResource(R.string.retry))
                }
                Text(error.localizedMessage.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
        }
        blockedLogs.itemCount == 0 -> item(key = "logs_empty") { LogsEmptyState() }
        else -> {
            items(
                count = blockedLogs.itemCount,
                key = { index -> blockedLogs[index]?.id ?: "placeholder_$index" },
                contentType = { "blocked_log" },
            ) { index ->
                blockedLogs[index]?.let { log ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BlockedLogCard(
                            log = log,
                            locale = locale,
                            zoneId = zoneId,
                            onDelete = { onDeleteLog(log.id) },
                        )
                    }
                }
            }
            if (blockedLogs.loadState.append is LoadState.Loading) {
                item(key = "logs_append_loading") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            if (blockedLogs.loadState.append is LoadState.Error) {
                item(key = "logs_append_error") {
                    TextButton(onClick = blockedLogs::retry, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun LogsEmptyState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.screening_log_clean),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.screening_log_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
