package com.yma16.dailychallenge

import android.content.SharedPreferences
import android.icu.text.DateFormat
import android.icu.util.HebrewCalendar
import java.util.Date
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Night = Color(0xFF0B1020)
private val White = Color(0xFFF4F5FC)
private val Muted = Color(0xFF9DA5BB)
private val Blue = Color(0xFF8AA7FF)
private val Green = Color(0xFF69D6A3)
private val Rose = Color(0xFFFF7D8B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(11, 16, 32)
        window.navigationBarColor = android.graphics.Color.rgb(11, 16, 32)
        val prefs = getSharedPreferences("challenge", MODE_PRIVATE)
        setContent { DailyChallenge(prefs) }
    }
}

private data class Entry(val status: String, val score: Int)

private fun readEntries(prefs: SharedPreferences): Map<LocalDate, Entry> =
    prefs.all.mapNotNull { (key, value) ->
        if (!key.startsWith("d_") || value !is String) return@mapNotNull null
        runCatching {
            val pieces = value.split("|")
            LocalDate.parse(key.removePrefix("d_")) to
                Entry(pieces[0], pieces.getOrElse(1) { "5" }.toInt().coerceIn(1, 10))
        }.getOrNull()
    }.toMap()

private fun saveEntry(prefs: SharedPreferences, date: LocalDate, status: String, score: Int) {
    prefs.edit().putString("d_$date", "$status|${score.coerceIn(1, 10)}").apply()
}

