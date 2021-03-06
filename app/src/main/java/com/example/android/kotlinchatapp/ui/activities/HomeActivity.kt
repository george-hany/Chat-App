package com.example.android.kotlinchatapp.ui.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.android.kotlinchatapp.ui.model.User
import com.example.android.kotlinchatapp.R
import com.example.android.kotlinchatapp.ui.chats.ChatsFragment
import com.example.android.kotlinchatapp.ui.profile.ProfileFragment
import com.example.android.kotlinchatapp.ui.fragments.UsersFragment
import com.example.android.kotlinchatapp.ui.login.LoginActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.profile_image
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class HomeActivity : AppCompatActivity() {
    lateinit var reference: DatabaseReference
    internal var firebaseUser: FirebaseUser? = null
    lateinit var mFirebaseDatabase: FirebaseDatabase
    lateinit var mAuth: FirebaseAuth
    internal var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
     var userID: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        mAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        reference = mFirebaseDatabase.reference
        firebaseUser = mAuth.currentUser
        userID = firebaseUser!!.uid
        reference=FirebaseDatabase.getInstance().getReference("Users").child(userID?:"")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            Toasty.error(applicationContext,p0.message,Toasty.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user = dataSnapshot.getValue(User::class.java)
                username.text = user!!.userName?:""
//                if (user.imageURL == "default")
//                    profile_image.setImageResource(R.mipmap.ic_launcher_round)
//                else
                    Glide.with(applicationContext).load(user.imageURL).error(R.drawable.profile_default_icon).into(profile_image)
                    progress_bar.visibility=GONE

                //profile_image.setImageResource(R.mipmap.ic_launcher);
            }
        })
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        viewPagerAdapter.addFragment(UsersFragment(), "Users")
        viewPagerAdapter.addFragment(ProfileFragment(), "Profile")

        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                val i = Intent(this, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                setStatus("offline")
                return true
            }
        }
        return false

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setStatus(status: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser?.uid!!)
        val hash: java.util.HashMap<String, String> = java.util.HashMap<String, String>()

        val date = LocalDateTime.now()
        val formater = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
        val formatedDate = date.format(formater)
        hash.put("status", status)
        reference.child("status").setValue(status)
        reference.child("lastSeen").setValue(formatedDate)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
//        setStatus("online")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
//        setStatus("online")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
//        setStatus("offline")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
//        setStatus("offline")

    }

//    override fun onStart() {
//        super.onStart()
//        mAuth.addAuthStateListener(mAuthStateListener!!)
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (mAuthStateListener != null) {
//            mAuth.removeAuthStateListener(mAuthStateListener!!)
//        }
//    }




    internal inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>

        init {
            this.fragments = ArrayList()
            this.titles = ArrayList()
        }

        override fun getItem(i: Int): Fragment {
            return fragments[i]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(f: Fragment, t: String) {
            fragments.add(f)
            titles.add(t)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}
