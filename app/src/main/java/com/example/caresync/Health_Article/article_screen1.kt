package com.example.caresync.Health_Article

// File: HealthArticlesScreen.kt

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import java.text.SimpleDateFormat
import java.util.*

data class HealthArticle(
    val title: String,
    val author: String?,
    val publishedAt: String?,
    val url: String,
    val content: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HealthArticlesScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var articles by remember { mutableStateOf(listOf<HealthArticle>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch articles from API
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val result = fetchHealthArticles()
                articles = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Health Articles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = 1200f
                    )
                )
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primary
                )
            } else {
                if (articles.isEmpty()) {
                    Text(
                        text = "No health articles found.",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(articles) { article ->
                            ArticleCard(article = article) {
                                // IMPORTANT: encode JSON before sending in route
                                val encoded = Uri.encode(Gson().toJson(article))
                                navController.navigate("healthArticleDetail/${Uri.encode(article.url)}")

                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: HealthArticle, onClick: () -> Unit) {
    val primary = colorResource(id = R.color.primary_button_color)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, primary),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(article.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            if (!article.author.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("By ${article.author}", fontSize = 13.sp, color = Color.DarkGray)
            }
            if (!article.publishedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(formatDate(article.publishedAt), fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

// ✅ Retrofit Fetch Function (uses your RetrofitClient & NewsApiService)
suspend fun fetchHealthArticles(): List<HealthArticle> = withContext(Dispatchers.IO) {
    val apiKey = "db08da7c76e841ad99915b88cdd709a0"
    try {
        val response = RetrofitClient.apiService.getHealthArticles(apiKey = apiKey).awaitResponse()
        if (response.isSuccessful) {
            val body = response.body()
            body?.articles?.map {
                HealthArticle(
                    title = it.title ?: "Untitled",
                    author = it.author ?: "Unknown",
                    publishedAt = it.publishedAt ?: "",
                    url = it.url ?: "",
                    content = it.content ?: ""
                )
            } ?: emptyList()
        } else emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

// --- Format date ---
fun formatDate(input: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date = parser.parse(input)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        formatter.format(date!!)
    } catch (e: Exception) {
        input
    }
}
