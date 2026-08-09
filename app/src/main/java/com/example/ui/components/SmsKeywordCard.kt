package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.entity.BlockedKeyword

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmsKeywordCard(
    blockedKeywords: List<BlockedKeyword>,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var rawKeywordInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Blocked SMS Keywords",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Add specific words or phrases. Any incoming text message containing any of these keywords from non-contacts will be filtered.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rawKeywordInput,
                        onValueChange = { rawKeywordInput = it },
                        placeholder = { Text("e.g. lottery, crypto, claim") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keyword_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (rawKeywordInput.isNotBlank()) {
                                onAddKeyword(rawKeywordInput)
                                Toast.makeText(context, "Added keyword: $rawKeywordInput", Toast.LENGTH_SHORT).show()
                                rawKeywordInput = ""
                                keyboardController?.hide()
                            }
                        },
                        enabled = rawKeywordInput.isNotBlank(),
                        modifier = Modifier.testTag("add_keyword_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Add")
                    }
                }

                if (blockedKeywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Active Keywords (${blockedKeywords.size})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        blockedKeywords.forEach { rule ->
                            ElevatedAssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = rule.keyword,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete keyword",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                onRemoveKeyword(rule.keyword)
                                                Toast.makeText(context, "Removed keyword: ${rule.keyword}", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
