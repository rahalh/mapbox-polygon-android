package com.example.androiddevchallenge

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androiddevchallenge.data.Parcel
import com.example.androiddevchallenge.ui.theme.MyTheme
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.launch
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import dagger.hilt.android.AndroidEntryPoint

const val LINE_LAYER_ID: String = "LINE_LAYER_ID"
const val LINE_SOURCE_ID: String = "LINE_SOURCE_ID"
const val FILL_SOURCE_ID: String = "FILL_SOURCE_ID"
const val FILL_LAYER_ID: String = "FILL_LAYER_ID"
const val CIRCLE_SOURCE_ID: String = "CIRCLE_SOURCE_ID"
const val CIRCLE_LAYER_ID: String = "CIRCLE_LAYER_ID"

enum class Mode {
    Viewing,
    Drawing
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: ParcelViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                Map(viewModel)
            }
        }
    }
}

@Composable
fun Map(viewModel: ParcelViewModel) {
    Mapbox.getInstance(LocalContext.current, stringResource(id = R.string.mapbox_access_token))
    MapboxMap(viewModel)
}

@Composable
fun MapboxMap(viewModel: ParcelViewModel) {
    val mapView = rememberMapViewWithLifecycle()

    val parcels by viewModel.getParcels().collectAsState(initial = listOf())
    var selectedParcel by remember { mutableStateOf<Parcel?>(null) }
    var mode by remember { mutableStateOf(Mode.Viewing) }
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(TextFieldValue()) }
    val vertices by remember { mutableStateOf(mutableListOf<LatLng>()) }

    if (showDialog) {
        AlertDialog(
            title = { Text(text = "Veuillez entrer un titre") },
            text = {
                TextField(
                    value = title,
                    onValueChange = { title = it }
                )
            },
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.createParcel(Parcel(title.text, vertices.map { com.example.androiddevchallenge.data.LatLng(it.latitude, it.longitude) }))
                    mode = Mode.Viewing
                    title = TextFieldValue()
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    title = TextFieldValue()
                }) { Text("Annuler") }
            },
        )
    }
    Box(Modifier.fillMaxSize()) {
        MapboxMapContainer(mapView, mode, selectedParcel, onMapClick = { point -> vertices.add(point) })
        if (mode == Mode.Viewing) {
            if (parcels.isNotEmpty()) {
                if (selectedParcel == null) selectedParcel = parcels[0]
                Spinner(parcels, selectedParcel, onClick = { selectedParcel = it })
            }

            CreateParcelButton(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 8.dp, vertical = 24.dp),
                onClick = {
                    mode = Mode.Drawing;
                    vertices.clear()
                })
        }
        else {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(8.dp, 24.dp)
            ) {
                IconButton(modifier = Modifier.background(MaterialTheme.colors.primary, MaterialTheme.shapes.medium).padding(1.dp), onClick = { mode =
                    Mode.Viewing; vertices.clear() }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = Icons.Filled.Close.name, tint = MaterialTheme.colors.onPrimary)
                }
                IconButton(modifier = Modifier.background(MaterialTheme.colors.primary, MaterialTheme.shapes.medium).padding(1.dp), onClick = { showDialog = true }) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = Icons.Filled.Check.name, tint = MaterialTheme.colors.onPrimary)
                }
            }
        }
    }
}

@Composable()
fun MapboxMapContainer(map: MapView, mode: Mode, selectedParcel: Parcel?, onMapClick: (LatLng) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    var mapbox by remember { mutableStateOf<MapboxMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }

    var circleSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    var lineSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    var fillSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    val points by remember { mutableStateOf(mutableListOf<Point>()) }
//    val listener by remember { mutableStateOf({ target: LatLng ->
//        val point = target.toPoint()
//
//        onMapClick(target)
//
//        if (points.size < 2) {
//            points.add(point);
//        } else if (points.size == 2) {
//            points.add(point);
//            points.add(points[0]);
//        } else {
//            points.removeAt(points.size - 1);
//            points.add(point);
//            points.add(points[0]);
//        }
//
//        circleSource?.setGeoJson(FeatureCollection.fromFeatures(
//            points.map { p -> Feature.fromGeometry(p) }
//        ))
//
//        lineSource?.setGeoJson(
//            FeatureCollection.fromFeatures(
//                listOf(Feature.fromGeometry(LineString.fromLngLats(points)))
//            )
//        )
//
//        fillSource?.setGeoJson(
//            FeatureCollection.fromFeatures(
//                listOf(
//                    Feature.fromGeometry(
//                        Polygon.fromLngLats(
//                            listOf(
//                                points
//                            )
//                        )
//                    )
//                )
//            )
//        )
//        true
//    }) }
    val listener by remember { mutableStateOf(object : MapboxMap.OnMapClickListener{
        override fun onMapClick(target: LatLng): Boolean { val point = target.toPoint()

            onMapClick(target)

            if (points.size < 2) {
                points.add(point);
            } else if (points.size == 2) {
                points.add(point);
                points.add(points[0]);
            } else {
                points.removeAt(points.size - 1);
                points.add(point);
                points.add(points[0]);
            }

            circleSource?.setGeoJson(FeatureCollection.fromFeatures(
                points.map { p -> Feature.fromGeometry(p) }
            ))

            lineSource?.setGeoJson(
                FeatureCollection.fromFeatures(
                    listOf(Feature.fromGeometry(LineString.fromLngLats(points)))
                )
            )

            fillSource?.setGeoJson(
                FeatureCollection.fromFeatures(
                    listOf(
                        Feature.fromGeometry(
                            Polygon.fromLngLats(
                                listOf(
                                    points
                                )
                            )
                        )
                    )
                )
            )
            return true }})}

    AndroidView(
        factory = { map },
        update = {
            coroutineScope.launch {
                if (mapbox == null) {
                    it.getMapAsync { m ->
                        mapbox = m;
                        mapbox?.setStyle(Style.SATELLITE) { s ->
                            circleSource = initCircleSource(s)
                            initCircleLayer(s)

                            fillSource = initFillSource(s)
                            initFillLayer(s)

                            lineSource = initLineSource(s)
                            initLineLayer(s)

                            // if selected parcel is not null then move the camera
                            selectedParcel?.polygon?.map { LatLng(it.lat, it.lng) }?.let { points ->
                                fillSource!!.drawPolygon(points)
                                m.moveCamera(points)
                            }
                            style = s
                        }
                    }
                } else if (mapbox != null && style != null) {
                    points.clear()

                    if (mode == Mode.Viewing) {
                        clearSources(fillSource!!, circleSource!!, lineSource!!)
                        mapbox!!.removeOnMapClickListener(listener)

                        selectedParcel?.polygon?.map { LatLng(it.lat, it.lng) }?.let { points ->
                            fillSource!!.drawPolygon(points)
                            mapbox!!.moveCamera(points)
                        }
                    }

                    if (mode == Mode.Drawing) {
                        mapbox!!.addOnMapClickListener(listener)
                    }
                }
            }
        }
    )
}

