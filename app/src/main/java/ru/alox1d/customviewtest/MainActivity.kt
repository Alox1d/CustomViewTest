package ru.alox1d.customviewtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import ru.alox1d.customviewtest.databinding.ActivityMainBinding
import ru.alox1d.customviewtest.ui.AvatarImageView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val stId = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createCustomViewDynamically()
        binding.btnBorder.setOnClickListener {
            binding.aiv.setBorderWidth((2..10).random())
        }
        binding.btnColor.setOnClickListener {
            binding.aiv.setBorderColor((AvatarImageView.bgColors).random())
        }
    }

    private fun createCustomViewDynamically() {
        val view = AvatarImageView(this).apply {
            id = stId
            layoutParams = LinearLayout.LayoutParams(1000, 1000)
            setImageResource(R.drawable.ava_big)
        }
        binding.container.addView(view)
    }
}