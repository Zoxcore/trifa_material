/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2021 Zoff <zoff@zoff.cc>
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

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class SelectionRectangle
{
    public static int capture_x = 0;
    public static int capture_y = 0;
    public static int capture_width = 0;
    public static int capture_height = 0;
    public static boolean cancel = false;
    public static boolean showing = true;

    public SelectionRectangle()
    {
        cancel = false;
        showing = true;
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Take Screenshot");
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(new BackgroundPane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public class BackgroundPane extends JPanel
    {
        private BufferedImage background;
        private Point mouseAnchor;
        private Point dragPoint;
        private SelectionPane selectionPane;

        public BackgroundPane()
        {
            selectionPane = new SelectionPane();
            try
            {
                Robot bot = new Robot();
                background = bot.createScreenCapture(getScreenViewableBounds());
            }
            catch (AWTException ex)
            {
                // Logger.getLogger(SelectionRectangle.class.getName()).log(Level.SEVERE, null, ex);
            }

            selectionPane = new SelectionPane();
            setLayout(null);
            add(selectionPane);

            MouseAdapter adapter = new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    mouseAnchor = e.getPoint();
                    dragPoint = null;
                    selectionPane.setLocation(mouseAnchor);
                    selectionPane.setSize(60, 60);
                }

                @Override
                public void mouseDragged(MouseEvent e)
                {
                    dragPoint = e.getPoint();
                    int width = dragPoint.x - mouseAnchor.x;
                    int height = dragPoint.y - mouseAnchor.y;

                    int x = mouseAnchor.x;
                    int y = mouseAnchor.y;

                    if (width < 0)
                    {
                        x = dragPoint.x;
                        width *= -1;
                    }
                    if (height < 0)
                    {
                        y = dragPoint.y;
                        height *= -1;
                    }
                    selectionPane.setBounds(x, y, width, height);
                    selectionPane.revalidate();
                    repaint();
                }

            };
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawImage(background, 0, 0, this);
            g2d.dispose();
        }

    }

    public class SelectionPane extends JPanel
    {
        private JButton button;
        private JButton cancel_button;
        private JLabel label;

        public SelectionPane()
        {
            button = new JButton("Capture");
            cancel_button = new JButton("Cancel");
            setOpaque(false);

            label = new JLabel("Rectangle");
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(4, 4, 4, 4));
            label.setBackground(Color.GRAY);
            label.setForeground(Color.WHITE);
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(label, gbc);

            gbc.gridy++;
            add(button, gbc);
            gbc.gridy++;
            add(cancel_button, gbc);

            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    SwingUtilities.getWindowAncestor(SelectionPane.this).dispose();
                    showing = false;
                }
            });

            cancel_button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    cancel = true;
                    SwingUtilities.getWindowAncestor(SelectionPane.this).dispose();
                    showing = false;
                }
            });

            addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    label.setText("Rectangle " + getX() + "x" + getY() + "x" + getWidth() + "x" + getHeight());
                    capture_x = getX();
                    capture_y = getY();
                    capture_width = getWidth();
                    capture_height = getHeight();
                }
            });

        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(128, 128, 128, 64));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            float dash1[] = {10.0f};
            BasicStroke dashed = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1,
                    0.0f);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(dashed);
            g2d.drawRect(0, 0, getWidth() - 3, getHeight() - 3);
            g2d.dispose();
        }

    }

    public static Rectangle getScreenViewableBounds()
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        return getScreenViewableBounds(gd);
    }

    public static Rectangle getScreenViewableBounds(GraphicsDevice gd)
    {
        Rectangle bounds = new Rectangle(0, 0, 0, 0);
        if (gd != null)
        {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            bounds = gc.getBounds();

            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= (insets.left + insets.right);
            bounds.height -= (insets.top + insets.bottom);
        }
        return bounds;
    }
}