fun MapboxMap.moveCamera(polygon: List<LatLng>): LatLng {
    val bounds = LatLngBounds.Builder().includes(polygon).build()
    animateCamera { getCameraForLatLngBounds(bounds) }
    return bounds.center
}

fun MapboxMap.centerTo(lat: Double, lng: Double, zoom: Double) {
    animateCamera {
        CameraPosition.Builder()
            .target(LatLng(lat, lng))
            .zoom(zoom)
            .build()
    }
}

@Composable
private fun CreateParcelButton(modifier: Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier.background(MaterialTheme.colors.primary, MaterialTheme.shapes.medium),
        onClick = onClick
    ) {
        Row {
            Icon(Icons.Filled.Add, Icons.Filled.Add.name, tint = MaterialTheme.colors.onPrimary)
            Spacer(modifier = Modifier.padding(end = 8.dp))
            Text(text = "Create a parcel")
        }
    }
}

private fun GeoJsonSource.drawPolygon(polygon: List<LatLng>) {
    val points = polygon.map { Point.fromLngLat(it.longitude, it.latitude) }
    setGeoJson(
        FeatureCollection.fromFeatures(
            listOf(
                Feature.fromGeometry(
                    Polygon.fromLngLats(
                        listOf(
                            points
                        )
                    )
                )
            )
        )
    )
}

private fun initCircleSource(style: Style): GeoJsonSource {
    val circleFeatureCollection = FeatureCollection.fromFeatures(listOf());
    val circleGeoJsonSource = GeoJsonSource(CIRCLE_SOURCE_ID, circleFeatureCollection);
    style.addSource(circleGeoJsonSource);
    return circleGeoJsonSource;
}

private fun initCircleLayer(style: Style) {
    val circleLayer = CircleLayer(
        CIRCLE_LAYER_ID,
        CIRCLE_SOURCE_ID
    )
    circleLayer.setProperties(
        circleRadius(7f),
        circleColor(android.graphics.Color.WHITE)
    )
    style.addLayer(circleLayer)
}

private fun initFillSource(style: Style): GeoJsonSource {
    val fillFeatureCollection = FeatureCollection.fromFeatures(listOf())
    val fillGeoJsonSource = GeoJsonSource(FILL_SOURCE_ID, fillFeatureCollection)
    style.addSource(fillGeoJsonSource)
    return fillGeoJsonSource
}

private fun clearSources(vararg sources: GeoJsonSource) {
    sources.forEach{ source ->
        source.setGeoJson(FeatureCollection.fromFeatures(listOf()))
    }
}

private fun initFillLayer(style: Style) {
    val fillLayer = FillLayer(
        FILL_LAYER_ID,
        FILL_SOURCE_ID
    )
    fillLayer.setProperties(
        fillOpacity(.6f),
        fillColor(android.graphics.Color.WHITE)

    )
    style.addLayer(fillLayer)
}

private fun initLineSource(style: Style): GeoJsonSource {
    val lineFeatureCollection = FeatureCollection.fromFeatures(arrayOf())
    val lineGeoJsonSource = GeoJsonSource(LINE_SOURCE_ID, lineFeatureCollection)
    style.addSource(lineGeoJsonSource)
    return lineGeoJsonSource
}

private fun initLineLayer(style: Style) {
    val lineLayer = LineLayer(
        LINE_LAYER_ID,
        LINE_SOURCE_ID
    )
    lineLayer.setProperties(
        lineColor(android.graphics.Color.WHITE),
        lineWidth(2f),
    )
    style.addLayerBelow(lineLayer, CIRCLE_LAYER_ID)
}

private fun LatLng.toPoint(): Point {
    return Point.fromLngLat(longitude, latitude)
}

@Composable
fun Spinner(items: List<Parcel>, selectedParcel: Parcel?, onClick: (Parcel) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .background(MaterialTheme.colors.surface)) {

        TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                if (selectedParcel != null) {
                    Text(text = selectedParcel.title, color = MaterialTheme.colors.onSurface)
                } else { Text("") }
                if (expanded) {
                    Icon(
                        imageVector = Icons.Filled.ExpandLess,
                        contentDescription = "ExpandLess",
                        tint = MaterialTheme.colors.onSurface
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = "ExpandMore",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
        if(expanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEach {
                    TextButton(onClick = { expanded = false; onClick(it) }, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) { Text(text = it.title, color = MaterialTheme.colors.onSurface) }
                    }
                }
            }
        }
    }
}


