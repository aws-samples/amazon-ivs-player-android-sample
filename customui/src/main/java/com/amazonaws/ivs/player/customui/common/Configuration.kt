package com.amazonaws.ivs.player.customui.common

object Configuration {
    const val TAG = "CustomUI"
    const val LINK = "https://fcc3ddae59ed.us-west-2.playback.live-video.net/api/video/v1/us-west-2.893648527354.channel.DmumNckWFTqz.m3u8"
    const val PORTRAIT_OPTION = "Live stream Portrait"
    const val LANDSCAPE_OPTION = "Recorded video Landscape"
    const val LIVE_PORTRAIT_LINK = "https://fcc3ddae59ed.us-west-2.playback.live-video.net/api/video/v1/us-west-2.893648527354.channel.YtnrVcQbttF0.m3u8"
    const val RECORDED_LANDSCAPE_LINK = "https://d6hwdeiig07o4.cloudfront.net/ivs/956482054022/cTo5UpKS07do/2020-07-13T22-54-42.188Z/OgRXMLtq8M11/media/hls/master.m3u8"
    const val AUTO = "Auto"
    val PlaybackRate = listOf("2.0", "1.5", "1.0", "0.5")

    const val PLAYBACK_RATE_DEFAULT = "1.0"

    const val HIDE_CONTROLS_DELAY = 5000L
}
