package com.zoffcc.applications.trifa;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class SingleComponentAspectRatioKeeperLayout implements LayoutManager
{
    private static final String TAG = "trifa.tAspectRatio";

    private static Component fakeComponent = new JPanel(true);

    public SingleComponentAspectRatioKeeperLayout()
    {
        fakeComponent.setPreferredSize(new Dimension(0, 0));
    }

    @Override
    public void addLayoutComponent(String s, Component component)
    {
    }

    @Override
    public void removeLayoutComponent(Component component)
    {

    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        return getSingleComponent(parent).getPreferredSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent)
    {
        Component component = getSingleComponent(parent);
        Insets insets = parent.getInsets();
        int maxWidth = parent.getWidth() - (insets.left + insets.right);
        int maxHeight = parent.getHeight() - (insets.top + insets.bottom);

        Dimension prefferedSize = component.getPreferredSize();
        Dimension targetDim = getScaledDimension(prefferedSize, new Dimension(maxWidth, maxHeight));

        double targetWidth = targetDim.getWidth();
        double targetHeight = targetDim.getHeight();

        double hgap = (maxWidth - targetWidth) / 2;
        double vgap = (maxHeight - targetHeight) / 2;

        // Set the single component's size and position.
        component.setBounds((int) hgap, (int) vgap, (int) targetWidth, (int) targetHeight);
    }

    private Component getSingleComponent(Container parent)
    {
        int parentComponentCount = parent.getComponentCount();
        if (parentComponentCount > 1)
        {
            throw new IllegalArgumentException(
                    this.getClass().getSimpleName() + " can not handle more than one component");
        }
        Component comp = (parentComponentCount == 1) ? parent.getComponent(0) : fakeComponent;
        return comp;
    }

    private Dimension getScaledDimension(Dimension imageSize, Dimension boundary)
    {
        double widthRatio = boundary.getWidth() / imageSize.getWidth();
        double heightRatio = boundary.getHeight() / imageSize.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);
        // Log.i(TAG, "w=" + imageSize.width + " h=" + imageSize.height + " r=" + ratio);
        return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
    }

}
