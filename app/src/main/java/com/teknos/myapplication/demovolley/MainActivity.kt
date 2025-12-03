package com.teknos.myapplication.demovolley

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // --- CONFIG ---
    private val URL_GET = "http://10.0.2.2:8081/api/pizzas"
    private val URL_POST = "http://10.0.2.2:8081/api/pizzas"

    // --- UI ELEMENTS ---
    private lateinit var containerPizzas: LinearLayout
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var fabAdd: ExtendedFloatingActionButton

    private val queue: RequestQueue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind Views
        containerPizzas = findViewById(R.id.containerPizzas)
        progressBar = findViewById(R.id.progressBar)
        fabAdd = findViewById(R.id.fabAdd)

        // FAB Click -> Open the "Add Pizza" Sheet
        fabAdd.setOnClickListener {
            showAddPizzaBottomSheet()
        }

        // Initial Load
        obtenirLlistaPizzes()
    }

    // --- LOGIC: GET DATA ---
    private fun obtenirLlistaPizzes() {
        setLoading(true)

        // Clear previous list to avoid duplicates
        containerPizzas.removeAllViews()

        val peticio = JsonArrayRequest(Request.Method.GET, URL_GET, null,
            { response ->
                setLoading(false)
                mostrarLlista(response)
            },
            { error ->
                setLoading(false)
                Toast.makeText(this, "Error de connexió", Toast.LENGTH_SHORT).show()
                Log.e("VOLLEY", "Error: ${error.message}")
            }
        )
        queue.add(peticio)
    }

    // --- LOGIC: SHOW DATA (Expressive Cards) ---
    private fun mostrarLlista(jsonArray: JSONArray) {
        try {
            // If empty
            if (jsonArray.length() == 0) {
                Toast.makeText(this, "No hi ha pizzes encara", Toast.LENGTH_SHORT).show()
                return
            }

            for (i in 0 until jsonArray.length()) {
                val pizza = jsonArray.getJSONObject(i)
                val nom = pizza.optString("nom", "Sense nom")
                val preu = pizza.optDouble("preu", 0.0)
                val ingredients = pizza.optString("ingredients", "-")

                // Inflate the Card Layout
                val cardView = LayoutInflater.from(this).inflate(R.layout.item_pizza_card, containerPizzas, false)

                // Fill Data
                cardView.findViewById<TextView>(R.id.tvNomPizza).text = nom.uppercase()
                cardView.findViewById<TextView>(R.id.tvIngredients).text = ingredients
                cardView.findViewById<TextView>(R.id.chipPreu).text = "$preu €"

                // Add to container with animation
                containerPizzas.addView(cardView)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    // --- LOGIC: INPUT FORM (Bottom Sheet) ---
    private fun showAddPizzaBottomSheet() {
        // 1. Inflate the bottom sheet layout
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_pizza, null)
        dialog.setContentView(view)

        // 2. Bind elements inside the sheet
        val etNom = view.findViewById<TextInputEditText>(R.id.etNomSheet)
        val etPreu = view.findViewById<TextInputEditText>(R.id.etPreuSheet)
        val etIng = view.findViewById<TextInputEditText>(R.id.etIngredientsSheet)
        val btnSave = view.findViewById<Button>(R.id.btnSaveSheet)

        // 3. Button Action
        btnSave.setOnClickListener {
            val nom = etNom.text.toString()
            val preuString = etPreu.text.toString()
            val ingredients = etIng.text.toString()

            if (nom.isNotEmpty() && preuString.isNotEmpty()) {
                enviarNovaPizza(nom, preuString, ingredients)
                dialog.dismiss() // Close the sheet
            } else {
                etNom.error = "Obligatori"
            }
        }

        dialog.show()
    }

    // --- LOGIC: POST DATA ---
    private fun enviarNovaPizza(nom: String, preu: String, ingredients: String) {
        setLoading(true)

        val jsonBody = JSONObject()
        try {
            jsonBody.put("nom", nom)
            jsonBody.put("preu", preu.toDouble())
            jsonBody.put("ingredients", ingredients)
        } catch (e: JSONException) { e.printStackTrace() }

        val peticioPost = JsonObjectRequest(Request.Method.POST, URL_POST, jsonBody,
            { response ->
                setLoading(false)
                Toast.makeText(this, "Pizza guardada!", Toast.LENGTH_SHORT).show()
                // Refresh list automatically to show the new item
                obtenirLlistaPizzes()
            },
            { error ->
                setLoading(false)
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(peticioPost)
    }

    // --- HELPER: LOADING STATE ---
    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            // Optionally hide the FAB while loading
            fabAdd.hide()
        } else {
            progressBar.visibility = View.GONE
            fabAdd.show()
        }
    }
}