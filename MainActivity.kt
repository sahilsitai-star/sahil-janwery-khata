package com.sahiljanwery.khata
import android.app.*
import android.os.Bundle
import android.graphics.Color
import android.content.*
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

data class Account(val id:Long,val name:String,var opening:Double,var balance:Double)

class MainActivity: Activity() {
    lateinit var box: LinearLayout
    val prefs by lazy { getSharedPreferences("db",0) }
    val blue=Color.rgb(37,99,235)
    val accounts= mutableListOf<Account>()

    override fun onCreate(b:Bundle?){super.onCreate(b); load(); dashboard()}
    fun load(){ val n=prefs.getInt("n",0); for(i in 0 until n){val id=prefs.getLong("id$i",i.toLong());accounts.add(Account(id,prefs.getString("name$i","Khata")!!,prefs.getFloat("open$i",0f).toDouble(),prefs.getFloat("bal$i",0f).toDouble()))}}
    fun save(){val e=prefs.edit().clear();e.putInt("n",accounts.size);accounts.forEachIndexed{i,a->e.putLong("id$i",a.id).putString("name$i",a.name).putFloat("open$i",a.opening.toFloat()).putFloat("bal$i",a.balance.toFloat())};e.apply()}
    fun txt(s:String,size:Float=16f,bold:Boolean=false):TextView{val t=TextView(this);t.text=s;t.textSize=size;t.setTextColor(Color.rgb(25,35,55));t.setPadding(12,12,12,12);if(bold)t.setTypeface(null,1);return t}
    fun button(s:String, click:()->Unit):Button{val b=Button(this);b.text=s;b.setTextColor(Color.WHITE);b.setBackgroundColor(blue);b.setOnClickListener{click()};return b}
    fun dashboard(){
        box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(18,18,18,90)
        val scroll=ScrollView(this);scroll.addView(box)
        val root=LinearLayout(this);root.orientation=LinearLayout.VERTICAL;root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this);nav.orientation=LinearLayout.HORIZONTAL
        listOf("🏠 Home","➕ Entry","🏪 Khate","📊 Reports").forEachIndexed{i,s->{val b=Button(this);b.text=s;b.setOnClickListener{when(i){0->dashboard();1->entry();2->khate();3->reports()}};nav.addView(b,LinearLayout.LayoutParams(0,60,1f))}}
        root.addView(nav);setContentView(root)
        box.addView(txt("Sahil Janwery",24f,true));box.addView(txt("My Expense & Khata",14f))
        val cards=GridLayout(this);cards.columnCount=2
        val items=arrayOf("💰 Salary / Income","🧾 Expenses","🏪 Khate","💳 Payments","📒 Opening Dues","📊 Reports")
        items.forEachIndexed{i,s->{val b=Button(this);b.text=s;b.textSize=15f;b.setOnClickListener{when(i){0->entryType("income");1->entryType("expense");2->khate();3->entryType("paid");4->khate();5->reports()}};cards.addView(b,GridLayout.LayoutParams().apply{width=0;height=130;columnSpec=GridLayout.spec(i%2);rowSpec=GridLayout.spec(i/2);setMargins(8,8,8,8)})}}
        box.addView(cards)
        val dues=accounts.sumOf{maxOf(0.0,it.balance)}
        box.addView(txt("کل واجب الادا: ${fmt(dues)}",19f,true));box.addView(txt("کھاتے: ${accounts.size}",16f))
    }
    fun entryType(type:String="due"){entry()}
    fun entry(){
        box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(18,18,18,90)
        val scroll=ScrollView(this);scroll.addView(box);setContentView(scroll)
        box.addView(txt("➕ New Entry",23f,true));box.addView(txt("Sahil Janwery • صرف entry کریں، balance خود update ہوگا",14f))
        if(accounts.isEmpty()){box.addView(txt("پہلے Khate میں ایک کھاتہ بنائیں۔"));box.addView(button("🏪 Khate"){khate()});return}
        val sp=Spinner(this);sp.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,accounts.map{it.name});box.addView(sp)
        val types=arrayOf("ادھار لیا / خرچہ بڑھا","ادائیگی کی","Salary / Income","اپنا خرچہ");val ts=Spinner(this);ts.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,types);box.addView(ts)
        val amount=EditText(this);amount.hint="رقم";amount.inputType=2;box.addView(amount)
        val note=EditText(this);note.hint="تفصیل";box.addView(note)
        box.addView(button("💾 Entry Save"){val x=amount.text.toString().toDoubleOrNull()?:0.0;if(x<=0){toast("رقم لکھیں");return@button};val a=accounts[sp.selectedItemPosition];when(ts.selectedItemPosition){0->a.balance+=x;1->a.balance-=x};save();toast("Entry محفوظ ہوگئی");dashboard()})
        box.addView(button("⬅ Home"){dashboard()})
    }
    fun khate(){
        box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(18,18,18,90);val scroll=ScrollView(this);scroll.addView(box);setContentView(scroll)
        box.addView(txt("🏪 Khate / دکاندار",23f,true));box.addView(txt("Opening Balance پہلے کا بقایا ہے۔",14f))
        accounts.forEach{a->box.addView(txt("${a.name}\nOpening: ${fmt(a.opening)}   •   باقی: ${fmt(a.balance)}",17f,true));box.addView(button("➕ Entry"){entryFor(a.id)})}
        box.addView(button("➕ نیا کھاتہ"){newAccount()});box.addView(button("⬅ Home"){dashboard()})
    }
    fun entryFor(id:Long){entry()}
    fun newAccount(){
        val l=LinearLayout(this);l.orientation=LinearLayout.VERTICAL;val n=EditText(this);n.hint="دکاندار / کھاتے کا نام";val o=EditText(this);o.hint="Opening Balance";o.inputType=2;l.addView(n);l.addView(o)
        AlertDialog.Builder(this).setTitle("نیا کھاتہ").setView(l).setPositiveButton("Save"){_->val name=n.text.toString().trim();if(name.isNotEmpty()){val v=o.text.toString().toDoubleOrNull()?:0.0;accounts.add(Account(System.currentTimeMillis(),name,v,v));save();khate()}}.setNegativeButton("Cancel",null).show()
    }
    fun reports(){box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(18,18,18,90);val s=ScrollView(this);s.addView(box);setContentView(s);box.addView(txt("📊 Reports",23f,true));accounts.forEach{box.addView(txt("${it.name}: ${fmt(it.balance)} باقی",17f,true))};box.addView(button("⬅ Home"){dashboard()})}
    fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_SHORT).show()}
    fun fmt(x:Double)="%.2f".format(x)
}
