package ru.alox1d.customviewtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.alox1d.customviewtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TestView.Listener {

    private lateinit var binding: ActivityMainBinding

    private val menuList = listOf(
        "Add", "Edit", "Paint", "Clear", "Delete", "Exit", "Hello", "Draw"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testView.listener = this
    }

    override fun onClick(index: Int) {
        binding.tvSelection.text = menuList[index]
    }
}