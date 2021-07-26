package com.example.restaurantmanagement.Services

import android.content.Context
import android.util.Log
import com.android.volley.Request.Method.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.restaurantmanagement.RESTAURANT_URL
import com.example.restaurantmanagement.structur.RestaurantStructur
import org.json.JSONObject

object RestaurantServices {

    var restaurantId = ""
    var restaurantInfo = RestaurantStructur(
        "",
        "Restaurant Name",
        "false",
        "MAD",
        "0",
        "0",
        "0"
    )

    fun getRestaurantInfo(context : Context, id : String, complete : (Boolean) -> Unit){

        val url = "${RESTAURANT_URL}/${id}"
        restaurantId = id

        val getData = JsonObjectRequest(GET, url, null, Response.Listener {response ->

            val name = response.getString("name")
            val english = response.getString("english")
            val priceUnit = response.getString("priceUnit")
            val tabletSyn = response.getString("tabletSyn")
            val categoriesNbm = response.getString("categoriesNbm")
            val productsNbm = response.getString("productsNbm")

            restaurantInfo = RestaurantStructur(id, name, english, priceUnit, tabletSyn, categoriesNbm, productsNbm)
            complete(true)

        }, Response.ErrorListener {error ->
            complete(false)
            Log.e("JsonERROR", "faied to get restaurant : error -> ${error.localizedMessage}")
        })

        Volley.newRequestQueue(context).add(getData)
    }


    fun postRestorant(context : Context, restaurant : RestaurantStructur, complete : (Boolean) -> Unit){

        val jsonBody = makeJsonBody(restaurant)
        val requestBody = jsonBody.toString()

        val sendData = object : JsonObjectRequest(POST, RESTAURANT_URL, null, Response.Listener {response ->

            restaurantId = response.getString("_id")
            Log.d("Success", "success to add restaurant")
            complete(true)

        }, Response.ErrorListener { error ->
            complete(false)
            Log.e("JsonERROR", "failed post restaurant error : ${error.localizedMessage}")
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(sendData)

    }

    private fun makeJsonBody(theRestaurant : RestaurantStructur) : JSONObject{
        val result = JSONObject()

        result.put("name", theRestaurant.name)
        result.put("english", theRestaurant.english)
        result.put("priceUnit", theRestaurant.priceUnit)
        result.put("tabletsSyn", 0)

        return result
    }

    fun patchRestaurant(context : Context, restaurant: RestaurantStructur, complete: (Boolean) -> Unit){

        val url = "${RESTAURANT_URL}/${restaurant.id}"
        val jsonBody = makePatchJsonRequest(restaurant)
        val requestBody = jsonBody.toString()


        val sendData = object : JsonObjectRequest(PATCH, url, null, Response.Listener {
            Log.d("Success", "success patch restaurant")
            complete(true)
        }, Response.ErrorListener {error ->
            Log.e("Failed", "Failed to patch restaurant error => { ${error.localizedMessage} }")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(sendData)

    }

    private fun makePatchJsonRequest(restaurant: RestaurantStructur) : JSONObject{
        val result = JSONObject()

        putValue("name", restaurant.name, result)
        putValue("english", restaurant.english, result)
        putValue("priceUnit", restaurant.priceUnit, result)
        putValue("tabletSyn", restaurant.tableSyn, result)
        putValue("categoriesNbm", restaurant.categoriesNum, result)
        putValue("productsNbm", restaurant.productsNum, result)
        print("sdas = > $result")

        return result
    }

    private fun putValue(tag : String, value : String, result : JSONObject) : JSONObject{
        if(value != ""){ result.put(tag, value) }
        return result
    }

}