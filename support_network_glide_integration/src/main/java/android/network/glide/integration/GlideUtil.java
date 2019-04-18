package android.network.glide.integration;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bumptech.glide.load.model.GlideUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Create by LingYan on 2018-09-27
 */
public class GlideUtil {
    private static final String WITH_SUFFIX = "?";
    private static final String FILTER_SUFFIX = "?x-oss-process=style/pre";
    private static final String FORMAT_URL = "%1$s?x-oss-process=image/resize,m_fill,h_%2$d,w_%3$d";
    // 目前阿里支持图片缩放的样式， jpg、png、bmp、gif、webp、tiff
    private List<String> imageType = new ArrayList<>();

    private static GlideUtil glideUtil;

    private List<String> urls = new ArrayList<>();

    private GlideUtil() {
        imageType.add("jpg");
        imageType.add("png");
        imageType.add("bmp");
        imageType.add("webp");
        imageType.add("tiff");
    }

    /**
     * 支持图片缩放的样式
     */
    public GlideUtil addImageType(String type) {
        if (!imageType.contains(type)) {
            imageType.add(type);
        }
        return this;
    }

    /**
     * 过滤图片地址
     */
    public GlideUtil addWhiteUrl(String url) {
        if (!urls.contains(url)) {
            urls.add(url);
        }
        return this;
    }

    public boolean isWhiteUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            for (String _url : urls) {
                if (url.contains(_url)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 图片地址在白名单内就会改变请求地址，否则不改变
     */
    public GlideUrl changeRequestUrl(@NonNull GlideUrl model, int width, int height) {
        String url = model.toStringUrl();
        if (isWhiteUrl(url)) {
            if (width == Integer.MIN_VALUE || height == Integer.MIN_VALUE) {
                return getOriginalGlideUrl(model);
            } else {
                return changeGlideUrl(model, width, height);
            }
        }
        return model;
    }

    private String formatUlr(String url, int width, int height) {
        return String.format(Locale.SIMPLIFIED_CHINESE, FORMAT_URL, url, width, height);
    }

    /**
     * 图片地址包含特定后缀，截取原图地址,否则，返回原地址
     */
    private String subUrl(String url) {
        int index = url.indexOf(FILTER_SUFFIX);
        if (index != -1) {
            return url.substring(0, index);
        }
        return url;
    }

    /**
     * 如果请求大小是原始尺寸，就获取原图地址
     */
    public GlideUrl getOriginalGlideUrl(GlideUrl glideUrl) {
        String url = glideUrl.toStringUrl();
        String subUrl = subUrl(url);
        if (!subUrl.equals(url)) {
            try {
                return getGlideUrl(glideUrl, subUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return glideUrl;
    }

    /**
     * 图片请求地址包含特定后缀，替换为新的后缀。图片请求为原图，则添加新的后缀
     *
     * @param glideUrl 原始请求GlideUrl
     * @param width    图片控件请求宽
     * @param height   图片控件请求高
     */
    public GlideUrl changeGlideUrl(GlideUrl glideUrl, int width, int height) {
        String url = glideUrl.toStringUrl();
        String changeUrl = null;
        if (url.contains(FILTER_SUFFIX) || canScaleImageType(url)) {
            changeUrl = subUrl(url);
        }

        if (changeUrl != null) {
            final String formatUlr = formatUlr(changeUrl, width, height);
            try {
                return getGlideUrl(glideUrl, formatUlr);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return glideUrl;
    }

    /**
     * true，表示阿里云支持缩放的图片后缀格式，false，表示不支持。
     */
    private boolean canScaleImageType(String url) {
        for (String imgType : imageType) {
            if (url.endsWith(imgType) && !url.contains(WITH_SUFFIX)) {
                return true;
            }
        }
        return false;
    }

    private GlideUrl getGlideUrl(GlideUrl glideUrl, String spec) throws MalformedURLException {
        URL _url = new URL(glideUrl.toURL(), spec);
        return new GlideUrl(_url);
    }

    public static synchronized GlideUtil instance() {
        if (glideUtil == null) {
            glideUtil = new GlideUtil();
        }
        return glideUtil;
    }
}
