package com.radroid.drawingapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.icu.text.LocaleDisplayNames.DialectHandling
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random
import java.util.*;
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var drawingView: DrawingView
    private lateinit var brushButton: ImageButton
    private lateinit var purpleButton: ImageButton
    private lateinit var redButton: ImageButton
    private lateinit var greenButton: ImageButton
    private lateinit var blueButton: ImageButton
    private lateinit var orangeButton: ImageButton
    private lateinit var coloPickerButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var undoButton: ImageButton
    private lateinit var saveButton: ImageButton

    private val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts
            .StartActivityForResult()
    ) { result ->
        findViewById<ImageView>(R.id.gallery_image).setImageURI(result.data?.data)
    }
    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted && permissionName == android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()

                    val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    if (permissionName == android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        brushButton = findViewById(R.id.brush_button)
        purpleButton = findViewById(R.id.purple_button)
        blueButton = findViewById(R.id.blue_button)
        redButton = findViewById(R.id.red_button)
        orangeButton = findViewById(R.id.orange_button)
        greenButton = findViewById(R.id.green_button)
        drawingView = findViewById(R.id.drawing_view)
        undoButton = findViewById(R.id.undo_button)
        coloPickerButton = findViewById(R.id.colorPicker_button)
        galleryButton = findViewById(R.id.gallery_button)
        saveButton=findViewById(R.id.save_button)
        drawingView.changeBrushSize(23.toFloat())
        brushButton.setOnClickListener {
            showBrushChooserDialog()
        }
        purpleButton.setOnClickListener(this)
        greenButton.setOnClickListener(this)
        redButton.setOnClickListener(this)
        orangeButton.setOnClickListener(this)
        blueButton.setOnClickListener(this)
        undoButton.setOnClickListener(this)
        coloPickerButton.setOnClickListener(this)
        galleryButton.setOnClickListener(this)
        saveButton.setOnClickListener(this)
    }

    private fun showBrushChooserDialog() {
        val brushDialog = Dialog(this@MainActivity)
        brushDialog.setContentView(R.layout.dialog_brush)
        val seekBarProgress = brushDialog.findViewById<SeekBar>(R.id.dialog_seekbar)
        val showProgressTv = brushDialog.findViewById<TextView>(R.id.dialog_text_view_progress)

        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, p1: Int, p2: Boolean) {
                drawingView.changeBrushSize(seekBar.progress.toFloat())
                showProgressTv.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })
        brushDialog.show()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.purple_button -> {
                drawingView.setColor("#803EA1")
            }
            R.id.red_button -> {
                drawingView.setColor("#E63A3A")
            }
            R.id.blue_button -> {
                drawingView.setColor("#285BC3")
            }
            R.id.orange_button -> {
                drawingView.setColor("#C3853A")
            }
            R.id.green_button -> {
                drawingView.setColor("#62A836")
            }
            R.id.undo_button -> {
                drawingView.undoPath()
            }
            R.id.colorPicker_button -> {
                showColorPickerDialog()
            }
            R.id.gallery_button -> {
                if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission()
                } else {
//                    get the image the below code will open the gallery
                    val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                }
            }
            R.id.save_button->{
//                save image
                if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission()
                }else{
                    val layout=findViewById<ConstraintLayout>(R.id.constraint_l3)
                    val bitmap=getBitMapFromView(layout)
                    CoroutineScope(IO).launch {
                        saveImage(bitmap)
                    }

                }
            }
        }
    }

    private fun showColorPickerDialog() {
        val dialog = AmbilWarnaDialog(this, Color.RED, object : OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                TODO("Not yet implemented")
            }
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.setColor(color)
            }
        })
        dialog.show()
    }
    //    function if user denied the permission so this dialog box will appear to tell user why this permission necessary
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationalDialog()
        } else {
            requestPermission.launch(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        }
    }
    private fun showRationalDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Storage Permission")
            .setMessage("we need this permission in order to access the internal storage")
            .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                requestPermission.launch(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitMapFromView(view: View):Bitmap{
        val bitmap=Bitmap.createBitmap(view.width,view.height,
                     Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    private suspend fun saveImage(bitmap: Bitmap){
        val root=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val myDir= File("$root/saved_images")
        myDir.mkdir()
        val generator=java.util.Random()
        var n=10000
        n=generator.nextInt(n)
        val outputFile=File(myDir,"Images-$n.jpg")
        if(outputFile.exists()){
            outputFile.delete()
        }else{
            try {
                val out=FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,out)
                out.flush()
                out.close()
            }catch (e:java.lang.Exception){
                e.stackTrace
            }
            withContext(Main){
                Toast.makeText(this@MainActivity, "${outputFile.absolutePath} saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
}