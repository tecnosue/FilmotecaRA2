package com.campusdigitalfp.filmotecav2.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.campusdigitalfp.filmotecav2.R
import com.campusdigitalfp.filmotecav2.common.FilmTopAppBar
import com.campusdigitalfp.filmotecav2.model.Film
import com.campusdigitalfp.filmotecav2.viewmodel.FilmViewModel

@Composable
fun FilmListScreen(navController: NavHostController, viewModel: FilmViewModel = viewModel()) {
    var isActionMode by remember { mutableStateOf(false) }
    val selectedFilms = remember { mutableStateListOf<Film>() }
    val films by viewModel.films.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        FilmTopAppBar(
            navController,
            principal = true,
            editar = false,
            selectedFilms = selectedFilms,
            isActionMode = isActionMode,
            onActionModeChange = {isActionMode = it},
            viewModel = viewModel

        )
    }) { innerPadding ->
        FilmListContent(
            films = films,
            selectedFilms = selectedFilms,
            isActionMode = isActionMode,
            navController = navController,
            innerPadding = innerPadding
        ) { isActionMode = it }
    }
}

@Composable
fun FilmListContent(
    films: List<Film>,
    selectedFilms: MutableList<Film>,
    isActionMode: Boolean,
    navController: NavHostController,
    innerPadding: PaddingValues,
    onActionModeChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        items(films) { film ->
            VistaFilm(film = film, onClick = {
                if (isActionMode) {
                    if (selectedFilms.contains(film)) {
                        selectedFilms.remove(film)
                        if (selectedFilms.isEmpty()) {
                            onActionModeChange(false)
                        }
                    } else {
                        selectedFilms.add(film)
                    }
                } else {
                    android.util.Log.d("Navigation", "Navigating to data/${film.id}")
                    navController.navigate("data/${film.id}")
                }
            }, onLongClick = {
                selectedFilms.add(film)
                onActionModeChange(true)
            }, isSelected = selectedFilms.contains(film)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VistaFilm(film: Film, onClick: () -> Unit, onLongClick: () -> Unit, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val imageResId = context.resources.getIdentifier(film.imagen, "drawable", context.packageName)
        if (imageResId != 0) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = film.title,
                modifier = Modifier.size(80.dp)
            )
        } else if (film.imagen.startsWith("file://") || film.imagen.startsWith("/")) {
            Image(
                painter = rememberAsyncImagePainter(film.imagen),
                contentDescription = film.title,
                modifier = Modifier.size(80.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.icono_pelicula),
                contentDescription = film.title,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = film.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = film.director,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}