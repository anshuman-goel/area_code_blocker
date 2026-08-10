package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AreaCodeData

@Composable
fun RegionalBlockCard(
    selectedCountryIndex: Int,
    selectedStateIndex: Int,
    onSelectedCountryIndexChange: (Int) -> Unit,
    onSelectedStateIndexChange: (Int) -> Unit,
    onBlockAreaCodes: (List<String>, String) -> Unit,
) {
    val context = LocalContext.current

    val selectedCountry = AreaCodeData.countries.getOrElse(selectedCountryIndex) { AreaCodeData.countries[0] }
    val selectedState = selectedCountry.states.getOrNull(selectedStateIndex)
    var countryExpanded by remember { mutableStateOf(value = false) }
    var stateExpanded by remember { mutableStateOf(value = false) }

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Block by Region",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select a country and state to quickly block all known area codes from that region.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Country Selection
                Box {
                    OutlinedTextField(
                        value = selectedCountry.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Country") },
                        trailingIcon = { 
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { countryExpanded = true }
                    )
                    DropdownMenu(
                        expanded = countryExpanded,
                        onDismissRequest = { countryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                            AreaCodeData.countries.forEachIndexed { idx, country ->
                            DropdownMenuItem(
                                text = { Text(country.name) },
                                onClick = {
                                    onSelectedCountryIndexChange(idx)
                                    onSelectedStateIndexChange(0)
                                    countryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedCountry.isSupported) {
                    // State Selection
                    Box {
                        OutlinedTextField(
                            value = selectedState?.name ?: "Select State",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State / Province") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { stateExpanded = true }
                        )
                        DropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                                                    selectedCountry.states.forEachIndexed { idx, state ->
                                DropdownMenuItem(
                                    text = { Text(state.name) },
                                    onClick = {
                                        onSelectedStateIndexChange(idx)
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    selectedState?.let { state ->
                        Button(
                            onClick = {
                                onBlockAreaCodes(state.areaCodes, "${selectedCountry.name} - ${state.name}")
                                Toast.makeText(context, "Blocked ${state.areaCodes.size} area codes for ${state.name}", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Block All Area Codes in ${state.name}")
                        }
                        
                        Text(
                            text = "Includes: ${state.areaCodes.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    // Not supported country message
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "This country does not follow the standard area code pattern supported by this app.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
