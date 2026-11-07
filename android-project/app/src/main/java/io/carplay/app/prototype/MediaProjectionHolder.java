package io.carplay.app.prototype;

/**
 * 保存用户在「录制或投放」对话框里授权后返回的 MediaProjection token。
 *
 * 用途：AudioPlaybackCapture 需要 MediaProjection，而 MediaProjection 只能由 Activity
 * 通过 startActivityForResult 拿到，Service 无法自行申请。因此 Activity 把结果放到这里，
 * Service 在 startPcmCapture() 里取用。
 *
 * 背景：CarPlay 的音频流要传的是「手机里正在播放的音乐」，不是麦克风环境声。
 * OcarPlay 0.8.53 的 PCM 由 ColorOS 宿主 native 层直接喂入（其 manifest 无 RECORD_AUDIO /
 * MEDIA_PROJECTION / CAPTURE_AUDIO_OUTPUT），该通路第三方应用不可用；
 * 公开 API 中唯一能拿到其他应用媒体音频的方案就是 AudioPlaybackCapture + MediaProjection。
 */
public final class MediaProjectionHolder {

    /** Activity 回调的 resultCode（RESULT_OK = -1），0 表示尚未授权。 */
    private static volatile int resultCode = 0;

    /** Activity 回调返回的 Intent，getMediaProjection 需要它。用后置空（token 一次性）。 */
    private static volatile android.content.Intent resultData;

    /** 已创建的 MediaProjection 实例，整个进程复用，避免重复 getMediaProjection 抛异常。 */
    private static volatile android.media.projection.MediaProjection projection;

    private MediaProjectionHolder() {
    }

    public static synchronized void set(int code, android.content.Intent data) {
        resultCode = code;
        resultData = data;
        projection = null;
    }

    /** 只要还有 token 或已建好的实例，就可以走内录路径。 */
    public static boolean ready() {
        return resultData != null || projection != null;
    }

    /**
     * 取出（或首次创建）MediaProjection。
     * 返回 null 表示没有可用授权，调用方应回退到麦克风采集。
     */
    public static synchronized android.media.projection.MediaProjection obtain(android.content.Context context) {
        if (projection != null) {
            return projection;
        }
        if (resultData == null) {
            return null;
        }
        try {
            android.media.projection.MediaProjectionManager manager =
                    (android.media.projection.MediaProjectionManager)
                            context.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE);
            if (manager == null) {
                return null;
            }
            projection = manager.getMediaProjection(resultCode, resultData);
        } catch (Throwable failure) {
            projection = null;
        } finally {
            // token 只能用一次，无论成败都清掉，避免下次拿到过期 token。
            resultData = null;
            resultCode = 0;
        }
        return projection;
    }

    public static synchronized void clear() {
        resultData = null;
        resultCode = 0;
        projection = null;
    }
}
