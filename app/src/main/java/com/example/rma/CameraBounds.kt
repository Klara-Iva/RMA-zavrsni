package com.example.rma

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

object CameraBounds {
    var camerapostion: CameraPosition = CameraPosition.fromLatLngZoom(LatLng(45.5549, 18.6956),13.7f)
    var latitude:Double=0.0
    var longitude:Double=0.0
    var showSpecifiedLocationOnMap=false

    fun setCoordinates(lat:Double,lng:Double){
        latitude=lat
        longitude=lng
    }
    fun setCameraPosition(position: CameraPosition){
        camerapostion=position

    }
    fun getCameraPosition(): CameraPosition {
        return camerapostion
    }
}