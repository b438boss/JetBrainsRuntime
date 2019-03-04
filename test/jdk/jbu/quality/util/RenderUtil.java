/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package quality.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RenderUtil {
    final static int TOLERANCE = 40;

    public static BufferedImage capture(int width, int height, Consumer<Graphics2D> painter)
            throws Exception
    {
        JFrame[] f = new JFrame[1];
        Point[] p = new Point[1];
        double[] scale = new double[2];
        SwingUtilities.invokeAndWait(() -> {
            f[0] = new JFrame();

            JComponent c = new MyComponent(painter);

            f[0].add(c);
            c.setSize(width + 10, height + 10);
            f[0].setSize(width + 100, height + 100); // giving some space
            // for frame border effects,
            // e.g. rounded frame
            c.setLocation(50, 50);
            f[0].setVisible(true);
            p[0]= c.getLocationOnScreen();
            scale[0] = f[0].getGraphicsConfiguration().getDefaultTransform().getScaleX();
            scale[1] = f[0].getGraphicsConfiguration().getDefaultTransform().getScaleY();
        });

        Rectangle screenRect;
        Robot r = new Robot();
        while (!Color.black.equals(r.getPixelColor(p[0].x+1, p[0].y))) {
            Thread.sleep(100);
        }
        screenRect = new Rectangle(
                p[0].x + 5,
                p[0].y + 5,
                (int)((width - 20)  * scale[0]), (int)((height - 30) * scale[1]));

        BufferedImage result = r.createScreenCapture(screenRect);
        SwingUtilities.invokeAndWait(f[0]::dispose);
        return result;
    }

    private static class MyComponent extends JComponent {
        private final Consumer<Graphics2D> painter;

        private MyComponent(Consumer<Graphics2D> painter) {
            this.painter = painter;
        }


        @Override
        protected void paintComponent(Graphics g) {
            Shape savedClip = g.getClip();
            g.translate(5, 5);
            painter.accept((Graphics2D)g);
            g.translate(-5, -5);
            g.setClip(savedClip);
            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth() + 10, 5);
            g.fillRect(0, getHeight()-5, getWidth() + 10, 5);
            g.fillRect(getWidth() - 10, -10, getWidth() + 5, getHeight() + 5);
            g.fillRect(-5, -10,  10, getHeight() + 5);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static void checkImage(BufferedImage image, String path, String gfName) throws Exception {

        String[] testDataVariant = {
                "osx_hardware_rendering", "osx_software_rendering",
                "osx_sierra_rendering", "osx_lowres_rendering",
                "linux_rendering", "windows_rendering"};

        String testDataStr = System.getProperty("testdata");
        assertNotNull("testdata property is not set", testDataStr);

        File testData = new File(testDataStr, "quality" + File.separator + path);
        assertTrue("Test data dir does not exist", testData.exists());

        if (System.getProperty("gentestdata") == null) {
            boolean failed = true;
            StringBuilder failureReason = new StringBuilder();
            for (String variant : testDataVariant) {
                File goldenFile = new File(testData, variant + File.separator +
                        gfName);
                if (!goldenFile.exists()) continue;

                BufferedImage goldenImage = ImageIO.read(goldenFile);
                failed = true;
                if (image.getWidth() != goldenImage.getWidth() ||
                        image.getHeight() != image.getHeight())
                {
                    failureReason.append(variant).append(" : Golden image and result have different sizes\n");
                    continue;
                }

                Raster gRaster = goldenImage.getData();
                Raster rRaster = image.getData();
                int[] gArr = new int[3];
                int[] rArr = new int[3];
                failed = false;
                scan:
                for (int i = 0; i < gRaster.getWidth(); i++) {
                    for (int j = 0; j < gRaster.getHeight(); j++) {
                        gRaster.getPixel(i, j, gArr);
                        rRaster.getPixel(i, j, rArr);
                        assertTrue(gArr.length == rArr.length);
                        for (int k = 0; k < gArr.length; k++) {
                            int diff = Math.abs(gArr[k] - rArr[k]);
                            if (diff > TOLERANCE) {
                                failureReason.append(variant).append(" : Different pixels found (").
                                        append("c[").append(k).append("]=").append(diff).
                                        append(") at (").append(i).append(",").append(j).append(")");
                                failed = true;
                                break scan;
                            }
                        }
                    }
                }

                if (!failed) break;
            }

            if (failed) throw new RuntimeException(failureReason.toString());
        }
        else {
            ImageIO.write(image, "png", new File(testData, gfName));
        }
    }
}
