package com.example.installedapps.ui.compose

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.installedapps.R
import com.example.installedapps.ui.model.AppInfo
import com.example.installedapps.ui.theme.InstalledAppsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AppDetails(
    appInfoFlow: StateFlow<AppInfo?>, innerPadding: PaddingValues, getChecksum: (String) -> Unit,
    onClick: () -> Unit
) {
    val appInfoVal by appInfoFlow.collectAsStateWithLifecycle()
    appInfoVal?.let { appInfo ->
        if (appInfo.checkSum.isEmpty()) {
            getChecksum(appInfo.packageName)
        }
        Card(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),

                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        bitmap = appInfo.icon.drawableToImageBitmap(),
                        contentDescription = appInfo.name,
                        modifier = Modifier.size(70.dp)
                    )
                    Text(
                        text = appInfo.name,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }
                TwoTextChip(
                    primaryText = appInfo.version,
                    secondaryText = stringResource(R.string.version),
                    modifier = Modifier.fillMaxWidth()
                )
                TwoTextChip(
                    primaryText = appInfo.packageName,
                    secondaryText = stringResource(R.string.app_package),
                    modifier = Modifier.fillMaxWidth()
                )

                TwoTextChip(
                    primaryText = appInfo.checkSum,
                    secondaryText = stringResource(R.string.checksum),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = stringResource(R.string.open))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppDetailsPreview() {
    InstalledAppsTheme {
        val context: Context = LocalContext.current
        val icon = AppCompatResources.getDrawable(context, R.mipmap.ic_launcher)
        AppDetails(
            MutableStateFlow(
                AppInfo(
                    icon = icon!!,
                    name = "My App",
                    version = "1.0.0",
                    packageName = "package",
                    checkSum = "1234"
                ),

                ),
            getChecksum = {},
            innerPadding = PaddingValues.Zero
        ) {}
    }
}