package com.yma16.dailychallenge

import android.icu.util.HebrewCalendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

private val bg=Color(0xFF0B1020); private val ink=Color(0xFFF4F5FC); private val muted=Color(0xFF9DA5BB); private val blue=Color(0xFF8AA7FF); private val green=Color(0xFF69D6A3); private val panel=Color(0xCC1A2238)
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(11,16,32);window.navigationBarColor=android.graphics.Color.rgb(11,16,32);val prefs=getSharedPreferences("challenge",MODE_PRIVATE)
 setContent {
  var refresh by remember{mutableIntStateOf(0)};var selected by remember{mutableStateOf(LocalDate.now())};var month by remember{mutableStateOf(YearMonth.now())};var dark by remember{mutableStateOf(true)}
  val entries=remember(refresh){prefs.all.mapNotNull{(k,v)->if(!k.startsWith("d_"))null else runCatching{val p=(v as String).split("|");LocalDate.parse(k.drop(2)) to Entry(p[0],p.getOrElse(1){"5"}.toInt())}.getOrNull()}.toMap()}
  val fg=if(dark)ink else Color(0xFF17213A);val card=if(dark)panel else Color(0xEFFFFFFF);var streak=0;var d=LocalDate.now();if(entries[d]?.status!="yes")d=d.minusDays(1);while(entries[d]?.status=="yes"){streak++;d=d.minusDays(1)};val current=entries[selected]
  MaterialTheme(colorScheme=darkColorScheme(primary=blue,background=bg,surface=panel)){
   Box(Modifier.fillMaxSize().background(Brush.verticalGradient(if(dark)listOf(bg,Color(0xFF172443))else listOf(Color(0xFFF0F4FF),Color(0xFFE5ECFF))))) {
    Column(Modifier.fillMaxSize().padding(horizontal=18.dp).padding(top=22.dp,bottom=90.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
     Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("האתגר היומי",color=fg,fontSize=27.sp,fontWeight=FontWeight.Bold);Text("צעד קטן בכל יום. שינוי גדול לאורך זמן.",color=muted,fontSize=13.sp)};IconButton(onClick={dark=!dark}){Icon(if(dark)Icons.Default.LightMode else Icons.Default.DarkMode,null,tint=fg)}}
     CardBox(card){Text("הרצף הנוכחי",color=muted,fontSize=14.sp);Row(verticalAlignment=Alignment.CenterVertically){Text("$streak",color=fg,fontSize=46.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.width(12.dp));Column{Text(if(streak==1)"יום ברצף"else"ימים ברצף",color=fg,fontWeight=FontWeight.SemiBold);Text(if(streak>0)"כל הכבוד! ממשיכים קדימה 🔥"else"כל התחלה היא הזדמנות חדשה",color=muted,fontSize=12.sp)};Spacer(Modifier.weight(1f));Text("🔥",fontSize=34.sp)}}
     CardBox(card){Text("האתגר של היום",color=fg,fontSize=18.sp,fontWeight=FontWeight.Bold);Text(hebrewDate(selected),color=muted,fontSize=13.sp);Spacer(Modifier.height(8.dp));Text("איך עבר עליך היום?",color=fg);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={save(prefs,selected,"yes",current?.score?:8);refresh++},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=green,contentColor=Color(0xFF092318))){Icon(Icons.Default.CheckCircle,null);Text("הצלחתי")};OutlinedButton(onClick={save(prefs,selected,"no",current?.score?:1);refresh++},modifier=Modifier.weight(1f),colors=ButtonDefaults.outlinedButtonColors(contentColor=fg)){Icon(Icons.Default.Refresh,null);Text("לא הצלחתי")}};Text("דירוג: ${current?.score?:5}/10",color=fg);Slider(value=(current?.score?:5).toFloat(),onValueChange={save(prefs,selected,current?.status?:"pending",it.toInt());refresh++},valueRange=1f..10f,steps=8);Text(when(current?.status){"yes"->"הצלחת היום — כל הכבוד על ההתמדה!";"no"->"גם יום מאתגר הוא חלק מהדרך. מחר מתחילים מחדש.";else->"כל צעד נחשב. היה כן עם עצמך."},color=muted,fontSize=12.sp)}
     CardBox(card){Text("לוח ההתקדמות",color=fg,fontSize=18.sp,fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick={month=month.minusMonths(1)}){Icon(Icons.Default.ChevronLeft,null,tint=fg)};Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale("he"))),color=fg,modifier=Modifier.weight(1f),textAlign=TextAlign.Center);IconButton(onClick={month=month.plusMonths(1)}){Icon(Icons.Default.ChevronRight,null,tint=fg)}};Row(Modifier.fillMaxWidth()){listOf("א","ב","ג","ד","ה","ו","ש").forEach{Text(it,color=muted,modifier=Modifier.weight(1f),textAlign=TextAlign.Center)}};val blanks=month.atDay(1).dayOfWeek.value%7;val dates:List<LocalDate?>=List(blanks){null}+(1..month.lengthOfMonth()).map{month.atDay(it)};dates.chunked(7).forEach{week->Row(Modifier.fillMaxWidth()){(week+List(7-week.size){null}).forEach{date->val status=if(date==null)null else entries[date]?.status;Box(Modifier.weight(1f).padding(2.dp).aspectRatio(1f).background(when(status){"yes"->green.copy(alpha=.23f);"no"->Color(0xFFFF7D8B).copy(alpha=.2f);else->if(date==selected)blue.copy(alpha=.22f)else Color.Transparent},RoundedCornerShape(10.dp)).clickable(enabled=date!=null){selected=date!!},contentAlignment=Alignment.Center){if(date!=null)Column(horizontalAlignment=Alignment.CenterHorizontally){Text("${date.dayOfMonth}",color=if(date==selected)blue else fg,fontWeight=if(date==selected)FontWeight.Bold else FontWeight.Normal);Text(when(status){"yes"->"✓";"no"->"•";else->""},color=green,fontSize=10.sp)}}}}};Text("נבחר: ${selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} · ${hebrewDate(selected)}",color=muted,fontSize=12.sp)}
     CardBox(card){Text("מילה טובה להיום ✨",color=fg,fontSize=17.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(6.dp));Text("״הצלחה היא לא סופית, כישלון הוא לא קטלני: האומץ להמשיך הוא שקובע.״",color=fg,fontSize=15.sp);Spacer(Modifier.height(6.dp));Text("גם אם היום לא הלך כפי שרצית, עצם הבחירה לנסות שוב היא התקדמות.",color=muted,fontSize=13.sp);Box(Modifier.fillMaxWidth().padding(top=10.dp).height(85.dp).background(Brush.linearGradient(listOf(Color(0xFF354B7B),Color(0xFF9479C5),Color(0xFFEDB5C7))),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){Text("כל יום הוא דף חדש",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)}}
     val done=entries.values.filter{it.status=="yes"||it.status=="no"};val yes=done.count{it.status=="yes"};CardBox(card){Text("הנתונים שלך",color=fg,fontWeight=FontWeight.Bold,fontSize=17.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Metric("ימי תיעוד","${done.size}",fg);Metric("אחוז הצלחה",if(done.isEmpty())"—"else"${yes*100/done.size}%",fg);Metric("ממוצע דירוג",if(done.isEmpty())"—"else String.format(Locale.US,"%.1f",done.map{it.score}.average()),fg)}}
    }
    NavigationBar(Modifier.align(Alignment.BottomCenter),containerColor=card){NavigationBarItem(selected=true,onClick={},icon={Icon(Icons.Default.Today,null)},label={Text("היום")});NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Default.Insights,null)},label={Text("התקדמות")});NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Default.FormatQuote,null)},label={Text("השראה")})}
   }
  }
 }
 private fun save(p:android.content.SharedPreferences,date:LocalDate,status:String,score:Int){p.edit().putString("d_$date","$status|${score.coerceIn(1,10)}").apply()}
}
private data class Entry(val status:String,val score:Int)
@Composable private fun CardBox(color:Color,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxWidth().background(color,RoundedCornerShape(22.dp)).padding(16.dp),content=content)}
@Composable private fun Metric(label:String,value:String,fg:Color){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(value,color=fg,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(label,color=muted,fontSize=11.sp)}}
private fun hebrewDate(date:LocalDate):String=runCatching{val c=HebrewCalendar().apply{set(date.year,date.monthValue-1,date.dayOfMonth)};val names=arrayOf("תשרי","חשוון","כסלו","טבת","שבט","אדר א׳","אדר","ניסן","אייר","סיוון","תמוז","אב","אלול");"${c.get(HebrewCalendar.DAY_OF_MONTH)} ב${names[c.get(HebrewCalendar.MONTH).coerceIn(0,12)]} ${c.get(HebrewCalendar.YEAR)}"}.getOrDefault("")
