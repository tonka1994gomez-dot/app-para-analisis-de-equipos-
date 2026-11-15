package com.footballintelligence.ai

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ApiException(message: String) : Exception(message)
data class Played(val date: String, val opponent: String, val home: Boolean, val gf: Int, val ga: Int)
data class TeamForm(val id: Int, val name: String, val country: String, val matches: List<Played>)

object ApiFootball {
    private const val BASE = "https://v3.football.api-sports.io"

    private fun get(path: String, key: String): JSONObject {
        val conn = URL(BASE + path).openConnection() as HttpURLConnection
        conn.setRequestProperty("x-apisports-key", key)
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        try {
            val code = conn.responseCode
            if (code == 401 || code == 403) throw ApiException("La clave de API no es válida.")
            if (code !in 200..299) throw ApiException("El servicio respondió con error " + code + ".")
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val errors = json.opt("errors")
            if (errors is JSONObject && errors.length() > 0) {
                val msg = errors.keys().asSequence().joinToString("; ") { errors.optString(it) }
                throw ApiException("El servicio indicó: " + msg)
            }
            return json
        } finally { conn.disconnect() }
    }

    private fun findTeam(name: String, key: String): Triple<Int, String, String> {
        if (name.trim().length < 3) throw ApiException("Escribe al menos 3 letras del equipo.")
        val q = URLEncoder.encode(name.trim(), "UTF-8")
        val arr: JSONArray = get("/teams?search=" + q, key).optJSONArray("response") ?: JSONArray()
        if (arr.length() == 0) throw ApiException("No encontré el equipo: " + name)
        var pick = arr.getJSONObject(0).getJSONObject("team")
        for (i in 0 until arr.length()) {
            val team = arr.getJSONObject(i).getJSONObject("team")
            if (team.optString("name").equals(name.trim(), ignoreCase = true)) { pick = team; break }
        }
        return Triple(pick.getInt("id"), pick.optString("name"), pick.optString("country"))
    }

    fun teamForm(name: String, key: String): TeamForm {
        val (id, realName, country) = findTeam(name, key)
        val arr = get("/fixtures?team=" + id + "&last=5", key).optJSONArray("response") ?: JSONArray()
        val played = mutableListOf<Played>()
        for (i in 0 until arr.length()) {
            val fixture = arr.getJSONObject(i)
            val status = fixture.getJSONObject("fixture").getJSONObject("status").optString("short")
            if (status != "FT" && status != "AET" && status != "PEN") continue
            val teams = fixture.getJSONObject("teams")
            val goals = fixture.getJSONObject("goals")
            if (goals.isNull("home") || goals.isNull("away")) continue
            val isHome = teams.getJSONObject("home").getInt("id") == id
            val opponent = teams.getJSONObject(if (isHome) "away" else "home").optString("name")
            val homeGoals = goals.getInt("home")
            val awayGoals = goals.getInt("away")
            val date = fixture.getJSONObject("fixture").optString("date").take(10)
            played.add(Played(date, opponent, isHome, if (isHome) homeGoals else awayGoals, if (isHome) awayGoals else homeGoals))
        }
        played.sortBy { it.date }
        if (played.isEmpty()) throw ApiException("No encontré partidos recientes de " + realName + ".")
        return TeamForm(id, realName, country, played)
    }
}
