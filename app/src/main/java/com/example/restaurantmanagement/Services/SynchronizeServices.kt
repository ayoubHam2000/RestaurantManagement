package com.example.restaurantmanagement.Services

import android.content.Context
import android.util.Log
import com.android.volley.Request.Method.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.restaurantmanagement.SYNCHRONIZE_URL
import com.example.restaurantmanagement.structur.SynchronizeStructur
import org.json.JSONObject

object SynchronizeServices {

    val synchronizeList = ArrayList<SynchronizeStructur>()
    var currentTime : Long = 10
    var synchronizeLifeTime : Long = 30

    fun getAllSynchronize(context : Context, complete : (Boolean) -> Unit){

        val getData = JsonObjectRequest(GET, SYNCHRONIZE_URL, null, Response.Listener { result ->

            synchronizeList.clear()
            val response = result.getJSONArray("result")
            currentTime = result.getLong("currentTime")
            synchronizeLifeTime = result.getLong("synchronizeLifeTime")
            for(x in 0 until response.length()){
                val id = response.getJSONObject(x).getString("id")
                val time  = response.getJSONObject(x).getLong("time")
                val newSynchronize = SynchronizeStructur(id, time)
                synchronizeList.add(newSynchronize)
            }
            complete(true)
            Log.d("Success", "success to get all synchronize")

        }, Response.ErrorListener {error ->
            Log.d("JsonERORR", "Failed to get all synchronize : => ${error.localizedMessage}")
            complete(false)
        } )

        Volley.newRequestQueue(context).add(getData)
    }

    fun postSynchronize(context: Context,synchronizeId : String, complete: (Boolean) -> Unit){

        val jsonObject = JSONObject()
        jsonObject.put("_id", synchronizeId)
        val requestBody = jsonObject.toString()


        val sendData = object : JsonObjectRequest(POST, SYNCHRONIZE_URL, null, Response.Listener {response ->
            synchronizeLifeTime = response.getLong("synchronizeLifeTime")
            println(response)
            complete(true)
            Log.d("Success", "success to post  synchronize")
        }, Response.ErrorListener {error ->
            Log.d("JsonERORR", "Failed to post synchronize : => ${error.localizedMessage}")
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

    fun deleteSynchronize(context: Context, id : String, complete: (Boolean) -> Unit){

        val url = "${SYNCHRONIZE_URL}/${id}"

        val sendData = JsonObjectRequest(DELETE, url, null, Response.Listener {
            Log.d("Success", "success to delete  synchronize")
            complete(true)
        }, Response.ErrorListener {
            Log.d("FAILED", "failed to delete  synchronize")
            complete(false)
        })

        Volley.newRequestQueue(context).add(sendData)
    }


}