package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrewMethodType
import com.example.ui.theme.PaletteCherryMaroon
import com.example.ui.theme.PaletteIvoryWhite
import com.example.ui.theme.PaletteSlateWaterBlue
import com.example.ui.theme.PaletteWarmSand

data class MethodGuideInfo(
    val type: BrewMethodType,
    val title: String,
    val subtitle: String,
    val tagline: String,
    val icon: ImageVector,
    val badges: List<String>,
    val concept: String,
    val technique: String,
    val recommendation: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrewMethodsGuideScreen(
    currentMethod: BrewMethodType,
    onSelectMethod: (BrewMethodType) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val methodsData = listOf(
        MethodGuideInfo(
            type = BrewMethodType.KASUYA_4_6,
            title = "Metode 4:6 (Tetsu Kasuya)",
            subtitle = "World Brewers Cup Champion 2016",
            tagline = "Kontrol presisi rasa manis, keasaman, dan ketebalan body kopi",
            icon = Icons.Default.Star,
            badges = listOf("5 Penuangan", "Gilingan Kasar (Coarse)", "Suhu 88-92°C", "Rasio 1:15"),
            concept = "Total air dibagi menjadi 40% dan 60%:\n" +
                    "• 40% Pertama (2 penuangan): Mengatur keseimbangan sweetness vs acidity. Penuangan pertama yang lebih sedikit membuat kopi terasa lebih manis.\n" +
                    "• 60% Kedua (3 penuangan sama rata): Mengatur kekuatan (strength) dan body kopi.",
            technique = "Gunakan gilingan kasar (coarse). Berikan interval waktu ~45 detik antar penuangan, biarkan air hampir surut sebelum menuang tahap berikutnya.",
            recommendation = "Sangat cocok untuk single origin light roast dengan citarasa fruity & floral (misal: Ethiopia, Kenya, Gesha)."
        ),
        MethodGuideInfo(
            type = BrewMethodType.HOFFMANN_V60,
            title = "Metode James Hoffmann (The Ultimate V60)",
            subtitle = "World Barista Champion 2007",
            tagline = "Ekstraksi merata, clarity tinggi, dan aftertaste manis tanpa sepat",
            icon = Icons.Default.Science,
            badges = listOf("3 Fase + Swirl", "Gilingan Medium-Fine", "Suhu 95-100°C", "Rasio 1:16.6"),
            concept = "Pendekatan ilmiah untuk ekstraksi tinggi yang konsisten:\n" +
                    "• Blooming Ekstensif: 2x hingga 3x dosis kopi dengan gentle swirl agar seluruh bubuk kopi basah merata.\n" +
                    "• Fase 1: Penuangan spiral hingga 60% total air pada menit 1:15.\n" +
                    "• Fase 2: Penuangan lambat hingga 100% total air pada menit 1:45.\n" +
                    "• Finishing Swirl: Aduk perlahan dan swirl lembut agar permukaan kopi (coffee bed) rata sempurna saat drawdown.",
            technique = "Gunakan air bersuhu tinggi (95-100°C untuk light roast) agar komponen rasa manis dan asam terekstraksi maksimal.",
            recommendation = "Ideal untuk kopi washed specialty berkepadatan tinggi yang membutuhkan kejernihan rasa tinggi (high clarity)."
        ),
        MethodGuideInfo(
            type = BrewMethodType.CLASSIC_3_POUR,
            title = "Metode 3-Pour Klasik",
            subtitle = "Standar Emas Kedai Kopi Spesialti",
            tagline = "Metode paling konsisten, ramah barista, dan mudah direproduksi setiap hari",
            icon = Icons.Default.Coffee,
            badges = listOf("3 Penuangan", "Gilingan Medium", "Suhu 90-93°C", "Rasio 1:15"),
            concept = "Tiga penuangan ritmis yang seimbang:\n" +
                    "• Blooming: Penuangan 2x dosis kopi selama 45 detik untuk degassing gas CO2.\n" +
                    "• Penuangan Ke-2: Gerakan melingkar dari dalam ke luar untuk ekstraksi komponen rasa tengah.\n" +
                    "• Penuangan Ke-3: Penuangan di tengah untuk menstabilkan aliran hingga drawdown tuntas.",
            technique = "Jaga corong ceret leher angsa tetap rendah dekat permukaan air untuk menghindari cipratan dan turbulensi tak terkontrol.",
            recommendation = "Sangat serbaguna untuk semua jenis biji kopi (Natural, Honey, maupun Washed) dari level roasting light hingga medium."
        ),
        MethodGuideInfo(
            type = BrewMethodType.RAO_SINGLE_POUR,
            title = "Metode Rao Single Pour (Scott Rao)",
            subtitle = "Konsultan Kopi & Penulis Spesialis Roasting",
            tagline = "Satu penuangan kontinu untuk temperatur stabil & ekstraksi homogen",
            icon = Icons.Default.WaterDrop,
            badges = listOf("Single Continuous Pour", "Gilingan Medium-Fine", "Suhu 92-95°C", "Rasio 1:16"),
            concept = "Mempertahankan suhu seduh konstan dan meminimalkan channeling:\n" +
                    "• Blooming & Excavation: Aduk cepat dan lembut saat bloom agar tidak ada 'dry pockets' (bubuk kopi kering di dalam corong).\n" +
                    "• Single Pour: Penuangan tunggal panjang secara kontinu dan teratur tanpa jeda hingga target air tercapai.\n" +
                    "• Rao Spin: Goyangkan dripper perlahan saat air mendekati akhir agar tidak ada partikel kopi menempel di dinding (high-and-dry).",
            technique = "Penuangan harus konstan dan tenang dari ketinggian stabil. Jangan menuang langsung ke kertas filter.",
            recommendation = "Menghasilkan ekstraksi sangat tinggi dengan mouthfeel halus (silky) dan sweetness yang mendalam."
        ),
        MethodGuideInfo(
            type = BrewMethodType.CUSTOM,
            title = "Metode Kustom (Eksperimen Pribadi)",
            subtitle = "Eksplorasi Tanpa Batas Barista",
            tagline = "Rancang tahapan seduh, rasio air, dan instruksi sesuka hati Anda",
            icon = Icons.Default.Tune,
            badges = listOf("Multi-Tahap Bebas", "Fleksibel", "Auto-Balance Air", "Simpan Resep"),
            concept = "Kebebasan 100% untuk menciptakan resep signature Anda:\n" +
                    "• Tambah atau hapus tahapan seduh secara bebas.\n" +
                    "• Sesuaikan durasi detik, gramasi air, dan deskripsi instruksi tiap tahap.\n" +
                    "• Tombol 'Auto-Balance Air' memudahkan menyelaraskan total air otomatis dengan rasio yang Anda pilih.",
            technique = "Sangat cocok untuk menguji variabel baru seperti bypass water, pulse pouring khusus, atau temperatur air bertingkat.",
            recommendation = "Terbaik untuk kopi fermentasi khusus (anaerobic, thermal shock, carbonic maceration) atau persiapan kompetisi."
        )
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("brew_methods_guide_screen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Penjelasan Metode Seduh",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panduan & Karakteristik V60",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("button_back_methods_guide")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Atur Resep"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Pilih Metode Sesuai Karakter Beans",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Setiap metode memiliki profil ekstraksi, teknik penuangan, dan rekomendasi ukuran gilingan yang berbeda. Pilih metode di bawah untuk langsung menerapkannya ke resep Anda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Cards for each brewing method
            items(methodsData) { info ->
                val isSelected = currentMethod == info.type

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("method_guide_card_${info.type.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }
                    ),
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title row with icon and selection status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = info.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = info.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = info.subtitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Aktif",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Tagline
                        Text(
                            text = info.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Badges flow row
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            info.badges.forEach { badge ->
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.8.dp
                        )

                        // Concept section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Konsep & Prinsip Seduh",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = info.concept,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 19.sp
                            )
                        }

                        // Technique section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Teknik Penuangan",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = info.technique,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 19.sp
                            )
                        }

                        // Recommendation section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Rekomendasi Biji Kopi:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = info.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        // Action Button
                        if (isSelected) {
                            FilledTonalButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_active_method_${info.type.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Metode Ini Sedang Aktif (Kembali)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    onSelectMethod(info.type)
                                    onBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_select_method_${info.type.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gunakan Metode ${info.type.displayName}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
