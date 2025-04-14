package components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Timer;

public class OpeningPage extends JFrame {

    public OpeningPage() {
        setTitle("ACTION");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        PixelRevealPanel panel = new PixelRevealPanel(() -> {
           new javax.swing.Timer(5000, e -> {
               ((javax.swing.Timer) e.getSource()).stop();
            }).start();
           dispose();
            new LoginForm();
        });
        setContentPane(panel);
        setVisible(true);

        SwingUtilities.invokeLater(panel::startAnimation);
    }

    class PixelRevealPanel extends JPanel {
        private BufferedImage image;
        private final int blockSize = 20;
        private List<Point> allBlocks = new ArrayList<>();
        private Set<Point> revealedBlocks = new HashSet<>();
        private Timer timer;
        private Runnable onComplete;

        public PixelRevealPanel(Runnable onComplete) {
            this.onComplete = onComplete;
            try {
                File imgFile = new File("src/static/banner.png");
                image = ImageIO.read(imgFile);
                System.out.println("Loaded image: " + imgFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("Image load error: " + e.getMessage());
            }

            if (image == null) return;

            int panelW = 1200;
            int panelH = 800;
            double imgAspect = (double) image.getWidth() / image.getHeight();
            int drawW = panelW;
            int drawH = (int) (panelW / imgAspect);
            if (drawH > panelH) {
                drawH = panelH;
                drawW = (int) (panelH * imgAspect);
            }

            for (int y = 0; y < drawH; y += blockSize) {
                for (int x = 0; x < drawW; x += blockSize) {
                    allBlocks.add(new Point(x, y));
                }
            }

            Collections.shuffle(allBlocks);
        }

        public void startAnimation() {
            if (image == null || timer != null) return;

            timer = new Timer(10, e -> {
                for (int i = 0; i < 100 && !allBlocks.isEmpty(); i++) {
                    revealedBlocks.add(allBlocks.remove(0));
                }
                repaint();

                if (allBlocks.isEmpty()) {
                    ((Timer) e.getSource()).stop();
                    if (onComplete != null) {
                        Timer delay = new Timer(4000, evt -> onComplete.run());
                        delay.setRepeats(false);
                        delay.start();
                    }
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(98, 0, 238));
            g.fillRect(0, 0, getWidth(), getHeight());

            if (image == null) return;

            int panelW = getWidth();
            int panelH = getHeight();
            double imgAspect = (double) image.getWidth() / image.getHeight();
            int drawW = panelW;
            int drawH = (int) (panelW / imgAspect);
            if (drawH > panelH) {
                drawH = panelH;
                drawW = (int) (panelH * imgAspect);
            }

            int offsetX = (panelW - drawW) / 2;
            int offsetY = (panelH - drawH) / 2;

            for (Point p : revealedBlocks) {
                int x = p.x;
                int y = p.y;

                int w = Math.min(blockSize, drawW - x);
                int h = Math.min(blockSize, drawH - y);

                BufferedImage sub = image.getSubimage(
                        x * image.getWidth() / drawW,
                        y * image.getHeight() / drawH,
                        w * image.getWidth() / drawW,
                        h * image.getHeight() / drawH
                );

                g.drawImage(sub, offsetX + x, offsetY + y, w, h, null);
            }
        }
    }

    public static void main(String[] args) {
        new OpeningPage();
    }
}
