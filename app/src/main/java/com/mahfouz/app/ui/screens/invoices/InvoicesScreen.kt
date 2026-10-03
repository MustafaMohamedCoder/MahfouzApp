package com.mahfouz.app.ui.screens.invoices

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.ui.theme.PaidGreen
import com.mahfouz.app.ui.theme.UnpaidOrange
import com.mahfouz.app.ui.utils.responsivePadding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    categoryName: String,
    viewModel: InvoicesViewModel,
    onNavigateBack: () -> Unit,
    onAddInvoiceClick: () -> Unit,
    onInvoiceClick: (Long) -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val paymentFilter by viewModel.paymentFilter.collectAsState()
    val horizontalPadding = responsivePadding()
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = { Text("بحث برقم الفاتورة أو المورد...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column {
                            Text(
                                text = categoryName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${invoices.size} فاتورة مؤرشفة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) viewModel.onSearchQueryChanged("")
                    }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "إغلاق البحث" else "بحث"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddInvoiceClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "إضافة فاتورة") },
                text = { Text("أرشفة فاتورة", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1200.dp)
            ) {
                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = paymentFilter == PaymentFilterType.ALL,
                        onClick = { viewModel.onPaymentFilterChanged(PaymentFilterType.ALL) },
                        label = { Text("الكل") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = paymentFilter == PaymentFilterType.PAID,
                        onClick = { viewModel.onPaymentFilterChanged(PaymentFilterType.PAID) },
                        label = { Text("مدفوعة") },
                        leadingIcon = if (paymentFilter == PaymentFilterType.PAID) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = paymentFilter == PaymentFilterType.UNPAID,
                        onClick = { viewModel.onPaymentFilterChanged(PaymentFilterType.UNPAID) },
                        label = { Text("آجل / ديون") },
                        leadingIcon = if (paymentFilter == PaymentFilterType.UNPAID) {
                            { Icon(Icons.Default.WarningAmber, contentDescription = null, tint = UnpaidOrange, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                if (invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "لم يتم العثور على أي فاتورة تطابق بحثك" else "لا توجد فواتير في هذه المجموعة حتى الآن",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "تأكد من رقم الفاتورة أو اسم المورد" else "اضغط على زر 'أرشفة فاتورة' لإضافة وتصوير أول فاتورة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Adaptive Grid for phone & tablet
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 340.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            bottom = 88.dp,
                            top = 4.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(invoices, key = { it.id }) { invoice ->
                            InvoiceItemCard(
                                invoice = invoice,
                                onClick = { onInvoiceClick(invoice.id) },
                                onTogglePayment = { viewModel.togglePaymentStatus(invoice) },
                                onDelete = { viewModel.deleteInvoice(invoice) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceItemCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onTogglePayment: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo Preview or Icon
            if (!invoice.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = invoice.imageUri,
                    contentDescription = "صورة الفاتورة",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.supplierName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                if (invoice.invoiceNumber.isNotBlank()) {
                    Text(
                        text = "رقم: ${invoice.invoiceNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = dateFormat.format(Date(invoice.invoiceDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%,.2f ر.س", invoice.totalAmount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive Payment Badge that can be clicked to toggle status
                Surface(
                    color = (if (invoice.isPaid) PaidGreen else UnpaidOrange).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onTogglePayment() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (invoice.isPaid) PaidGreen else UnpaidOrange)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (invoice.isPaid) "مدفوع" else "آجل",
                            color = if (invoice.isPaid) PaidGreen else UnpaidOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("حذف الفاتورة") },
            text = { Text("هل أنت متأكد من حذف فاتورة المورد '${invoice.supplierName}'؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
