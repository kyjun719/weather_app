package com.jun.weather.util

import android.util.Log
import com.jun.weather.BaseApplication

/**
 * 디버그 빌드일때만 로그를 찍기위한 클래스
 */
class CLogger {
    companion object {
        private var isDebug = false
        @JvmStatic fun init() {
            isDebug = BaseApplication.isDebug
        }

        /**
         * verbose 로그 출력
         * @param msg msg
         */
        @JvmStatic fun v(msg: Any) {
            if (isDebug) {
                Log.v(tag, msg.toString())
            }
        }

        /**
         * debug 로그 출력
         * @param msg msg
         */
        @JvmStatic fun d(msg: Any) {
            if (isDebug) {
                Log.d(tag, msg.toString())
            }
        }

        /**
         * info 로그 출력
         * @param msg msg
         */
        @JvmStatic fun i(msg: Any) {
            if (isDebug) {
                Log.i(tag, msg.toString())
            }
        }

        /**
         * error 로그 출력
         * @param msg msg
         */
        @JvmStatic fun e(msg: Any) {
            if (isDebug) {
                Log.e(tag, msg.toString())
            }
        }

        private val tag: String
            get() {
                val ste = Throwable().stackTrace
                //순서
                //com.jun.weather.util.CLogger>>getTag
                //com.jun.weather.util.CLogger>>d
                //com.jun.weather.ui.MainActivity>>onResume
                return ste[2].className
            }
    }
}