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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_attendance.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.slide_item_container.*

class MainActivity : AppCompatActivity() {

    private val PERMISSION_ID = 1234
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    companion object{
        lateinit var sharedPreferences: SharedPreferences
        private var username: String? = "noFound"
        private var userRollNo: String? = "noFound"
    }

    private val introSliderAdapter = IntroSliderAdapter(
        listOf(
            IntroSlide(
                "UNIQUE ID VERIFICATION",
                "One time secure login with unique ID verification.",
                R.drawable.icon_one
            ),
            IntroSlide(
                "QR CODE SCANNING",
                "Quick & Efficient way to scan while keeping a safe distance.",
                R.drawable.icon_two
            ),
            IntroSlide(
                "ATTENDANCE TIME STAMP",
                "Check in & Check out time is marked as soon as the code is scanned.",
                R.drawable.icon_three
            ),
            IntroSlide(
                "GEO LOCATION TAGGING",
                "Check in & Check out location is also marked as soon as the code is scanned.",
                R.drawable.icon_four
            ),
            IntroSlide(
                "AUTOMATED SELFIES",
                "Automatic selfie camera activates as soon as the code is scanned.",
                R.drawable.icon_five
            ),
            IntroSlide(
                "Welcome!!",
                "Allow Permissions & Get Started",
                R.drawable.welcome_logo
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        sharedPreferences = getSharedPreferences("SP_info", Context.MODE_PRIVATE)

        //LoginActivity
        username = sharedPreferences.getString("NAME","noFound")
        userRollNo = sharedPreferences.getString("ROLLNO","noFound")

//        if(checkPermissions()){
//            btn_welcome.visibility = View.VISIBLE
//            if(btn_welcome.visibility == View.VISIBLE)
//                setBtnWelcome()
//        }

        if(username == "noFound" && userRollNo == "noFound"){
            // IntroSlider
            introSliderViewPager.adapter = introSliderAdapter
            setupIndicators()
            setCurrentIndicator(0)
            setupIntroSlider()
        }else{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun setupIntroSlider(){
        introSliderViewPager.registerOnPageChangeCallback(object:
            ViewPager2.OnPageChangeCallback(){

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    setCurrentIndicator(position)

                    val transformer = ViewPager2.PageTransformer { page, position ->
                        page.apply {
                            val r = 1 - Math.abs(position)
                            page.alpha = 0.25f + r
                            page.scaleY = 0.75f + r * 0.25f
                        }
                    }
0
                    introSliderViewPager.setPageTransformer(transformer)

                    Log.e("POS",introSliderViewPager.currentItem.toString())
                    Log.e("POS",introSliderAdapter.itemCount.toString())

                    if(checkPermissions() && introSliderViewPager.currentItem + 1 == introSliderAdapter.itemCount){
                        setBtnWelcome()
                    }
                    else{
                        btn_welcome.setOnClickListener {
                            getLastLocation()
                        }
                    }

//                    if(introSliderViewPager.currentItem + 1 < introSliderAdapter.itemCount){
//                        indicatorsContainer.visibility = View.VISIBLE
//                    }
//                    else{
//                        indicatorsContainer.visibility = View.GONE
//                    }



                }


            }

//                btn_welcome.setOnClickListener{
//            if(introSliderViewPager.currentItem + 1 < introSliderAdapter.itemCount){
//                introSliderViewPager.currentItem += 1
//            }
//            else{
//
//            }
//        }
//
//        textSkipIntro.setOnClickListener {
//            Intent(applicationContext,AnotherActivity::class.java).also {
//                startActivity(it)
//                finish()
//            }
//        }
        )
    }

    private fun setupIndicators(){
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount-1)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8,0,8,0)

        for(i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            indicatorsContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int){
        val childCount = indicatorsContainer.childCount

        for(i in 0 until childCount){
            val imageView = indicatorsContainer[i] as ImageView
            if(i == index){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            }
            else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if(resultCode == LOGIN_CODE){
//            if(requestCode == Activity.RESULT_OK){
//                if(data!=null) {
//                    username = data.getStringExtra("USERNAME")
//                    userRollNo = data.getStringExtra("ROLLNO")
//
//                    finish()
//                }
//                else{
//                    Toast.makeText(this,"Failed to Get the DATA",Toast.LENGTH_SHORT).show()
//                }
//            }
//            else{
//                Toast.makeText(this,"Something went wrong while Logging",Toast.LENGTH_SHORT).show()
//            }
//        }
//        else if(resultCode == Activity.RESULT_CANCELED){
//            Toast.makeText(this, "Result Cancelled",Toast.LENGTH_SHORT).show()
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }

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
                        LoginActivity.mLatitude = location.latitude.toString()
                        LoginActivity.mLongitude = location.longitude.toString()
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

            LoginActivity.mLatitude = mLastLocation.latitude.toString()
            LoginActivity.mLongitude = mLastLocation.longitude.toString()
        }
    }

    private fun askForPermissions(){
        ActivityCompat.requestPermissions(this@MainActivity,
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
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                setBtnWelcome()
            }
            else{
                Toast.makeText(applicationContext,"Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setBtnWelcome(){

        if(!checkPermissions()){
            val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            btn_welcome.startAnimation(animation)
        }

        val editor = sharedPreferences.edit()
        editor.putBoolean("GETSTARTED",true)
        editor.apply()

        btn_welcome.text = "GET Started"
        btn_welcome.setBackgroundResource(R.drawable.btn_wel_main)

        btn_welcome.setOnClickListener {
            Intent(this@MainActivity,LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

}