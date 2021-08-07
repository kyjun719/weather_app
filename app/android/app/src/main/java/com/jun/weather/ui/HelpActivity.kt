package com.jun.weather.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jun.weather.R
import com.jun.weather.databinding.ActivityHelpBinding
import java.util.*

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_help)

        initView()
    }

    private fun initView() {
        binding.btnBack.setOnClickListener { v: View? -> onBackPressed() }

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
        private var context: Context? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.component_help_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == 0) {
                holder.layout_item_2.visibility = View.GONE
                val image1 = holder.layout_item_1.findViewById<ImageView>(R.id.item_image1)
                val image2 = holder.layout_item_1.findViewById<ImageView>(R.id.item_image2)
                //Glide.with(this).load(nowWeatherModel.nowSkyDrawableId).into((ImageView) mActivity.findViewById(R.id.image_sky));
                Glide.with(context!!).load(
                        BitmapFactory.decodeStream(context!!.resources.openRawResource(R.raw.help_image1))
                ).into(image1)
                Glide.with(context!!).load(
                        BitmapFactory.decodeStream(context!!.resources.openRawResource(R.raw.help_image2))
                ).into(image2)
            } else if (position == 1) {
                holder.layout_item_1.visibility = View.GONE
            }
            holder.onBind(dataList[position])
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title_help: TextView
            private val btn_more: ImageButton
            private val text_content: TextView
            val layout_item_1: ConstraintLayout
            val layout_item_2: ConstraintLayout
            private val layout_content: ConstraintLayout
            fun onBind(data: Data) {
                title_help.text = data.title
                if (data.content != null) {
                    text_content.text = data.content
                }
            }

            init {
                title_help = itemView.findViewById(R.id.title_help)
                btn_more = itemView.findViewById(R.id.btn_more)
                text_content = itemView.findViewById(R.id.text_content)
                layout_content = itemView.findViewById(R.id.layout_content)
                layout_item_1 = itemView.findViewById(R.id.layout_item_1)
                layout_item_2 = itemView.findViewById(R.id.layout_item_2)
                layout_content.visibility = View.GONE
                btn_more.setOnClickListener { v: View? ->
                    if (layout_content.visibility == View.GONE) {
                        btn_more.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up)
                        layout_content.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        layout_content.visibility = View.VISIBLE
                                    }
                                })
                    } else {
                        btn_more.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down)
                        layout_content.visibility = View.GONE
                        layout_content.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .setListener(null)
                    }
                }
            }
        }
    }

    private class Data {
        var title: String? = null
        var content: String? = null
    }
}