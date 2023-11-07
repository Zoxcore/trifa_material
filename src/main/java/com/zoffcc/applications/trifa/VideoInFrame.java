package com.zoffcc.applications.trifa;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import static com.zoffcc.applications.trifa.MainActivity.*;
import static com.zoffcc.applications.trifa.MainActivity.setVideo_play_count_frames;

public class VideoInFrame {
    private static final String TAG = "trifa.VideoInFrame";

    public static int width = 640;
    public static int height = 480;
    static byte[] imageInByte = null;
    static BufferedImage imageIn = null;
    final static Semaphore semaphore_video_in_convert = new CustomSemaphore(1);
    static int semaphore_video_in_convert_active_threads = 0;
    static int semaphore_video_in_convert_max_active_threads = 1;

    public static void clear_video_in_frame()
    {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ImageIcon i = new ImageIcon(new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB));
        try
        {
            JPictureBox.videoinbox.setIcon(i);
            EventQueue.invokeLater(() -> {
                try
                {
                    JPictureBox.videoinbox.revalidate();
                    JPictureBox.videoinbox.repaint();
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

    public static void new_video_in_frame(ByteBuffer vbuf, int w, int h)
    {
        try
        {
            semaphore_video_in_convert.acquire();
            if (semaphore_video_in_convert_active_threads >= semaphore_video_in_convert_max_active_threads)
            {
                semaphore_video_in_convert.release();
                //Log.i(TAG,
                //      "semaphore_video_in_convert_active_threads:" + semaphore_video_in_convert_active_threads + " " +
                //      semaphore_video_in_convert_max_active_threads);
                return;
            }
            semaphore_video_in_convert.release();
        }
        catch (Exception e)
        {
        }

        try
        {
            vbuf.rewind();
            vbuf.get(imageInByte, 0, imageInByte.length);

            final Thread paint_thread = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        semaphore_video_in_convert.acquire();
                        semaphore_video_in_convert_active_threads++;
                        semaphore_video_in_convert.release();
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
                                            rColor = getRGBFromStream(i, j, w, h, imageInByte);
                                            imageIn.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageIn.setRGB(i, j, 0);
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
                                            rColor = getRGBFromStream(i, j, w, h, imageInByte);
                                            imageIn.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageIn.setRGB(i, j, 0);
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
                                            rColor = getRGBFromStream(i, j, w, h, imageInByte);
                                            imageIn.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageIn.setRGB(i, j, 0);
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
                        //final long tt2 = System.currentTimeMillis();
                        javax.swing.ImageIcon i = new javax.swing.ImageIcon(imageIn);
                        //Log.i(TAG, "new_video_in_frame:007:" + (System.currentTimeMillis() - tt2) + " ms");
                        try
                        {
                            if (i != null)
                            {
                                setVideo_play_count_frames(getVideo_play_count_frames() + 1);
                                final int play_measure_after_frame = getVideo_play_measure_after_frame();
                                if (getVideo_play_count_frames() >= play_measure_after_frame) {
                                    setVideo_play_count_frames(0);
                                    int fps = (int)((System.currentTimeMillis() - getVideo_play_last_timestamp()) / (float)play_measure_after_frame);
                                    if (fps > 0) {
                                        fps = 1000 / fps;
                                    } else {
                                        fps = 0;
                                    }
                                    if ((fps < 1) || (fps > 120))
                                    {
                                        setVideo_play_fps(0);
                                    }
                                    else
                                    {
                                        setVideo_play_fps(fps);
                                    }
                                    MainActivity.setVideo_play_last_timestamp(System.currentTimeMillis());
                                }
                                JPictureBox.videoinbox.setIcon(i);
                                //Log.i(TAG, "new_video_in_frame:008:" + (System.currentTimeMillis() - tt3) + " ms");
                                EventQueue.invokeLater(() -> {
                                    try
                                    {
                                        if (i != null)
                                        {
                                            final long tt4 = System.currentTimeMillis();
                                            JPictureBox.videoinbox.repaint();
                                            //Log.i(TAG, "new_video_in_frame:009:" + (System.currentTimeMillis() - tt4) +
                                            //           " ms");
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
                        semaphore_video_in_convert.acquire();
                        semaphore_video_in_convert_active_threads--;
                        semaphore_video_in_convert.release();
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
            Log.i(TAG, "new_video_in_frame:007:EE02:" + e.getMessage());
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

    public static void setup_video_in_resolution(int w, int h, int num_bytes)
    {
        Log.i(TAG, "w=" + w + " h=" + h + " num_bytes=" + num_bytes);
        imageInByte = null;
        imageInByte = new byte[num_bytes];
        Log.i(TAG, "w=" + w + " h=" + h + " len=" + (int) ((float) (w * h) * (float) (1.5)));
        imageIn = null;
        imageIn = new java.awt.image.BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        width = w;
        height = h;

        JPictureBox.videoinbox.setSize(width, height);
        JPictureBox.videoinbox.setPreferredSize(new Dimension(width, height));
        JPictureBox.videoinbox.revalidate();
        JPictureBox.videoinbox.repaint();
    }
}
