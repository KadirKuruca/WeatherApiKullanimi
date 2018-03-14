package com.kadirkuruca.havadurumu

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var location : SimpleLocation? = null
    var latitude : String? = null
    var longitude : String? = null
    var tvsehir : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.sehirler,R.layout.spn_tek_satir)
        spnSehirler.background.setColorFilter(resources.getColor(R.color.colorAccent),PorterDuff.Mode.SRC_ATOP)
        spnSehirler.setTitle("Şehir Seçin")
        spnSehirler.setPositiveButton("İPTAL")
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnSehirler.adapter = spinnerAdapter

        spnSehirler.setOnItemSelectedListener(this)

        spnSehirler.setSelection(9)
        verileriGetir("Ankara")
    }

    private fun lokasyonGetir(latitude: String?, longitude : String?) {

        var sehir : String ? = null
        val url = "http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=abbcd2fcfec741ec783669c98b7f39d1&lang=tr&units=metric"
        val havaDurumuObjeRequest2 = JsonObjectRequest(Request.Method.GET,url,null,object:Response.Listener<JSONObject>{

            override fun onResponse(response: JSONObject?) {

                var main = response?.getJSONObject("main")
                var sicaklik = main?.getInt("temp")
                sehir = response?.getString("name")
                var weather = response?.getJSONArray("weather")
                var durum = weather?.getJSONObject(0)?.getString("description")
                var icon = weather?.getJSONObject(0)?.getString("icon")
                Log.e("Sehir"," "+sehir)
                tvsehir?.setText(sehir)

                if(icon?.last() == 'd'){

                    rootLayout.background = getDrawable(R.drawable.bg)
                }else{

                    rootLayout.background = getDrawable(R.drawable.gece)
                }

                var resimDosyaAdi = resources.getIdentifier("icon_"+icon?.sonKarakteriSil(),"drawable",packageName) // R.drawable.icon_50n

                imgDurum.setImageResource(resimDosyaAdi)

                tvSicaklik.text = sicaklik.toString()
                tvDurum.text = durum.toString()
                tvTarih.text = tarihYazdir()

            }

        },object:Response.ErrorListener{

            override fun onErrorResponse(error: VolleyError?) {

            }

        })

        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest2)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        tvsehir = view as TextView?

        if(position == 0){

            location = SimpleLocation(this)

            if(!location!!.hasLocationEnabled()){
                spnSehirler.setSelection(9)
                Toast.makeText(this,"Konumunuza Erişmek İçin GPS in Açılması Gereklidir.",Toast.LENGTH_SHORT).show()
                SimpleLocation.openSettings(this)
            }
            else{
                if(checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),60)
                }else{
                    location = SimpleLocation(this)
                    latitude = String.format("%.6f",location?.latitude)
                    longitude = String.format("%.6f",location?.longitude)

                    var sehirKonum = lokasyonGetir(latitude,longitude)
                }
            }
        }
        else{
            var secilenSehir = parent?.getItemAtPosition(position).toString()
            verileriGetir(secilenSehir)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == 60)
        {
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                location = SimpleLocation(this)
                latitude = String.format("%.6f",location?.latitude)
                longitude = String.format("%.6f",location?.longitude)
                var sehirKonum = lokasyonGetir(latitude,longitude)
            }
            else{
                spnSehirler.setSelection(9)
                Toast.makeText(this,"Konumunuza Erişmek İçin İzin Gereklidir.",Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    fun verileriGetir(sehir : String){

        val url = "http://api.openweathermap.org/data/2.5/weather?q="+sehir+"&appid=abbcd2fcfec741ec783669c98b7f39d1&lang=tr&units=metric"
        val havaDurumuObjeRequest = JsonObjectRequest(Request.Method.GET,url,null,object:Response.Listener<JSONObject>{

            override fun onResponse(response: JSONObject?) {

                var main = response?.getJSONObject("main")
                var sicaklik = main?.getInt("temp")
                var sehir = response?.getString("name")
                var weather = response?.getJSONArray("weather")
                var durum = weather?.getJSONObject(0)?.getString("description")
                var icon = weather?.getJSONObject(0)?.getString("icon")

                if(icon?.last() == 'd'){

                    rootLayout.background = getDrawable(R.drawable.bg)
                }else{

                    rootLayout.background = getDrawable(R.drawable.gece)
                }

                var resimDosyaAdi = resources.getIdentifier("icon_"+icon?.sonKarakteriSil(),"drawable",packageName) // R.drawable.icon_50n

                imgDurum.setImageResource(resimDosyaAdi)

                tvSicaklik.text = sicaklik.toString()
                tvDurum.text = durum.toString()
                tvTarih.text = tarihYazdir()

            }

        },object:Response.ErrorListener{

            override fun onErrorResponse(error: VolleyError?) {

            }

        })

        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest)
    }

    fun tarihYazdir() : String{

        var takvim = Calendar.getInstance().time
        var formatla = SimpleDateFormat("EEEE, MMMM yyyy", Locale("tr"))
        var tarih = formatla.format(takvim)

        return tarih
    }
}

private fun String.sonKarakteriSil(): String {

    return this.substring(0,this.length-1) // 50 n ifadesinden n yi siler.
}
