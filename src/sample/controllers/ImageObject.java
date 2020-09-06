package sample.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class ImageObject {
    private double mHeight, mWidth;
    private String mImagePath;
    private ImageView mImage;

    public ImageObject(double height, double width, double X, String imagePath, boolean roundCorners) {
        mHeight = height;
        mWidth = width;
        mImagePath = imagePath;

        Image image = new Image("file:///" + imagePath);
        mImage = new ImageView(image);

        if (image.getHeight() > image.getWidth()) {
            height = image.getHeight() * X / image.getWidth();
            width = X;
        } else {
            height = X;
            width = image.getWidth() * X / image.getHeight();
        }

        mImage.getStyleClass().add("cursor-hand");
        mImage.setFitHeight(height);
        mImage.setFitWidth(width);
        mImage.setSmooth(true);
        mImage.setPreserveRatio(true);
        mImage.setCache(true);

        if (roundCorners)
            setRoundCorners(50);
    }

    public ImageObject(int borderRadius) {
        setRoundCorners(borderRadius);
    }

    public void setRoundCorners(int value) {
        Rectangle clip = new Rectangle(mWidth, mHeight);
        clip.setArcHeight(value);
        clip.setArcWidth(value);
        mImage.setClip(clip);
    }
}
