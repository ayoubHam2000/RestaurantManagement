package com.example.restaurantmanagement.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantmanagement.R
import com.example.restaurantmanagement.Services.SynchronizeServices
import com.example.restaurantmanagement.structur.SynchronizeStructur
import kotlin.collections.ArrayList

class SynchronizeAdapter(private val context : Context, val listSynchronize : ArrayList<SynchronizeStructur>, val onClick : (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.synchronize_recyclerview, parent, false )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSynchronize.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    inner class ViewHolder(viewItem : View?) : RecyclerView.ViewHolder(viewItem!!){

        //resources
        private val synchronizedId = viewItem?.findViewById<TextView>(R.id.synchronizeId)
        private val restTimeDisplay = viewItem?.findViewById<TextView>(R.id.restTimeDisplay)

        fun bindView(position : Int){
            synchronizedId?.text = listSynchronize[position].id
            changeTime(restTimeDisplay, position)


            synchronizedId?.setOnClickListener {
                onClick(listSynchronize[position].id)
            }
        }
    }



    private fun changeTime(textView : TextView?, position : Int){

        val mainHandler = Handler(Looper.getMainLooper())
        var continu = true

        mainHandler.post(object : Runnable {
            override fun run() {
                if(continu && position < listSynchronize.size){
                    listSynchronize[position].time--
                    val different = SynchronizeServices.currentTime - listSynchronize[position].time
                    restTime(textView, different){complete ->
                        if(complete){
                            mainHandler.removeCallbacksAndMessages(null)
                            continu = false
                            deleteSynchronize(listSynchronize[position].id)
                        }
                    }
                    if (continu) mainHandler.postDelayed(this, 1000)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun restTime(textView : TextView?,different : Long, complete : (Boolean) -> Unit) {

        if(different == SynchronizeServices.synchronizeLifeTime){
            complete(true)
        }else{
            val minute = different / 60
            val second =  (different % 60)
            textView?.text = "${numberDijit(minute)} : ${numberDijit(second)}"
        }
    }

    private fun deleteSynchronize(id : String){
        for(item in SynchronizeServices.synchronizeList){
            if(item.id == id){
                SynchronizeServices.synchronizeList.remove(item)
                notifyDataSetChanged()
                break
            }
        }
    }

    private fun numberDijit(nbr : Long) : String{
        return if(nbr / 10 == 0L){
            "0$nbr"
        }else{
            "$nbr"
        }
    }
}