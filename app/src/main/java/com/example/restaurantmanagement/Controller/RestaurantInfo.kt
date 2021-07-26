package com.example.restaurantmanagement.Controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantmanagement.Adapters.SynchronizeAdapter
import com.example.restaurantmanagement.R
import com.example.restaurantmanagement.Services.RestaurantServices
import com.example.restaurantmanagement.Services.SynchronizeServices
import com.example.restaurantmanagement.structur.RestaurantStructur
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_restaurant_info.*
import kotlinx.android.synthetic.main.activity_restaurant_info.progressBar
import kotlinx.android.synthetic.main.activity_restaurant_info.stepProgress
import kotlinx.android.synthetic.main.activity_restaurant_info.tryAgain
import kotlinx.android.synthetic.main.activity_restaurant_info.waitForProcessMain

class RestaurantInfo : AppCompatActivity() {

    lateinit var priceUnitAdapter : ArrayAdapter<CharSequence>

    var serverProcessing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //full size screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_restaurant_info)

        //getRestaurantInformation
        if(RestaurantServices.restaurantId != ""){
            getRestaurantInfo()
        }

        //buttons
        setUpButtons()

        //disableStatusBar
        //disableStatusBar(this)
    }



    /*###################################################################*/
    /*###################################################################*/
    /*###########################-- Buttons --############################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun setUpButtons(){
        //post / patch button
        postPatchActualRestaurant.setOnClickListener {
            postOrPatchRestaurant()
        }

        //synchronize
        synchronizeButton.setOnClickListener {
            if(RestaurantServices.restaurantId != ""){
                setPostSynchronize(RestaurantServices.restaurantId)
            }
        }
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- synchronize  --##########################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun setPostSynchronize(RestaurantId : String){
        val message = R.string.setUpSynchronize
        tryAgain(message)
        tryAgain.setOnClickListener {
            tryAgain(message)
            setPostSynchronize(RestaurantId)
        }

        SynchronizeServices.postSynchronize(this, RestaurantId){complete ->
            if(complete){
                success()
                Log.d("Success", "success to post synchronize")
                setSynchronizeWaiteView()
            }else{
                failed()
                Log.d("Failed", "failed to post synchronize")
            }
        }
    }

    private fun setSynchronizeWaiteView(){
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.synchronize_waite_dialog, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.setOnShowListener {
            Handler().postDelayed({
                dialog.dismiss()
            }, SynchronizeServices.synchronizeLifeTime * 1000L)
        }

        dialog.setOnDismissListener {
            setDeleteSynchronize(RestaurantServices.restaurantId)
        }
        dialog.show()
    }

    private fun setDeleteSynchronize(RestaurantId : String){
        val message = R.string.deleteSynchronize
        tryAgain(message)
        tryAgain.setOnClickListener{
            setDeleteSynchronize(RestaurantId)
            tryAgain(message)
        }
        SynchronizeServices.deleteSynchronize(this, RestaurantId){complete ->
            if(complete){
                success()
                Log.d("Success", "success to delete synchronize")
            }else{
                failed()
                Log.d("Failed", "failed to delete synchronize")
            }
        }
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- post or patch  --########################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun postOrPatchRestaurant() {

        //set up builder
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.add_restaurant_dialog, null)
        builder.setView(dialogView).setPositiveButton("ok", null)
        val dialog = builder.create()

        dialog.setOnShowListener {

            //resources
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val restaurantNameInput = dialogView.findViewById<EditText>(R.id.restaurantNameInput)
            val restaurantEnglishInput = dialogView.findViewById<Switch>(R.id.restaurantEnglishInput)
            val restaurantPriceUnitInput = dialogView.findViewById<Spinner>(R.id.restaurantPriceUnitInput)

            //update
            setUpPriceUnitSpinner(restaurantPriceUnitInput)
            if(RestaurantServices.restaurantId != ""){
                restaurantNameInput.setText(RestaurantServices.restaurantInfo.name)
                restaurantEnglishInput.isChecked = RestaurantServices.restaurantInfo.english.toBoolean()
                restaurantPriceUnitInput.setSelection(searchPriceUnitByString(RestaurantServices.restaurantInfo.priceUnit))
            }

            //on press ok
            okButton.setOnClickListener {
                val name = restaurantNameInput.text.toString()
                val english = restaurantEnglishInput.isChecked.toString()
                val priceUnit = searchPriceUnitByInt(restaurantPriceUnitInput.selectedItemPosition)

                val newRestaurant = RestaurantStructur(RestaurantServices.restaurantId, name, english, priceUnit,
                    "", "", "")
                if(RestaurantServices.restaurantId != ""){
                    setPatchRestaurant(newRestaurant)
                }else{
                    setPostRestaurant(newRestaurant)
                }
                dialog.dismiss()
            }


        }
        dialog.show()
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    private fun setUpPriceUnitSpinner(theSpinner : Spinner){
        priceUnitAdapter = ArrayAdapter.createFromResource(this,
            R.array.price_unit,
            R.layout.support_simple_spinner_dropdown_item)

        priceUnitAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        theSpinner.adapter = priceUnitAdapter
        theSpinner?.setSelection(0)
    }

    private fun searchPriceUnitByString(str : String) : Int{
        return when(str){
            "USD" -> 1
            "EUR" -> 2
            else -> 0
        }
    }

    private fun searchPriceUnitByInt(nbr : Int) : String{
        return when(nbr){
            1 -> "USD"
            2 -> "EUR"
            else -> "MAD"
        }
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- server methods --########################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun getRestaurantInfo(){
        val message = R.string.getRestaurantInfo
        tryAgain(message)
        tryAgain.setOnClickListener {
            tryAgain(message)
            getRestaurantInfo()
        }

        RestaurantServices.getRestaurantInfo(this, RestaurantServices.restaurantId){success ->
            if(success){
                success()
                restaurantIdSet.text = RestaurantServices.restaurantId
                restaurantNameSet.text = RestaurantServices.restaurantInfo.name
                restaurantEnglishSet.text = RestaurantServices.restaurantInfo.english
                restaurantPriceUnitSet.text = RestaurantServices.restaurantInfo.priceUnit
                RestaurantTabletesSet.text = RestaurantServices.restaurantInfo.tableSyn
                restaurantCategoriesSet.text = RestaurantServices.restaurantInfo.categoriesNum
                restaurantProductsSet.text = RestaurantServices.restaurantInfo.productsNum

                postPatchActualRestaurant.setImageResource(R.drawable.write)
                makeMessageToast(R.string.successGetRestaurant)
                Log.d("Success", "Success get restaurant info")
            }else{
                failed()
                makeMessageToast(R.string.failedGetRestaurant)
                Log.d("Failed", "Failed to get restaurant info")
            }
        }
    }

    private fun setPostRestaurant(restaurant: RestaurantStructur){
        val message = R.string.postRestaurant
        tryAgain(message)
        tryAgain.setOnClickListener {
            tryAgain(message)
            setPostRestaurant(restaurant)
        }

        RestaurantServices.postRestorant(this, restaurant){success ->
            if(success){
                success()
                Log.d("Success", "success to patch restaurant")
                makeMessageToast(R.string.successRegisterRestaurant)
                getRestaurantInfo()
            }else{
                failed()
                makeMessageToast(R.string.failedRegisterRestaurant)
                Log.d("Failed", "Failed to patch restaurant")
            }
        }
    }

    private fun setPatchRestaurant(restaurant : RestaurantStructur){
        val message = R.string.postRestaurant
        tryAgain(message)
        tryAgain.setOnClickListener {
            tryAgain(message)
            setPostRestaurant(restaurant)
        }

        RestaurantServices.patchRestaurant(this, restaurant) { success ->
            if(success){
                success()
                Log.d("Success", "success to post restaurant")
                makeMessageToast(R.string.successModifyRestaurant)
                getRestaurantInfo()
            }else{
                failed()
                makeMessageToast(R.string.failedModifyRestaurant)
                Log.d("Failed", "Failed to post restaurant")
            }
        }
    }

    /*###################################################################*/
    /*###################################################################*/
    /*####################--Server Processing --#########################*/
    /*###################################################################*/
    /*###################################################################*/


    private fun failed(){

        serverProcessing = true
        progressBar.visibility = View.INVISIBLE
        tryAgain.visibility = View.VISIBLE
    }

    private fun tryAgain(message : Int){

        serverProcessing = true
        waitForProcessMain.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        tryAgain.visibility = View.INVISIBLE
        stepProgress.text = resources.getString(message)
    }

    private fun success(){

        serverProcessing = false
        waitForProcessMain.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        tryAgain.visibility = View.INVISIBLE
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- Lock APP  --#############################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun disableStatusBar(context: Context){

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                setDisableStatusBar()
                mainHandler.postDelayed(this, 10000)
            }
        })
    }



    private fun setDisableStatusBar(){
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        this.sendBroadcast(it)
    }

    override fun onStop() {
        super.onStop()
        /*println("on stop")
        if(statusBar){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }*/
    }

    /*###################################################################*/
    /*###################################################################*/
    /*###########################-- Others --############################*/
    /*###################################################################*/
    /*###################################################################*/


    private fun makeMessageToast(theMessage : Int){
        Toast.makeText(this, theMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if(serverProcessing){
            success()
        }else{
            super.onBackPressed()
        }
    }

}
