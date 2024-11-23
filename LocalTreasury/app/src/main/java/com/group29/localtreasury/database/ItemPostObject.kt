package com.group29.localtreasury.database

import com.google.type.LatLng

class ItemPostObject {
    var PostID: String = ""
    var sellerID: String = ""
    var itemName: String = ""
    var itemPrice: String = ""
    var itemDescription: String = ""
    var address: String = ""
    lateinit var latLng: LatLng
    var ImageURL: String = ""
}

/*
Glide.with(this)
            .load(ItemPostObject.ImageURL)
            .placeholder(R.drawable.placeholder_image)
            .into(imageview)
 */