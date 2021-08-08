package com.jun.weather.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jun.weather.R
import com.jun.weather.databinding.ActivityHelpBinding
import com.jun.weather.databinding.ComponentHelpItemBinding
import java.util.*

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_help)

        initView()
    }

    private fun initView() {
        binding.btnBack.setOnClickListener { onBackPressed() }

        val customRecyclerAdapter = CustomRecyclerAdapter()
        val data1 = Data()
        data1.title = "앱 사용법"
        val data2 = Data()
        data2.title = "라이센스"
        data2.content = """
            ${getString(R.string.data_author)}
            ${getString(R.string.icon_author)}
            """.trimIndent()

        customRecyclerAdapter.dataList.add(data1)
        customRecyclerAdapter.dataList.add(data2)
        val recyclerView = findViewById<RecyclerView>(R.id.list_help)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this,
                LinearLayoutManager(this).orientation)
        ResourcesCompat.getDrawable(resources, R.drawable.help_item_divider, null)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.adapter = customRecyclerAdapter
    }

    private class CustomRecyclerAdapter : RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder>() {
        var dataList: MutableList<Data> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ViewHolder(DataBindingUtil.inflate(inflater, R.layout.component_help_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(position, dataList[position])
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        open class ViewHolder(val binding: ComponentHelpItemBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.layoutContent.visibility = View.GONE
                binding.btnMore.setOnClickListener {
                    if (binding.layoutContent.visibility == View.GONE) {
                        binding.btnMore.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up)
                        binding.layoutContent.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        binding.layoutContent.visibility = View.VISIBLE
                                    }
                                })
                    } else {
                        binding.btnMore.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down)
                        binding.layoutContent.visibility = View.GONE
                        binding.layoutContent.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .setListener(null)
                    }
                }
            }

            fun onBind(pos: Int, data: Data) {
                if(pos == 0) {
                    setItem0()
                }
                if(pos == 1) {
                    setItem1()
                }
                binding.titleHelp.text = data.title
                if (data.content != null) {
                    binding.textContent.text = data.content
                }
            }

            private fun setItem0() {
                val context = binding.root.context
                binding.layoutItem2.visibility = View.GONE

                val image1 = binding.layoutItem1.findViewById<ImageView>(R.id.item_image1)
                val image2 = binding.layoutItem1.findViewById<ImageView>(R.id.item_image2)
                Glide.with(context).load(
                        BitmapFactory.decodeStream(context.resources.openRawResource(R.raw.help_image1))
                ).into(image1)
                Glide.with(context).load(
                        BitmapFactory.decodeStream(context.resources.openRawResource(R.raw.help_image2))
                ).into(image2)
            }

            private fun setItem1() {
                binding.layoutItem1.visibility = View.GONE
            }
        }
    }

    private class Data {
        var title: String? = null
        var content: String? = null
    }
}