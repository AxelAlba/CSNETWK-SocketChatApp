package chatapp.controllers;

import chatapp.Main;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageObject {
    private double mHeight, mWidth;
    private ImageView mImageView = null;

    public ImageObject(String imagePath, double factor, boolean roundCorners) {
        Image image = new Image("file:///" + imagePath);
        mImageView = new ImageView(image);

        this.setImageDimensions(image, factor);

        mImageView.getStyleClass().add("cursor-hand");
        mImageView.setFitHeight(mHeight);
        mImageView.setFitWidth(mWidth);
        mImageView.setSmooth(true);
        mImageView.setPreserveRatio(true);
        mImageView.setCache(true);

        if (roundCorners)
            setRoundCorners(50);
    }

    public ImageObject(ImageView imageView) {
        mImageView = imageView;
    }

    public ImageObject(String path, double height, double width) {
        mHeight = height;
        mWidth = width;

        Image image = new Image(path);
        mImageView = new ImageView(image);

        mImageView.setFitHeight(height);
        mImageView.setFitWidth(width);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public Node createImageMessageItem() {
        VBox wrapper = new VBox(mImageView);
        wrapper.getStyleClass().add("message-image-wrapper");
        return wrapper;
    }

    public void downloadImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save File");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.gif")
        );

        File savedFile = fc.showSaveDialog(Main.getPrimaryStage());
        String path = savedFile.getPath();

        File output = new File(path);
        BufferedImage bImage = SwingFXUtils.fromFXImage(mImageView.getImage(), null);
        try {
            ImageIO.write(bImage, "png", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRoundCorners(int value) {
        Rectangle clip = new Rectangle(mWidth, mHeight);
        clip.setArcHeight(value);
        clip.setArcWidth(value);
        mImageView.setClip(clip);
    }

    public static void setRoundCorners(ImageView img, int value) {
        Rectangle clip = new Rectangle(img.getFitWidth(), img.getFitHeight());
        clip.setArcHeight(value);
        clip.setArcWidth(value);
        img.setClip(clip);
    }

    private void setImageDimensions(Image image, double factor) {
        if (image.getHeight() > image.getWidth()) {
            mHeight = image.getHeight() * factor / image.getWidth();
            mWidth = factor;
        } else {
            mHeight = factor;
            mWidth = image.getWidth() * factor / image.getHeight();
        }
    }
}
