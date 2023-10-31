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

import java.awt.*;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;

public class AudioBar extends JPanel
{
    private static final String TAG = "trifa.AudioBar";

    public static AudioBar audio_in_bar = new AudioBar();
    public static AudioBar audio_out_bar = new AudioBar();

    private Color bg_color = Color.BLACK;
    private int cur_value = 0;
    private final int yellow_value = 75;
    private final int red_value = 85;
    private final int default_width = 400;
    private final int default_height = 10;

    AudioBar()
    {
        super();
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(default_width, default_height));
        setSize(default_width, default_height);
        revalidate();
        repaint();
        set_cur_value(0, this);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        setBackground(bg_color);
        int w = this.getWidth();
        int h = this.getHeight();
        if (cur_value > red_value)
        {
            g.setColor(Color.RED);
            g.fillRect(0, 0, (w / 100) * cur_value, h);
            g.setColor(Color.YELLOW);
            g.fillRect(0, 0, (w / 100) * red_value, h);
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, (w / 100) * yellow_value, h);
        }
        else if (cur_value > yellow_value)
        {
            g.setColor(Color.YELLOW);
            g.fillRect(0, 0, (w / 100) * cur_value, h);
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, (w / 100) * yellow_value, h);
        }
        else if (cur_value == 0)
        {
            // nothing to do, already full black bg
        }
        else
        {
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, (w / 100) * cur_value, h);
        }
    }

    public static void set_bar_bgcolor(int bgcolor, AudioBar c)
    {
        c.bg_color = new Color(bgcolor);
        c.repaint();
    }

    public static void set_cur_value(int value, AudioBar c)
    {
        if (c != null)
        {
            c.cur_value = value;
            c.repaint();
        }
        else
        {
            Log.i(TAG, "set_cur_value:EE01");
        }
    }

    public static float audio_vu(byte[] data, int sample_count)
    {
        float sum = 0.0f;
        final float factor = (100.0f / 140.0f);

        if (sample_count > 1)
        {
            for (int i = 0; i < sample_count; i = i + 2)
            {
                short s = (short) ((data[i] & 0xff) | (data[i + 1] << 8));
                sum = sum + (Math.abs(s) / 32767.0f);
            }

            float vu = (float) (20.0f * Math.log(sum));
            return vu * factor;
        }
        return 0.0f;
    }
}
