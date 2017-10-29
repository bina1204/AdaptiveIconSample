package com.gsbina.adaptiveiconsample

import android.annotation.SuppressLint
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Path
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import com.gsbina.adaptiveiconsample.databinding.ActivityMainBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpIcons(createPathFromPathData(paths[pathIndex]))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_main, menu)
        return true
    }

    private var pathIndex: Int = 0
    private val paths: Array<Int> = arrayOf(R.string.mask_circle, R.string.mask_rounded_square, R.string.mask_empty)

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_shape -> setUpIcons(createPathFromPathData(paths[++pathIndex % 3]), true)
            R.id.action_foreground -> switchForegroundIcons(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private var foregroundVisible = true

    private fun switchForegroundIcons(item: MenuItem?) {
        foregroundVisible = !foregroundVisible
        setUpIcons(createPathFromPathData(paths[pathIndex % 3]))
        item?.setIcon(if (foregroundVisible) R.drawable.ic_visibility_24dp else R.drawable.ic_visibility_off_24dp)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private var subscribe: Disposable? = null

    private fun setUpIcons(path: Path?, force: Boolean = false) {
        if (subscribe != null && !subscribe?.isDisposed!!) {
            return
        }
        subscribe = getAdaptiveIcons(force)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toList()
                .subscribe(
                        { icons -> binding.iconContainer.adapter = AdaptiveIconAdapter(icons, path, foregroundVisible) }
                )
    }

    private val adaptiveIcons = ArrayList<AdaptiveIconDrawable>()

    private fun getAdaptiveIcons(force: Boolean = false): Observable<AdaptiveIconDrawable> {
        if (force || adaptiveIcons.isEmpty()) {
            adaptiveIcons.clear()
            val launcherIntent = Intent().apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            packageManager.getInstalledApplications(0).forEach { appInfo ->
                launcherIntent.`package` = appInfo.packageName
                if (packageManager.queryIntentActivities(launcherIntent, 0).size > 0) {
                    val icon = appInfo.loadIcon(packageManager)
                    if (icon is AdaptiveIconDrawable) {
                        adaptiveIcons += icon
                    }
                }
            }
        }

        return Observable.fromIterable(adaptiveIcons)
    }

    @SuppressLint("PrivateApi")
    private fun createPathFromPathData(pathResId: Int): Path? {
        val pathParserClass = Class.forName("android.util.PathParser")
        val method = pathParserClass?.getMethod(
                "createPathFromPathData", String::class.java)
        return method?.invoke(null, getString(pathResId)) as Path?
    }

    private class AdaptiveIconAdapter(val icons: List<AdaptiveIconDrawable>, val path: Path?, val visibleForeground: Boolean = true) : RecyclerView.Adapter<IconViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                IconViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.icon, parent, false))

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            holder.icon.setImageDrawable(icons[position])
            holder.icon.setForegroundVisibility(visibleForeground)
            path?.let {
                try {
                    holder.icon.setMask(path)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun getItemCount() = icons.size

    }

    private class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon = itemView as AdaptiveIconView
    }
}
