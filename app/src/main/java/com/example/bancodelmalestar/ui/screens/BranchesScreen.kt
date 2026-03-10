package com.example.bancodelmalestar.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleEventObserver
import com.example.bancodelmalestar.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun BranchesScreen() {
    val tepicCenter = GeoPoint(21.5042, -104.8947)
    val points = remember {
        listOf(
            Triple("Sucursal La Cantera", 21.4879679, -104.8318394),
            Triple("Sucursal Av. México", 21.474311, -104.8586215),
            Triple("Sucursal Principal", 21.471942, -104.853678),
            Triple("Sucursal Las Brisas", 21.5148355, -104.9229643),
            Triple("Sucursal Cecy", 21.4784741, -104.8541761)
        )
    }
    val tepicBbox = remember {
        BoundingBox(21.55, -104.78, 21.42, -104.95)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(14.5)
                controller.setCenter(tepicCenter)
                setMultiTouchControls(true)
                minZoomLevel = 13.0
                maxZoomLevel = 18.0
                setScrollableAreaLimitDouble(tepicBbox)
                points.forEach { (name, lat, lon) ->
                    val marker = Marker(this)
                    marker.position = GeoPoint(lat, lon)
                    marker.title = name
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val iconDrawable = ContextCompat.getDrawable(ctx, R.drawable.banco_mapa)
                    if (iconDrawable != null) {
                        marker.icon = iconDrawable
                    }
                    overlays.add(marker)
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { mapView ->
            mapView.onDetach()
        }
    )
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ -> }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
