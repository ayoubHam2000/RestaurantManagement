package com.example.restaurantmanagement.Controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantmanagement.Adapters.SynchronizeAdapter
import com.example.restaurantmanagement.R
import com.example.restaurantmanagement.Services.RestaurantServices
import com.example.restaurantmanagement.Services.SynchronizeServices
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var settingAdapter : ArrayAdapter<CharSequence>
    lateinit var synchronizeAdapter : SynchronizeAdapter
    var buttonLock  = 0
    var onProcess = false

    private val lockTimeRange : Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //full size screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //get restaurant Id
        getSavedRestaurantId()

        //set up setting
        setUpSettingSpinner()

        //Buttons
        setUpSettingButtons()
        setUpAppsButtons()

        //lock app
        //disableStatusBar(this)
    }


    private fun getSavedRestaurantId(){
        val preferences = getSharedPreferences("label", 0)
        val storedId: String? = preferences.getString("shared_token", "")
        RestaurantServices.restaurantId = storedId!!
    }

    private fun setUpSettingSpinner(){

        settingAdapter = ArrayAdapter.createFromResource(this,
            R.array.setting,
            R.layout.support_simple_spinner_dropdown_item
        )
        settingAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        setting.adapter = settingAdapter
        setting?.setSelection(0)
        setting.onItemSelectedListener = this
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- Setting Items --#########################*/
    /*###################################################################*/
    /*###################################################################*/


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selected = parent?.getItemAtPosition(position).toString()
        setting?.setSelection(0)
        println(selected)
        when(selected){
            "Restaurant Information" -> openRestaurantInformationActivity()
            "Synchronization" -> setGetAllSynchronizeBuilder()
        }
    }

    private fun openRestaurantInformationActivity(){
        val intent = Intent(this, RestaurantInfo::class.java)
        startActivity(intent)
    }

    private fun setGetAllSynchronizeBuilder(){
        val message = R.string.synchronize

        tryAgain(message)
        tryAgain.setOnClickListener {
            tryAgain(message)
            setGetAllSynchronizeBuilder()
        }
        SynchronizeServices.getAllSynchronize(this){ success ->
            if(success){
                success()
                Log.d("Success", "success to get all synchronize")
                openSynchronizeWithRestaurant()
            }else{
                failed()
                Log.d("Failed", "failed to get all synchronize")
            }
        }
    }

    private fun openSynchronizeWithRestaurant(){
        val builder = AlertDialog.Builder(this)
        val builderView = layoutInflater.inflate(R.layout.synchronize_restaurant_dialog, null)
        builder.setView(builderView)
        val dialog = builder.create()

        dialog.setOnShowListener {

            synchronizeAdapter = SynchronizeAdapter(this, SynchronizeServices.synchronizeList){ id ->

                setAndSaveRestaurantId(id)
                dialog.dismiss()
            }
            val layoutManager = LinearLayoutManager(this)

            val recyclerView = builderView.findViewById<RecyclerView>(R.id.synchronizeRecyclerView)
            recyclerView.adapter = synchronizeAdapter
            recyclerView.layoutManager = layoutManager

        }

        dialog.show()
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun setAndSaveRestaurantId(id : String){
        RestaurantServices.restaurantId = id


        val preferences = getSharedPreferences("label", 0)
        val editor = preferences.edit()
        editor.putString("shared_token", id)
        editor.apply()
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################-- Buttons --###############################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun setUpAppsButtons(){
        adminButton.setOnClickListener {
            val sendIntent = packageManager.getLaunchIntentForPackage("com.example.restaurantapp")
            sendIntent?.putExtra("RestaurantId", "hello from this app")
            startActivity(sendIntent)
            finishAffinity()
        }
    }

    private fun setUpSettingButtons(){
        //Buttons
        openSetting.setOnClickListener {
            setting.performClick()
        }
        button1.setOnClickListener {
            buttonLock++
            Handler().postDelayed({ buttonLock = 0 }, lockTimeRange)
        }
        button2.setOnClickListener {
            if(buttonLock == 1){
                buttonLock++
            }
        }
        button3.setOnClickListener {
            if(buttonLock == 2){
                openSetting.visibility = View.VISIBLE
            }else if(buttonLock == 0){
                openSetting.visibility = View.INVISIBLE
            }
        }
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

    /*override fun onStop() {
        super.onStop()
        println("on stop")
        if(statusBar){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //spinner to element 0
        setting?.setSelection(0)
    }*/

    /*###################################################################*/
    /*###################################################################*/
    /*####################--Server Processing --#########################*/
    /*###################################################################*/
    /*###################################################################*/


    private fun failed(){

        progressBar.visibility = View.INVISIBLE
        tryAgain.visibility = View.VISIBLE
        onProcess = true
    }

    private fun tryAgain(message : Int){

        waitForProcessMain.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        tryAgain.visibility = View.INVISIBLE
        stepProgress.text = resources.getString(message)
        onProcess = true
    }

    private fun success(){

        waitForProcessMain.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        tryAgain.visibility = View.INVISIBLE
        onProcess = false
    }



    /*###################################################################*/
    /*###################################################################*/
    /*###########################-- Others --############################*/
    /*###################################################################*/
    /*###################################################################*/

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
    private fun makeMessageToast(theMessage : Int){
        Toast.makeText(this, theMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if(onProcess){
            success()
        }else{
            super.onBackPressed()
        }
    }

}
