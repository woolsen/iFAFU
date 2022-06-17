package cn.ifafu.ifafu.ui.upload

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import cn.ifafu.ifafu.databinding.InformationActivityShowPictureBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

/**
 * Activity that gets transitioned to
 */
class ShowPictureActivity : AppCompatActivity() {

    private lateinit var binding: InformationActivityShowPictureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InformationActivityShowPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        intent.getParcelableExtra<Uri>("uri")?.also {
            Glide.with(this)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivPhoto)
        }
        intent.getStringExtra("url")?.also {
            Glide.with(this)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivPhoto)
        }
        binding.ivPhoto.setOnClickListener { finishAfterTransition() }
    }

    companion object {
        fun intentFor(context: Context, uri: Uri): Intent {
            val intent = Intent(context, ShowPictureActivity::class.java)
            intent.putExtra("uri", uri)
            return intent
        }

        fun intentFor(context: Context, url: String): Intent {
            val intent = Intent(context, ShowPictureActivity::class.java)
            intent.putExtra("url", url)
            return intent
        }
    }
}