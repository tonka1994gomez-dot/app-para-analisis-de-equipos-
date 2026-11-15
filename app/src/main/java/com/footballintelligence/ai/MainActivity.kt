package com.footballintelligence.ai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val Bg = Color(0xFF080D18)
private val Panel = Color(0xFF121B2B)
private val Accent = Color(0xFF36D6A0)
private val Muted = Color(0xFF91A0B7)
private val White = Color(0xFFF4F7FB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FootballApp() }
    }
}

@Composable
fun FootballApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fi_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var home by remember { mutableStateOf("") }
    var away by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf(prefs.getString("api_key", "") ?: "") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<MatchAnalysis?>(null) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = White, unfocusedTextColor = White,
        focusedBorderColor = Accent, unfocusedBorderColor = Muted,
        focusedLabelColor = Accent, unfocusedLabelColor = Muted, cursorColor = Accent
    )
    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Panel, primary = Accent,
        onPrimary = Bg, onBackground = White, onSurface = White, secondary = Color(0xFF66A7FF))) {
        Scaffold(containerColor = Bg) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .background(Bg).padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("FOOTBALL", color = White, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("INTELLIGENCE AI", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Text("Compara. Analiza. Predice.", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                CardBox {
                    Text("ENFRENTAMIENTO", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = home, onValueChange = { home = it }, singleLine = true,
                        label = { Text("Equipo local") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("VS", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    OutlinedTextField(value = away, onValueChange = { away = it }, singleLine = true,
                        label = { Text("Equipo visitante") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        error = null; result = null
                        if (apiKey.isBlank()) error = "Primero pega tu clave de API en el recuadro de abajo."
                        else if (home.isBlank() || away.isBlank()) error = "Escribe los dos equipos."
                        else {
                            prefs.edit().putString("api_key", apiKey.trim()).apply()
                            loading = true
                            scope.launch {
                                try {
                                    result = withContext(Dispatchers.IO) {
                                        val h = ApiFootball.teamForm(home, apiKey.trim())
                                        val a = ApiFootball.teamForm(away, apiKey.trim())
                                        buildAnalysis(h, a)
                                    }
                                } catch (e: ApiException) { error = e.message }
                                catch (e: Exception) { error = "No se pudo conectar. Revisa tu internet e inténtalo de nuevo." }
                                loading = false
                            }
                        }
                    }, enabled = !loading, modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                        shape = RoundedCornerShape(14.dp)) {
                        if (loading) {
                            CircularProgressIndicator(color = Bg, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp)); Text("BUSCANDO DATOS...", fontWeight = FontWeight.ExtraBold)
                        } else {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(Modifier.width(8.dp)); Text("ANÁLISIS", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                error?.let { message -> CardBox { Text(message, color = Color(0xFFFFB4AB), fontSize = 13.sp) } }
                result?.let { ResultView(it) }
                CardBox {
                    Text("CLAVE DE API (API-Football)", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, singleLine = true,
                        label = { Text("Pega tu clave aquí") }, colors = fieldColors,
                        visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    Text("La clave se guarda solo en este teléfono. Consíguela en api-football.com.", color = Muted, fontSize = 11.sp)
                }
                Text("Estimación estadística basada en los últimos partidos disponibles; no garantiza resultados.", color = Muted, fontSize = 10.sp)
                Text("FOOTBALL INTELLIGENCE AI • Versión 0.2.0", color = Muted, fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

private fun pct(x: Double) = String.format(Locale.US, "%.0f%%", x * 100)
private fun dec(x: Double) = String.format(Locale.US, "%.1f", x)

@Composable
fun ResultView(r: MatchAnalysis) {
    CardBox {
        Text("RESULTADO DEL ANÁLISIS", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(r.home.name + " vs " + r.away.name, color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(r.verdict, color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        InfoLine("Gana " + r.home.name, pct(r.pHome))
        InfoLine("Empate", pct(r.pDraw))
        InfoLine("Gana " + r.away.name, pct(r.pAway))
        InfoLine("Marcador más probable", r.likelyScore)
        InfoLine("Más de 2.5 goles", pct(r.pOver25))
        InfoLine("Ambos anotan", pct(r.pBothScore))
        InfoLine("Goles esperados", dec(r.expHome) + " - " + dec(r.expAway))
    }
    TeamCard(r.home); TeamCard(r.away)
}

@Composable
fun TeamCard(t: TeamForm) {
    CardBox {
        Text(t.name + " (" + t.country + ")", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        InfoLine("Últimos " + t.matches.size, t.wins.toString() + "G  " + t.draws + "E  " + t.losses + "P")
        InfoLine("Puntos", t.pointsTotal.toString() + " de " + (t.matches.size * 3))
        InfoLine("Goles a favor / en contra", t.goalsFor.toString() + " / " + t.goalsAgainst)
        InfoLine("Promedio de goles marcados", dec(t.avgFor))
        InfoLine("Promedio de goles recibidos", dec(t.avgAgainst))
        InfoLine("Partidos anotando", t.scoredIn.toString() + " de " + t.matches.size)
        InfoLine("Porterías en cero", t.cleanSheets.toString())
        Text("Partidos usados:", color = Muted, fontSize = 11.sp)
        t.matches.forEach { match ->
            val condition = if (match.home) "(L)" else "(V)"
            Text(match.date + "  " + condition + " " + match.opponent + "  " + match.gf + "-" + match.ga,
                color = White, fontSize = 12.sp)
        }
    }
}

@Composable
fun CardBox(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
