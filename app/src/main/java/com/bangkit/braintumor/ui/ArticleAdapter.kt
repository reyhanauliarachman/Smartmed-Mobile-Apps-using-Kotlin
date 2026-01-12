package com.example.capstonefinal.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.braintumor.R
import com.bangkit.braintumor.data.ArticlesItem
import com.bumptech.glide.Glide

class ArticleAdapter(private val articles: List<ArticlesItem>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val articleImage: ImageView = view.findViewById(R.id.articleImage)
        val articleTitle: TextView = view.findViewById(R.id.articleTitle)
        val detailLink: Button = view.findViewById(R.id.detailLink) // Menambahkan tombol
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.article_item, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]

        // Mengubah title dari API untuk deskripsi
        holder.articleTitle.text = article.title ?: "No Title"

        // Memuat gambar artikel
        Glide.with(holder.itemView.context)
            .load(article.urlToImage)
            .placeholder(R.drawable.ic_capture_24)
            .into(holder.articleImage)

        // Set up the "Baca Selengkapnya" button
        holder.detailLink.setOnClickListener {
            val url = article.url // Ambil URL dari artikel
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = articles.size
}
