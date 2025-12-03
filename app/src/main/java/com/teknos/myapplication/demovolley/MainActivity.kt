package com.teknos.myapplication.demovolley

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest // IMPORTANT: Request per POST
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // 1. URL GET: Per llegir la llista (La que ja tens de Mocky)
    private val URL_GET = "http://10.0.2.2:8081/api/pizzas" // <--- POSA LA TEVA URL DE MOCKY AQUÍ

    // 2. URL POST: Per a la demo amb Mocky, podem fer servir la mateixa o una nova que retorni { "status": "ok" }
    // Quan tingueu el servidor del company, serà la mateixa (ex: .../api/pizzas)
    private val URL_POST = "http://10.0.2.2:8081/api/pizzas" // <--- POSA LA MATEIXA URL DE MOMENT

    private lateinit var etNom: EditText
    private lateinit var etPreu: EditText
    private lateinit var etIngredients: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCarregar: Button
    private lateinit var txtResultat: TextView
    private lateinit var txtStatus: TextView

    private val queue: RequestQueue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vinculem vistes
        etNom = findViewById(R.id.etNom)
        etPreu = findViewById(R.id.etPreu)
        etIngredients = findViewById(R.id.etIngredients)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCarregar = findViewById(R.id.btnCarregar)
        txtResultat = findViewById(R.id.txtResultat)
        txtStatus = findViewById(R.id.txtStatus)

        // Listener del botó GET
        btnCarregar.setOnClickListener {
            obtenirLlistaPizzes()
        }

        // Listener del botó POST
        btnGuardar.setOnClickListener {
            enviarNovaPizza()
        }

        obtenirLlistaPizzes()
    }

    // FUNCIÓ GET (LLEGIR)
    private fun obtenirLlistaPizzes() {
        txtStatus.text = "Carregant llista..."

        val peticio = JsonArrayRequest(Request.Method.GET, URL_GET, null,
            { response ->
                txtStatus.text = "Llista actualitzada!"
                mostrarLlista(response)
            },
            { error ->
                txtStatus.text = "Error al carregar"
                Toast.makeText(this, "Error GET: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.i("ttt"," ${error.message}");
            }
        )
        queue.add(peticio)
    }

    // FUNCIÓ POST (ESCRIURE)
    private fun enviarNovaPizza() {
        val nom = etNom.text.toString()
        val preuString = etPreu.text.toString()
        val ingredients = etIngredients.text.toString()

        if (nom.isEmpty() || preuString.isEmpty()) {
            Toast.makeText(this, "Omple els camps obligatoris!", Toast.LENGTH_SHORT).show()
            return
        }

        txtStatus.text = "Enviant dades al servidor..."

        // 1. Creem l'objecte JSON amb les dades (El "Body" de la petició)
        val jsonBody = JSONObject()
        try {
            jsonBody.put("nom", nom)
            jsonBody.put("preu", preuString.toDouble())
            jsonBody.put("ingredients", ingredients)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // 2. Creem la petició POST
        // Nota: Fem servir JsonObjectRequest (no Array) perquè enviem un objecte { }
        val peticioPost = JsonObjectRequest(Request.Method.POST, URL_POST, jsonBody,
            { response ->
                // ÈXIT (200 OK)
                txtStatus.text = "Guardat amb èxit!"
                Toast.makeText(this, "Pizza enviada al núvol!", Toast.LENGTH_LONG).show()

                // Netegem el formulari
                etNom.text.clear()
                etPreu.text.clear()
                etIngredients.text.clear()


                // Afegim manualment la pizza al textview
                val textActual = txtResultat.text.toString()
                val novaLinia = "\n[NOVA] 🍕 ${nom.uppercase()} (${preuString}€)\n-----------------"
                txtResultat.text = novaLinia + textActual
            },
            { error ->
                // ERROR
                txtStatus.text = "Error al guardar"
                Toast.makeText(this, "Error POST: ${error.message}", Toast.LENGTH_LONG).show()

            }
        )

        queue.add(peticioPost)
    }

    private fun mostrarLlista(jsonArray: JSONArray) {
        val sb = StringBuilder()
        try {
            for (i in 0 until jsonArray.length()) {
                val pizza = jsonArray.getJSONObject(i)
                val nom = pizza.optString("nom", "Sense nom")
                val preu = pizza.optDouble("preu", 0.0)
                val ingredients = pizza.optString("ingredients", "-")

                sb.append("🍕 ${nom.uppercase()}\n")
                sb.append("   💰 $preu € | 📝 $ingredients\n")
                sb.append("--------------------------------------\n")
            }
            txtResultat.text = sb.toString()
        } catch (e: JSONException) { e.printStackTrace() }
    }
}