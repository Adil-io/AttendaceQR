package com.anheart.attendanceqr

import android.content.Context
import android.media.AudioManager
import android.media.MediaActionSound
import android.os.Bundle
import android.os.CountDownTimer
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.CameraSource.CAMERA_FACING_FRONT
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_success.*
import kotlin.system.exitProcess


class SuccessActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null;
    private var timerDuration: Long = 4000;

    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        btn_end.isEnabled = false

        startTimer();

        btn_end.setOnClickListener {
            finishAffinity();
            exitProcess(0);
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(cameraSource!=null)
            cameraSource.stop()
    }

    private fun startTimer(){
        setupControls()

        countDownTimer = object: CountDownTimer(timerDuration,1000){
            override fun onTick(millisUntilFinished: Long) {
                tv_timer.text = "${(millisUntilFinished/1000).toString()}";
            }

            override fun onFinish() {
                tv_timer.startAnimation(AnimationUtils.loadAnimation(this@SuccessActivity,R.anim.fade_out))
                tv_timer.visibility = View.INVISIBLE

                tv_closeApp.startAnimation(AnimationUtils.loadAnimation(this@SuccessActivity,R.anim.fade_in))
                tv_closeApp.text = "You may now close the app"

                btn_end.isEnabled = true
                btn_end.startAnimation(AnimationUtils.loadAnimation(this@SuccessActivity,R.anim.fade_in))
                btn_end.visibility = View.VISIBLE

                val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                when (audio.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {
                        val sound = MediaActionSound()
                        sound.play(MediaActionSound.SHUTTER_CLICK)
                    }
                    AudioManager.RINGER_MODE_SILENT -> {
                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                    }
                }

                tv_cheese.visibility = View.GONE

                cameraSurfaceView.startAnimation(AnimationUtils.loadAnimation(this@SuccessActivity,R.anim.fade_out))
                if(cameraSource!=null)
                    cameraSource.stop()

                cameraSurfaceView.visibility = View.GONE

                Toast.makeText(this@SuccessActivity,"Image Captured",Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun setupControls(){
        detector = BarcodeDetector.Builder(this@SuccessActivity).build()
        cameraSource = CameraSource.Builder(this@SuccessActivity, detector).setFacing(CAMERA_FACING_FRONT).setAutoFocusEnabled(true).build()
        cameraSurfaceView.holder.addCallback(surfaceCallback)
        detector.setProcessor(processor)
    }

    private val surfaceCallback = object: SurfaceHolder.Callback {

        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            cameraSource.stop()
        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
            try{
                cameraSource.start(surfaceHolder)
            }
            catch(e: Exception){
                Toast.makeText(applicationContext,"Something went wrong",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val processor = object: Detector.Processor<Barcode>{

        override fun release() {

        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {

//            if(detections != null && detections.detectedItems.isNotEmpty()){
//                val qrCode: SparseArray<Barcode> = detections.detectedItems
//                val code = qrCode.valueAt(0)
////                tv_scanResult.text = code.displayValue
////                tv_scanResult.setTextColor(Color.GREEN)
////                url += "action=in&id=${code.displayV
//            }
//            else{
////                tv_scanResult.text = "Focus on valid QR Code"
////                tv_scanResult.setTextColor(Color.RED)
//            }
        }

    }
}


