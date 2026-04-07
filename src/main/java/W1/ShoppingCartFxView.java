package W1;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;

public class ShoppingCartFxView extends VBox {
    private final Label totalLabel;
    private double total;

    public ShoppingCartFxView() {
        setSpacing(12);
        setPadding(new Insets(16));

        this.total = 0.0;
        this.totalLabel = new Label(formatTotal());
        this.totalLabel.setId("totalLabel");

        Button addButton = new Button("Add");
        addButton.setId("addButton");
        addButton.setOnAction(event -> addDemoItem());

        getChildren().addAll(totalLabel, addButton);
    }

    public void addDemoItem() {
        total += 10.0;
        totalLabel.setText(formatTotal());
    }

    public double getTotal() {
        return total;
    }

    private String formatTotal() {
        return "Total: " + new DecimalFormat("0.00").format(total);
    }
}