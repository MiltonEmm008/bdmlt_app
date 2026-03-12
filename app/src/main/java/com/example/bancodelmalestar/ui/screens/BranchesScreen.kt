package com.example.bancodelmalestar.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.bancodelmalestar.R
import com.google.gson.JsonObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

private const val MARKER_IMAGE_ID = "banco_marker"
private const val TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"

private const val STYLE_JSON = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["$TILE_URL"],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
  "layers": [{
    "id": "osm-tiles",
    "type": "raster",
    "source": "osm"
  }]
}
"""

@Composable
fun BranchesScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado para controlar qué sucursal se seleccionó
    var selectedBranchName by remember { mutableStateOf<String?>(null) }

    val branches = remember {
        listOf(
            Triple("Sucursal La Cantera",   21.4879679, -104.8318394),
            Triple("Sucursal Av. México",   21.474311,  -104.8586215),
            Triple("Sucursal Principal",    21.471942,  -104.853678),
            Triple("Sucursal Las Brisas",   21.5148355, -104.9229643),
            Triple("Sucursal Cecy",         21.4784741, -104.8541761)
        )
    }

    val tepicBounds = remember {
        LatLngBounds.Builder()
            .include(LatLng(21.55, -104.78))
            .include(LatLng(21.42, -104.95))
            .build()
    }

    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner)  = mapView.onCreate(null)
            override fun onStart(owner: LifecycleOwner)   = mapView.onStart()
            override fun onResume(owner: LifecycleOwner)  = mapView.onResume()
            override fun onPause(owner: LifecycleOwner)   = mapView.onPause()
            override fun onStop(owner: LifecycleOwner)    = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    // Mostrar el popup si hay una sucursal seleccionada
    selectedBranchName?.let { name ->
        AlertDialog(
            onDismissRequest = { selectedBranchName = null },
            title = { Text("Información de Sucursal") },
            text = { Text("Has seleccionado: $name") },
            confirmButton = {
                TextButton(onClick = { selectedBranchName = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { map: MapLibreMap ->
                    map.setStyle(Style.Builder().fromJson(STYLE_JSON)) { style ->
                        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.banco_mapa)
                        if (bitmap != null) {
                            style.addImage(MARKER_IMAGE_ID, bitmap)
                        }

                        val manager = SymbolManager(this, map, style).apply {
                            iconAllowOverlap = true
                            iconIgnorePlacement = true
                        }
                        
                        // Escuchar clicks en los iconos
                        manager.addClickListener { symbol ->
                            // Recuperar el nombre guardado en los datos del símbolo
                            val name = symbol.data?.asJsonObject?.get("name")?.asString
                            selectedBranchName = name
                            true // Indica que el evento fue manejado
                        }

                        branches.forEach { (name, lat, lon) ->
                            // Guardamos el nombre en un JsonObject dentro del marcador
                            val data = JsonObject().apply { addProperty("name", name) }
                            
                            manager.create(
                                SymbolOptions()
                                    .withLatLng(LatLng(lat, lon))
                                    .withIconImage(MARKER_IMAGE_ID)
                                    .withIconSize(1.0f)
                                    .withIconAnchor("bottom")
                                    .withData(data)
                            )
                        }
                    }

                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(21.48, -104.85))
                        .zoom(12.5)
                        .build()

                    map.setLatLngBoundsForCameraTarget(tepicBounds)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
