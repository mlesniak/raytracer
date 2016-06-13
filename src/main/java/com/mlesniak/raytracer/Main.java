package com.mlesniak.raytracer;

import com.mlesniak.raytracer.animation.Animation;
import com.mlesniak.raytracer.math.Raytracer;
import com.mlesniak.raytracer.math.Vector3D;
import com.mlesniak.raytracer.scene.Scene;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Application entry point.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public final class Main extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private double prevX = -1;
    private double prevY = -1;
    public Main() {
        // Empty.
    }

    public static void main(String[] args) throws Exception {
        LOG.info("Application starting");
        // launch(args);
        // Create simple JavaFX interface.

        Optional<Scene> scene = readScene(args);
        if (scene.isPresent()) {
            Scene s = scene.get();

            if (Animation.isAnimated(s)) {
                new Animation(s).animate();
            } else {
                BufferedImage image = new Raytracer(s).raytrace();
                //writeSingleImage(s, image);
            }
        }
        LOG.info("Application finished");
    }

    private static Optional<Scene> readScene(Object[] args) throws IOException {
        if (args.length < 1) {
            LOG.error("No filename given. Aborting.");
            return Optional.empty();
        }
        return Optional.of(Scene.readScene(args[0].toString()));
    }

    private static void writeSingleImage(Scene scene, BufferedImage image) throws IOException {
        final String pathname = scene.getFilename();
        ImageIO.write(image, "png", new File(pathname));
        LOG.info("Wrote image to file {}", pathname);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ImageView imageView = new ImageView();

        Optional<Scene> scene = readScene(getParameters().getRaw().toArray());
        if (scene.isPresent()) {
            final Scene s = scene.get();

            if (Animation.isAnimated(s)) {
                new Animation(s).animate();
            } else {
                renderScene(imageView, s);
                //writeSingleImage(s, image);
            }
            imageView.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (!event.isShiftDown()) {
                        return;
                    }

                    if (prevX == -1) {
                        prevX = event.getX();
                    }
                    if (prevY == -1) {
                        prevY = event.getY();
                    }
                    double deltaX = event.getX() - prevX;
                    double deltaY = event.getY() - prevY;
                    prevX = event.getX();
                    prevY = event.getY();
                    LOG.info("deltaX={}, deltaY={}", deltaX, deltaY);

                    // Vector3D should be immutable...
                    Vector3D lookAt = s.getLookAt();
                    lookAt.setX(lookAt.getX() + 0.01 * deltaX);
                    lookAt.setY(lookAt.getY() + 0.01 * deltaY);
                    try {
                        renderScene(imageView, s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    event.consume();
                }
            });
        }

        Group root = new Group();
        root.getChildren().add(imageView);
        javafx.scene.Scene sc = new javafx.scene.Scene(root);
        stage.setScene(sc);
        stage.sizeToScene();
        stage.setTitle("Realtime view");
        stage.show();
    }

    private void renderScene(ImageView imageView, Scene s) throws InterruptedException {
        BufferedImage image = new Raytracer(s).raytrace();
        WritableImage img = new WritableImage(s.getWidth(), s.getHeight());
        SwingFXUtils.toFXImage(image, img);
        imageView.setImage(img);
    }
}
