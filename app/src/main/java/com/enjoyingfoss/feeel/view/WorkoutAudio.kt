package com.enjoyingfoss.feeel.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
import android.util.Log
import com.enjoyingfoss.feeel.R
import com.enjoyingfoss.feeel.WorkoutContract
import com.enjoyingfoss.feeel.data.ExerciseMeta
import java.lang.ref.WeakReference

/**
@author Miroslav Mazel
 */
class WorkoutAudio(private val context: WeakReference<Context>) : WorkoutContract.View, TextToSpeech.OnInitListener {
    companion object {
        val COUNTDOWN = 5
        val HALFTIME_MIN = 8
    }

    private var ttsInited = false
    private val tts = TextToSpeech(context.get(), this)
    private var halfTime = 0

    fun shutdown() { //todo test for this
        tts.shutdown()
    }

    //
    // Speech
    //

    private fun speak(string: String?) {
        if (ttsInited && string != null) {
            if (Build.VERSION.SDK_INT >= 21)
                speakAPI21(string)
            else
                speakAPI15(string)
        }
    }

    @RequiresApi(api = 21)
    private fun speakAPI21(string: String) {
        tts.speak(string, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    @TargetApi(15)
    private fun speakAPI15(string: String) {
        tts.speak(string, TextToSpeech.QUEUE_FLUSH, null)
    }

    @RequiresApi(api = 24)
    private fun setLanguageAPI24(tts: TextToSpeech): Boolean {
        val locales = context.get()?.resources?.configuration?.locales
        for (i in 0 until (locales?.size() ?: 0)) {
            val result = tts.setLanguage(locales?.get(i))
            if (result == TextToSpeech.LANG_AVAILABLE) {
                return true
            }
        }
        return false
    }

    @TargetApi(15)
    private fun setLanguageAPI15(tts: TextToSpeech): Boolean {
        val result = tts.setLanguage(context.get()?.resources?.configuration?.locale)
        if (result == TextToSpeech.LANG_AVAILABLE)
            return true
        return false
    }

    //
    // TextToSpeech.OnInitListener
    //

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageSet =
                    if (Build.VERSION.SDK_INT >= 24)
                        setLanguageAPI24(tts)
                    else
                        setLanguageAPI15(tts)
            if (languageSet)
                speak("Language set!") //todo delete
            else
                Log.e("TTS", "No system languages supported");
            ttsInited = true
            println("inited TTS")
        } else Log.e("TTS", "Init failed")
    }

    //
    // WorkoutContract.View
    //

    override fun setExercise(exerciseMeta: ExerciseMeta) {
        halfTime = exerciseMeta.duration / 2

        speak(context.get()?.getString(exerciseMeta.exercise.titleResource))
    }

    override fun setBreak(nextExerciseMeta: ExerciseMeta) {
        val nextString = context.get()?.getString(nextExerciseMeta.exercise.titleResource)

        speak(context.get()?.getString(R.string.audio_break) +
                String.format(
                        context.get()?.getString(R.string.next_label) ?: "",
                        nextString)
        )
    }

    override fun finishWorkout() {
        speak(context.get()?.getString(R.string.audio_done))
    }

    override fun setSeconds(seconds: Int) {
        if (seconds <= COUNTDOWN)
            speak(seconds.toString())

        if (!tts.isSpeaking) {
            if (seconds == halfTime && halfTime >= HALFTIME_MIN) {
                speak(String.format(context.get()?.getString(R.string.audio_halftime) ?: "", seconds))
            }
        }
    }

    override fun setPlaying() {}

    override fun setPaused() {}
}