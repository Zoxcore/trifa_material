package com.zoffcc.applications.trifa;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public class VideoOutFrame {
    private static final String TAG = "trifa.VideoInFrame";

    public static int width = 640;
    public static int height = 480;
    static byte[] imageOutByte = null;
    static BufferedImage imageOut = null;
    final static Semaphore semaphore_video_out_convert = new CustomSemaphore(1);
    static int semaphore_video_out_convert_active_threads = 0;
    static int semaphore_video_out_convert_max_active_threads = 1;

    public static void clear_video_out_frame()
    {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ImageIcon i = new ImageIcon(new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB));
        try
        {
            JPictureBoxOut.videooutbox.setIcon(i);
            EventQueue.invokeLater(() -> {
                try
                {
                    JPictureBoxOut.videooutbox.revalidate();
                    JPictureBoxOut.videooutbox.repaint();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void new_video_out_frame(ByteBuffer vbuf, int w, int h)
    {
        try
        {
            semaphore_video_out_convert.acquire();
            if (semaphore_video_out_convert_active_threads >= semaphore_video_out_convert_max_active_threads)
            {
                semaphore_video_out_convert.release();
                //Log.i(TAG,
                //      "semaphore_video_in_convert_active_threads:" + semaphore_video_in_convert_active_threads + " " +
                //      semaphore_video_in_convert_max_active_threads);
                return;
            }
            semaphore_video_out_convert.release();
        }
        catch (Exception e)
        {
        }

        try
        {
            vbuf.rewind();
            vbuf.get(imageOutByte, 0, imageOutByte.length);

            final Thread paint_thread = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        semaphore_video_out_convert.acquire();
                        semaphore_video_out_convert_active_threads++;
                        semaphore_video_out_convert.release();
                    }
                    catch (Exception e)
                    {
                    }

                    // final long tt1 = System.currentTimeMillis();

                    try
                    {
                        final int h0 = 0;
                        final int h1 = (h / 3);
                        final int h2 = 2 * (h / 3);
                        final int h3 = h;

                        final Thread t1 = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                int rColor;
                                int i;
                                for (int j = h0; j < h1; j++)
                                {
                                    for (i = 0; i < w; i++)
                                    {
                                        try
                                        {
                                            rColor = getRGBFromStream(i, j, w, h, imageOutByte);
                                            imageOut.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageOut.setRGB(i, j, 0);
                                            }
                                            catch (Exception e2)
                                            {
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        t1.start();

                        final Thread t2 = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                int rColor;
                                int i;
                                for (int j = h1; j < h2; j++)
                                {
                                    for (i = 0; i < w; i++)
                                    {
                                        try
                                        {
                                            rColor = getRGBFromStream(i, j, w, h, imageOutByte);
                                            imageOut.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageOut.setRGB(i, j, 0);
                                            }
                                            catch (Exception e2)
                                            {
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        t2.start();

                        final Thread t3 = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                int rColor;
                                int i;
                                for (int j = h2; j < h3; j++)
                                {
                                    for (i = 0; i < w; i++)
                                    {
                                        try
                                        {
                                            rColor = getRGBFromStream(i, j, w, h, imageOutByte);
                                            imageOut.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageOut.setRGB(i, j, 0);
                                            }
                                            catch (Exception e2)
                                            {
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        t3.start();

                        t1.join();
                        t2.join();
                        t3.join();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // Log.i(TAG, "new_video_in_frame:006:" + (System.currentTimeMillis() - tt1) + " ms");

                    // if (Callstate.state != 0)
                    {
                        ImageIcon i = new ImageIcon(imageOut);
                        try
                        {
                            if (i != null)
                            {
                                JPictureBoxOut.videooutbox.setIcon(i);
                                EventQueue.invokeLater(() -> {
                                    try
                                    {
                                        if (i != null)
                                        {
                                            JPictureBoxOut.videooutbox.repaint();
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                    }
                                });
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }

                    try
                    {
                        semaphore_video_out_convert.acquire();
                        semaphore_video_out_convert_active_threads--;
                        semaphore_video_out_convert.release();
                    }
                    catch (Exception e)
                    {
                    }
                }
            };
            paint_thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "new_video_out_frame:007:EE02:" + e.getMessage());
        }
        // Log.i(TAG, "new_video_in_frame:099");
    }

    public static int unsignedByteToInt(final byte b)
    {
        return (int) b & 0xFF;
    }

    public static int getRGBFromStream(final int x, final int y, final int width, final int height, final byte[] buf)
    {
        final int arraySize = height * width;
        final int Y = unsignedByteToInt(buf[y * width + x]);
        final int U = unsignedByteToInt(buf[(y / 2) * (width / 2) + x / 2 + arraySize]);
        final int V = unsignedByteToInt(buf[(y / 2) * (width / 2) + x / 2 + arraySize + arraySize / 4]);

        //~ int R = (int)(Y + 1.370705 * (V-128));
        //~ int G = (int)(Y - 0.698001 * (V-128) - 0.337633 * (U-128));
        //~ int B = (int)(Y + 1.732446 * (U-128));

        int R = (int) (Y + 1.4075 * (V - 128));
        int G = (int) (Y - 0.3455 * (U - 128) - (0.7169 * (V - 128)));
        int B = (int) (Y + 1.7790 * (U - 128));


        if (R > 255)
        {
            R = 255;
        }
        if (G > 255)
        {
            G = 255;
        }
        if (B > 255)
        {
            B = 255;
        }

        if (R < 0)
        {
            R = 0;
        }
        if (G < 0)
        {
            G = 0;
        }
        if (B < 0)
        {
            B = 0;
        }

        return (0xff << 24) | (R << 16) | (G << 8) | B;
    }

    public static void setup_video_out_resolution(int w, int h, int num_bytes)
    {
        Log.i(TAG, "w=" + w + " h=" + h + " num_bytes=" + num_bytes);
        imageOutByte = null;
        imageOutByte = new byte[num_bytes];
        Log.i(TAG, "w=" + w + " h=" + h + " len=" + (int) ((float) (w * h) * (float) (1.5)));
        imageOut = null;
        imageOut = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        width = w;
        height = h;

        JPictureBoxOut.videooutbox.setSize(width, height);
        JPictureBoxOut.videooutbox.setPreferredSize(new Dimension(width, height));
        JPictureBoxOut.videooutbox.revalidate();
        JPictureBoxOut.videooutbox.repaint();
    }
}
