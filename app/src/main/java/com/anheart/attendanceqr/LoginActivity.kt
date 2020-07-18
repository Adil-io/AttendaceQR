package com.anheart.attendanceqr

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_attendance.*
import kotlinx.android.synthetic.main.activity_login.*
import java.net.URL


class LoginActivity : AppCompatActivity() {

    private var name: String? = "noFound"
    private var rollNo: String? = "noFound"

    private val PERMISSION_ID = 1234
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var sharedPreferences: SharedPreferences = MainActivity.sharedPreferences

    companion object{
        lateinit var mLatitude: String
        lateinit var mLongitude: String
    }

   // private var sUrl = "https://script.google.com/macros/s/AKfycbx6uKAfS8UVbZorUFlQZWIyCVdaJ-t1oASlnhtweNXjIHurePc8/exec?"
    private var sUrl = "https://script.google.com/macros/s/AKfycbxVhG1ewG2B_jMWMrYxpy1AduzqzcpkrCZkn2yc_s0asTj7DCk/exec?"

    private val scanner = IntentIntegrator(this)

    override fun onBackPressed() {
        Toast.makeText(this,"Can't go Back at this stage",Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_waveView.setAnimDuration(0)

        name = sharedPreferences.getString("NAME","noFound")
        rollNo = sharedPreferences.getString("ROLLNO","noFound")

        if(name != "noFound" || rollNo != "noFound"){
            iv_avatar.visibility = View.GONE
            tv_OTL.visibility = View.GONE
            iv_underline.visibility = View.GONE
            et_username.visibility = View.GONE
            et_rollNo.visibility = View.GONE
            tv_welcomeBack.text = "Welcome Back\n $name"
            tv_welcomeBack.visibility = View.VISIBLE
        }

//        setupWave()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@LoginActivity)

        getLastLocation()

        var isLoggedIn = et_username.visibility == View.GONE

        btn_submit.setOnClickListener {

            if(rollNo == "noFound" && name == "noFound"){
                rollNo = et_rollNo.text.toString()
                name = et_username.text.toString()
            }

            if((rollNo != "" && name != "") || isLoggedIn){
                login_waveView.startAnimation()

                    val editor = sharedPreferences.edit()
                    editor.putString("NAME",name)
                    editor.putString("ROLLNO",rollNo)

                    editor.apply()


                login_waveView.progressValue = 100
                login_waveView.setAmplitudeRatio(10)

                btn_submit.startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_out))
                btn_submit.visibility = View.INVISIBLE

                Handler().postDelayed(Runnable {
                    setupAttendanceLayout()
                }, 860)

            }
            else{
                Toast.makeText(this,"Fields cannot be Empty",Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun setupAttendanceLayout(){
        ll_Login.visibility = View.GONE
        setContentView(R.layout.activity_attendance)

//        attendance_waveView.setAnimDuration(3000)

        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        //starting the animation
        tv_Attendance.startAnimation(animation)
        btn_entrance.startAnimation(animation)
        btn_exit.startAnimation(animation)

        setScanner()

        btn_entrance.setOnClickListener {
            sUrl += "action=in&"
            scanner.initiateScan()
        }

        btn_exit.setOnClickListener {
            sUrl += "action=out&"
            scanner.initiateScan()
        }
    }

    private fun checkPermissions(): Boolean{
        return ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission") // To suppress warning of checking permissions as we are checking it
    private fun getLastLocation(){
        if(checkPermissions()){
            if(isLocationEnabled()){
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) {task->
                    var location: Location? = task.result
                    if(location == null){
                        requestNewLocationData()
                    }
                    else{
                        mLatitude = location.latitude.toString()
                        mLongitude = location.longitude.toString()
                    }
                }
            }
            else{
                showAlertDialog()

            }
        }
        else{
            askForPermissions()
        }
    }

    private fun showAlertDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Activate Location Services")
        builder.setMessage("Press Ok to enable location services")
        builder.setIcon(R.drawable.icon_four)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener{
            dialog, which ->
            run {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        })

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation

            mLatitude = mLastLocation.latitude.toString()
            mLongitude = mLastLocation.longitude.toString()
        }
    }

    private fun askForPermissions(){
        ActivityCompat.requestPermissions(this@LoginActivity,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_ID){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                btn_entrance.isClickable = true
                btn_exit.isClickable = true
            }
            else{
                Toast.makeText(applicationContext,"Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setScanner(){
        scanner.captureActivity = AnyOrientationCaptureActivity::class.java
        scanner.setPrompt("Place a QR Code in the BOX")
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        scanner.setBeepEnabled(true)
        scanner.setCameraId(0)
        scanner.setTimeout(20000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            val result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)

            if(result != null){

                if(result.contents == null){
                    Toast.makeText(this,"Invalid QR Code",Toast.LENGTH_SHORT).show()
                }
                else{
                    tv_loader.visibility = View.VISIBLE
                    ll_attendanceLayout.visibility = View.GONE

                    val makeRequest = MakeRequest(result.contents,mLatitude,mLongitude,sUrl)
                    sUrl = "https://script.google.com/macros/s/AKfycbxVhG1ewG2B_jMWMrYxpy1AduzqzcpkrCZkn2yc_s0asTj7DCk/exec?"
                    makeRequest.execute()
                }
            }
            else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    inner class MakeRequest(private val contents: String, private val latitude: String, private val longitude: String, private var sUrl: String) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            sUrl += "urlsrc=${contents}&lat=${latitude}&long=${longitude}&id=${rollNo}"
            Log.e("ReqURL",sUrl)

            return URL(sUrl).readText()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.e("URL",result)

            if(result == "" || result == null){
                Toast.makeText(this@LoginActivity,"Something went Wrong Try Again",Toast.LENGTH_SHORT).show()
            }
            else if (result != null) {
                if(result != "" && result != "NotFound" && !result.startsWith("<!")){
                    btn_entrance.isClickable = false
                    btn_exit.isClickable = false
                    Intent(this@LoginActivity,SuccessActivity::class.java).also {
                        startActivity(it)
                        finish()
                    }
                } else{
                    btn_entrance.isClickable = false
                    btn_exit.isClickable = false
                    Intent(this@LoginActivity,FailedActivity::class.java).also {
                        startActivity(it)
                        finish()
                    }
                }
            }
        }

    }

}