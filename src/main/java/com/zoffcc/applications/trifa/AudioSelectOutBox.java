/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/*
 *
 *  This selects the Audio Playback Device, and iterates (processes to be ready to play) incoming Audio
 *
 */
public class AudioSelectOutBox
{
    private static final String TAG = "trifa.AudioSelectOutBox";

    static SourceDataLine sourceDataLine = null;
    static AudioFormat audioformat = null;
    static Mixer.Info _SelectedItem = null;

    final static CustomSemaphore semaphore_audio_out_convert = new CustomSemaphore(1);
    static int semaphore_audio_out_convert_active_threads = 0;
    static int semaphore_audio_out_convert_max_active_threads = 2;
    final static CustomSemaphore semaphore_audio_device_changes = new CustomSemaphore(1);

    final static int SAMPLE_RATE_DEFAULT = 48000;
    static int CHANNELS_DEFAULT = 1;
    static int SAMPLE_RATE = SAMPLE_RATE_DEFAULT;
    static int CHANNELS = CHANNELS_DEFAULT;
    static int SAMPLE_SIZE_BIT = 16;
    public static boolean init_ready = false;
    public final static int n_buf_iterate_ms = 60; // fixed ms interval for audio play (call and groups)

    public synchronized static void init()
    {
        if (init_ready == false) {
            Log.i(TAG, "____________init");
            audioformat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS, true, false);
            reload_device_list();
            init_ready = true;
        }
    }

    public static void reload_device_list()
    {
        Log.i(TAG, "____________reload_device_list");
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        DataLine.Info sourceDLInfo = new DataLine.Info(SourceDataLine.class, audioformat);
        for (int cnt = 0; cnt < mixerInfo.length; cnt++)
        {
            Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
            // Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
            if (currentMixer.isLineSupported(sourceDLInfo))
            {
                // Log.i(TAG, "ADD:" + cnt);
                if (_SelectedItem == null) {
                    _SelectedItem = mixerInfo[cnt];
                }
            }

            for (Line t : currentMixer.getTargetLines())
            {
                Log.i(TAG, "T:mixer_line:" + t.getLineInfo());
            }

            for (Line t : currentMixer.getSourceLines())
            {
                Log.i(TAG, "S:mixer_line:" + t.getLineInfo());
            }
        }

        for (int cnt = 0; cnt < mixerInfo.length; cnt++)
        {
            Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
            // Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
            if (!currentMixer.isLineSupported(sourceDLInfo))
            {
                // Log.i(TAG, "ADD:+++:" + cnt);
            }
        }
    }

    public static void change_audio_format(int sample_rate, int channels)
    {
        Log.i(TAG, "____________change_audio_format");
        try
        {
            Log.i(TAG, "AA::OUT::change_audio_format:001:" + sample_rate + " " + channels);
            Log.i(TAG, "change_audio_format:sample_rate=" + sample_rate + " SAMPLE_RATE=" + SAMPLE_RATE + " channels=" +
                    channels + " CHANNELS=" + CHANNELS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            SAMPLE_RATE = sample_rate;
            CHANNELS = channels;
            change_device(_SelectedItem);
            Log.i(TAG, "AA::OUT::change_audio_format:099");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized static void change_device(Mixer.Info i)
    {
        Log.i(TAG, "____________change_device");
        try
        {
            semaphore_audio_device_changes.acquire();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "AA::OUT::change_device:001:" + i.getDescription() + " " + SAMPLE_RATE + " " + CHANNELS);

        audioformat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS, true, false);

        Log.i(TAG, "change_device:001");
        Log.i(TAG, "select audio out:" + i.getDescription());

        // Log.i(TAG, "select audio in:?:" + mixerInfo[cnt].getDescription());
        Mixer currentMixer = AudioSystem.getMixer(i);

        Log.i(TAG, "select audio out:" + "sel:" + i.getDescription());

        if (sourceDataLine != null)
        {
            try
            {
                sourceDataLine.stop();
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            try
            {
                sourceDataLine.flush();
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            try
            {
                sourceDataLine.close();
                Log.i(TAG, "select out out:" + "close old line");
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            sourceDataLine = null;
        }

        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioformat);
        try
        {
            if (currentMixer.isLineSupported(dataLineInfo))
            {
                Log.i(TAG, "linesupported:TRUE");
            }
            else
            {
                Log.i(TAG, "linesupported:**false**");
            }

            if (dataLineInfo.isFormatSupported(audioformat))
            {
                Log.i(TAG, "linesupported:TRUE");
            }
            else
            {
                Log.i(TAG, "linesupported:**false**");
            }

            // sourceDataLine = (SourceDataLine) currentMixer.getLine(dataLineInfo);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

            if (sourceDataLine.isRunning())
            {
                Log.i(TAG, "isRunning:TRUE");
            }
            else
            {
                Log.i(TAG, "isRunning:**false**");
            }

            sourceDataLine.open(audioformat);
            sourceDataLine.start();
            Log.i(TAG, "getBufferSize=" + sourceDataLine.getBufferSize());

            if (sourceDataLine.isRunning())
            {
                Log.i(TAG, "isRunning:2:TRUE");
            }
            else
            {
                Log.i(TAG, "isRunning:2:**false**");
            }
        }
        catch (SecurityException se1)
        {
            se1.printStackTrace();
            Log.i(TAG, "select audio out:EE3:" + se1.getMessage());
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
            Log.i(TAG, "select audio out:EE2:" + e1.getMessage());
        }

        Log.i(TAG, "change_device:099");

        semaphore_audio_device_changes.release();
        Log.i(TAG, "AA::OUT::change_device:099");
    }
}
