package w1;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<ShoppingItem> items;

    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    public void addItem(double price, int quantity) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        double itemTotal = price * quantity;
        items.add(new ShoppingItem(items.size() + 1, price, quantity, itemTotal));
    }

    public double calculateItemTotal(double price, int quantity) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return price * quantity;
    }

    public double calculateTotalCost() {
        return items.stream()
                .mapToDouble(ShoppingItem::getTotal)
                .sum();
    }

    public List<ShoppingItem> getItems() {
        return new ArrayList<>(items);
    }

    public void clearCart() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.size();
    }

    // Inner class for shopping items
    public static class ShoppingItem {
        private int number;
        private double price;
        private int quantity;
        private double total;

        public ShoppingItem(int number, double price, int quantity, double total) {
            this.number = number;
            this.price = price;
            this.quantity = quantity;
            this.total = total;
        }

        public int getNumber() { return number; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return total; }

        // For testing equality
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShoppingItem that = (ShoppingItem) o;
            return number == that.number &&
                    Double.compare(that.price, price) == 0 &&
                    quantity == that.quantity &&
                    Double.compare(that.total, total) == 0;
        }
    }
}