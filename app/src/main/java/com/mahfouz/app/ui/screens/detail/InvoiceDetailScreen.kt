package com.mahfouz.app.ui.screens.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.mahfouz.app.data.local.entity.InvoiceWithItems
import com.mahfouz.app.data.repository.InvoiceRepository
import com.mahfouz.app.ui.theme.PaidGreen
import com.mahfouz.app.ui.theme.UnpaidOrange
import com.mahfouz.app.ui.utils.isTabletOrLandscape
import com.mahfouz.app.ui.utils.responsivePadding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    repository: InvoiceRepository,
    onNavigateBack: () -> Unit,
    onEditInvoice: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val invoiceWithItems by repository.getInvoiceWithItems(invoiceId).collectAsState(initial = null)
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()) }
    val isTablet = isTabletOrLandscape()
    val horizontalPadding = responsivePadding()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isImageFullScreen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل الفاتورة", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    invoiceWithItems?.let { data ->
                        // Share Invoice Button
                        IconButton(onClick = {
                            val inv = data.invoice
                            val itemsSummary = if (data.items.isNotEmpty()) {
                                "\n\n📋 الأصناف:\n" + data.items.joinToString("\n") {
                                    "- ${it.productName}: ${it.quantity} × ${it.unitPrice} = ${it.subtotal} ر.س"
                                }
                            } else ""

                            val shareText = """
                                📄 فاتورة مشتريات: ${inv.supplierName}
                                🔢 رقم الفاتورة: ${if (inv.invoiceNumber.isNotBlank()) inv.invoiceNumber else "غير محدد"}
                                📅 التاريخ: ${dateFormat.format(Date(inv.invoiceDate))}
                                💰 الإجمالي: ${String.format(Locale.getDefault(), "%,.2f ر.س", inv.totalAmount)}
                                🏷 الحالة: ${if (inv.isPaid) "مدفوعة" else "آجل / دين"}
                                ${if (inv.notes.isNotBlank()) "📝 ملاحظات: " + inv.notes else ""}$itemsSummary
                                
                                — تم تصديرها عبر تطبيق «مَحْفُوظ»
                            """.trimIndent()

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الفاتورة"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة")
                        }

                        // Edit Invoice Button
                        IconButton(onClick = {
                            onEditInvoice(data.invoice.categoryId, data.invoice.id)
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل")
                        }

                        // Delete Invoice Button
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val data = invoiceWithItems
        if (data == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val inv = data.invoice
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isTablet) {
                    // Two-Pane Layout for Tablets
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 1200.dp)
                            .padding(horizontal = horizontalPadding, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left Pane: Image
                        Box(modifier = Modifier.weight(1f)) {
                            if (!inv.imageUri.isNullOrBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isImageFullScreen = true },
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(3.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        AsyncImage(
                                            model = inv.imageUri,
                                            contentDescription = "صورة الفاتورة",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 500.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                        )

                                        Surface(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تكبير", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("لا توجد صورة مرفقة لهذه الفاتورة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        // Right Pane: Details & Items
                        LazyColumn(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                InvoiceSummaryCard(
                                    inv = inv,
                                    dateFormat = dateFormat,
                                    onTogglePaid = {
                                        coroutineScope.launch {
                                            repository.updatePaymentStatus(inv.id, !inv.isPaid)
                                        }
                                    }
                                )
                            }

                            if (data.items.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "الأصناف والسلع (${data.items.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                items(data.items) { item ->
                                    InvoiceItemDetailCard(item)
                                }
                            }
                        }
                    }
                } else {
                    // Single Column Layout for Phones
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        if (!inv.imageUri.isNullOrBlank()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isImageFullScreen = true },
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(3.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        AsyncImage(
                                            model = inv.imageUri,
                                            contentDescription = "صورة الفاتورة",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 300.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                        )

                                        Surface(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تكبير الصورة", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            InvoiceSummaryCard(
                                inv = inv,
                                dateFormat = dateFormat,
                                onTogglePaid = {
                                    coroutineScope.launch {
                                        repository.updatePaymentStatus(inv.id, !inv.isPaid)
                                    }
                                }
                            )
                        }

                        if (data.items.isNotEmpty()) {
                            item {
                                Text(
                                    text = "الأصناف والسلع المشتراة (${data.items.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            items(data.items) { item ->
                                InvoiceItemDetailCard(item)
                            }
                        }
                    }
                }
            }
        }
    }

    // Full screen image dialog
    if (isImageFullScreen && invoiceWithItems?.invoice?.imageUri != null) {
        Dialog(
            onDismissRequest = { isImageFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = invoiceWithItems?.invoice?.imageUri,
                    contentDescription = "صورة مكبرة",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { isImageFullScreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("حذف الفاتورة") },
            text = { Text("هل أنت متأكد من حذف هذه الفاتورة نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            invoiceWithItems?.invoice?.let { repository.deleteInvoice(it) }
                            showDeleteConfirm = false
                            onNavigateBack()
                        }
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

@Composable
fun InvoiceSummaryCard(
    inv: com.mahfouz.app.data.local.entity.InvoiceEntity,
    dateFormat: SimpleDateFormat,
    onTogglePaid: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inv.supplierName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (inv.invoiceNumber.isNotBlank()) {
                        Text(
                            text = "رقم الفاتورة: ${inv.invoiceNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    color = (if (inv.isPaid) PaidGreen else UnpaidOrange).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { onTogglePaid() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (inv.isPaid) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (inv.isPaid) PaidGreen else UnpaidOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (inv.isPaid) "مدفوعة" else "آجل / دين",
                            color = if (inv.isPaid) PaidGreen else UnpaidOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("تاريخ الفاتورة:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(dateFormat.format(Date(inv.invoiceDate)), fontWeight = FontWeight.Medium)
            }

            if (inv.notes.isNotBlank()) {
                Column {
                    Text("ملاحظات:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(inv.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("المبلغ الإجمالي:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = String.format(Locale.getDefault(), "%,.2f ر.س", inv.totalAmount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InvoiceItemDetailCard(item: com.mahfouz.app.data.local.entity.InvoiceItemEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.quantity} × ${item.unitPrice} ر.س",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Text(
                text = String.format(Locale.getDefault(), "%,.2f ر.س", item.subtotal),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
