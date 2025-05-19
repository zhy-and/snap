import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.snap.intelligence.musics.local.randomBitmap
import jp.wasabeef.glide.transformations.CropCircleTransformation

val options: RequestOptions = RequestOptions()
    .placeholder(randomBitmap())
    .error(randomBitmap())
    .transform(RoundedCorners(SizeUtils.dp2px(6f)))


val artOptions1: RequestOptions = RequestOptions()
//    .override(SizeUtils.dp2px(50f),SizeUtils.dp2px(50f))
//    .centerCrop()
    .transform(RoundedCorners(SizeUtils.dp2px(6f)))
//    .downsample(DownsampleStrategy.CENTER_INSIDE)
//    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .placeholder(randomBitmap())
    .error(randomBitmap())

val artOptions2: RequestOptions = RequestOptions()
//    .override(SizeUtils.dp2px(135f),SizeUtils.dp2px(135f))
//    .centerCrop()
    .transform(RoundedCorners(SizeUtils.dp2px(6f)))

//    .downsample(DownsampleStrategy.CENTER_INSIDE)
//    .diskCacheStrategy(DiskCacheStrategy.ALL)
//    .placeholder(randomBitmap())
//    .error(randomBitmap())


val artOptions3: RequestOptions = RequestOptions()
//    .override(SizeUtils.dp2px(189f),SizeUtils.dp2px(115f))
    .centerCrop()
    .transform(RoundedCorners(SizeUtils.dp2px(6f)))
//    .downsample(DownsampleStrategy.CENTER_INSIDE)
//    .diskCacheStrategy(DiskCacheStrategy.ALL)
//    .placeholder(randomBitmap())
//    .error(randomBitmap())
//    .downsample(DownsampleStrategy.CENTER_INSIDE)
//    .diskCacheStrategy(DiskCacheStrategy.ALL)
//    .placeholder(randomBitmap())
//    .error(randomBitmap())




val options2: RequestOptions = RequestOptions()
    .transform(RoundedCorners(SizeUtils.dp2px(6f)))

val roundOptions = RequestOptions().placeholder(randomBitmap())
    .error(randomBitmap()).transform(CropCircleTransformation())


val roundOptions2 = RequestOptions()
//    .override(SizeUtils.dp2px(92f),SizeUtils.dp2px(92f))
//    .centerCrop()
    .transform(CropCircleTransformation())
//    .downsample(DownsampleStrategy.CENTER_INSIDE)
//    .diskCacheStrategy(DiskCacheStrategy.ALL)
//    .placeholder(randomBitmap())
//    .error(randomBitmap())