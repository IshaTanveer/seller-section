package com.example.sellersection

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sellersection.databinding.ActivityAddProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase


class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var selectedWeightUnit: String
    private val product = Products()
    private val shipInfo = ShippingInfo()


    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        product.photo = uri.toString()
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

    @SuppressLint("InflateParams", "MissingInflatedId")
    private fun descriptionDialog() {
        binding.productDescription.setOnClickListener{
            val descriptionDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.description_layout, null)
            descriptionDialog.setContentView(view)
            descriptionDialog.show()

            val desc: EditText = view.findViewById(R.id.description)
            val saveDescription: AppCompatButton = view.findViewById(R.id.saveDescription)
            saveDescription.setOnClickListener{
                product.description = desc.text.toString()
                Toast.makeText(this, "description saved", Toast.LENGTH_SHORT).show()
                descriptionDialog.dismiss()
            }

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
            product.name = binding.productName.text.toString()
            product.price = binding.productPrice.text.toString().toIntOrNull() ?: 0
            product.stock = binding.productStock.text.toString().toIntOrNull() ?: 0
            if (product.name.isBlank() || product.category.isBlank() || product.photo.isBlank()
                || product.price == 0 || product.stock == 0)
                Toast.makeText(this, "One of the required fields is empty", Toast.LENGTH_SHORT).show()
            else if(product.description.isBlank())
                Toast.makeText(this, "PLease write the description of the product", Toast.LENGTH_SHORT).show()
            else if(shipInfo.pkgWeight <= 0F || shipInfo.pkgLength <= 0F && shipInfo.pkgWidth <= 0F && shipInfo.pkgHeight <= 0F){
                Toast.makeText(this, "PLease enter complete Shipping Information", Toast.LENGTH_SHORT).show()
            }
            else{
                val firebaseDatabase = FirebaseDatabase.getInstance("https://ecom-652e8-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val productReference = firebaseDatabase.getReference("product")
                val productId = productReference.push().key
                Log.d("adding Product to firebase db", "Attempting to write product data...")
                if (productId != null) {
                    productReference.child(productId).setValue(product)
                        .addOnSuccessListener {
                            Log.d("adding product", "Write successful")
                            Toast.makeText(this@AddProductActivity, "product added successfully!", Toast.LENGTH_SHORT).show()
                            productReference.child(productId).child("shippingInfo").setValue(shipInfo)
                                .addOnSuccessListener {
                                    Log.d("adding shipping info", "Shipping info added to product")
                                    Toast.makeText(this@AddProductActivity, "shipping info added!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    Log.e("FirebaseError", "Error adding shipping info: ${error.message}")
                                }
                        }
                        .addOnFailureListener { error ->
                            Log.e("FirebaseError", "Error: ${error.message}")
                            Toast.makeText(this@AddProductActivity, "Failed to add product: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                Toast.makeText(this, "data saved", Toast.LENGTH_SHORT).show()
            }
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
                    product.category = parent.getItemAtPosition(position).toString()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun shippingInfoDialog(){
        binding.productShippingInfo.setOnClickListener{
            val shippingInfoDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.shipping_information, null)
            shippingInfoDialog.setContentView(view)
            shippingInfoDialog.show()

            manageWeightSpinner(view)
            val back: ImageView = view.findViewById(R.id.backToTools)

            val saveShipInfo: AppCompatButton = view.findViewById(R.id.saveShippingInfo)
            val pkgWeight: EditText = view.findViewById(R.id.packageWeight)
            val pkgLength: EditText = view.findViewById(R.id.pkgLength)
            val pkgWidth: EditText = view.findViewById(R.id.pkgWidth)
            val pkgHeight: EditText = view.findViewById(R.id.pkgHeight)
            saveShipInfo.setOnClickListener{
                shipInfo.pkgWeight = pkgWeight.text.toString().toFloatOrNull() ?: 0f
                shipInfo.pkgLength  = pkgLength.text.toString().toFloatOrNull() ?: 0f
                shipInfo.pkgWidth  = pkgWidth.text.toString().toFloatOrNull() ?: 0f
                shipInfo.pkgHeight  = pkgHeight.text.toString().toFloatOrNull() ?: 0f
                if(shipInfo.pkgWeight > 300){
                    shipInfo.pkgWeight = 300.0F
                    pkgWeight.setText(shipInfo.pkgWeight.toString())
                }
                if(shipInfo.pkgLength > 300){
                    shipInfo.pkgLength = 300.0F
                    pkgLength.setText(shipInfo.pkgLength.toString())
                }
                if(shipInfo.pkgWidth > 300){
                    shipInfo.pkgWidth = 300.0F
                    pkgWidth.setText(shipInfo.pkgWidth.toString())
                }
                if(shipInfo.pkgHeight > 300){
                    shipInfo.pkgHeight = 300.0F
                    pkgHeight.setText(shipInfo.pkgHeight.toString())
                }
                if(shipInfo.pkgWeight > 0F && shipInfo.pkgLength > 0F && shipInfo.pkgWidth > 0F && shipInfo.pkgHeight > 0F){
                    Toast.makeText(this, "shipping Info saved", Toast.LENGTH_SHORT).show()
                    shippingInfoDialog.dismiss()
                }
            }

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
            product.availability = isChecked
            if (product.availability) {
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
    }
}