@Composable
private fun DailyChallenge(prefs: SharedPreferences) {
    var refresh by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var dark by remember { mutableStateOf(true) }
    val entries = remember(refresh) { readEntries(prefs) }
    val fg = if (dark) White else Color(0xFF17213A)
    val muted = if (dark) Muted else Color(0xFF64708A)
    val card = if (dark) Color(0xCC1A2238) else Color(0xEFFFFFFF)
    val background = if (dark) listOf(Night, Color(0xFF172443))
        else listOf(Color(0xFFF0F4FF), Color(0xFFE5ECFF))
    val today = LocalDate.now()
    var streak = 0
    var cursor = today
    if (entries[cursor]?.status != "yes") cursor = cursor.minusDays(1)
    while (entries[cursor]?.status == "yes") {
        streak++
        cursor = cursor.minusDays(1)
    }
    val current = entries[selected]
    val score = current?.score ?: 5

    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Blue) else lightColorScheme(primary = Blue)) {
        Box(
            Modifier.fillMaxSize().background(Brush.verticalGradient(background))
        ) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("האתגר היומי", color = fg, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                        Text("צעד קטן בכל יום. שינוי גדול לאורך זמן.", color = muted, fontSize = 13.sp)
                    }
                    IconButton(onClick = { dark = !dark }) {
                        Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, null, tint = fg)
                    }
                }
                Panel(card) {
                    Text("הרצף הנוכחי", color = muted, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$streak", color = fg, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(if (streak == 1) "יום ברצף" else "ימים ברצף", color = fg, fontWeight = FontWeight.SemiBold)
                            Text(if (streak > 0) "כל הכבוד! ממשיכים קדימה 🔥" else "כל התחלה היא הזדמנות חדשה", color = muted, fontSize = 12.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("🔥", fontSize = 32.sp)
                    }
                }
                Panel(card) {
                    Text(if (selected == today) "האתגר של היום" else "תיעוד יום נבחר", color = fg, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(gregorianHebrewDate(selected), color = muted, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("איך עבר עליך היום?", color = fg)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { saveEntry(prefs, selected, "yes", score); refresh++ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color(0xFF092318))
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(5.dp))
                            Text("הצלחתי")
                        }
                        OutlinedButton(
                            onClick = { saveEntry(prefs, selected, "no", score); refresh++ },
                            modifier = Modifier.weight(1f)
                        ) { Text("לא הצלחתי") }
                    }
                    Text("דירוג: $score/10", color = fg)
                    Slider(
                        value = score.toFloat(),
                        onValueChange = { saveEntry(prefs, selected, current?.status ?: "pending", it.toInt().coerceIn(1, 10)); refresh++ },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text(
                        when (current?.status) {
                            "yes" -> "הצלחת היום — כל הכבוד על ההתמדה!"
                            "no" -> "גם יום מאתגר הוא חלק מהדרך. מחר מתחילים מחדש."
                            else -> "כל צעד נחשב. היה כן עם עצמך."
                        }, color = muted, fontSize = 12.sp
                    )
                }
                Panel(card) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Default.ChevronLeft, null, tint = fg) }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("he"))), color = fg, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold); Text("לוח לועזי · תאריך עברי בבחירת יום", color = muted, fontSize = 10.sp) }
                        IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Default.ChevronRight, null, tint = fg) }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        listOf("א", "ב", "ג", "ד", "ה", "ו", "ש").forEach {
                            Text(it, color = muted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                    val blanks = month.atDay(1).dayOfWeek.value % 7
                    val cells: List<LocalDate?> = List(blanks) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            (week + List(7 - week.size) { null }).forEach { date ->
                                val status = date?.let { entries[it]?.status }
                                val fill = when (status) {
                                    "yes" -> Green.copy(alpha = .25f)
                                    "no" -> Rose.copy(alpha = .22f)
                                    else -> if (date == selected) Blue.copy(alpha = .24f) else Color.Transparent
                                }
                                Box(
                                    Modifier.weight(1f).padding(2.dp).aspectRatio(1f)
                                        .background(fill, RoundedCornerShape(9.dp))
                                        .clickable(enabled = date != null) { selected = date!! },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${date.dayOfMonth}", color = if (date == selected) Blue else fg, fontWeight = if (date == selected) FontWeight.Bold else FontWeight.Normal)
                                            Text(when (status) { "yes" -> "✓"; "no" -> "•"; else -> "" }, color = if (status == "no") Rose else Green, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text("נבחר: ${selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} · ${hebrewDate(selected)}", color = muted, fontSize = 11.sp)
                }
                Panel(card) {
                    Text("מילה טובה להיום ✨", color = fg, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("״הצלחה היא לא סופית, כישלון הוא לא קטלני: האומץ להמשיך הוא שקובע.״", color = fg, fontSize = 14.sp)
                    Text("גם אם היום לא הלך כפי שרצית, עצם הבחירה לנסות שוב היא התקדמות.", color = muted, fontSize = 12.sp)
                    Box(
                        Modifier.fillMaxWidth().height(64.dp).background(
                            Brush.linearGradient(listOf(Color(0xFF354B7B), Color(0xFF9479C5), Color(0xFFEDB5C7))),
                            RoundedCornerShape(14.dp)
                        ), contentAlignment = Alignment.Center
                    ) { Text("כל יום הוא דף חדש", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold) }
                }
                val recorded = entries.values.filter { it.status == "yes" || it.status == "no" }
                val successes = recorded.count { it.status == "yes" }
                Panel(card) {
                    Text("הנתונים שלך", color = fg, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("ימי תיעוד", recorded.size.toString(), fg, muted)
                        Metric("אחוז הצלחה", if (recorded.isEmpty()) "—" else "${successes * 100 / recorded.size}%", fg, muted)
                        Metric("ממוצע דירוג", if (recorded.isEmpty()) "—" else String.format(Locale.US, "%.1f", recorded.map { it.score }.average()), fg, muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun Panel(color: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(color, RoundedCornerShape(20.dp)).padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
}

@Composable
private fun Metric(label: String, value: String, fg: Color, muted: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = fg, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(label, color = muted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

private fun gregorianHebrewDate(date: LocalDate): String {
    val gregorian = DateFormat.getDateInstance(DateFormat.FULL, Locale("he", "IL"))
    val civil = date.atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val gregorianText = gregorian.format(Date(civil))
    return "$gregorianText · ${hebrewDate(date)}"
}

/** ICU performs the Hebrew calendar conversion, including leap-year Adar handling. */
private fun hebrewDate(date: LocalDate): String = runCatching {
    val millis = date.atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val calendar = HebrewCalendar().apply { timeInMillis = millis }
    val formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale("he", "IL")).apply {
        setCalendar(calendar)
        timeZone = calendar.timeZone
    }
    formatter.format(Date(millis))
}.getOrElse { "תאריך עברי לא זמין" }
