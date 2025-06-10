package com.pasundane_budget.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pasundane_budget.viewmodel.SummaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    userId: Int,
    summaryViewModel: SummaryViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToTransaction: () -> Unit,
) {
    val uiState by summaryViewModel.uiState.collectAsState()
    val chartData by summaryViewModel.chartData.collectAsState()

    LaunchedEffect(userId) {
        summaryViewModel.loadSummary(userId)
        summaryViewModel.loadWeeklySummary(userId)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ringkasan Keuangan", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                // Card pemasukan
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pemasukan", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text(
                            text = "Rp ${"%,.2f".format(uiState.income)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card pengeluaran
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pengeluaran", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text(
                            text = "Rp ${"%,.2f".format(uiState.expense)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onNavigateToCategory, modifier = Modifier.fillMaxWidth()) {
                    Text("Kelola Kategori")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = onNavigateToTransaction, modifier = Modifier.fillMaxWidth()) {
                    Text("Kelola Transaksi")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF4CAF50)
) {
    val maxVal = values.maxOrNull() ?: 0f

    Canvas(modifier = modifier.padding(horizontal = 8.dp, vertical = 40.dp)) {
        val barWidth = size.width / values.size
        val barMaxHeight = size.height * 0.7f
        val labelTextSize = 14f

        values.forEachIndexed { index, value ->
            val barHeight = if (maxVal == 0f) 0f else (value / maxVal) * barMaxHeight
            val x = index * barWidth
            val y = size.height - barHeight

            drawRect(
                color = barColor,
                topLeft = Offset(x + barWidth * 0.15f, y),
                size = Size(barWidth * 0.7f, barHeight)
            )

            val labelY = size.height + 20f
            drawContext.canvas.nativeCanvas.drawText(
                labels.getOrNull(index) ?: "",
                x + barWidth / 2,
                labelY,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = labelTextSize
                    color = android.graphics.Color.DKGRAY
                }
            )
        }
    }
}