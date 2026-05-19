package com.martins.assignmentschronometer.ui.screens.licenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R

// ─── Model ────────────────────────────────────────────────────────────────────

private data class LibraryLicense(
    val name: String,
    val version: String,
    val author: String,
    val licenseType: LicenseType
)

private enum class LicenseType { APACHE_2, MIT, BSD }

// ─── Data ─────────────────────────────────────────────────────────────────────

private val licenses = listOf(
    LibraryLicense("Jetpack Compose", "1.6+", "Google / AOSP", LicenseType.APACHE_2),
    LibraryLicense("Material3 for Compose", "1.2+", "Google / AOSP", LicenseType.APACHE_2),
    LibraryLicense("Navigation Compose", "2.7+", "Google / AOSP", LicenseType.APACHE_2),
    LibraryLicense("Lifecycle ViewModel Compose", "2.7+", "Google / AOSP", LicenseType.APACHE_2),
    LibraryLicense("DataStore Preferences", "1.1+", "Google / AOSP", LicenseType.APACHE_2),
    LibraryLicense("ML Kit Text Recognition", "16+", "Google", LicenseType.APACHE_2),
    LibraryLicense("Kotlinx Serialization JSON", "1.6+", "JetBrains", LicenseType.APACHE_2),
    LibraryLicense("Kotlin Coroutines", "1.8+", "JetBrains", LicenseType.APACHE_2),
    LibraryLicense("AndroidX Core KTX", "1.13+", "Google / AOSP", LicenseType.APACHE_2),
    LibraryLicense("AndroidX Activity Compose", "1.9+", "Google / AOSP", LicenseType.APACHE_2),
    LibraryLicense("AndroidX SavedState", "1.2+", "Google / AOSP", LicenseType.APACHE_2),
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.licenses_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.licenses_screen_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(licenses) { lib ->
                LicenseCard(lib)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─── Card ─────────────────────────────────────────────────────────────────────

@Composable
private fun LicenseCard(lib: LibraryLicense) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lib.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                LicenseBadge(lib.licenseType)
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${lib.author} · v${lib.version}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicenseBadge(type: LicenseType) {
    val (label, container, content) = when (type) {
        LicenseType.APACHE_2 -> Triple(
            stringResource(R.string.licenses_license_apache),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        LicenseType.MIT -> Triple(
            stringResource(R.string.licenses_license_mit),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        LicenseType.BSD -> Triple(
            stringResource(R.string.licenses_license_bsd),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = container
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}