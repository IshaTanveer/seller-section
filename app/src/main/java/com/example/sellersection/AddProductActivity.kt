package com.example.sellersection

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sellersection.databinding.ActivityAddProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DatabaseReference

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var database: DatabaseReference
    private lateinit var imageUriString: String
    private var availability: Boolean = false
    private var dangerous: Boolean = false
    private lateinit var selectedWeightUnit: String
    private lateinit var category: String

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUriString = uri.toString()
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            binding.productImages.setImageURI(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backToTools.setOnClickListener{
            finish()
        }
        toggleElements()
        manageCategorySpinner()
        shippingInfoDialog()
        descriptionDialog()
        getData()
    }

    private fun descriptionDialog() {
        binding.productDescription.setOnClickListener{
            val descriptionDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.description_layout, null)
            descriptionDialog.setContentView(view)
            descriptionDialog.show()
            val back: ImageView = view.findViewById(R.id.backToTools)
            back.setOnClickListener{
                descriptionDialog.dismiss()
            }
        }
    }

    private fun getData() {
        binding.productImages.setOnClickListener{
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.saveProduct.setOnClickListener{
            var name = binding.productName.text.toString()
            var price = binding.productPrice.text.toString()
            var stock = binding.productStock.text.toString()
            manageCategorySpinner()
            if (listOf(name, category, price, stock).any { it.isEmpty() })
                Toast.makeText(this, "One of the required fields is empty", Toast.LENGTH_SHORT).show()
            else
                addProductToDB(name, category, price, stock)
        }
    }

    private fun manageCategorySpinner() {
        val spinner: Spinner = findViewById(R.id.productCategory)
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent != null) {
                    category = parent.getItemAtPosition(position).toString()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

    }

    private fun shippingInfoDialog(){
        binding.productShippingInfo.setOnClickListener{
            val shippingInfoDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.shipping_information, null)
            shippingInfoDialog.setContentView(view)
            shippingInfoDialog.show()

            manageWeightSpinner(view)
            val back: ImageView = view.findViewById(R.id.backToTools)
            back.setOnClickListener{
                shippingInfoDialog.dismiss()
            }
        }
    }

    private fun manageWeightSpinner(view: View) {
        val weightSpinner: AppCompatSpinner = view.findViewById(R.id.weightSpinner)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("kg", "g")
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weightSpinner.adapter = adapter

        weightSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent != null) {
                    selectedWeightUnit = parent.getItemAtPosition(position).toString()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun toggleElements() {
        binding.productAvailability.setOnCheckedChangeListener{_, isChecked ->
            availability = isChecked
            if (availability) {
                Toast.makeText(this, "available", Toast.LENGTH_SHORT).show()
                binding.productAvailability.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkBlue))
                binding.productAvailability.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.codeBlue))
            }
            else {
                Toast.makeText(this, "not available", Toast.LENGTH_SHORT).show()
                binding.productAvailability.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey))
                binding.productAvailability.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightGrey))
            }
        }
        binding.dangerousGoods.setOnCheckedChangeListener{_, isChecked ->
            dangerous = isChecked
            if (dangerous) {
                Toast.makeText(this, "dangerous", Toast.LENGTH_SHORT).show()
                binding.dangerousGoods.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkBlue))
                binding.dangerousGoods.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.codeBlue))
            }
            else {
                Toast.makeText(this, "not dangerous", Toast.LENGTH_SHORT).show()
                binding.dangerousGoods.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey))
                binding.dangerousGoods.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightGrey))
            }
        }
    }

    private fun addProductToDB(name: String, category: String, price: String, stock: String) {
        Toast.makeText(this, "data saved", Toast.LENGTH_SHORT).show()
    }
}