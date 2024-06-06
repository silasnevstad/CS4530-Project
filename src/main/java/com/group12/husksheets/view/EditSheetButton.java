package com.group12.husksheets.view;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EditSheetButton extends Button{

    Label nameLabel;

    public EditSheetButton(VBox parent, String sheetName) {
        this.nameLabel = new Label(sheetName);
        this.setAlignment(Pos.CENTER_LEFT);

        HBox labelHolder = new HBox();
        labelHolder.setPrefWidth(parent.getWidth());
        labelHolder.setAlignment(Pos.CENTER_LEFT);
        labelHolder.getChildren().addAll(this.nameLabel);
    }

}
