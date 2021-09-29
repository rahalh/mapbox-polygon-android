

- Composable functions can store a single object in memory by using the remember
composable
- A value computed by remember is stored in the composition during initial
    composition and the stored value is returned during recomposition
    (use remember to preserve a value across recompositions, the stored value
can be an object)
- In our case the map view is created once and reused across recompositions
- Remember can be used to store both mutable and immutable objects
- mutableStateOf() creates a mutableState which is an observable type, any changes
    to state will schedule recomposition of any composable functions that read the state
- observable type is integrated with the compose runtime, changing the value of an observable
    triggers re-composition

- observer lets you define a subscription mechanism to notify multiple
    objects about any event that happen to the object they're observing
the subject notifies other objects about the changes to its state
https://refactoring.guru/design-patterns/observer

- mutableStateOf creates an observable property with value equal to the
    value passed as parameter to mutableStateOf, whenever the value of the obs
    changes a recomposition is scheduled

- use keys ??

//val (isMapInitialized, setMapInitialized) = remember(map) { mutableStateOf(false) }

//    When LaunchedEffect enters the composition it will launch block into the composition's CoroutineContext. The coroutine will be cancelled and re-launched when LaunchedEffect is recomposed with a different key1 or key2. The coroutine will be cancelled when the LaunchedEffect leaves the composition.
//    LaunchedEffect(map, isMapInitialized) {
//        if (!isMapInitialized) {
//            // This only runs once
//            map.getMapAsync { mapbox ->
//                mapbox.setStyle(Style.SATELLITE) {
//                    if (parcel == null) {
//                        // TODO: default to current user position
//                        mapbox.centerTo(lat = 37.419857, lng = -122.078827, zoom = 10.0)
//                        Log.d("here", "map initialized")
//                    }
//                }
//            }
//            setMapInitialized(true)
//        }
//    }


//                        val clickListener: (LatLng) -> Unit = {
//                            onClick(it)
//                            // TODO: add marker
//                        }
//                        if (mode == Mode.Drawing) {
//                            mapbox.addOnMapClickListener { clickListener(it); true }
//                        } else {
//                            mapbox.removeOnMapClickListener { clickListener(it); true }
//                        }
