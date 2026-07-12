package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.data.entity.TimetableSubjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import com.example.ui.viewmodel.TimetableViewModel
import com.example.ui.viewmodel.AppBlockerViewModel
import java.util.Calendar

data class DayItem(val name: String, val fullName: String, val isToday: Boolean = false)

fun formatTo12Hour(time24: String): String {
    return try {
        val parts = time24.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        val suffix = if (h >= 12) "PM" else "AM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val mStr = m.toString().padStart(2, '0')
        "$h12:$mStr $suffix"
    } catch(e: Exception) {
        time24
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel(),
    appBlockerViewModel: AppBlockerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        appBlockerViewModel.loadInstalledApps(context)
    }

    val appsList by appBlockerViewModel.installedApps.collectAsStateWithLifecycle()
    val dbWebsites by appBlockerViewModel.blockedWebsites.collectAsStateWithLifecycle()

    // Dynamic Mon-Sun Week slider data
    val days = remember {
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        listOf(
            DayItem("Mon", "Monday", currentDayOfWeek == Calendar.MONDAY),
            DayItem("Tue", "Tuesday", currentDayOfWeek == Calendar.TUESDAY),
            DayItem("Wed", "Wednesday", currentDayOfWeek == Calendar.WEDNESDAY),
            DayItem("Thu", "Thursday", currentDayOfWeek == Calendar.THURSDAY),
            DayItem("Fri", "Friday", currentDayOfWeek == Calendar.FRIDAY),
            DayItem("Sat", "Saturday", currentDayOfWeek == Calendar.SATURDAY),
            DayItem("Sun", "Sunday", currentDayOfWeek == Calendar.SUNDAY)
        )
    }
    var selectedDay by remember {
        mutableStateOf(days.find { it.isToday } ?: days[0])
    }

    // Collect subjects from ViewModel (Room Database)
    val allSubjects by viewModel.timetableSubjects.collectAsStateWithLifecycle()

    // Filter subjects for selected day (check if subject dayOfWeek split contains selectedDay name)
    val filteredSubjects = remember(allSubjects, selectedDay) {
        allSubjects.filter { it.dayOfWeek.split(",").contains(selectedDay.name) }
    }

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<TimetableSubjectEntity?>(null) }

    // Dialog Input states
    var subjectName by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("09") }
    var startMinute by remember { mutableStateOf("00") }
    var startAmPm by remember { mutableStateOf("AM") }
    var endHour by remember { mutableStateOf("10") }
    var endMinute by remember { mutableStateOf("30") }
    var endAmPm by remember { mutableStateOf("AM") }
    var category by remember { mutableStateOf("Study") }
    var notes by remember { mutableStateOf("") }
    var selectedRepeatingDays by remember { mutableStateOf(setOf(selectedDay.name)) }
    var selectedAppsForBlock by remember { mutableStateOf(setOf<String>()) }
    var selectedWebsitesForBlock by remember { mutableStateOf(setOf<String>()) }

    val subjectColors = remember {
        listOf(
            Color(0xFF8B7BFF), // Soft Violet Accent
            Color(0xFFD6CFFF), // Lavender Highlight
            Color(0xFFB5A9FF), // Light Slate Violet
            Color(0xFF5140CD), // Deep Royal Indigo
            Color(0xFF80F0FF), // Ice Cyan Glow
            Color(0xFFFF9EE2), // Nebula Rose
            Color(0xFF7B98FF)  // Cosmic Slate Blue
        )
    }
    var selectedColor by remember { mutableStateOf(subjectColors[0]) }

    // When dialog opens/changes
    LaunchedEffect(showDialog, editingSubject) {
        if (showDialog) {
            val subject = editingSubject
            if (subject != null) {
                subjectName = subject.name
                
                // Parse 24-hour startTime to 12-hour parts
                val startParts = subject.startTime.split(":")
                val sH = startParts.getOrNull(0)?.toIntOrNull() ?: 9
                startAmPm = if (sH >= 12) "PM" else "AM"
                val sH12 = if (sH == 0) 12 else if (sH > 12) sH - 12 else sH
                startHour = sH12.toString().padStart(2, '0')
                startMinute = startParts.getOrElse(1) { "00" }

                // Parse 24-hour endTime to 12-hour parts
                val endParts = subject.endTime.split(":")
                val eH = endParts.getOrNull(0)?.toIntOrNull() ?: 10
                endAmPm = if (eH >= 12) "PM" else "AM"
                val eH12 = if (eH == 0) 12 else if (eH > 12) eH - 12 else eH
                endHour = eH12.toString().padStart(2, '0')
                endMinute = endParts.getOrElse(1) { "30" }

                category = subject.category
                notes = subject.notes
                selectedColor = Color(subject.colorArgb)
                selectedRepeatingDays = subject.dayOfWeek.split(",").filter { it.isNotBlank() }.toSet()
                selectedAppsForBlock = subject.blockedApps.split(",").filter { it.isNotBlank() }.toSet()
                selectedWebsitesForBlock = subject.blockedWebsites.split(",").filter { it.isNotBlank() }.toSet()
            } else {
                // Set default/empty fields for creation
                subjectName = ""
                startHour = "09"
                startMinute = "00"
                startAmPm = "AM"
                endHour = "10"
                endMinute = "30"
                endAmPm = "AM"
                category = "Study"
                notes = ""
                selectedColor = subjectColors[0]
                selectedRepeatingDays = setOf(selectedDay.name)
                selectedAppsForBlock = emptySet()
                selectedWebsitesForBlock = emptySet()
            }
        }
    }

    GradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 40.dp, bottom = 110.dp)
            ) {
                // Header with floating Action Button Trigger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Schedule",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Plan and bridge focus sessions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // Premium glassy Add Button
                        FilledTonalIconButton(
                            onClick = {
                                editingSubject = null
                                showDialog = true
                            },
                            modifier = Modifier.size(46.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Schedule Block",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Mon-Sun Slider Item
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(days) { day ->
                            val isSelected = selectedDay.name == day.name
                            val activeBrush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .width(54.dp)
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(activeBrush)
                                        } else {
                                            Modifier.background(if (isSystemInDarkTheme()) Color(0x11FFFFFF) else Color(0x0F000000))
                                        }
                                    )
                                    .clickable { selectedDay = day }
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (day.isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            else Color.Transparent
                                        ),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.name,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (day.isToday) "Today" else day.fullName.take(3),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (day.isToday) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Vertical timeline layout
                if (filteredSubjects.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                com.example.ui.components.BeautifulEmptyStateIllustration()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Unscheduled Day",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No focus sessions booked for ${selectedDay.fullName}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredSubjects, key = { it.id }) { subject ->
                        val subjectColor = Color(subject.colorArgb)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .animateContentSize(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Timeline rail with nodes
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor)
                                        .border(BorderStroke(3.dp, Color.White), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(100.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    subjectColor,
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // Right Glass details card
                            GlassCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        editingSubject = subject
                                        showDialog = true
                                    },
                                cornerRadius = 20.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(subjectColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = subject.category,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = subjectColor
                                                )
                                            }
                                            Text(
                                                text = "${formatTo12Hour(subject.startTime)} - ${formatTo12Hour(subject.endTime)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = subject.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        val daysJoined = subject.dayOfWeek.split(",").joinToString(", ")
                                        Text(
                                            text = "Days: $daysJoined",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )

                                        if (subject.notes.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = subject.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.height(80.dp)
                                    ) {
                                        Text(
                                            text = formatTo12Hour(subject.startTime),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        // Delete visual action trigger
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteSubject(subject)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove session block",
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Add/Edit Event Dialog Box with premium Frosted Glass inputs
            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        cornerRadius = 24.dp,
                        elevation = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (editingSubject == null) "Schedule Focus" else "Edit Schedule",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Title input
                            OutlinedTextField(
                                value = subjectName,
                                onValueChange = { subjectName = it },
                                label = { Text("Subject Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Time Inputs
                            Text(
                                text = "Duration & Timing (12-Hour)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Start Time
                                OutlinedTextField(
                                    value = startHour,
                                    onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) startHour = it },
                                    label = { Text("Start Hour") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Text(":", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                OutlinedTextField(
                                    value = startMinute,
                                    onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) startMinute = it },
                                    label = { Text("Min") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                // AM/PM Start Selector
                                Row(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.08f)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("AM", "PM").forEach { label ->
                                        val isSel = startAmPm == label
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                .clickable { startAmPm = label },
                                            contentAlignment = Alignment.Center
                                         ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // End Time
                                OutlinedTextField(
                                    value = endHour,
                                    onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) endHour = it },
                                    label = { Text("End Hour") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Text(":", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                OutlinedTextField(
                                    value = endMinute,
                                    onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) endMinute = it },
                                    label = { Text("Min") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // AM/PM End Selector
                                Row(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.08f)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("AM", "PM").forEach { label ->
                                        val isSel = endAmPm == label
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                .clickable { endAmPm = label },
                                            contentAlignment = Alignment.Center
                                         ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Repeating Days
                            Text(
                                text = "Select Repeating Weekdays",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val daysOfWeekList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                daysOfWeekList.forEach { dayName ->
                                     val isDaySelected = selectedRepeatingDays.contains(dayName)
                                     Box(
                                         modifier = Modifier
                                             .size(38.dp)
                                             .clip(CircleShape)
                                             .background(if (isDaySelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f))
                                             .clickable {
                                                 selectedRepeatingDays = if (isDaySelected) {
                                                     if (selectedRepeatingDays.size > 1) selectedRepeatingDays - dayName else selectedRepeatingDays
                                                 } else {
                                                     selectedRepeatingDays + dayName
                                                 }
                                             },
                                         contentAlignment = Alignment.Center
                                     ) {
                                         Text(
                                             text = dayName.take(1),
                                             style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                             color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurface
                                         )
                                     }
                                }
                            }

                            // Choose Category
                            Text(
                                text = "Choose Category",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val categoryColors = remember {
                                listOf(
                                    "Study" to subjectColors[0],
                                    "Coding" to subjectColors[1],
                                    "Meeting" to subjectColors[2],
                                    "Health" to subjectColors[3],
                                    "Admin" to subjectColors[4]
                                )
                            }

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categoryColors) { (catName, catColor) ->
                                    val isCatSelected = category == catName
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isCatSelected) catColor else catColor.copy(alpha = 0.15f)
                                            )
                                            .clickable {
                                                category = catName
                                                selectedColor = catColor
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isCatSelected) Color.White else catColor
                                        )
                                    }
                                }
                            }

                            // Choose Custom Color representation
                            Text(
                                text = "Choose Theme Color",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                subjectColors.forEach { color ->
                                    val isColorSelected = selectedColor == color
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                BorderStroke(
                                                    if (isColorSelected) 3.dp else 0.dp,
                                                    if (isColorSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                                                ),
                                                CircleShape
                                            )
                                            .clickable { selectedColor = color },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isColorSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Blocked Apps Expandable Section
                            var showAppsExpanded by remember { mutableStateOf(false) }
                            var appsSearchQuery by remember { mutableStateOf("") }
                            val filteredApps = remember(appsList, appsSearchQuery) {
                                if (appsSearchQuery.isBlank()) appsList else appsList.filter { it.appName.contains(appsSearchQuery, ignoreCase = true) }
                            }
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAppsExpanded = !showAppsExpanded }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🤖 Apps to Block (${selectedAppsForBlock.size} selected)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = if (showAppsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand Apps",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                if (showAppsExpanded) {
                                    OutlinedTextField(
                                        value = appsSearchQuery,
                                        onValueChange = { appsSearchQuery = it },
                                        label = { Text("Search Installed Apps") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Box(modifier = Modifier.height(140.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            filteredApps.forEach { app ->
                                                val isAppSel = selectedAppsForBlock.contains(app.packageName)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            selectedAppsForBlock = if (isAppSel) {
                                                                selectedAppsForBlock - app.packageName
                                                            } else {
                                                                selectedAppsForBlock + app.packageName
                                                            }
                                                        }
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = isAppSel,
                                                        onCheckedChange = {
                                                            selectedAppsForBlock = if (isAppSel) {
                                                                selectedAppsForBlock - app.packageName
                                                            } else {
                                                                selectedAppsForBlock + app.packageName
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        text = app.appName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Blocked Websites Expandable Section
                            var showWebsitesExpanded by remember { mutableStateOf(false) }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showWebsitesExpanded = !showWebsitesExpanded }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🌐 Websites to Block (${selectedWebsitesForBlock.size} selected)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = if (showWebsitesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand Websites",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                if (showWebsitesExpanded) {
                                    Box(modifier = Modifier.height(110.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            dbWebsites.forEach { website ->
                                                val isWebSel = selectedWebsitesForBlock.contains(website.domain)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            selectedWebsitesForBlock = if (isWebSel) {
                                                                selectedWebsitesForBlock - website.domain
                                                            } else {
                                                                selectedWebsitesForBlock + website.domain
                                                            }
                                                        }
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                    Checkbox(
                                                        checked = isWebSel,
                                                        onCheckedChange = {
                                                            selectedWebsitesForBlock = if (isWebSel) {
                                                                selectedWebsitesForBlock - website.domain
                                                            } else {
                                                                selectedWebsitesForBlock + website.domain
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        text = website.domain,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                            if (dbWebsites.isEmpty()) {
                                                Text(
                                                    text = "No websites configured in Website Blocker.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Notes input
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Optional Notes") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Dialog buttons layout
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        if (subjectName.isNotBlank()) {
                                            val to24Hour = { hourStr: String, minuteStr: String, amPm: String ->
                                                val h12 = hourStr.toIntOrNull() ?: 9
                                                val m = minuteStr.toIntOrNull() ?: 0
                                                val h24 = when (amPm) {
                                                    "PM" -> if (h12 < 12) h12 + 12 else 12
                                                    "AM" -> if (h12 == 12) 0 else h12
                                                    else -> h12
                                                }.coerceIn(0, 23)
                                                "${h24.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
                                            }

                                            val formattedStart = to24Hour(startHour, startMinute, startAmPm)
                                            val formattedEnd = to24Hour(endHour, endMinute, endAmPm)
                                            val repeatingDaysCsv = selectedRepeatingDays.joinToString(",")
                                            val blockedAppsCsv = selectedAppsForBlock.joinToString(",")
                                            val blockedWebsitesCsv = selectedWebsitesForBlock.joinToString(",")

                                            val subject = editingSubject
                                            if (subject != null) {
                                                 // Edit Mode
                                                 viewModel.updateSubject(
                                                     subject.copy(
                                                         name = subjectName,
                                                         startTime = formattedStart,
                                                         endTime = formattedEnd,
                                                         colorArgb = selectedColor.toArgb(),
                                                         category = category,
                                                         notes = notes,
                                                         dayOfWeek = repeatingDaysCsv,
                                                         blockedApps = blockedAppsCsv,
                                                         blockedWebsites = blockedWebsitesCsv
                                                     )
                                                 )
                                            } else {
                                                 // Add Mode
                                                 viewModel.addSubject(
                                                     name = subjectName,
                                                     dayOfWeek = repeatingDaysCsv,
                                                     startTime = formattedStart,
                                                     endTime = formattedEnd,
                                                     colorArgb = selectedColor.toArgb(),
                                                     category = category,
                                                     notes = notes,
                                                     blockedApps = blockedAppsCsv,
                                                     blockedWebsites = blockedWebsitesCsv
                                                 )
                                            }
                                            showDialog = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(if (editingSubject == null) "Add Block" else "Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
