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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class JPictureBox extends JComponent
{
    private static final String TAG = "trifa.JPictureBox";

    public static JPictureBox videoinbox = new JPictureBox();

    private Icon icon = null;
    private final Dimension dimension = new Dimension(100, 100);
    private Image image = null;
    private ImageIcon ii = null;
    private SizeMode sizeMode = SizeMode.STRETCH;
    private int newHeight, newWidth, originalHeight, originalWidth;

    public JPictureBox()
    {
        JPictureBox.this.setPreferredSize(dimension);
        JPictureBox.this.setOpaque(false);
        JPictureBox.this.setSizeMode(SizeMode.STRETCH);
        setDoubleBuffered(true);
        setIgnoreRepaint(true);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        // Log.i(TAG, "paintComponent:001");
        if (ii != null)
        {
            // Log.i(TAG, "paintComponent:002");

            switch (getSizeMode())
            {
                case NORMAL:
                    g.drawImage(image, 0, 0, ii.getIconWidth(), ii.getIconHeight(), null);
                    break;
                case ZOOM:
                    aspectRatio();
                    g.drawImage(image, 0, 0, newWidth, newHeight, null);
                    break;
                case STRETCH:
                    g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
                    break;
                case CENTER:
                    g.drawImage(image, (int) (this.getWidth() / 2) - (int) (ii.getIconWidth() / 2),
                            (int) (this.getHeight() / 2) - (int) (ii.getIconHeight() / 2), ii.getIconWidth(),
                            ii.getIconHeight(), null);
                    break;
                default:
                    g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
            }
        }
    }

    public Icon getIcon()
    {
        return icon;
    }

    public void setIcon(Icon icon)
    {
        this.icon = icon;
        ii = (ImageIcon) icon;
        image = ii.getImage();
        originalHeight = ii.getIconHeight();
        originalWidth = ii.getIconWidth();
    }

    public SizeMode getSizeMode()
    {
        return sizeMode;
    }

    public void setSizeMode(SizeMode sizeMode)
    {
        this.sizeMode = sizeMode;
    }

    public enum SizeMode
    {
        NORMAL, STRETCH, CENTER, ZOOM
    }

    private void aspectRatio()
    {
        if (ii != null)
        {
            newHeight = this.getHeight();
            newWidth = (originalWidth * newHeight) / originalHeight;
        }
    }

}